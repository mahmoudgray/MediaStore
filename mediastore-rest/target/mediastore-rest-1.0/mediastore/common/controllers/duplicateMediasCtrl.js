/**
 * Created by mahmoud on 29/11/2015.
 */

'use strict';

(function(){

	var app = angular.module('duplicatemedias.ctrl', []);

	app.controller('DuplicateMediasCtrl', ['$scope', function($scope){

		var app = this;

		$scope.input = "";

		$scope.getDuplicateMedias = function(){
			//var folders = getSelectedFolders();

			var folders = {folders: []}	
			var requestParam = {
				max: $scope.max,
				folder: JSON.stringify(folders)
			}

			$.get("rest/hello/identical", requestParam, function(data){
					$scope.result = data;
					$scope.resetForm();
				});
		};

		$scope.resetForm = function(){
			$scope.$apply();
			$scope.input = "";
		};

	}]);
})();