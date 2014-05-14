GeneMANIA
=========

http://genemania.org

GeneMANIA helps you predict the function of your favourite genes and gene sets.


## Build instructions

### General

1. **Requirements:**
 1. JDK
 1. Maven
1. **Projects:**
 1. `common` : common APIs used amongst the projects
 1. `engine` : the GeneMANIA algorithm engine
 1. `broker` : for communicating between the website and workers that use the engine
 1. `website` : the website backend (webservices etc)
 1. `adminweb` : the administrative interface for editing data used in the website
 1. `plugin` : the Cytoscape plugin/app
1. **Building:**
 1. To build all Java projects: `mvn package`
 1. To build a particular project, e.g. website `mvn package -pl website -am`

### Website UI

The `website-ui` contains the frontend interface for the website.  Naturally, JavaScript is used for the UI and the build process, so the build process here differs from the previous Java projects.  The `website` should pull in the latest UI by calling the appropriate `website-ui` target &mdash; that way, the website always automatically gets the latest UI on each build.

1. **Requirements:**
 1. Node.js & npm
 1. Gulp
 1. NB: you must `npm install` before using `gulp`
1. **Targets:** `gulp <target1> <target2> ...`
 1. TODO build and copy to `website`
 1. TODO build locally
 1. TODO minify


## Deployment instructions

### Deploying a local copy of the website

1. TODO
1. TODO


## Funding

GeneMANIA is actively developed at the [University of Toronto](http://www.utoronto.ca/), in the [Donnelly Centre for Cellular and Biomolecular Research](http://www.thedonnellycentre.utoronto.ca/), in the labs of [Gary Bader](http://www.baderlab.org/) and [Quaid Morris](http://morrislab.med.utoronto.ca/).  GeneMANIA development was originally funded by [Genome Canada](http://www.genomecanada.ca/), through the [Ontario Genomics Institute](http://www.ontariogenomics.ca/) (2007-OGI-TD-05) and is now funded by the [Ontario Ministry of Research and Innovation](http://www.mri.gov.on.ca/english/programs/orf/gl2/program.asp).
