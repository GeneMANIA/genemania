<div id="navbar" class="navbar navbar-static">
    <div class="navbar-inner">
        <div class="container" style="width: auto;">
            <a class="brand" href='${pageContext.request.contextPath}'>Build-A-MANIA</a>

            <ul class="nav" role="navigation">
                <li class="dropdown"><a id="drop1" href="#" role="button" class="dropdown-toggle" data-toggle="dropdown">New <b
                        class="caret"></b>
                </a>

                    <ul class="dropdown-menu" role="menu" aria-labelledby="drop1">
                        <li><a id="new-organism" tabindex="-1" href="#">Organism</a></li>
                        <li class="disabled"><a tabindex="-1" href="#">Network</a></li>
                        <li class="disabled"><a tabindex="-1" href="#">Attributes</a></li>
                        <li class="divider"></li>
                        <li class="disabled"><a tabindex="-1" href="#">Import Wizard</a></li>
                    </ul>
                    </li>
                    
                <li class="dropdown"><a id="drop2" href="#" role="button" class="dropdown-toggle" data-toggle="dropdown">Export <b
                        class="caret"></b>
                </a>

                    <ul class="dropdown-menu" role="menu" aria-labelledby="drop2">
                        <li class="disabled"><a tabindex="-1" href="#">As website dataset</a></li>
                        <li class="disabled"><a tabindex="-1" href="#">As plugin dataset</a></li>
                        <li class="disabled"><a tabindex="-1" href="#">Input data as text</a></li>
                        <li class="disabled"><a tabindex="-1" href="#">Processed data as text</a></li>
                    </ul>
                    </li>
                <li class="dropdown"><a id="drop3" href="#" role="button" class="dropdown-toggle" data-toggle="dropdown">Build <b
                        class="caret"></b>
                </a>

                    <ul class="dropdown-menu" role="menu" aria-labelledby="drop3">
                        <li><a tabindex="-1" href="${pageContext.request.contextPath}/build_info">Build data</a></li>
                        <li><a tabindex="-1" href="http://${pageContext.request.serverName}:${GENEMANIA_PORT}/genemania" target="_blank">View in GeneMANIA</a></li>
                    </ul>
                    </li>

            </ul>
            <ul class="nav pull-right">
                <li>
                    <div class="navbar-form">
                    <div class="input-append">
                        <input id="filter-tree" type="text" class="input-medium" type="text" placeholder="Search" />
                        <button id="clear-search" class="btn" type="reset"><i class="icon-remove"></i></button>
                    </div>
                    </div>
                </li>
                <li id="fat-menu" class="dropdown"><a href="#" id="drop3" role="button" class="dropdown-toggle" data-toggle="dropdown">Help
                        <b class="caret"></b>
                </a>

                    <ul class="dropdown-menu" role="menu" aria-labelledby="drop3">
                        <li><a tabindex="-1" href="help_formats">File Formats</a></li>
                        <li class="disabled"><a tabindex="-1" href="#">Contact Us</a></li>
                        <li class="divider"></li>
                        <li><a tabindex="-1" href="about">About</a></li>
                    </ul></li>
            </ul>
        </div>
    </div>
</div>
