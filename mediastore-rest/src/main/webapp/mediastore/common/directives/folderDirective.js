'use strict';

var app = angular.module('folders.directive', []);

app.directive('mediaFolder', [function() {
	
	return {
		template: '<div id="db_paths" style="margin-bottom: 0"  ng-repeat="p in paths"><label><input type="checkbox" ng-model="path" name="folder" checked="true" value="{{p}}">{{p}}</label><br></div>'
	};
}]);
