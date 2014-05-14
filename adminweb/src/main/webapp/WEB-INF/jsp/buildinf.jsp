<!DOCTYPE HTML>
<%@include file="header.jsp"%>

<html>
<body>

    <div class="container-fluid">
        <div class="row-fluid">
            <div class="span12">
                <%@include file="navbar_simple.jsp"%>
            </div>
        </div>
        <div class="row-fluid">
            <p>Running build: ${TRIGGER_DATE}</p>
            <p>Previous build: ${DONE_DATE} (<a href="http://${pageContext.request.serverName}:${GENEMANIA_PORT}/genemania" target="_blank">View in GeneMANIA</a>)
            </p>
            <form class="form-horizontal" id="buildFormForm" action="build" method="POST">

            <select name="organismToBuild">
                <c:forEach items="${organisms}" var="org">
                    <option value="${org.code}">${org.title}</option>
                </c:forEach>
            </select>

            <button name="buildButton" value="buildButton" type="submit" class="btn btn-primary">Build now</button>
            </form>
            
        </div>
    </div>

</body>
</html>