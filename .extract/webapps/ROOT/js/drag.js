

function displaySrcImage(image) {
    //  $('.qq-upload-list').children().remove();
    var img = document.createElement("img");
    // li.appendChild(img);

    reader = new FileReader();
    reader.onload = (function (theImg) {

        return function (evt) {
            theImg.src = evt.target.result;

        };
    }(img));
    reader.readAsDataURL(image);
    // display the source image
    $("#duplicate_upload_source").children().remove();
    $("#duplicate_upload_result").children().remove();
    div_t = document.createElement("div");
    div_t.className = "smallImage";
    div_t.style.float = "left";
    div_t.appendChild(img);

    $("#duplicate_upload_source").append(div_t);
}

function localFinished(id, name, json) {
    // debugger;
    displaySimilarImages(json.sourceSig, json.images);
    $("#lshStatus").text("  " + json.lshCandidates + " candidates / " + json.lshSize + " total");

    // debugger;
    //  $("#droppedFiles").children().remove();
}


function dataURItoBlob(dataURI) {
    //  debugger;
    // convert base64 to raw binary data held in a string
    var byteString;
    if (dataURI.split(',')[0].indexOf('base64') >= 0)
        byteString = atob(dataURI.split(',')[1]);
    else
        byteString = decodeURIComponent(dataURI.split(',')[1]);
    // separate out the mime component
    var mimeString = dataURI.split(',')[0].split(':')[1].split(';')[0]

    // write the bytes of the string to an ArrayBuffer
    var ab = new ArrayBuffer(byteString.length);
    var ia = new Uint8Array(ab);
    for (var i = 0; i < byteString.length; i++) {
        ia[i] = byteString.charCodeAt(i);
    }

    // write the ArrayBuffer to a blob, and you're done
    var bb = new Blob([ab], { type:mimeString });
    // bb.append(ab);
    return bb; //bb.getBlob(mimeString);
}


function imageURLToBlob(url) {
    // debugger;

    var xhr = new XMLHttpRequest();
    // debugger;
    xhr.onreadystatechange = function(){
        if (this.readyState == 4 && this.status == 200) {
            //this.response is what you're looking for
            //  handler(this.response);
            console.log(this.response, typeof this.response);
            var tab =[];
            tab.push(this.response);
            uploadBlobs(tab);
            //   return response;
            //    var img = document.getElementById('img');
            //   var url = window.URL = window.webkitURL;
            //  img.src = url.createObjectURL(this.response);
        }
    }
    xhr.open('GET', "rest/hello/getImageFromWeb?url="+url, true);
    xhr.responseType = 'blob';
    xhr.send();



//
//    return $.ajax({
//        type:'GET',
//        url:url
//    });
}
//var dataURI;
function processData(data) {
    dataURI = "";
   // debugger;
    // console.log("success " + data);
    //Do some stuff with the data
}
//  return dataURI;
//}


function uploadOneFile(file, indice, nbFiles) {
    var xhr = new XMLHttpRequest();
    xhr.open('POST', 'rest/hello/findSimilar');
    xhr.setRequestHeader("X_FILENAME", file.name);
    var progressBar = document.getElementById("progressBar" + indice);
    xhr.upload.onprogress = function (e) {
        //  debugger;
        progressBar.value = e.loaded;
        progressBar.max = e.total;

    };
    xhr.onreadystatechange = function (e) {
        if (xhr.readyState == 4) {
            //    debugger;
            // continue only if HTTP status is "OK"
            if (xhr.status == 200) {
                //      debugger;
//                    alert("finished!");
                localFinished(0, 0, jQuery.parseJSON(xhr.responseText));
            }
        }
    };

    // Send the Ajax request
    //debugger;
    displaySrcImage(file);
    xhr.send(file);
}

//from http://codeaid.net/javascript/convert-size-in-bytes-to-human-readable-format-(javascript)
function bytesToSize(bytes) {
    var sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB'];
    if (bytes == 0) return 'n/a';
    var i = parseInt(Math.floor(Math.log(bytes) / Math.log(1024)));
    return Math.round(bytes / Math.pow(1024, i), 2) + ' ' + sizes[[i]];
}


function dropHandler(event) {
    //  console.log('drop event');
    //   debugger;

    //clean the text area
    $("#dropText").hide();
    $("#droppedFiles").children().remove();
    // Do not propagate the event
    event.stopPropagation();
    // Prevent default behavior, in particular when we drop images or links
    event.preventDefault();


    // reset the visual look of the drop zone to default
    event.target.classList.remove('draggedOver');

    // get the files from the clipboard
    var files = event.dataTransfer.files;

    //check if image was drag and drop from another tab
    //var link = $(event.dataTransfer.getData('text/html'));
    var tab_files = [];
   // debugger;
    if (files.length == 0) {
        //this is either a link to an image or the base64 code
        var src = event.dataTransfer.getData('text/uri-list');//link.attr('src');
        //   debugger;
        if (src.substring(0, 4).indexOf("http") == 0) {
            // this is an url to an image, let's download it
            //TODO : I am not sure this can really happen
            imageURLToBlob(src); //.done(processData);

        } else {
            var blob = dataURItoBlob(src);
            tab_files.push(blob);
            uploadBlobs(tab_files);
        }
    } else {
        var filesLen = files.length;

        for (var i = 0; i < filesLen; i++) {
            tab_files.push(files[i]);
        }
        uploadBlobs(tab_files);
    }
}

