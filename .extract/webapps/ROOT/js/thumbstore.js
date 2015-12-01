//used to store details of duplicateFolders
var duplicateFolderDetails = {};

function getSize() {
    $.get("rest/hello/db/size", function (data) {
        document.getElementById('db_size').innerHTML = data;
    });
}

function getPath() {
    $.get("rest/hello/db/path", function (data) {
        document.getElementById('db_path').innerHTML = data;
    });
}

function getIndexedPaths(div) {
    $.get("rest/hello/paths", function (data) {
        var val = 1;
        var cbh = document.getElementById('db_paths');
        //   data.push("toto");
        for (i in data) {
            var cb = document.createElement('input');
            cb.type = 'checkbox';
            cb.checked = true;
            cbh.appendChild(cb);
            cb.name = "folder";
            cb.value = data[i];
            cbh.appendChild(document.createTextNode(data[i]));
            val++;
            cbh.appendChild(document.createElement('br'));
        }
    });
}

function getStatus() {
    $.get("rest/hello/status",function (data) {
        document.getElementById('db_status').innerHTML = data["stringStatus"];
    }).error(function () {
            document.getElementById('db_status').innerHTML = "Cannot connect to REST service";

        });
}

function getDuplicate() {
    var folders = getSelectedFolders();
    $.get(
        "rest/hello/identical",
        {
            folder:JSON.stringify(folders), //folders,
            max:$("input[name=max]").val()
        },
        function (data) {
            var i = 1;
            var html_table = '<thead> <tr> <th class="ay-sort sorted-asc"><span>Size</span></th>'
                + ' <th class="ay-sort"><span>#Files</span></th>  <th class="ay-sort"><span>Paths</span></th></tr></thead> <tbody>';

            var template = ' <tr >'
                + '<td class="size"><a href="#"  onclick="">{{fileSize}}</a></td>'
                + '<td class="files">{{occurences}}</td><td class="paths"> ';

            for (i in data) {
                data[i]['occurences'] = data[i].al.length;
                data[i]['fileSize'] = data[i]['fileSize'] / 1024 / 1024;
                data[i]['fileSize'] = data[i]['fileSize'].toFixed(4);
                var rowTag = Mustache.to_html(template, data[i]);
                for (f in data[i].al) {
                    rowTag += '<div class="paths">' + toFolderAndFileLink(data[i].al[f]) + ' ' + toDeleteLink(data[i].al[f]) + '</div> ';
                }
                html_table += rowTag + '</td></tr> ';
            }
            html_table += '</tbody>';
            updateDuplicateTable('#duplicate-file-table', html_table);
        });
}


function updateDuplicateTable(table, html_table) {
    $(table).children().remove();
    $(table).append(html_table);
    $(table).delegate("tr", "click", function () {
        $(this).addClass("selected").siblings().removeClass("selected");
    });
    $(document).ready(function () {
        $.ay.tableSort({target:$('table'), debug:false});
        generatePathLink();
        generateDeleteLink();
    });
}
//
//
//function toDirectLink(path) {
//    return '<a  class="pathlink" href="#!' + path + '">[path]</a>'
//}

function toFolderLink(path) {
    var a1 = '  <a class="pathlink btn-mini btn-info" href="#!"  data-p1="' + path + '">';
    var a2 = 'folder<a>';
    return  a1 + a2;
}

function toFileLink(path) {
    return  '<a class="pathlink btn-mini btn-primary" href="#!"  data-p1="' + path + '">file</a>'
}

function toFolderLinks(path1, path2) {
    var a1 = '  <a class="pathlink btn-mini btn-info" href="#!"  data-p1="' + path1 + '" data-p2="' + path2 + '">';
    var a2 = ' [folders] <a>';
    return  a1 + a2;
}

function toThumbnailLink(path) {
    //   debugger;
    return   '<a class="thumbnaillink btn-mini btn-info" href="#!"  data-p1="' + path + '">thumbnail</a>';
}


