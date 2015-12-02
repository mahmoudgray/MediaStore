/**
 * Created by mahmo_000 on 29/11/2015.
 */
'use strict';


(function(){

    var app = angular.module('duplicatefolders.ctrl', []);

    app.controller('DuplicateFoldersCtrl', ['$scope', '$http', 'FoldersFactory', function($scope, $http, FoldersFactory){
        var app = this;

        $scope.getDuplicateFolders = function(){

            var folders = {folders: ["folder1", "folder2"]}
            //var folders = FoldersFactory.getSelectedFolders();
            var request = {folder: JSON.stringify(folders)}

            $.getJSON('rest/hello/duplicateFolder', request, function(data){
                $scope.result = data;
                console.log($scope.result);
            });
        };
    }]);
})();