GeneMANIA
=========

http://genemania.org

GeneMANIA helps you predict the function of your favourite genes and gene sets.


## Build instructions

### General

**Requirements:**
 1. JDK (v1.7; other vesions may work)
 1. Maven (v3.2+)

**Projects:**
 1. `common` : common APIs used amongst the projects
 1. `engine` : the GeneMANIA algorithm engine
 1. `broker` : for communicating between the website and workers that use the engine
 1. `website` : the website backend (webservices etc)
 1. `adminweb` : the administrative interface for editing data used in the website
 1. `plugin` : the Cytoscape plugin/app

**Building:**
 1. To build all Java projects: `mvn package`
 1. To build a particular project, e.g. website `mvn package -pl website -am`

### Website UI

**Notes:**
 * Short UI build summary :
  * Install Node.js v6 and npm
  * `cd website-ui`
  * `npm run website`
 * The `website-ui` contains the frontend interface for the website.  Naturally, JavaScript is used for the UI and the build process, so the build process here differs from the previous Java projects.  The `website` should pull in the latest UI by calling the appropriate `website-ui` target &mdash; that way, the website always automatically gets the latest UI on each build.
 * The website UI assumes a local development environment, unless deployed to the `website` Java project.  For local development, the UI assumes the website server resides at `http://localhost:8080/genemania`.  You can configure this in `js/debug/debug.js`.

**Requirements:**
 1. Node.js v6 & npm
 1. Gulp: `sudo npm install -g gulp`
 1. NB: you must `npm install` before using `gulp`

**Targets:** `gulp <target1> <target2> ...`
 * `clean` : clean built ui files (resets ui to default unminified state)
 * `clean-all` : clean ui & website
 * `minify`/`build` : build the minified ui
 * `watch` (default) : autocompilation & livereload for dev
 * Building java website
  * `javac` : build java website
  * `javac-wdeps` : build java website and its java dependencies
  * `javac-deploy` : build java website and deploy to tomcat
  * `javac-clean` : clean java website built files
  * `java-deploy` : deploy java website to tomcat
  * `java-deploy-clean` : clean java website in tomcat
  * `website` : deploys the built ui to the tomcat app war dir
  * `website-unmin` : deploys the unminified, built ui to the tomcat app war dir
  * `website-clean` : cleans the website proj & tomcat war dir


## Deployment instructions

Follow these instructions to deploy your own external instance of GeneMANIA:

**Requirements:**
 1. JDK (v1.7+; other vesions may work)
 1. Maven (v3.2+)
 1. Tomcat (v8; other versions may work)

**Deploying the data:**
 1. TODO

**Deploying the website:**
 1. Build the UI: `cd website-ui && npm i && npm run website && cd ..`
 1. You must clean the engine before building (at least once to install mtj): `mvn clean`
 1. Build the website: `mvn package -pl website -am -P local`
 1. Deploy the produced `.war` file to Tomcat



## Debugging tools

* [Eclipse](https://www.eclipse.org) works fairly well as an IDE for this project
 * Configure Eclipse to use the JDK you want for building or else it will use the old system default
 * [m2e](https://www.eclipse.org/m2e/) for maven; reports some errors but works OK despite that
 * [Sysdeo Tomcat plugin] for debugging the webservices website; can be used on Tomcat 8 even though it's not listed on their site


## Funding

GeneMANIA is actively developed at the [University of Toronto](http://www.utoronto.ca/), in the [Donnelly Centre for Cellular and Biomolecular Research](http://www.thedonnellycentre.utoronto.ca/), in the labs of [Gary Bader](http://www.baderlab.org/) and [Quaid Morris](http://morrislab.med.utoronto.ca/).  GeneMANIA development was originally funded by [Genome Canada](http://www.genomecanada.ca/), through the [Ontario Genomics Institute](http://www.ontariogenomics.ca/) (2007-OGI-TD-05) and is now funded by the [Ontario Ministry of Research and Innovation](http://www.mri.gov.on.ca/english/programs/orf/gl2/program.asp).
