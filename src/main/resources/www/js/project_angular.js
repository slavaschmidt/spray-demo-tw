var routerConfig = function($routeProvider, $locationProvider) {
  $routeProvider.when('/', {
    templateUrl: 'assets/html/list.html',
    controller: ProjectListCtrl,
    controllerAs: 'projects'
  });
  $routeProvider.
    when('/project/new',              { controller: ProjectCreateCtrl,  templateUrl: 'assets/html/detail.html' }).
    when('/project/edit/:projectId',  { controller: ProjectEditCtrl,    templateUrl: 'assets/html/detail.html' }).
    when('/project/files/:path',      { controller: ProjectViewCtrl,    templateUrl: 'assets/html/files.html' }).
    when('/interaction',              { controller: InteractionViewCtrl,  templateUrl: 'assets/html/interaction.html' }).
    when('/scan_path',                { controller: InteractionViewCtrl,  templateUrl: 'assets/html/scan_path.html' }).
    when('/perception_curve',              { controller: InteractionViewCtrl,  templateUrl: 'assets/html/perception_curve.html' }).

    otherwise({ redirectTo: '/' })
  ;

  /*
  $routeProvider.when('/Book/:bookId/ch/:chapterId', {
    templateUrl: 'chapter.html',
    controller: ChapterCntl,
    controllerAs: 'chapter'
  });
  */
};

var projectsApp = angular.module('project', ['ngRoute'], routerConfig);

function ProjectListCtrl($scope, $http) {
  $http.get('/projects').success(function (data) {
    $scope.projects = data;
  });

  $scope.orderProp = 'accessed';
}

function ProjectViewCtrl($scope, $routeParams, $http, $route, $location) {
  var filesUrl = '/project/files/' + $routeParams.path;
  var toList = function() { $route.reload() };
  $http.get(filesUrl).success(function (data) {
    $scope.files = data.files;
    $scope.level = data.level;
    $scope.path = data.path;
    $scope.isTopLevel = function() { return $scope.level == 1; }
    $scope.create = function() {
      $http.post(filesUrl + "/" + $scope.path + "/" + $scope.fileName).success(toList).error(toList);
    }
    $scope.levelUp = function() {
      $location.path('/project/files/' + $routeParams.path.substring(0, $routeParams.path.lastIndexOf('|')));
    }
  });
  $scope.orderProp = 'accessed';
}

function ProjectCreateCtrl($scope, $location, $http) {
  var serviceUrl = '/project/';
  var toList = function() { $location.path('/'); };
  $scope.project = {};
  $scope.isClean = function () {
    return false;
  };
  $scope.destroyable = function() { false; }
  $scope.save = function () {
      $http.put(serviceUrl, $scope.project).success(toList).error(toList);
  }
}

function ProjectEditCtrl($scope, $location, $routeParams, $http) {
  var serviceUrl = '/project/' + $routeParams.projectId;
  var toList = function() { $location.path('/'); };
  $http.get(serviceUrl).success(function (data) {
    $scope.project = angular.copy(data);
    $scope.isClean = function () {
      return angular.equals(data, $scope.project);
    };
    $scope.destroy = function () {
      $http.delete(serviceUrl).success(toList).error(toList);
    };
    $scope.destroyable = $scope.isClean
    $scope.save = function () {
      if ($routeParams.projectId != undefined) {
        $http.post(serviceUrl, $scope.project).success(toList).error(toList);
      } else {
        $http.put(serviceUrl, $scope.project).success(toList).error(toList);
      }
    };
  });
}


function InteractionViewCtrl($routeParams, $scope) {
  var filesUrl = '/interaction/' + $routeParams.path;
  $scope.orderProp = 'accessed';
}