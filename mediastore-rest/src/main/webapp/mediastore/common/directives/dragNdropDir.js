(function () {
	// body...
	var app = angular.module('dragndrop.dir', []);

	app.directive('dragNdrop', ['$http', function($http){
		return {
			restrict: 'A',
			link: function(scope, element, attrs){

			}
		};
	}]);
})();