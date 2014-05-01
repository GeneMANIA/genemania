<label>GeneMANIA web app version</label>
<pre>${version}</pre>

<label>GeneMANIA data version</label>
<pre>${dbversion}</pre>

<label>Host instance</label>
<pre><%=request.getServerName()%>:<%=request.getServerPort()%></pre>

<%
	try {
	    java.net.InetAddress addr = java.net.InetAddress.getLocalHost();

	    // Get IP Address
	    byte[] ipAddr = addr.getAddress();

	    // Get hostname
	    String hostname = addr.getHostName();
	    
	    out.println("<label>Server hostname</label>");
	    out.println("<pre>" + hostname + "</pre>");
	    
	    String ipString = "";
	    for(int i = 0; i < ipAddr.length; i++){
	    	byte b = ipAddr[i];
	    	ipString += ((int) b & 0xFF) + (i < ipAddr.length - 1 ? "." : "");
	    }
	    
	    out.println("<label>Server IP address</label>");
	    out.println("<pre>" + ipString + "</pre>");
	    
	} catch (Exception e) {
		out.println("<label>Can not display server network info</label><pre>" + e.getMessage() + "</pre>");
	}
%>

<label>Date and time of server response</label>
<pre><%= new java.text.SimpleDateFormat("d MMMMM yyyy HH:mm:ss").format(new java.util.Date()) %></pre>

<label>User agent</label>
<pre><%= request.getHeader("user-agent") %></pre>

<label>Client IP address</label>
<pre><%= request.getRemoteAddr() %></pre>

<label>Forwarded client IP address</label>
<pre><%= request.getHeader("X-Forwarded-For") %></pre>

<label>External request URL</label>
<pre><%= request.getAttribute("javax.servlet.forward.request_uri") %></pre>

<label>Internal request URI</label>
<pre><%= request.getRequestURI() %></pre>

<label>Request method</label>
<pre><%= request.getMethod() %></pre>

<label>Organism ID</label>
<pre>${organismId}</pre>

<label>Gene lines</label>
<pre>${geneLines}</pre>

<label>Network IDs</label>
<pre><c:forEach items="${networkIds}" var="netid">${netid}
</c:forEach></pre>

<label>Attribute group IDs</label>
<pre><c:forEach items="${attrGroupIds}" var="attrid">${attrid}
</c:forEach></pre>

<label>Network weighing</label>
<pre>${weighting}</pre>

<label>Number of gene results</label>
<pre>${threshold}</pre>

<label>Session ID</label>
<pre>${sessionId}</pre>