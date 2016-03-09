'use strict';

angular.module('mediastore',  ['ngRoute',
    'header.controller',
    'folders.service',
    'fileupload.directive',
    'folders.directive',
    'dragndrop.ctrl',
    'thumbstore.ctrl',
    'dragndrop.controller',
    'thumbnail.ctrl',
    'duplicatefolders.ctrl',
    'duplicatemedias.ctrl']).

config(['$routeProvider', function($routeProvider){
    $routeProvider.when('/', {templateUrl:'mediastore/app/welcome/thumbstore-control.html', controller:'ThumbstoreCtrl'});
    $routeProvider.when('/thumbnail-list', {templateUrl:'mediastore/app/welcome/thumbnail-list.html', controller:'ThumbnailCtrl'});
    $routeProvider.when('/thumbnail-gallery', {templateUrl:'mediastore/app/welcome/thumbnail-gallery.html', controller:'ThumbnailCtrl'});
    $routeProvider.when('/duplicate-medias', {templateUrl:'mediastore/app/welcome/duplicate-medias.html', controller:'DuplicateMediasCtrl'});
    $routeProvider.when('/duplicate-folders', {templateUrl:'mediastore/app/welcome/duplicate-folders.html', controller:'DuplicateFoldersCtrl'});
    $routeProvider.when('/dragndrop', {templateUrl:'mediastore/app/welcome/dragndrop.html', controller:'DragndropController'});
    $routeProvider.when('/thumbnail-drag', {templateUrl:'mediastore/app/welcome/thumbnail-drag.html', controller:'DragNdropCtrl'});
    $routeProvider.otherwise({redirectTo : '/'});
}]);
