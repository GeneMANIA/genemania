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
var replace = require('gulp-replace');

var paths = {
  js: [
    './js/lib/jquery-*.js',
    './js/lib/fastclick.js',
    './js/lib/angular.js',
    './js-build/angular-templates.js', // include templates if built
    './js/lib/*.js',
    './js/config.js',
    './js/website-config.js', // so if website config exists, then it overwrites the local config
    './js/pubsub.js',
    './js/app.js',
    './js/app-*.js',
    './js/*.js'
  ],

  debug: [
    './js/debug/livereload.js' // for live reloading when source files change
  ],

  css: [
    './css/constants.less',
    './css/reset.css',
    './css/lesshat.less',
    './css/animate.less',
    './css/animate-extras.less',
    './css/font-awesome.css',
    './css/bio-icons.css',
    './css/jquery.qtip.css',
    './css/qtip-config.less',
    './css/ng-animations.less',
    './css/popover.less',
    './css/widgets.less',
    './css/app.less',
    './css/active.less',
    './css/cytoscape.less',
    './css/data.less',
    './css/query.less',
    './css/query-genes.less',
    './css/query-genes-sizes.less',
    './css/query-organisms.less',
    './css/query-networks.less',
    './css/query-history.less',
    './css/result.less',
    './css/result-networks.less'
  ],

  // config for java project integration & debugging locally
  javaTargetDir: '../website/target',
  builtJava: '../website/target/genemania/**/*',
  deployJavaDir: '../../tomcat/webapps/genemania',
  springFiles: [
    '../website/target/**/ApplicationConfig.properties'
  ]
};

var debugLessOpts = {
  paths: ['./css'],
  sourceMap: true,
  relativeUrls: true,
  sourceMapRootpath: '../',
  sourceMapBasepath: process.cwd()
};

var prodLessOpts = {
  paths: ['./css'],
  sourceMap: false,
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

paths.cssOnly = paths.css.filter(function( path ){
  return path.match(/\.css$/);
});

paths.lessOnly = paths.css.filter(function( path ){
  return path.match(/\.less$/);
});

paths.cssCombined = './css-build/all.css';


function isLessFile( file ){
  return file.path.match('.less');
}

gulp.task('default', ['watch']);

// clean built files
gulp.task('clean', ['javac-clean', 'htmlrefs'], function(){
  return gulp.src([ './js-build', './js/website-config.js', './css-build' ])
    .pipe( clean() )
  ;
});

gulp.task('clean-all', ['javac-clean', 'java-deploy-clean', 'clean'], function( next ){
  next();
});

// convenient shortcut
gulp.task('java-debug', function(){
  return runSequence( 'javac-wdeps', 'java-deploy', next );
});

// compile java projects for debugging (incl. dependent projects)
gulp.task( 'javac-wdeps', shell.task([
  'export PRIVATE_REPO=' + path.resolve( process.cwd(), '../../genemania-private' ) + ' && mvn clean install -pl website -am -P dev-debug'
], { cwd: '..' }) );

// compile java projects for debugging
gulp.task( 'javac', shell.task([
  'export PRIVATE_REPO=' + path.resolve( process.cwd(), '../../genemania-private' ) + ' && mvn clean install -pl website -P dev-debug'
], { cwd: '..' }) );

// fix dir refs w/ `~` in springmvc confs
// (can't hardcode single user's homedir in spring config)
gulp.task('fix-spring-dir-refs', function(){
  return gulp.src( paths.springFiles )
    .pipe( replace('~', process.env['HOME']) )
    .pipe( gulp.dest( paths.javaTargetDir ) )
  ;
});

// deploy built java files to tomcat
gulp.task('java-deploy', ['fix-spring-dir-refs', 'java-deploy-clean'], function(){
  return gulp.src( paths.builtJava )
    .pipe( gulp.dest( paths.deployJavaDir ) )
  ;
});

// compile java website and deploy to tomcat
gulp.task('javac-deploy', function(next){
  return runSequence( 'javac', 'java-deploy', next );
});

// clean built java files
gulp.task('javac-clean', function(next){
  return gulp.src( paths.javaTargetDir, { read: false } )
    .pipe( clean({ force: true }) )
  ;
});

// compile java website and deploy to tomcat
gulp.task('java-deploy-clean', function(next){
  return gulp.src( paths.deployJavaDir, { read: false } )
    .pipe( clean({ force: true }) )
  ;
});

// use website config
gulp.task('website-config', function(){
  gulp.src('./js/website/*.js')
    .pipe( gulp.dest('./js') );
  ;
});

// minified website config
gulp.task('website', function( next ){
  return runSequence( 'website-config', 'minify', next );
});

// build minified ui
gulp.task('minify', ['htmlminrefs'], function(next){
  next();
});

// alt name for minify
gulp.task('build', ['minify'], function(next){
  next();
});

// update path refs
gulp.task('htmlrefs', function(){
  return gulp.src( './index.html' )
    .pipe(inject( gulp.src(paths.js.concat(paths.debug).concat(paths.cssCombined), { read: false }), {
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

gulp.task('css-unmin', function(){
  return gulp.src( paths.css )

    .pipe( concat('all.css') )

    .pipe( less(debugLessOpts) )
      .on('error', handleError)
    
    .pipe( gulp.dest('./css-build') )
  ;
});

// minify css
gulp.task('css', function(){
  return gulp.src( paths.css )

    .pipe( concat('all.min.css') )

    .pipe( less(prodLessOpts) )

    .pipe( mincss() )

    .pipe( gulp.dest('./css-build') )
  ;
});

// make sure everything is uptodate before watching
gulp.task('prewatch', function( next ){
  return runSequence( 'css-unmin', 'htmlrefs', next );
});

// auto less compilation & livereload
gulp.task('watch', ['prewatch'], function(){
  livereload.listen();

  // reload all when page or js changed
  gulp.watch( ['index.html', 'templates/*.html'].concat(paths.js) )
    .on('change', livereload.changed)
  ;

  // rebuild all less when at least one file changed
  gulp.watch( paths.css, function(e){
    runSequence( 'css-unmin', function(){
      livereload.changed('css-build/all.css');
    } );
  } );

});