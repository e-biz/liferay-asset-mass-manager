(function($) {
	var namespace = $("#portlet-namespace").data("namespace");

	$("#assets-column h2").on("click", function() {
		var ul = $(this).parent().children("ul");
		var isUlVisible = ul.is(":visible");
		if (isUlVisible) {
			$(this).parent().children("ul").hide();
			$(this).parent().children("ul").addClass("hidden");
		} else {
			$(this).parent().children("ul").show();
			$(this).parent().children("ul").removeClass("hidden");
		}
	});

	$("#submitFormAssets").on("click", function(event) {
		event.preventDefault();

		var hiddenAssetId = "#" + namespace + "hiddenAssetId";
		var hiddenTagId = "#" + namespace + "hiddenTagId";
		var hiddenCategoryId = "#" + namespace + "hiddenCategoryId";

		addToHidden("#assets-column", hiddenAssetId);
		addToHidden("#tags-box", hiddenTagId);
		addToHidden("#categories-box", hiddenCategoryId);
		$("#"+namespace+"form").trigger("submit");

	})
})(jQuery);

function addToHidden(fieldId, hiddenId) {
	var assetCheckbox = $(fieldId + " input:checked");
	var assetIds = Array();
	for (var i = 0; i < assetCheckbox.length; i++) {
		assetIds.push(assetCheckbox[i].value);
	}
	$(hiddenId).val(assetIds);
}