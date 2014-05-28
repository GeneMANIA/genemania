<!DOCTYPE HTML>
<%@include file="header.jsp"%>



<html>
<body>
<div class="container-fluid">
    <div class="row-fluid">
        <div class="span12">
            <%@include file="navbar.jsp"%>
        </div>
    </div>
    <div class="row-fluid">
        <div class="span4">
            <!-- left pane -->
            <div id="tree"></div>
        </div>
        <div class="span8">
            <!-- right pane -->
            <div id="details"></div>
        </div>
    </div>
</div>
<script type="text/javascript">
    $(function() {
                
    	// TODO: maybe these should all be hoisted up into init?
        dmw.init();
        dmw.setupTree(dmw.INIT_ORGANISM_ID);
        dmw.setupSearch();
        dmw.setupMenu();
        
    });
</script>
</body>
</html>