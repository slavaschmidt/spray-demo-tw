angular.module('SpraySpaDemo.controllers').
    controller('mapController', function ($scope, $rootScope) {

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
            clearInterval($scope.update);
        });

        $scope.act = function(data) {
            if (data.direction === 'right') {
                $scope.speed = data.speed;
            } else {
                $scope.speed = - data.speed;
            }
        };

        var canvas = document.getElementById("canvas");
        var ctx = canvas.getContext("2d");

        var W = canvas.width;
        var H = canvas.height;
        var bgcolor = "#AAA";
        var pointPosition = H/10*9;

        $scope.draw = function() {
            ctx.clearRect(0, 0, W, H);
            ctx.strokeStyle = bgcolor;
            ctx.lineWidth = 3;
            ctx.beginPath();
            for (var i = 0; i <= $scope.positions.length - 1; i++) {
                ctx.lineTo($scope.positions[i] + W/2, i);
                ctx.moveTo($scope.positions[i] + W/2, i);
            }
            ctx.arc($scope.positions[$scope.positions.length - 1] + W/2, pointPosition, 6, -0.5 * Math.PI, 1.5 * Math.PI, false);
            ctx.stroke();
            ctx.moveTo($scope.positions[$scope.positions.length - 1] + W/2, pointPosition);
            ctx.fillStyle = 'lightgreen';
            ctx.fill();
        };

        $scope.update = function() {
            var position = $scope.positions[$scope.positions.length-1] + $scope.speed / 20;
            $scope.positions.push(position);
            $scope.positions.shift();
            $scope.draw();
        };

        $scope.init = function() {
            $scope.speed = 0;
            $scope.positions = [];
            for (var i = 0; i <= pointPosition; i++) {
                $scope.positions.push(0);
            }
            setInterval($scope.update, 20);
        };

        $scope.init();
    });
