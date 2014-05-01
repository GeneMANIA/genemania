<ul id="networks_widget" class="checktree">
	<c:forEach items="${results.resultNetworkGroups}" var="rNetworkGroup">
		<li class="checktree_top_level checktree_network_group"
			netid="${rNetworkGroup.networkGroup.id}"><input class="widget"
			type="checkbox" />
			<div class="label">
				<div class="per_cent_text"
					weight="<fmt:formatNumber value="${rNetworkGroup.weight}" minFractionDigits="2" maxFractionDigits="10" />">
					<a target="_blank"
						href="http://pages.genemania.org/help/faq/#How_do_I_interpret_the_network_weights?">
						<span
						tooltip="<spring:message code='networks_tab.weight_text.tooltip'/>"><fmt:formatNumber
								value="${rNetworkGroup.weight * 100}" minFractionDigits="2"
								maxFractionDigits="2" /> %</span> </a>
				</div>
				<div class="name">${rNetworkGroup.networkGroup.name}</div>
			</div>
			<div class="per_cent_bar">
				<div class="bar"
					tooltip="<spring:message code='networks_tab.weight_bar.tooltip'/>"></div>
			</div>

			<ul>
				<c:forEach items="${rNetworkGroup.resultNetworks}" var="rNetwork">
					<li class="checktree_network" netid="${rNetwork.network.id}">
						<input class="widget" type="checkbox" />
						<div class="label">
							<div class="per_cent_text"
								weight="<fmt:formatNumber value="${rNetwork.weight}" minFractionDigits="2" maxFractionDigits="10" />">
								<a target="_blank"
									href="http://pages.genemania.org/help/faq/#How_do_I_interpret_the_network_weights?">
									<span
									tooltip="<spring:message code='networks_tab.weight_text.tooltip'/>"
									id="networkWeight${rNetwork.network.id}"> <c:choose>
											<c:when test="${rNetwork.weight * 100 >= 0.01}">
												<fmt:formatNumber value="${rNetwork.weight * 100}"
													minFractionDigits="2" maxFractionDigits="2" />
											</c:when>
											<c:otherwise>&lt; 0.01</c:otherwise>
										</c:choose> % </span> </a>
							</div>
							<div class="name">${rNetwork.network.name}</div>
						</div>
						<div class="per_cent_bar">
							<div class="bar"
								tooltip="<spring:message code='networks_tab.weight_bar.tooltip'/>"></div>
						</div>

						<ul>
							<li>
								<div class="text">

									<c:set var="network" value="${rNetwork.network}" />
									<%@ include file="network.jsp"%>

								</div>
							</li>
						</ul>
					</li>
				</c:forEach>
			</ul>
		</li>
	</c:forEach>

	<c:forEach items="${results.resultAttributeGroups}" var="rAttrGroup">
		<li class="checktree_top_level checktree_attr_group"
			attrid=${rAttrGroup.attributeGroup.id}><input class="widget"
			type="checkbox" />
			<div class="label">
				<div class="per_cent_text"
					weight="<fmt:formatNumber value="${rAttrGroup.weight}" minFractionDigits="2" maxFractionDigits="10" />">
					<span
						tooltip="<spring:message code='networks_tab.weight_text.tooltip'/>"><fmt:formatNumber
							value="${rAttrGroup.weight * 100}" minFractionDigits="2"
							maxFractionDigits="2" /> %</span>
				</div>
				<div class="name">${rAttrGroup.attributeGroup.name}</div>
			</div>
			<div class="per_cent_bar">
				<div class="bar"
					tooltip="<spring:message code='networks_tab.weight_bar.tooltip'/>"></div>
			</div>

			<ul>
				<c:forEach items="${rAttrGroup.resultAttributes}" var="rAttr">
					<li class="checktree_attr" attrid="${rAttr.attribute.id}"><input
						class="widget" type="checkbox" />
						<div class="label">
							<div class="per_cent_text"
								weight="<fmt:formatNumber value="${rAttr.weight}" minFractionDigits="2" maxFractionDigits="10" />">
								<span
									tooltip="<spring:message code='networks_tab.weight_text.tooltip'/>"><fmt:formatNumber
										value="${rAttr.weight * 100}" minFractionDigits="2"
										maxFractionDigits="2" /> %</span>
							</div>
							<div class="name">${rAttr.attribute.name}</div>
						</div>
						<div class="per_cent_bar">
							<div class="bar"
								tooltip="<spring:message code='networks_tab.weight_bar.tooltip'/>"></div>
						</div>

						<ul>
							<li>
								<div class="text">
									<%@ include file="attribute.jsp"%>
								</div>
							</li>
						</ul>
					</li>
				</c:forEach>
			</ul>
		</li>
	</c:forEach>
</ul>