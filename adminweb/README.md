
GeneMANIA Adminweb
==================

 * Data repository and administration user-interface for GeneMANIA
 * sql database, file store, and web application



Installation
============
                
Build from current development sources, requires JDK >= 6, Maven 2 or 3 (3 described).

To build the web app and the components it depends on, clone the repository 
and in the genemania dir run:
 
``` shell
mvn -pl adminweb -am clean package
```

The webapp is packaged in adminweb/target/genemania-adminweb-{someversion}.war, and a 
standalone jar with embedded web container is in adminweb/target/genemania-adminweb-{someversion}-war-exec.jar 

For a quick install, create a working directory for the webapp, copy the war-exec.jar there, and execute
standalone/embedded. Something like:

```
mkdir /somwhere/adminweb-work
cd /somewhere/adminweb-work
cp /path/to/adminweb/target/genemania-adminweb-{someversion}-war-exec.jar .
java -jar genemania-adminweb-{someversion}-war-exec.jar
```

Then browse to [http://localhost:8080](http://localhost:8080). Data files will be stored within adminweb-work. 

