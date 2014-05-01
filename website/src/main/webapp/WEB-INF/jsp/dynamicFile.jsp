<% 
    response.setContentType( request.getParameter("type") );
    
    if( request.getParameter("disposition") != null ) {
        response.setHeader( "Content-Disposition", request.getParameter("disposition") );
    } 
%>${content}