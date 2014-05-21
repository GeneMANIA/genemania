var gulp = require('gulp');
var ngHtml2Js = require('gulp-ng-html2js');
var ngmin = require('gulp-ngmin');
var uglify = require('gulp-uglify');
var concat = require('gulp-concat');
var clean = require('gulp-clean');
var inject = require('gulp-inject');
var open = require('gulp-open');
var shell = require('gulp-shell');
var exclude = require('gulp-ignore').exclude;
var tap = require('gulp-tap');
var path = require('path');
var rename = require('gulp-rename');
var mincss = require('gulp-minify-css');

var paths = {
  js: [
    './js/lib/jquery-*.js',
    './js/lib/fastclick.js',
    './js/lib/angular.js',
    './js-build/angular-templates.js', // include templates if built
    './ui/js/lib/*.js',
    './js/conf.js',
    './js/website-conf.js', // so if website conf exists, then it overwrites the local conf
    './js/app.js',
    './js/*.js'
  ],

  css: [
    './css/reset.css',
    './css/font-awesome.css',
    './css/jquery.qtip.css',
    './css/app.css'
  ],

  website: '../website/src/main/webapp/'
};

// clean built files
gulp.task('clean', ['htmlrefs'], function(){
  return gulp.src([ './js-build', './js/website-conf.js', './css-build' ])
    .pipe( clean() );
  ;
});

// deploy to genemania website java project
gulp.task('website', ['websiteconf'], function(){
  gulp.src('./js/website/*.js')
    .pipe( gulp.dest('./js') );
  ;
});

// clean website of built resources
gulp.task('websiteclean', function(){
  gulp.src( paths.website )
    .pipe( clean() )
  ;
});

// place website conf in js dir
gulp.task('websiteconf', function(){
  gulp.src('./js/website/*')
    .pipe( gulp.dest('./js') );
  ;
});

// build minified ui 
gulp.task('minify', ['htmlminrefs'], function(next){
  next();
});

function htmlrefs(){
  return gulp.src( './index.html' )
    .pipe(inject( gulp.src(paths.js.concat(paths.css), { read: false }), {
      addRootSlash: false
    } ))

    .pipe( gulp.dest('.') )
  ;
}

// TODO update all refs
gulp.task('refs');

// update path refs
gulp.task('htmlrefs', function(){
  return htmlrefs();
});

// update refs and include cached templates
gulp.task('htmltemplatesref', ['templates'], function(){
  return htmlrefs();
});

// update path refs with minified files
gulp.task('htmlminrefs', ['templates', 'js', 'css'], function(){

  return gulp.src( './index.html' )
    .pipe(inject( gulp.src(['./js-build/all.min.js', './css-build/all.min.css'], { read: false }), {
      addRootSlash: false
    } ))

    .pipe( gulp.dest('.') )
  ;

});

// build cached templates
gulp.task('templates', function(){

  return gulp.src( './templates/*.html' )
    .pipe( ngHtml2Js({
        moduleName: 'templates',
        prefix: 'templates/'
    }) )

    .pipe( ngmin() )

    .pipe( concat('angular-templates.js') )

    .pipe( gulp.dest('./js-build') )
  ;

});

// build minified js
gulp.task('js', ['templates'], function(){

  return gulp.src( paths.js )
    .pipe( concat('all.min.js') )

    .pipe( uglify() )

    .pipe( gulp.dest('./js-build') )
  ;

});

// TODO minify css
gulp.task('css', function(){
  return gulp.src( paths.css )
    .pipe( concat('all.min.css') )

    .pipe( mincss() )

    .pipe( gulp.dest('./css-build') )
  ;
});