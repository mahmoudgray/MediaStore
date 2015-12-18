/**
 * Created by mahmo_000 on 27/11/2015.
 */
'use strict';

(function(){
    var app = angular.module('thumbnail.ctrl', []);

    app.controller('ThumbnailCtrl',  ['$scope', '$http','FoldersFactory', function($scope, $http, FoldersFactory){
        var app = this;
        $scope.input = "";
        $scope.showThumbnail = false;
       // $scope.gps = false;
        $scope.getAll = function () {

            var folders = {folders: []}
            var request = {folder: JSON.stringify(folders), filter: $scope.input, gps: false}

            $.getJSON('rest/hello/getAll',request, function(data){
                $scope.images = data;
                $scope.resetSearch();
            });
        };

        $scope.getGallery = function () {

            var folders = {folders: []}
            var request = {folder: JSON.stringify(folders), filter: $scope.input, gps: false}

            $.getJSON('rest/hello/getAll',request, function(data){
                $scope.images = data;
                $scope.resetSearch();
            });
        };

        $scope.deleteImage = function(path){
            alert("delete", path);
        };

        $scope.getFolders = function () {
            $scope.paths = FoldersFactory.getSelectedFolders();
        };

        $scope.resetSearch = function(){
            $scope.$apply();
            $scope.input = "";
            //$scope.gps = false;
        };

       
        /**
        $scope.generateDeleteLink = function(par){
            FoldersFactory.callDelete(par);
            $scope.getAll();
        }**/
        $scope.generatePathLink1 = function(path){
            FoldersFactory.callOpen(path, null);
        }

        $scope.generatePathLink2 = function(path){
            var folder = FoldersFactory.getFolder(path);
            FoldersFactory.callOpen(folder, null);
        }

        $scope.generateThumbnailLink = function(){
            $scope.showThumbnail = !$scope.showThumbnail;
        }

        $scope.getFolders();
    }]);
})();