function uploadBlobs(tab_files) {
    filesLen = tab_files.length;
    var filenames = "";
    // debugger;
    // iterate on the files, get details using the file API
    // Display file names in a list.
    for (var i = 0; i < filesLen; i++) {
        filenames += '\n' + tab_files[i].name;
        // Create a li, set its value to a file name, add it to the ol
        var li = document.createElement('div');
        li.textContent = tab_files[i].name + " (" + bytesToSize(tab_files[i].size) + ") ";
        //  document.getElementById("namesAllFiles").value += tab_files[i].name;
        // add a progress bar for this file upload
        var progressBar = document.createElement('progress');
        progressBar.id = "progressBar" + i;
        progressBar.value = 0;
        progressBar.max = 100;
        li.appendChild(progressBar);

        var status = document.createElement('div');
        status.setAttribute('id', 'lshStatus');
        li.appendChild(status);


        document.querySelector("#droppedFiles").appendChild(li);
        uploadOneFile(tab_files[i], i, filesLen);
        //  debugger;
        if (i + 1 < filesLen) {
            document.getElementById("namesAllFiles").value += "::"; //For split
        }
    }
    console.log(tab_files.length + ' file(s) have been dropped:\n' + filenames);
}

function displaySimilarImages(sourceSignature, object) {
    $('#duplicate_upload_result').children().remove();
    var sourceSigHTML = '<div  style="float:left; margin-left:10px"/><img class="pathlink" src="data:image;base64,' + sourceSignature + '" height="100" width="100"></div>';
    var sourceSig = document.createElement('div');
    sourceSig.style.float = "left";
    //sourceSig.style.marginLeft="10px";
    sourceSig.className = "signatureDiv";

    var sourceSigCanvas = new customCanvas("data:image;base64," + sourceSignature, 100, 100);
    sourceSig.appendChild(sourceSigCanvas.canvas);

    document.getElementById("duplicate_upload_source").appendChild(sourceSig);

    var ul = document.createElement('ul');
    ul.className = "thumbnails";


    for (f in object) {
        //we want to build elements with the following form
//        <li class="span4">
//            <div class="thumbnail">
//                <div class="container">
//                    <div class="row">
//                        <div class="span2">
//                           '<img class="pathlink" src="data:image;base64,{{base64Data}}" title="{{path}}"/>'
//                        </div>
//                        <div class="span2" >
//                           canvas
//                        </div>
//                    </div>
//                </div>
//            </div>
//            <div class="caption">description </div>
//           </div>
//        </li>


        var li = document.createElement('li');
        li.className = "span4";


        var thumb = document.createElement('div');
        thumb.className = "thumbnail";

        var container = document.createElement('div');
        container.className = "container";

        var row = document.createElement('div');
        row.className = "row";


        var spanImg = document.createElement('div');
        spanImg.className = "span2";

        var spanSig = document.createElement('div');
        spanSig.className = "span2";

        var caption = document.createElement('div');
        caption.className = "caption";
        caption.style.wordWrap = "break-word"

        var image = object[f];
        var distance = (image.distance);
        var templateThumbnail = '<img class="smallImage" src="data:image;base64,{{base64Data}}" title="{{path}}"/>';
        var imgTag = Mustache.to_html(templateThumbnail, image);
        spanImg.innerHTML = imgTag;

        row.appendChild(spanImg);
        row.appendChild(spanSig);

        container.appendChild(row);

        thumb.appendChild(container);


        li.appendChild(thumb);
        li.appendChild(caption);
        //build the description

        var sigTag = "data:image;base64," + image.base64Sig;
        var canv = new customCanvas(sigTag, 100, 100);
        spanSig.appendChild(canv.canvas);
        sourceSigCanvas.addOther(canv);

        caption.innerHTML = 'Distance:' + distance + ', Files in folder:  ' + image.foldersize + ' <br>  ' + toFolderAndFileLink(image.path) + '</a><br>';
//        $('#duplicate_upload_result').append(li);
        ul.appendChild(li)
    }


//    $('#duplicate_upload_result').wrap('<ul class="thumbnails"/>');
    $('#duplicate_upload_result').append(ul);

//    $('#duplicate_upload_result').append('</ul>');
    jQuery(document).ready(function () {
        generatePathLink();
        jQuery('.nailthumb-container').nailthumb();
        jQuery('.nailthumb-image-titles-animated-onhover').nailthumb();
        equalHeight($(".caption"));
    });
}