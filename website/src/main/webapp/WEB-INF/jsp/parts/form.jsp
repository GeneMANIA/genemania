<div id="phrase_slice" class="slice">
	<%@ include file="basicSearch.jsp" %>
	<%@ include file="advancedSearch.jsp" %>
</div>
<div id="go_slice" class="slice">
    <div id="go_line" class="phrase line">
        <input type="button" id="findBtn" name="findBtn" value="Go" class="widget" tabindex="3"/>
        <input type="button" id="stopBtn" name="stopBtn" value="Stop" class="widget" tabindex="3"/>&nbsp;
        <input value="submit" class="hidden"/>
    </div>
    <div id="loading_line" class="phrase">
        <div class="icon"></div>
    </div>
</div>