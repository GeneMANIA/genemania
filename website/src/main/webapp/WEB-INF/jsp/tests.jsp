<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<html contextpath="${pageContext.request.contextPath}">
<head>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/test/qunit.css"/>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/test/style.css"/>
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/plugins/jquery-1.7.1.js"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/ga.js"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/test/qunit.js"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/test/util.js"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/test/tests.js"></script>
</head>
<body>

    <div id="qunit_report">
	    <h1 id="qunit-header">GeneMANIA Tests</h1>
	    <h2 id="qunit-banner"></h2>
	    <h2 id="qunit-userAgent"></h2>
	    <ol id="qunit-tests"></ol>
	    <p id="qunit-testresult"></p>
    </div>
    
    <div id="genemania">
        <iframe name="genemania_iframe" id="genemania_iframe" src="${pageContext.request.contextPath}/"></iframe>
         <div id="genemania_overlay"></div>
    </div>
    
</body>
</html>