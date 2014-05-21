GeneMANIA
=========

http://genemania.org

GeneMANIA helps you predict the function of your favourite genes and gene sets.


## Build instructions

### General

**Requirements:**
 1. JDK
 1. Maven

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
 * The `website-ui` contains the frontend interface for the website.  Naturally, JavaScript is used for the UI and the build process, so the build process here differs from the previous Java projects.  The `website` should pull in the latest UI by calling the appropriate `website-ui` target &mdash; that way, the website always automatically gets the latest UI on each build.
 * The website UI assumes a local development environment, unless deployed to the `website` Java project.  For local development, the UI assumes the website server resides at `http://localhost:8080/genemania`.  You can configure this in `js/conf.js`.

**Requirements:**
 1. Node.js & npm
 1. Gulp: `sudo npm install -g gulp`
 1. NB: you must `npm install` before using `gulp`

**Targets:** `gulp <target1> <target2> ...`
 * `website` : deploy to the `website` Java project
 * `websiteclean` : clean the web directory for the `website` Java project
 * `minify` : build a local minified UI for debugging
 * `clean` : cleans the website UI so it is in a fresh state for local debugging
 * `refs` : updates file references when you add JS, CSS, etc (useful during development)


## Deployment instructions

### Deploying a local copy of the website

1. TODO
1. TODO


## Funding

GeneMANIA is actively developed at the [University of Toronto](http://www.utoronto.ca/), in the [Donnelly Centre for Cellular and Biomolecular Research](http://www.thedonnellycentre.utoronto.ca/), in the labs of [Gary Bader](http://www.baderlab.org/) and [Quaid Morris](http://morrislab.med.utoronto.ca/).  GeneMANIA development was originally funded by [Genome Canada](http://www.genomecanada.ca/), through the [Ontario Genomics Institute](http://www.ontariogenomics.ca/) (2007-OGI-TD-05) and is now funded by the [Ontario Ministry of Research and Innovation](http://www.mri.gov.on.ca/english/programs/orf/gl2/program.asp).
