<ul class="checktree" id="genes_widget">
	<c:forEach items="${results.resultGenes}" var="rGene">

		<li id="gene${rGene.gene.node.id}" gene="${rGene.gene.node.id}" class="source_${rGene.queryGene} gene gene_valid_true">
			<div class="source_${rGene.queryGene} gene_valid_true label">
				<div class="score_text"><fmt:formatNumber value="${rGene.score * 100}" minFractionDigits="2" maxFractionDigits="2" /></div>
				<div class="gene_name">${rGene.gene.symbol} <c:if test="${!empty rGene.typedName && rGene.typedName != rGene.gene.symbol}">(${rGene.typedName})</c:if></div>
				<div class="mini_description">${rGene.gene.node.geneData.description}</div>
			</div>
			<ul>
				<li>
					<div class="arrow"></div>
					<div class="checkbox_spacer"></div>
					<div class="text">
						<p class="description">
							${rGene.gene.node.geneData.description}
						</p>
						<p class="go_list">
							<strong><spring:message code="genes_tab.go_title"/></strong><br />
							
							<span class="go <c:if test="${rGene.queryGene}">has</c:if>" ocid="-1">
								<span class="colour" tooltip="<spring:message code="go_tab.query_genes.name"/>"></span>
							</span>
							
							<c:forEach items="${results.resultAttributeGroups}" var="rAttrGr">
								<c:forEach items="${rAttrGr.resultAttributes}" var="rAttr">
									
									<c:set var="hasCat" value="0" />
									<c:forEach items="${rGene.resultAttributes}" var="geneRAttr">
										<c:if test="${geneRAttr.attribute.id == rAttr.attribute.id}">
											<c:set var="hasCat" value="1" />
										</c:if>
									</c:forEach>
									
									<span class="go <c:if test="${hasCat == 1}">has</c:if>" ocid="attr-${rAttr.attribute.id}">
										<span class="colour" tooltip="${rAttr.attribute.name}"></span>
									</span>
								</c:forEach>
							</c:forEach>
							
							<c:forEach items="${results.resultOntologyCategories}" var="rOCat">
							
								<c:set var="hasCat" value="0" />
								<c:forEach items="${rGene.resultOntologyCategories}" var="geneROCat">
									<c:if test="${rOCat.ontologyCategory.id == geneROCat.ontologyCategory.id}">
										<c:set var="hasCat" value="1" />
									</c:if>
								</c:forEach>
							
								<span class="go <c:if test="${hasCat == 1}">has</c:if>" ocid="${rOCat.ontologyCategory.id}">
									<span class="colour" tooltip="${rOCat.ontologyCategory.description}"></span>
								</span>
							</c:forEach>
						</p>
						<p class="synonyms">
							<strong><spring:message code="genes_tab.synonyms_title"/></strong>&nbsp;
							<c:forEach items="${rGene.gene.node.genes}" var="gene">
								${gene.symbol};&nbsp;
							</c:forEach>
						</p>
						<p class="linkout">
							<strong><spring:message code="genes_tab.link_title"/></strong>&nbsp;
							<c:forEach items="${rGene.links}" var="link" varStatus="iteration">
								<a href="${link.url}" class="external_link" target="_blank">${link.name}</a><c:if test="${ !iteration.last }">, </c:if>
							</c:forEach>
						</p>
					</div>
				</li>
			</ul>
		</li>
	</c:forEach>
	
	<c:forEach items="${invalidGeneNames}" var="invalidGeneName">
		<li id="geneinvalid" class="source_false gene_valid_false">
			<div class="source_false valid_false label">
				<div class="gene_name">${invalidGeneName}</div>
				<div class="mini_description"><spring:message code="genes_tab.unrecognised"/></div>
			</div>
		</li>
	</c:forEach>
</ul>