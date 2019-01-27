App.controller('fileController', [
		'$scope',
		'$rootScope',
		'cvsFilterService',
		'$http',
		function($scope, $rootScope, cvsFilterService, $http) {

			$scope.uploadBase64 = function() {
				cvsFilterService.saveFileBase64($scope.fileBase64Model).then(
						function(response) {
							$rootScope.rowList = response;
						}, function(errResponse) {
							$rootScope.rowList = errResponse;
						});
			}
			
			$scope.upload = function() {
				var file = $scope.file;
				cvsFilterService.saveFileMultipart(file).then(
						function(response) {
							$rootScope.rowList = response;
						}, function(errResponse) {
							$rootScope.rowList = errResponse;
						});
			}
		} ]);

App.constant('urls', {
	CVS_FILTER_URL : 'http://localhost:8080/api/cvs-filter/'
});

App.directive('fileModel', [ '$parse', function($parse) {
	return {
		restrict : 'A',
		link : function(scope, element, attrs) {
			var model = $parse(attrs.fileModel);
			var modelSetter = model.assign;

			element.bind('change', function() {
				scope.$apply(function() {
					modelSetter(scope, element[0].files[0]);
				});
			});
		}
	};
} ]);

