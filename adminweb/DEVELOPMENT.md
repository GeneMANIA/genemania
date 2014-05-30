
Data Storage
============


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

