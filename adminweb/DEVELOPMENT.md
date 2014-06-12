
Data Storage
============


Configuration
=============

Application config such as file paths and db connection parameters is
configured in properties files, loaded via spring into the app via spring.
The loading mechanism is configured in src/main/webapp/WEB-INF/web-context.xml.
Properties are gathered from multiple files into a single properties object
as follows:

 * adminweb-config.properties: anywhere in application classpath (as configured
 by app container). contains user config, optional. Any values not specified will
 be loaded from the default config below.
 
 * WEB-INF/adminweb-default-config.properties: packaged with the webapp,
 contains default values for user config values. This file can be copied
 and renamed to adminweb-config.properties, edited, and dropped into the classpath
 to allow custom configuration without changing the app war.
 
 * WEB-INF/app.properties: packaged with the webapp, contains app internal params
 such as version information. not intended to be modified by user.
 

ORM
===

Tried using jpa with eclipseLink, and hibernate in the past. Pain. Resort to using ORMLite,
which seems to take more (coding) work to setup and use, but somehow having less magic means being
able to actually get work done. :/

have to manually do the following to add a database entity:

 * create entity class
 * create Dao class, & impl, only have to implement custom methods, extending BaseDaoImpl
   gives you most of what you need.
 * update DatamartDb class

sorry if the boilerplate offends your objection-relational automagic tooling sensibilities.


UI libraries
============

 * [jquery](http://jquery.com/) of course
 * [File-Upload](http://blueimp.github.io/jQuery-File-Upload/)
 * [Bootstrap](http://getbootstrap.com/) version 2
 * [Bootbox.js](http://bootboxjs.com/)
 * [mustache](http://mustache.github.io/)
 * [jquery.form](http://malsup.com/jquery/form/)
 * [FancyTree](https://github.com/mar10/fancytree)
 * [DataTables](https://datatables.net/)

