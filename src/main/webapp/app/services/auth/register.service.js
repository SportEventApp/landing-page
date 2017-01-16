(function () {
    'use strict';

    angular
        .module('tournoidevincennesApp')
        .factory('Register', Register);

    Register.$inject = ['$resource'];

    function Register ($resource) {
        return $resource('api/register', {}, {});
    }
})();
