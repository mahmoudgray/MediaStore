'use strict';

(function () {
	
	var app = angular.module('dragndrop.ctrl', ['angularFileUpload']);


	app.factory('uploadService', ['$rootScope', function ($rootScope) {

    return {
        send: function (file) {
            var data = new FormData(),
                xhr = new XMLHttpRequest();

            // When the request starts.
            xhr.onloadstart = function () {
                console.log('Factory: upload started: ', file.name);
                $rootScope.$emit('upload:loadstart', xhr);
            };

            // When the request has failed.
            xhr.onerror = function (e) {
                $rootScope.$emit('upload:error', e);
            };

            // Send to server, where we can then access it with $_FILES['file].
            data.append('file', file, file.name);
            xhr.open('POST', 'rest/hello/findSimilar');
            xhr.send(data);
        }
    };

}]);

	app.controller('DragNdropCtrl', ['$scope', '$http', 'FileUploader', 'uploadService', function($scope, $http, FileUploader, uploadService){
		
		var app = this;

		var uploader = $scope.uploader = new FileUploader({
            url: 'upload.php'
        });

        // FILTERS

        uploader.filters.push({
            name: 'imageFilter',
            fn: function(item /*{File|FileLikeObject}*/, options) {
                var type = '|' + item.type.slice(item.type.lastIndexOf('/') + 1) + '|';
                return '|jpg|png|jpeg|bmp|gif|'.indexOf(type) !== -1;
            }
        });

        // CALLBACKS

        uploader.onWhenAddingFileFailed = function(item /*{File|FileLikeObject}*/, filter, options) {
            console.info('onWhenAddingFileFailed', item, filter, options);
        };
        uploader.onAfterAddingFile = function(fileItem) {
            console.info('onAfterAddingFile', fileItem);
        };
        uploader.onAfterAddingAll = function(addedFileItems) {
            console.info('onAfterAddingAll', addedFileItems);
        };
        uploader.onBeforeUploadItem = function(item) {
            console.info('onBeforeUploadItem', item);
        };
        uploader.onProgressItem = function(fileItem, progress) {
            console.info('onProgressItem', fileItem, progress);
        };
        uploader.onProgressAll = function(progress) {
            console.info('onProgressAll', progress);
        };
        uploader.onSuccessItem = function(fileItem, response, status, headers) {
            console.info('onSuccessItem', fileItem, response, status, headers);
        };
        uploader.onErrorItem = function(fileItem, response, status, headers) {
            console.info('onErrorItem', fileItem, response, status, headers);
        };
        uploader.onCancelItem = function(fileItem, response, status, headers) {
            console.info('onCancelItem', fileItem, response, status, headers);
        };
        uploader.onCompleteItem = function(fileItem, response, status, headers) {
            console.info('onCompleteItem', fileItem, response, status, headers);
        };
        uploader.onCompleteAll = function() {
            console.info('onCompleteAll');
        };

        console.info('uploader', uploader);


        $scope.result = [];
        $scope.uploadOneFile = function(file, indice, fileLen){
            var xhr = new XMLHttpRequest();
            xhr.open('POST', 'rest/hello/findSimilar');
            xhr.setRequestHeader("X_FILENAME", file.name);
            /*
            var progressBar = document.getElementById("progressBar" + indice);
            xhr.upload.onprogress = function (e) {
                //  debugger;
                progressBar.value = e.loaded;
                progressBar.max = e.total;

            };*/
            xhr.onreadystatechange = function (e) {
                if (xhr.readyState == 4) {
                    //    debugger;
                    // continue only if HTTP status is "OK"
                    alert("inside first IF "+xhr.status);

                    if (xhr.status == 200) {
                    	//console.log(jQuery.parseJSON(xhr.responseText));
                        //      debugger;
//                    alert("finished!");
                        alert("Inside Status 200");
                        //$scope.result.push(jQuery.parseJSON(xhr.responseText))
                        //localFinished(0, 0, jQuery.parseJSON(xhr.responseText));
                    }
                }
            };

            // Send the Ajax request
            //debugger;
            //displaySrcImage(file);
            xhr.send(file);
        };

        $scope.uploadBlobs = function(){
        	//$scope.clearResult();
        	var tab = uploader.queue;
        	var nbFiles = uploader.queue.length;
        	
            for(var i in tab){
                console.log("-> ", i);
            	var file = tab[i].file;
                console.log(file);
            	$scope.uploadOneFile(file, i, nbFiles);
            	//uploadService.send(file);
            }

            console.log("result_founds : ", $scope.result);
        };

        $scope.clearResult = function(){
        	$scope.result = [];
        }
	}]);
})();