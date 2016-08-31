// need to run in terminal at angular-seed/src/js location
// > npm install
// > grunt default
module.exports = function(grunt) {

    grunt.initConfig({
        pkg: grunt.file.readJSON('package.json'),
        connect:{
            example:{
                port:9000,
                // base:'/src'
                base:'.'
            }
        }
    });

    grunt.loadNpmTasks('grunt-connect');
    grunt.registerTask('default', 'connect:example');

};