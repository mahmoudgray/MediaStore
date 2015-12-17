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
		},
		callOpen: function(para1, para2) {
		    //  debugger;
		    var result = {}
		    var folders = [];
		    if (para1 != null) {
		        folders.push(para1);
		    }
		    if (para2 != null) {
		        folders.push(para2);
		    }

		    result.folders = folders;
		    $.getJSON("rest/hello/open", {path:JSON.stringify(result)});
		},

		callDelete: function(par){
			$.get("rest/hello/trash", {path:par});
		}
	};
}]);