function getFolder(path) {
    var n = path.lastIndexOf('/');
    if (n == -1) {
        //ok, maybe it's a windows path
        n = path.lastIndexOf('\\');
    }
    //  var file = path.substring(n + 1);
    var folder = path.substring(0, n);
    return folder;
}
function toFolderAndFileLink(path) {
    var folder = getFolder(path);
    return path + '' + toFileLink(path) + ' ' +
        toFolderLink(folder);
}

function toDeleteLink(path) {
    return '<a class="deletelink  btn-mini btn-danger" href="#!"  data-p1="' + path + '">delete</a>';
}

function toLinks(path) {
    return toFolderAndFileLink(path) + ' '
        + toThumbnailLink(path) + ' ' + toDeleteLink(path);

}

function getDuplicateFolderDetails(folder1, folder2) {
    var d = duplicateFolderDetails[folder1 + folder2];
    var tab = [];
    for (var i = 0; i < d[0].length; ++i) {
        tab.push({
            f1:d[0][i],
            f2:d[1][i]
        });
    }

    var html_table = '<thead> <tr> <th class="ay-sort sorted-asc"><span>Size</span></th>'
        + '<th class="ay-sort"><span>Paths</span></th>' +
        '</tr></thead> <tbody>';

    var template = ' <tr data-p1="{{folder1}}" data-p2="{{folder2}}">'
        + '<td class="size"><a href="#"  onclick=""> {{totalSize}}</a></td>'
        + '<td class="files">{{occurences}}</td>'
        + '<td class="f1">{{filesInFolder1}}</td>'
        + '<td class="f2">{{filesInFolder2}}</td>';

//    var templateFiles = ' {{#.}} ' + '<tr><td>{{f1.size}}</td><td><div class="paths">{{f1.path}}<a class="pathlink" href="#!"  data-p1="{{f1.path}}">[file]</a> ' +
//        '<a class="deletelink  btn btn-warning" href="#!"  data-p1="{{f1.path}}">[delete]</a>  <br>' +
//        '{{f2.path}}<a class="pathlink" href="#!"  data-p1="{{f2.path}}">[file]</a>  <a class="deletelink" href="#!"  data-p1="{{f2.path}}">[delete]</a></div></td></tr><br>' +
//        '{{/.}}';
    var templateFiles = '<tr><td>{{f1.size}}</td><td><div class="paths">{{f1.path}}' + toFileLink("{{f1.path}}") +
        toDeleteLink("{{f1.path}}") + '<br>' +
        '{{f2.path}}' + toFileLink("{{f2.path}}") + toDeleteLink("{{f2.path}}") + '</div></td></tr><br>';
    for (i in tab) {
        var htmlFiles = Mustache.to_html(templateFiles, tab[i]);
        html_table += htmlFiles;
    }


    html_table += "</tbody>"

    $('#duplicate-folder-details-table').children().remove();
    $('#duplicate-folder-details-table').append(html_table);
    $(document).ready(function () {
        $.ay.tableSort({target:$('#duplicate-folder-details-table'), debug:false});
        generatePathLink();
        generateDeleteLink();
    });
}
//
//function buildAllTable(array) {
//    var html_table = '<thead> <tr> <th class="size ay-sort sorted-asc"><span>Size</span></th>'
//        + '<th class="paths ay-sort"><span>Paths</span></th>' +
//        '</tr></thead> <tbody>';
//}

function getSelectedFolders() {
    var inputs = $("input[name=folder]");
    //debugger;
    //console.log($("#db_paths").serializeArray());
    var result = {}
    var folders = [];
    // debugger;
    for (i = 0; i < inputs.length; i++) {
        if (inputs[i].checked) {
            folders.push(inputs[i].value);
        }

    }
    //   folders.push("toto");
    result.folders = folders;
    return result;
    // return folders;
    //return $("input[name=folder]").serializeArray();
}

function getAll() {
   // debugger;
    var folders = getSelectedFolders();
    $.getJSON('rest/hello/getAll', {
        filter:$("input[name=filter]").val(),
        folder:JSON.stringify(folders),
        gps:$("input[name=gps]").is(":checked")
        //$.param(folders)
    }, function (data) {
        buildAllTable(data);
        //   debugger;
    });
}


