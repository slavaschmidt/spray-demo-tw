angular.module('SpraySpaDemo.controllers').
controller('headController', function ($scope, $rootScope, headService) {
    var sampling = 10;
    var current = [];

    $scope.submitHeadEvent = function (event) {
        if (event.detection == "CS") {
            var angle = Number(event.angle * (180 / Math.PI) - 90);
            current.push(angle);
            if (current.length === sampling) {
                angle = 0;
                for (var i=0;i<current.length;i++) {
                    angle += current[i];
                }
                headService.submitEvent($rootScope.clientName, angle / current.length);
                current = [];
            }
        }
    };

    $scope.stopAll = function() {
        $scope.htracker.stop();
        document.removeEventListener("headtrackrStatus", $scope.headTrackerStatus);
        document.removeEventListener("facetrackingEvent", $scope.drawHeadTracking);
        document.removeEventListener("facetrackingEvent", $scope.submitHeadEvent);
    };

    $scope.$on('$destroy', $scope.stopAll);

    $scope.init = function () {
        var videoInput      = document.getElementById('vid');
        var canvasInput     = document.getElementById('compare');
        var debugOverlay    = document.getElementById('debug');
        if (typeof videoInput === "undefined" || typeof canvasInput === "undefined" || typeof debugOverlay === "undefined") return;

        var htracker = new headtrackr.Tracker({altVideo: {ogv: "", mp4: ""}, calcAngles: true, ui: false, headPosition: false, debug: debugOverlay});
        htracker.init(videoInput, canvasInput);
        htracker.start();

        document.addEventListener("headtrackrStatus", $scope.headTrackerStatus, true);
        document.addEventListener("facetrackingEvent", $scope.drawHeadTracking);
        document.addEventListener("facetrackingEvent", $scope.submitHeadEvent);

        return htracker;
    };

    $scope.headTrackerStatus = function (event) {
        var statusMessages = {
            "whitebalance": "Проверка камеры или баланса белого",
            "detecting": "Обнаружено лицо",
            "hints": "Что-то не так, обнаружение затянулось",
            "redetecting": "Лицо потеряно, поиск..",
            "lost": "Лицо потеряно",
            "found": "Слежение за лицом"
        };

        var supportMessages = {
            "no getUserMedia": "Браузер не поддерживает getUserMedia",
            "no camera": "Не обнаружена камера."
        };

        if (event.status in supportMessages) {
            var messagep = document.getElementById('gUMMessage');
            messagep.innerHTML = supportMessages[event.status];
        } else if (event.status in statusMessages) {
            var messagep = document.getElementById('headtrackerMessage');
            messagep.innerHTML = statusMessages[event.status];
        }
    };

    $scope.drawHeadTracking = function (event) {
        var canvasOverlay = document.getElementById('overlay');
        var overlayContext = canvasOverlay.getContext('2d');
        overlayContext.clearRect(0, 0, 320, 240);
        // once we have stable tracking, draw rectangle
        if (event.detection == "CS") {
            overlayContext.translate(event.x, event.y);
            overlayContext.rotate(event.angle - (Math.PI / 2));
            overlayContext.strokeStyle = "#CC0000";
            overlayContext.strokeRect((-(event.width / 2)) >> 0, (-(event.height / 2)) >> 0, event.width, event.height);
            overlayContext.rotate((Math.PI / 2) - event.angle);
            overlayContext.translate(-event.x, -event.y);
            document.getElementById('ang').innerHTML = Number(event.angle * (180 / Math.PI) - 90);
        }
    };

    $scope.stopTracking = function() {
        $scope.stopAll();
    };
    $scope.restart = function() {
        $scope.htracker.stop();
        $scope.htracker.start();
    };

    $scope.htracker = $scope.init();

})
;
