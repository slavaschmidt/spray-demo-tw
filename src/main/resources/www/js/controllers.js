angular.module('SpraySpaDemo.controllers', []).
    controller('clientTypeController', function ($scope, $location, $rootScope, clientTypeService) {
        $scope.clientTypes = [
            {name: 'Button Director',   route: 'arrows' },
            {name: 'Head Director',     route: 'head'   },
            {name: 'Map Performer',     route: 'map'    },
            {name: 'Bar Performer',     route: 'bar'    },
            {name: 'Gauge Performer',   route: 'gauge'  }
        ];

        $scope.registerClient = function (item) {
            $rootScope.clientName = $scope.clientName;
            clientTypeService.createClient($scope.clientName, item.name);
            $location.path(item.route);
        };

    }).
    controller('arrowsController', function ($scope, $rootScope, arrowsService) {

        var action = function (action, direction) {
            arrowsService.submitEvent(action, direction, $rootScope.clientName);
        };
        $scope.pressed = function (direction) {
            return action('pressed', direction);
        };
        $scope.released = function (direction) {
            return action('released', direction);
        };

    }).
    controller('configController', function ($scope, $sce, $rootScope, configService) {

        $scope.headers  = [];
        $scope.rows     = [];
        $scope.config   = [];

        $scope.isActive = function (column, row) {
            return column !== "" && row !== "" & $scope.config[column][row];
        };

        $scope.active = function(column, row) {
            if ($scope.isActive(column, row)) return "btn active"; else return "btn";
        };
        $scope.connected = function(column, row) {
            if ($scope.isActive(column, row)) return "disconnect"; else return "connect";
        };

        $scope.source = new EventSource('/subscribe');

        $scope.source.onmessage = function (event) {
            $scope.$apply(function () {
                var data = JSON.parse(event.data);
                if (typeof data.columns !== "undefined") {
                    $scope.headers = data.columns;
                }
                if (typeof data.rows !== "undefined") {
                    $scope.rows = data.rows;
                }
                if (typeof data.config !== "undefined") {
                    $scope.config = data.config;
                }
            });
        };

        $scope.$on('$destroy', function iVeBeenDismissed() {
            $scope.source.close();
        });

        $scope.toggle = function(column, row) {
            configService.toggle(column, row, $scope.isActive(column, row))
        }
    }).
    controller('barController', function ($scope, $rootScope, configService) {

        $scope.rightStyle   = { width: 0 + '%'};
        $scope.leftStyle    = { width: 0 + '%', float: 'right'};

        $scope.source = new EventSource('/performer/' + $rootScope.clientName);

        $scope.source.onmessage = function (event) {
            $scope.$apply(function () {
                var data = JSON.parse(event.data);
                if (typeof data.direction !== "undefined") {
                    $scope.act(data)
                }
            });
        };
        $scope.$on('$destroy', function iVeBeenDismissed() {
            $scope.source.close();
        });

        $scope.act = function(data) {
            if (data.direction === 'right') {
                $scope.rightStyle = { width: data.speed + '%'};
                $scope.leftStyle =  { width: 0 + '%', float: 'right' };
            } else {
                $scope.leftStyle = { width: data.speed + '%', float: 'right'};
                $scope.rightStyle =  { width: 0 + '%'};
            }
        }
    })
;