function getGallery() {


    var folders = getSelectedFolders();
    $.getJSON('rest/hello/getAll', {
        filter:$("input[name=filter_path]").val(),
        folder:JSON.stringify(folders),
        gps:false
        //$.param(folders)
    }, function (data) {
        buildGallery(data);
        //   debugger;
    });
}

function buildAllTable(array) {
    //debugger;
    var html_table = '<thead> <tr> <th class="size ay-sort sorted-asc"><span>Size</span></th>'
        + '<th class="paths ay-sort"><span>Paths</span></th>' +
        '</tr></thead> <tbody>';

    for (i in array) {
        html_table += '<tr>'
            + '<td class="size">' + array[i].size + '</td>'
            + '<td class="paths">' + toLinks(array[i].path) + '</td></tr>';
        //   debugger;
    }
    html_table += "</tbody>"

    $('#all-table').children().remove();
    $('#all-table').append(html_table);
    $(document).ready(function () {
        $.ay.tableSort({target:$('#all-table'), debug:false});
        generatePathLink();
        generateDeleteLink();
        generateThumbnailLink();
    });
}


function buildGallery(array) {
    //we want to build elements with the following form
//        <li class="span4">
//            <div class="thumbnail">
//                <div class="container">
//                           '<img src=""rest/hello/getThumbnail?path='" title="{{path}}"/>'
//                </div>
//            </div>
//            <div class="caption">description </div>
//           </div>
//        </li>
    var ul = document.createElement('ul');
    ul.className = "thumbnails";

    for (f in array) {
        var li = document.createElement('li');
        li.className = "span4";


        var thumb = document.createElement('div');
        thumb.className = "thumbnail";
        var image = array[f];
        var imgTag=document.createElement("img");
        imgTag.src="rest/hello/getThumbnail?path="+image.path + "&w=600&h=600";

        imgTag.title=image.path;
        thumb.appendChild(imgTag);


        li.appendChild(thumb);
        ul.appendChild(li)
    }

    $('#gallery').children().remove();
    $('#gallery').append(ul);

}


function getDuplicateFolder() {

    var folders = getSelectedFolders();

    //debugger;
    $('#duplicate-folders-table-details').children().remove();

    var html_table = '<thead> <tr> <th class="ay-sort sorted-asc"><span>Size</span></th>'
        + ' <th class="ay-sort"><span>#Files</span></th>' +
        '<th class="ay-sort"><span>&#37;F1</span></th>' +
        '<th class="ay-sort"><span>&#37;F2</span></th>' +
        '<th class="ay-sort"><span>Paths</span></th>' +
        '</tr></thead> <tbody>';


    var template = ' <tr data-p1="{{folder1}}" data-p2="{{folder2}}" >'
        + '<td class="size"><a href="#"  onclick=""> {{totalSize}}</a></td>'
        + '<td class="files">{{occurences}}</td>'
        + '<td class="f1">{{filesInFolder1}}</td>'
        + '<td class="f2">{{filesInFolder2}}</td>';


    // debugger;
    $.getJSON('rest/hello/duplicateFolder', {
        folder:JSON.stringify(folders)
        //$.param(folders)
    }, function (data) {
        $.each(data, function (key, val) {
            val['totalSize'] = val['totalSize'] / 1024.0 / 1024;
            val['totalSize'] = val['totalSize'].toFixed(4);

            val['filesInFolder1'] = (val['occurences'] * 100.0 / val['filesInFolder1']).toFixed(0);
            val['filesInFolder2'] = (val['occurences'] * 100.0 / val['filesInFolder2']).toFixed(0);

            var rowTag = Mustache.to_html(template, val);
            html_table += rowTag
                + '<td class="paths"> <div id="folder1">' + val['folder1'] + '</div> ' +
                '<div id="folder2">' + val['folder2'] + ' ' + toFolderLinks(val['folder1'], val['folder2']) +
                '</div>  </td>'
                + '</tr> ';
            //   debugger;

            var fileArray = new Array();
            fileArray[0] = val['file1'];
            fileArray[1] = val['file2'];

            duplicateFolderDetails[val['folder1'] + val['folder2']] = fileArray;
        });
        html_table += '</tbody>';
        //  debugger;
        updateDuplicateTable('#duplicate-folder-table', html_table);

        $(document).ready(function () {
            $('#duplicate-folder-table tr').click(function () {
                var $this = $(this);
                var folder1 = $this.data('p1');
                var folder2 = $this.data('p2');
                getDuplicateFolderDetails(folder1, folder2);
            });
        });
    });
}
function callOpen(para1, para2) {
    //  debugger;
    var result = {}
    var folders = [];
    if (para1 != null) {
        folders.push(para1);
    }
    if (para2 != null) {
        folders.push(para2);
    }

    result.folders = folders;
    $.getJSON("rest/hello/open", {path:JSON.stringify(result)});
}

