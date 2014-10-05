angular.module('SpraySpaDemo', [
  'SpraySpaDemo.controllers',
  'SpraySpaDemo.services',
  'ngRoute'
]).
config(['$routeProvider', function($routeProvider) {
  $routeProvider.
	when("/welcome", {templateUrl: "partials/welcome.html", controller: "clientTypeController"}).
	when("/config",  {templateUrl: "partials/config.html", controller: "configController"}).
	when("/arrows",  {templateUrl: "partials/arrows.html", controller: "arrowsController"}).
	when("/head",    {templateUrl: "partials/head.html", controller: "headController"}).
	when("/map",     {templateUrl: "partials/mapclient.html", controller: "mapController"}).
	when("/bar",     {templateUrl: "partials/barclient.html", controller: "barController"}).
    when("/gauge",   {templateUrl: "partials/gauge.html", controller: "gaugeController"}).
	otherwise({redirectTo: '/welcome'});
}]);