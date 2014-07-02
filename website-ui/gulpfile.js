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
var livereload = require('gulp-livereload');
var less = require('gulp-less');
var runSequence = require('run-sequence');
var watch = require('gulp-watch');
var plumber = require('gulp-plumber');
var shell = require('gulp-shell');
var gulpif = require('gulp-if');
var combine = require('stream-combiner');

var paths = {
  js: [
    './js/lib/jquery-*.js',
    './js/lib/fastclick.js',
    './js/lib/angular.js',
    './js-build/angular-templates.js', // include templates if built
    './js/lib/*.js',
    './js/config.js',
    './js/website-config.js', // so if website config exists, then it overwrites the local config
    './js/app.js',
    './js/*.js'
  ],

  debug: [
    './js/debug/livereload.js' // for live reloading when source files change
  ],

  css: [
    './css/constants.less',
    './css/reset.css',
    './css/lesshat.less',
    './css/font-awesome.css',
    './css/jquery.qtip.css',
    './css/app.less',
    './css/data.less',
    './css/cytoscape.less'
  ]
};

var debugLessOpts = {
  paths: ['./css'],
  sourceMap: true,
  relativeUrls: true,
  sourceMapRootpath: '../',
  sourceMapBasepath: process.cwd()
};

function handleError(err) {
  console.log(err.toString());
  this.emit('end');
}

// map raw css/less files to built files
paths.cssBuild = paths.css.map(function( path ){
  path = path.replace('./css/', './css-build/');
  path = path.replace('.less', '.css');

  return path;
});

function isLessFile( file ){
  return file.path.match('.less');
}

gulp.task('default', ['watch']);

// clean built files
gulp.task('clean', ['htmlrefs'], function(){
  return gulp.src([ './js-build', './js/website-config.js', './css-build' ])
    .pipe( clean() );
  ;
});

// compile java projects for debugging
gulp.task( 'javac', shell.task([
  'export PRIVATE_REPO=' + path.resolve( process.cwd(), '../../genemania-private' ),
  'mvn install -pl website -am -P dev-debug'
], { cwd: '..' }) );

// use website config
gulp.task('websiteconfig', function(){
  gulp.src('./js/website/*.js')
    .pipe( gulp.dest('./js') );
  ;
});

// minified website config
gulp.task('website', function( next ){
  return runSequence( 'websiteconfig', 'minify', next );
});

// build minified ui 
gulp.task('minify', ['htmlminrefs'], function(next){
  next();
});

// update path refs
gulp.task('htmlrefs', function(){
  return gulp.src( './index.html' )
    .pipe(inject( gulp.src(paths.js.concat(paths.debug).concat(paths.cssBuild), { read: false }), {
      addRootSlash: false
    } ))

    .pipe( gulp.dest('.') )
  ;
});

// update refs and include cached templates
gulp.task('htmltemplatesref', ['templates'], function(next){
  return runSequence( 'htmlrefs', next );
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


// less => css
gulp.task('less', function(){
  return gulp.src( paths.css )
    .pipe( 
      gulpif( isLessFile, less(debugLessOpts) )
    )
    .pipe( gulp.dest('./css-build') )
  ;
});

gulp.task('safeless', function(){
  var all = combine(
    gulp.src( paths.css )
      .pipe( 
        gulpif( isLessFile, less(debugLessOpts) )
      ).on('error', handleError)

      .pipe( gulp.dest('./css-build') )
  );

  all.on('error', function(err){
    console.warn(err);
  });

  return all;
});

// minify css
gulp.task('css', function(){
  return gulp.src( paths.css )

    .pipe( less() )

    .pipe( concat('all.min.css') )

    .pipe( mincss() )

    .pipe( gulp.dest('./css-build') )
  ;
});

gulp.task('prewatch', function( next ){
  return runSequence( 'less', 'htmlrefs', next );
});

gulp.task('watch', ['prewatch'], function(){
  livereload.listen();

  // reload all when page or js changed
  gulp.watch( ['index.html'].concat(paths.js) )
    .on('change', livereload.changed)
  ;

  // rebuild less files on a per-file basis
  gulp.src( paths.css )
    .pipe( watch() )
    .pipe( plumber() )
    .pipe( 
      gulpif( isLessFile, less(debugLessOpts) )
    )
    .pipe( gulp.dest('./css-build') )
  ;

  // rebuild all less when at least one file changed
  // gulp.watch( paths.css, ['safeless'] );

  // reload all css when any css changed
  gulp.watch( paths.cssBuild )
    .on('change', livereload.changed)
  ;
});