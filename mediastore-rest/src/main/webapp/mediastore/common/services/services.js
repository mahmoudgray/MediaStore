
'use strict';

(function() {
	// body...
	var app = angular.module('medias.services', ['ngResource']);

	app.service('ThumbnailFactory', ['$resource', function(){

		return $resource("rest/hello", null, {'update':{method:'PUT'}});

	}]);
})();