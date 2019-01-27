'use strict';
var App = angular.module('uploadApp', [ 'naif.base64' ]);

App.factory('cvsFilterService', [
		'$http',
		'$q',
		'urls',
		function($http, $q, urls) {

			var factory = {
				saveFileMultipart : saveFileMultipart,
				saveFileBase64 : saveFileBase64
			};

			return factory;

			function saveFileBase64(value) {
				var deferred = $q.defer();
				$http.post(urls.CVS_FILTER_URL + 'upload-base64',
						JSON.stringify(value),
						setContentTypeHeader("application/json")).then(function(response) {
							deferred.resolve(response.data);
						}, function(errResponse) {
							deferred.reject(errResponse.data);
						});
				return deferred.promise;
			};

			function saveFileMultipart(file) {
				var deferred = $q.defer();
				var formData = new FormData();
				formData.append('file', file);

				$http.post(urls.CVS_FILTER_URL + 'upload', formData,
						setContentTypeHeader(undefined)).then(function(response) {
							deferred.resolve(response.data);
						}, function(errResponse) {
							deferred.reject(errResponse.data);
						});
				return deferred.promise;
			};

			function setContentTypeHeader(contentType) {
				var config = {
					transformRequest : angular.identity,
					headers : {
						'Content-Type' : contentType
					}
				}
				return config;
			}

		} ]);