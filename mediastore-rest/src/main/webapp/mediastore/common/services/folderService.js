'use strict';

var service = angular.module('folders.service', []);

service.factory('FoldersFactory', [function(){
	var result = {}
	return {
		getSelectedFolders: function(){
			var inputs = $("input[name=folder]");	
			
			var folders = [];
			for (var i = 0; i < inputs.length; i++) {
			    if (inputs[i].checked) {
			        folders.push(inputs[i].value);
			    }
			}
			result.folders = folders;
			return result;
		}
	};
}]);
