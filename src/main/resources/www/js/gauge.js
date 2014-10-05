angular.module('SpraySpaDemo.controllers').
    controller('gaugeController', function ($scope, $rootScope) {

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
                $scope.counterClockwise = false;
                $scope.new_degrees = data.speed / 100 * 180;
                $scope.color = 'red';
            } else {
                $scope.counterClockwise = true;
                $scope.new_degrees = - data.speed / 100 * 180;
                $scope.color = 'lightgreen';

            }
            $scope.draw();
        };

        var canvas = document.getElementById("canvas");
        var ctx = canvas.getContext("2d");
        var W = canvas.width;
        var H = canvas.height;
        var degrees = 0;
        var bgcolor = "#222";

        $scope.init = function() {
            ctx.clearRect(0, 0, W, H);

            ctx.beginPath();
            ctx.strokeStyle = bgcolor;
            ctx.lineWidth = 30;
            ctx.arc(W/2, H/2, 100, 0, Math.PI*2, false);
            ctx.stroke();

            var radians = degrees * Math.PI / 180;
            ctx.beginPath();
            ctx.strokeStyle = $scope.color;
            ctx.lineWidth = 30;
            ctx.arc(W/2, H/2, 100, 0 - 90 * Math.PI/180, radians - 90 * Math.PI/180, $scope.counterClockwise);
            ctx.stroke();
        };

        $scope.draw = function() {
            if(typeof $scope.animation_loop != "undefined") clearInterval($scope.animation_loop);
            var difference = $scope.new_degrees - degrees;
            $scope.animation_loop = setInterval($scope.animate_to, 1000/difference);
        };

        $scope.animate_to = function(){
            if(degrees == $scope.new_degrees) clearInterval($scope.animation_loop);
            if(degrees < $scope.new_degrees) degrees++; else if(degrees > $scope.new_degrees) degrees--;
            $scope.init();
        };



    });
