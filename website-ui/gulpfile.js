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
var server = require('http-server');
var util = require('gulp-util');

var $cordova = path.resolve( process.cwd(), 'node_modules/cordova/bin/cordova' );
var $crosswalkCreate = path.resolve( process.cwd(), 'crosswalk/bin/create' );
var $crosswalkCreate = path.resolve( process.cwd(), 'crosswalk/bin/create' );
var $crosswalkUrl = 'https://download.01.org/crosswalk/releases/crosswalk/android/stable/9.38.208.10/arm/crosswalk-cordova-9.38.208.10-arm.zip';
var $appAddr = 'org.genemania.user';
var $appName = 'GeneMANIA';
var $crosswalkAppDir = 'crosswalk/' + $appName;
var $crosswalkAppWww = 'crosswalk/' + $appName + '/assets/www';

var paths = {
  js: [
    './js/lib/jquery-*.js',
    './js/lib/fastclick.js',
    './js/lib/angular.js',
    './js-build/angular-templates.js', // include templates if built
    './js/lib/cola.v3.min.js',
    './js/lib/cytoscape.js',
    './js/lib/cytoscape-*.js',
    './js/lib/*.js',
    './js/config.js',
    './js/website-config.js', // so if website config exists, then it overwrites the local config
    './js/pubsub.js',
    './js/app.js',
    './js/app-*.js',
    './js/*.js'
  ],

  debug: [
    './js/debug/livereload.js', // for live reloading when source files change
    './js/debug/debug.js'
  ],

  css: [
    './css/constants.less',
    './css/reset.css',
    './css/lesshat.less',
    './css/animate.less',
    './css/animate-extras.less',
    './css/font-awesome.css',
    './css/bio-icons.css',
    './css/rangeslider.css',
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
    './css/result-networks.less',
    './css/result-genes.less',
    './css/result-selected-info.less',
    './css/result-functions.less',
    './css/result-interactions.less'
  ],

  // config for java project integration & debugging locally
  javaTargetDir: '../website/target',
  builtJava: '../website/target/genemania/**/*',
  deployJavaDir: '../../tomcat/webapps/genemania',
  springFiles: [
    '../website/target/**/ApplicationConfig.properties'
  ],

  cordova: [
    './index.html',
    './css-build/**',
    './js-build/**',
    './fonts/**',
    './img/**'
  ],

  cordovaDebug: [
    './index.html',
    './css-build/**',
    './js/**',
    './js-build/**',
    './fonts/**',
    './img/**'
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
gulp.task('clean', ['htmlrefs'], function(){
  return gulp.src([ './js-build', './js/website-config.js', './css-build' ])
    .pipe( clean() )
  ;
});

gulp.task('clean-all', ['javac-clean', 'java-deploy-clean', 'clean'], function( next ){
  next();
});

// convenient shortcut
gulp.task('java-debug', function( next ){
  return runSequence( 'javac-wdeps', 'java-deploy', next );
});

// compile java projects for debugging (incl. dependent projects)
gulp.task( 'javac-wdeps', shell.task([
  'export PRIVATE_REPO=' + path.resolve( process.cwd(), '../../genemania-private' ) + ' && mvn -Dmaven.test.skip=true clean install -e -pl website -am -P dev-debug'
], { cwd: '..' }) );

// compile java projects for debugging
gulp.task( 'javac', shell.task([
  'export PRIVATE_REPO=' + path.resolve( process.cwd(), '../../genemania-private' ) + ' && mvn -Dmaven.test.skip=true clean install -e -pl website -P dev-debug'
], { cwd: '..' }) );

gulp.task( 'javac-prod', ['website'], shell.task([
  'export PRIVATE_REPO=' + path.resolve( process.cwd(), '../../genemania-private' ) + ' && mvn -Dmaven.test.skip=true clean package -e -pl website -am -P prod'
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

// minified website config
gulp.task('website', function( next ){
  return runSequence( 'minify', 'website-clean', 'deploy-website-res', 'deploy-website-index', next );
});

gulp.task('deploy-website-res', function(){
  return gulp.src([
    './css-build/**',
    './fonts/**',
    './img/**',
    './js-build/**',
  ], { base: './' }).pipe( gulp.dest('../website/src/main/webapp/') );
});

gulp.task('deploy-website-index', function(){
  return gulp.src([
    'index.html'
  ])
    .pipe( rename('index.jsp') )
    .pipe( gulp.dest('../website/src/main/webapp/WEB-INF/jsp') )
  ;
});

gulp.task('website-clean', function(){
  return gulp.src( [
    'css-build/**',
    'fonts/**',
    'img/!(logo)',
    'js-build/**',
    'WEB-INF/jsp/index.jsp'
  ].map(function( f ){ return '../website/src/main/webapp/' + f; }) ).pipe( clean({ force: true }) );
});

gulp.task('website-unmin', function(next){
  return runSequence('htmlunminrefs', 'website-clean', 'deploy-website-res', 'deploy-website-index');
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
    .pipe(inject( gulp.src(paths.debug.concat(paths.js).concat(paths.cssCombined), { read: false }), {
      addRootSlash: false
    } ))

    .pipe( gulp.dest('.') )
  ;
});

// update refs and include cached templates
gulp.task('htmltemplatesref', ['templates', 'css-unmin'], function(next){
  return runSequence( 'htmlrefs', next );
});

var websiteTransform = function( filepath ){
  if( filepath.match('.js') ){
    return '<script>document.write(\'<script src="\' + tomcatContextPath() + \'' + filepath + '" async defer></\'+\'script>\');</'+'script>';
  } else if( filepath.match('.css') ){
    return '<script>document.write(\'<link rel="stylesheet" href="\' + tomcatContextPath() + \'' + filepath + '" />\');</'+'script>';
  }

  // Use the default transform as fallback:
  return inject.transform.apply(inject.transform, arguments);
};

// update path refs with minified files
gulp.task('htmlminrefs', ['templates', 'js', 'css'], function(){

  return gulp.src( './index.html' )
    .pipe(inject( gulp.src(['./js-build/all.min.js', './css-build/all.min.css'], { read: false }), {
      addRootSlash: false,
      transform: websiteTransform
    } ))

    .pipe( gulp.dest('.') )
  ;

});

gulp.task('htmlunminrefs', ['templates', 'js-unmin', 'css-unmin'], function(){

  return gulp.src( './index.html' )
    .pipe(inject( gulp.src(['./js-build/all.js', './css-build/all.css'], { read: false }), {
      addRootSlash: false,
      transform: websiteTransform
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
    .pipe( concat('all.min.js', { newLine: ';\n' }) )

    .pipe( uglify() )

    .pipe( gulp.dest('./js-build') )
  ;

});

gulp.task('js-unmin', ['templates'], function(){

  return gulp.src( paths.js )
    .pipe( concat('all.js', { newLine: ';\n' }) )

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
  return runSequence( 'clean', 'css-unmin', 'htmlrefs', next );
});

// auto less compilation & livereload
gulp.task('watch', ['prewatch'], function(){
  livereload.listen();

  server.createServer({
    root: './',
    cache: -1,
    cors: true
  }).listen( '9999', '0.0.0.0' );

  util.log( util.colors.green('GeneMANIA UI hosted on local HTTP server at http://localhost:9999') );

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

gulp.task('app-clean', ['clean'], function(){
  return gulp.src([ './cordova', './crosswalk' ])
    .pipe( clean() )
  ;
});

gulp.task( 'cordova-create', shell.task([
  '[ -d cordova ] || ( mkdir cordova && $cordova -d create cordova $appAddr $appName )'
    .replace('$cordova', $cordova)
    .replace('$appAddr', $appAddr)
    .replace('$appName', $appName)
]) );

gulp.task( 'cordova-projs', ['cordova-create'], shell.task([
  '[ -d platforms/ios ] || $cordova platform add ios'.replace('$cordova', $cordova),
  '[ -d platforms/firefoxos ] || $cordova platform add firefoxos'.replace('$cordova', $cordova)
], { cwd: 'cordova' }) );

gulp.task('cordova', ['htmlminrefs', 'cordova-projs'], function(){
  return gulp.src( paths.cordova, { base: '.' } )
    .pipe( gulp.dest('./cordova/www') )
  ;
});

gulp.task('cordova-debug', ['htmltemplatesref', 'cordova-projs'], function(){
  return gulp.src( paths.cordovaDebug, { base: '.' } )
    .pipe( gulp.dest('./cordova/www') )
  ;
});

gulp.task( 'ios-debug', ['cordova-debug'], shell.task([
  '$cordova run ios --debug'.replace('$cordova', $cordova),
], { cwd: 'cordova' }) );

gulp.task( 'ios-release', ['clean', 'cordova'], shell.task([
  '$cordova build ios --release'.replace('$cordova', $cordova),
], { cwd: 'cordova' }) );

gulp.task( 'crosswalk-create', shell.task([
  ('[ -d crosswalk ] || ( ' +
      ' curl $crosswalkUrl -o crosswalk.zip ' +
      ' && unzip crosswalk.zip && rm crosswalk.zip ' +
      ' && mv crosswalk* crosswalk ' +
  ' )').replace('$crosswalkUrl', $crosswalkUrl) // one big command b/c of if
]) );

gulp.task( 'crosswalk-projs', shell.task([
  '[ - d $crosswalkAppDir ] || $crosswalkCreate $appName $appAddr $appName'
    .replace('$crosswalkCreate', $crosswalkCreate)
    .replace('$crosswalkAppDir', $crosswalkAppDir)
    .replace('$appAddr', $appAddr)
    .replace('$appName', $appName)
], { cwd: 'crosswalk' }) );

gulp.task('crosswalk', ['htmlminrefs', 'crosswalk-projs'], function(){
  return gulp.src( paths.cordova, { base: '.' } )
    .pipe( gulp.dest($crosswalkAppWww) )
  ;
});

gulp.task('crosswalk-debug', ['htmltemplatesref', 'crosswalk-projs'], function(){
  return gulp.src( paths.cordovaDebug, { base: '.' } )
    .pipe( gulp.dest($crosswalkAppWww) )
  ;
});

gulp.task('android-debug', ['crosswalk'], shell.task([
'./cordova/build --debug',
'./cordova/run'
], { cwd: $crosswalkAppDir }));

gulp.task('android-release', ['clean', 'crosswalk-projs'], shell.task([
'./cordova/build --release'
], { cwd: $crosswalkAppDir }));
