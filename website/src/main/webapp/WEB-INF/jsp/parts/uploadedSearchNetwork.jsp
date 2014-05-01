<div id="descriptionFor${network.id}" class="query_network_info text">
	<c:choose>
		<c:when test="${network.valid}">
			<p>
                A <spring:message code='${network.metadata.networkType}'/> with: ${network.metadata.interactionCount} 
                    <em>interaction<c:if test="${network.metadata.interactionCount > 1}">s</c:if></em>
				<c:if test="${network.metadata.accessStats != '0'}"><span class="duplicatedInteractions">; ${network.metadata.accessStats} 
                    <em>duplicated interaction<c:if test="${network.metadata.accessStats > 1}">s</c:if></em></span></c:if>
                <c:if test="${network.metadata.other != '0'}"><span class="invalidInteractions">; ${network.metadata.other} 
                    <em>unrecognized line<c:if test="${network.metadata.other > 1}">s</c:if></em></span></c:if>.
			</p>
			<p>
				<b>Name:</b>
				<input id="networkNameEdit${network.id}" type="text" value="${network.name}" class="widget edit_network_name" size="50" maxlength="50"/>
			</p>
			<p>
				<b>Description:</b>
				<textarea id="networkDescriptionEdit${network.id}" class="widget edit_network_description">${network.metadata.comment}</textarea>
			</p>
		</c:when>
		<c:when test="${!network.valid}">
			<p>
				<b>Note:</b> ${network.metadata.comment}
			</p>
		</c:when>
	</c:choose>
</div>
