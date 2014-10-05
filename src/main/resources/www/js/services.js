angular.module('SpraySpaDemo.services', []).
    factory('clientTypeService', function ($http) {

        var clientTypeAPI = {};

        clientTypeAPI.getTypes = function () {
            return $http({
                method: 'JSONP',
                url: '/clientTypes.json?callback=JSON_CALLBACK'
            });
        };

        clientTypeAPI.createClient = function (name, type) {
            return $http({
                method: 'POST',
                url: '/labour/' + name + "/" + type
            });
        };

        return clientTypeAPI;
    }).
    factory('arrowsService', function ($http) {

        var api = {};

        api.submitEvent = function (action, direction, name) {
            return $http({
                method: 'POST',
                url: '/arrowEvent/' + name + '/' + action + "/" + direction
            });
        };

        return api;
    }).
    factory('headService', function ($http) {

        var api = {};

        api.submitEvent = function (name, angle) {
            return $http({
                method: 'POST',
                url: '/angleEvent/' + name,
                params: { angle: angle }
            });
        };

        return api;
    }).
    factory('configService', function ($http) {

        var api = {};

        api.toggle = function (column, row, active) {
            var action = 'connect';
            if (active) action = 'dis' + action;
            return $http({
                method: 'POST',
                url: '/' + action + '/' + column + '/' + row
            });
        };

        return api;

    })
;