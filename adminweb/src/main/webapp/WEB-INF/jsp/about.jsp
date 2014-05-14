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
            <p>app version: ${APP_VERSION}</p>
            <p>database: ${DB_INFO}</p>            
        </div>
    </div>

</body>
</html>