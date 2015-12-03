/**
 * Created by mahmo_000 on 27/11/2015.
 */
'use strict';

(function(){
    var app = angular.module('thumbnail.ctrl', []);

    app.controller('ThumbnailCtrl',  ['$scope', '$http','FoldersFactory', function($scope, $http, FoldersFactory){
        var app = this;
        $scope.input = "";
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
        $scope.getFolders();
    }]);
})();