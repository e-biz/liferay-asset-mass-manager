<%@page import="com.liferay.portlet.journal.model.JournalArticle"%>
<%@page import="com.excilys.AssetMassManagementPortlet"%>
<%@ include file="/html/init.jsp"%>

<script src="https://code.jquery.com/jquery-3.0.0.min.js"
	integrity="sha256-JmvOoLtYsmqlsWxa7mDSLMwa6dZ9rrIdtrrVYRnDRH0="
	crossorigin="anonymous"></script>

<div id="container" class="row-fluid span10">
	<portlet:actionURL var="formUrl" name='formAction'></portlet:actionURL>
	<a href="<portlet:actionURL name="beamMe"></portlet:actionURL>">Beam
		me!</a>
	<aui:form action="<%=formUrl%>" name='form' method="post">
		<div id="assets-column" class="span6">
			<h3>Asset Type</h3>
			<div class="list-assets">
				<c:forEach var="asset" items="${assets}">
					<div>
						<h2><liferay-ui:message key="${asset.key}"/> (${asset.value.size()})</h2>
						<ul class="hidden">
							<c:forEach var="ass" items="${asset.value}">
								<li><aui:input id="${ass.key}" name="asset" label="${ass.value}"
										type="checkbox" value="${ass.key}" /></li>
							</c:forEach>
						</ul>
					</div>
					<hr />
				</c:forEach>
			</div>
		</div>
		<div id="tags-column" class="span4">
			<div id="tags-box" class="row-fluid span12">
				<h3>Tags</h3>
				<ul class="list-scroll">
					<c:forEach var="tag" items="${tags}">
						<li><aui:input id="${tag.key}" name="tag-id" label="${tag.value}" type="checkbox"
								value="${tag.key}" /></li>
					</c:forEach>
				</ul>
			</div>
			<hr />
			<div id="categories-box" class="row-fluid span12">
				<h3>Categories</h3>
				<ul class="list-scroll">
					<c:forEach var="category" items="${categories}">
						<li><aui:input id="${category.key}" name="category-id" label="${category.value}"
								type="checkbox" value="${category.key}" /></li>
					</c:forEach>
				</ul>
			</div>
		</div>
		
		<aui:input name="hiddenAssetId" id="hiddenAssetId" type="hidden"/>
		<aui:input name="hiddenTagId" id="hiddenTagId" type="hidden"/>
		<aui:input name="hiddenCategoryId" id="hiddenCategoryId" type="hidden"/>
		
		<aui:button-row>
			<aui:button id="submitFormAssets" type="submit" value="save"/>
			<aui:button value="cancel" />
		</aui:button-row>
	</aui:form>
</div>

