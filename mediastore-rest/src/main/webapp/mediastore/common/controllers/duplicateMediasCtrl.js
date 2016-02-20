/**
 * Created by mahmoud on 29/11/2015.
 */

'use strict';

(function(){

	var app = angular.module('duplicatemedias.ctrl', []);

	app.controller('DuplicateMediasCtrl', ['$scope', 'FoldersFactory', function($scope, FoldersFactory){

		var app = this;

		$scope.input = "5";

		$scope.getDuplicateMedias = function(){
			//var folders = getSelectedFolders();

			var folders = {folders: ["folder1", "folder2"]}	
			var requestParam = {
				max: $scope.input,
				folder: JSON.stringify(folders)
			}

			$.get("rest/hello/identical", requestParam, function(data){
					$scope.result = data;
					console.log(data);
					$scope.resetForm();
				});
		};

		$scope.resetForm = function(){
			$scope.$apply();
			$scope.input = "";
		};

		$scope.generatePathLink1 = function(path){
			FoldersFactory.callOpen(path, null);
		}

		$scope.generatePathLink2 = function(path){
			var folder = FoldersFactory.getFolder(path);
			FoldersFactory.callOpen(folder, null);
		}

		$scope.generateDeleteLink = function(par){
			FoldersFactory.callDelete(par);
			$scope.getDuplicateMedias();
		}

	}]);
})();