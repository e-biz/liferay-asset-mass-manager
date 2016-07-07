package com.excilys;

import com.excilys.util.AssetMassManagementUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portlet.asset.model.AssetCategory;
import com.liferay.portlet.asset.model.AssetEntry;
import com.liferay.portlet.asset.model.AssetTag;
import com.liferay.portlet.asset.service.AssetCategoryLocalServiceUtil;
import com.liferay.portlet.asset.service.AssetEntryLocalServiceUtil;
import com.liferay.portlet.asset.service.AssetTagLocalServiceUtil;
import com.liferay.portlet.asset.service.persistence.AssetEntryQuery;
import com.liferay.util.bridges.mvc.MVCPortlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.ProcessAction;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

/**
 * Portlet implementation class AssetMassManagementPortlet
 */

public class AssetMassManagementPortlet extends MVCPortlet {
    private static final Log LOGGER = LogFactoryUtil
            .getLog(AssetMassManagementPortlet.class);

    public static Map<String, Map<String, String>> assetsMap = new HashMap<String, Map<String, String>>();
    AssetMassManagementUtil util = new AssetMassManagementUtil();
    Locale language;
    long groupID;

    @Override
    public void render(RenderRequest request, RenderResponse response)
            throws PortletException, IOException {
        ThemeDisplay themeDisplay = (ThemeDisplay) request
                .getAttribute(WebKeys.THEME_DISPLAY);
        // get groupId and Locale to filter Data
        groupID = themeDisplay.getScopeGroupId();
        long[] groups = { groupID };
        language = themeDisplay.getLocale();

        util.setLanguage(language);
        util.setGroupID(groupID);

        AssetEntryQuery query = new AssetEntryQuery();
        query.setGroupIds(groups);

        // fill assetsMap with default assets type
        util.setAssetEntriesByDefaultTypes(query, assetsMap);

        // Get Others assets (custom types)
        util.setDLFileByType(query, assetsMap);

        request.setAttribute("assets", assetsMap);
        // TODO instancier Util + call

        // Get Tags and categories
        request.setAttribute("categories", util.getCategories());
        request.setAttribute("tags", util.getTags());

        super.render(request, response);
    }

    @ProcessAction(name = "formAction")
    public void formAction(ActionRequest request, ActionResponse response) {
        List<String> assetIds = util.fromParameterToList(request,
                "hiddenAssetId");
        List<String> tagIds = util.fromParameterToList(request, "hiddenTagId");
        List<String> categoryIds = util.fromParameterToList(request,
                "hiddenCategoryId");

        List<AssetEntry> entries = new ArrayList<>(assetIds.size());
        List<AssetTag> tags = new ArrayList<>(tagIds.size());
        List<AssetCategory> categories = new ArrayList<>(categoryIds.size());

        try {
            // Getting all entries to modify
            for (String asset : assetIds) {
                AssetEntry entry = AssetEntryLocalServiceUtil.getEntry(groupID,
                        asset);
                entries.add(entry);
            }

            // Getting all tags that will be added
            if (tagIds.size() > 0) {
                long idtag;
                AssetTag tag;
                for (String id : tagIds) {
                    idtag = util.parseToLong(id);
                    tag = AssetTagLocalServiceUtil.getAssetTag(idtag);
                    tags.add(tag);
                }
            }

            // Getting all categories that will be added
            if (categoryIds.size() > 0) {
                long idcategory;
                AssetCategory category;
                for (String id : categoryIds) {
                    idcategory = util.parseToLong(id);
                    category = AssetCategoryLocalServiceUtil
                            .getAssetCategory(idcategory);
                    categories.add(category);
                }
            }
        } catch (PortalException | SystemException e) {
            LOGGER.error("AssetMassManagementPortlet.formAction", e);
            throw new RuntimeException(e);
        }

        try {
            for (AssetEntry entry : entries) {
                AssetTagLocalServiceUtil.addAssetEntryAssetTags(
                        entry.getEntryId(), tags);
                AssetCategoryLocalServiceUtil.addAssetEntryAssetCategories(
                        entry.getEntryId(), categories);
            }
        } catch (SystemException e) {
            LOGGER.error("AssetMassManagementPortlet.formAction", e);
            throw new RuntimeException(e);
        }

    }

}
