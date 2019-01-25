'use strict';
var App = angular.module('uploadApp', [ 'naif.base64' ]);

App.factory('docService', [ '$http', '$q', 'urls', function($http, $q, urls) {

	var factory = {
		saveDoc : saveDoc,
		saveDocBase64 : saveDocBase64
	};

	return factory;

	function saveDocBase64(value) {
		var deferred = $q.defer();
		$http.post(urls.DOC_URL + 'upload-base64', JSON.stringify(value), {
			headers : {
				'Content-Type' : "application/json"
			}
		}).then(function(response) {
			deferred.resolve(response.data);
		}, function(errResponse) {
			alert(errResponse.data.errorMessage);
			deferred.reject(errResponse);
		});
		return deferred.promise;
	};

	function saveDoc(file) {
		var deferred = $q.defer();
		var formData = new FormData();
		formData.append('file', file);

		$http.post(urls.DOC_URL + 'upload', formData, {
			transformRequest : angular.identity,
			headers : {
				'Content-Type' : undefined
			}
		}).then(function(response) {
			deferred.resolve(response.data);
		}, function(errResponse) {
			alert(errResponse.data.errorMessage);
			deferred.reject(errResponse);
		});
		return deferred.promise;
	};
} ]);