function callDelete(para1) {
    //$(".paths > div").contents().
    $(".paths").contents().filter(function () {
        debugger;
        return this.nodeValue == para1
    }).wrap('<div style="float:left; text-decoration:line-through"/>');

    $.get("rest/hello/trash", {path:para1});
}

function callThumbnail(source, p1) {
    //remove previous image
    if ($("img", source).length == 0) {
        //no previous image, add thumbnail
        $(source).append('<img src="rest/hello/getThumbnail?path=' + p1 + '&w=100&h=100"/>');
    } else {
        $("img", source).remove();
    }
}


function shrink() {
    var folders = getSelectedFolders();
    $.get("rest/hello/shrink", {
        folder:JSON.stringify(folders)
    }, function (data) {
    });
}

function update() {
    var folders = getSelectedFolders();
    $.get("rest/hello/update", {folder:JSON.stringify(folders)}, function (data) {
    });
}

function shrinkUpdate() {
    var folders = getSelectedFolders();
    debugger;
    $.get("rest/hello/shrinkUpdate", {
        folder:JSON.stringify(folders)
    }, function (data) {
    });
}


function index(currentForm) {

    prettyPrint(currentForm);
    val = document.getElementById("index_path").value;
    $.get("rest/hello/index", {
        path:val
    }, function (data) {
    });
}

function prettyPrint(object) {
    for (i in object) {
        console.log(i + " " + object[i]);
    }
}

function equalHeight(group) {
    tallest = 0;
    //                      debugger;
    group.each(function () {
        thisHeight = $(this).height();
        if (thisHeight > tallest) {
            tallest = thisHeight;
        }
    });
    // debugger;
    group.each(function () {
        $(this).height(tallest);
    });
}


function generatePathLink() {
    $('.pathlink').click(function () {
        var $this = $(this);
        var p1 = $this.data('p1');
        var p2 = $this.data('p2');
        var folders = [];
        folders.push(p1);
        folders.push(p2);
        callOpen(folders[0], folders[1]);
    });
}

function generateDeleteLink() {
  //  debugger;
    $('.deletelink').click(function () {
        var $this = $(this);
        var p1 = $this.data('p1');
        callDelete(p1);
    });
}

function generateThumbnailLink() {
    $('.thumbnaillink').click(function () {
        var $this = $(this);
        var p1 = $this.data('p1');
        callThumbnail($this, p1);
    });
}


function getWithRMSE(param, rmse) {
    $.get("rest/hello/getThumbnail/", param.path, function (image) {
        var template = "<img src=\"data:image;base64,{{base64Sig}}\" title=\" {{title}} \"/>";
        var imgTag = Mustache.to_html(template, image);
        $("#duplicate_upload_result").prepend('<div class="floated_img"><div class="nailthumb-container nailthumb-image-titles-animated-onhover square">' + imgTag + "</div>" + rmse + "  " + image.title + "</div>");
        jQuery(document).ready(function () {
            jQuery('.nailthumb-container').nailthumb();
            jQuery('.nailthumb-image-titles-animated-onhover').nailthumb();
        });

    });
}
