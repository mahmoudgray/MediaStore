'use strict';

(function() {
	// body...

	var app = angular.module('header.controller', []);

	app.controller('HeaderController', ['$scope', '$location', function($scope, $location){

		$scope.isActive = function(viewLocation){
			return viewLocation === $location.path();
		};
	}]);
})();