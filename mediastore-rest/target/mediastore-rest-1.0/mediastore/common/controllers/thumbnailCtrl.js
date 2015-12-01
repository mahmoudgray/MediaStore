/**
 * Created by mahmo_000 on 27/11/2015.
 */
'use strict';

(function(){
    var app = angular.module('thumbnail.ctrl', []);

    app.run(function ($rootScope, $templateCache) {
        $rootScope.$on('$viewContentLoaded', function () {
            $templateCache.removeAll();
        });
    });

    app.controller('ThumbnailCtrl',  ['$scope', '$http', function($scope, $http){
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

        $scope.resetSearch = function(){
            $scope.$apply();
            $scope.input = "";
            //$scope.gps = false;
        }
    }]);
})();