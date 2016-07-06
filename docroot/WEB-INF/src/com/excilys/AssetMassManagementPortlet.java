package com.excilys;

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
import com.liferay.portlet.documentlibrary.model.DLFileEntry;
import com.liferay.portlet.documentlibrary.model.DLFileEntryType;
import com.liferay.portlet.documentlibrary.service.DLFileEntryLocalServiceUtil;
import com.liferay.portlet.documentlibrary.service.DLFileEntryTypeLocalServiceUtil;
import com.liferay.util.bridges.mvc.MVCPortlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

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

	private static final String REGEX_INT = "^[-]?\\d*$";

	// Liferay Default Asset Types
	private static final String BLOG_ENTRY_CLASSNAME = "com.liferay.portlet.blogs.model.BlogsEntry";
	private static final String BOOKMARKS_ENTRY_CLASSNAME = "com.liferay.portlet.bookmarks.model.BookmarksEntry";
	private static final String CALENDAR_EVENT_CLASSNAME = "com.liferay.calendar.model.CalendarBooking";
	private static final String WEB_CONTENT_ARTICLE_CLASSNAME = "com.liferay.portlet.journal.model.JournalArticle";

	// Liferay DLType class name
	private static final String DL_FILE_CLASSNAME = "com.liferay.portlet.documentlibrary.model.DLFileEntry";

	// Liferay Assets types names
	private static final String BLOG_ENTRY_NAME = "asset-type-blog-entry";
	private static final String BOOKMARK_NAME = "asset-type-bookmark";
	private static final String CALENDAR_EVENT_NAME = "asset-type-calendar-event";
	private static final String WEB_CONTENT_NAME = "asset-type-web-content";

	public static Map<String, Map<String, String>> assetsMap = new HashMap<String, Map<String, String>>();
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

		AssetEntryQuery query = new AssetEntryQuery();
		query.setGroupIds(groups);

		// fill assetsMap with default assets type
		setAssetEntriesByDefaultTypes(query);

		// Get Others assets (custom types)
		setDLFileByType(query);

		request.setAttribute("assets", assetsMap);

		// Get Tags and categories
		request.setAttribute("categories", getCategories());
		request.setAttribute("tags", getTags());

		super.render(request, response);
	}

	/**
	 * Fill <i>assetsMap</i> with Assets Entries sortes by types
	 * 
	 * @param query
	 */
	private void setAssetEntriesByDefaultTypes(AssetEntryQuery query) {
		// Get Blog entries
		addToAssetsMap(query, BLOG_ENTRY_CLASSNAME, BLOG_ENTRY_NAME);

		// Get Bookmarks entries
		addToAssetsMap(query, BOOKMARKS_ENTRY_CLASSNAME, BLOG_ENTRY_NAME);

		// Get Calendar events //FIXME CalendarBooking
		addToAssetsMap(query, CALENDAR_EVENT_CLASSNAME, CALENDAR_EVENT_NAME);

		// Get Web content articles
		addToAssetsMap(query, WEB_CONTENT_ARTICLE_CLASSNAME, WEB_CONTENT_NAME);
	}

	private void addToAssetsMap(AssetEntryQuery query, String entryClassName,
			String entryName) {
		Map<String, String> entries = getAssetEntriesTitles(query,
				entryClassName);
		if (!entries.isEmpty()) {
			assetsMap.put(entryName,
					getAssetEntriesTitles(query, entryClassName));
		}
	}

	/**
	 * Return an ArrayList filled with the entries'title of type
	 * <i>classname</i>
	 * 
	 * @param query
	 *            query to pass at AssetEntryService for request
	 * @param className
	 *            the type of wanted Asset entries
	 * @return an ArrayList of all entries titles instance of <i>className</i>
	 */
	private Map<String, String> getAssetEntriesTitles(AssetEntryQuery query,
			String className) {
		List<AssetEntry> entries = null;
		Map<String, String> entriesTitles = null;
		// Construct query
		query.setClassName(className);
		try {
			entries = AssetEntryLocalServiceUtil.getEntries(query);
			entriesTitles = new HashMap<>(entries.size());
			// Extract titles from asset entries
			for (AssetEntry entry : entries) {
				entriesTitles.put(entry.getClassUuid(),
						entry.getTitle(language));
			}
		} catch (SystemException e) {
			LOGGER.error("AssetMassManagementPortlet.getAssetEntriesTitles", e);
			throw new RuntimeException(e);
		}
		return entriesTitles;
	}

	/**
	 * Get all asset custom types (DLFileEntry)
	 * 
	 * @return A Map in which every entry contains the ID of the type and its
	 *         name
	 */
	private Map<Long, String> getDLFileEntryType() {
		List<DLFileEntryType> entriesTypes = null;
		Map<Long, String> types = null;
		try {
			entriesTypes = DLFileEntryTypeLocalServiceUtil.getDLFileEntryTypes(
					0,
					DLFileEntryTypeLocalServiceUtil.getDLFileEntryTypesCount());
			types = new HashMap<>(entriesTypes.size());
			for (DLFileEntryType type : entriesTypes) {
				types.put(type.getFileEntryTypeId(), type.getName(language));
			}
		} catch (SystemException e) {
			LOGGER.error("AssetMassManagementPortlet.getDLFileEntryType", e);
			throw new RuntimeException(e);
		}
		return types;
	}

	/**
	 * Fill <i>assetsMap</i> with DLFileEntries sorted by types
	 */
	private void setDLFileByType(AssetEntryQuery query) {
		query.setClassName(DL_FILE_CLASSNAME);
		List<AssetEntry> entries = null;
		try {
			// TODO
			entries = AssetEntryLocalServiceUtil.getEntries(query);
			List<DLFileEntryType> fileTypes = DLFileEntryTypeLocalServiceUtil
					.getDLFileEntryTypes(0,
							DLFileEntryLocalServiceUtil.getDLFileEntriesCount());

			for (AssetEntry entry : entries) {
				for (DLFileEntryType fileType : fileTypes) {
					if (entry.getClassTypeId() == fileType
							.getFileEntryTypeId()) {
						if (assetsMap.get(fileType.getName(language)) == null) {
							assetsMap.put(fileType.getName(language),
									new HashMap<String, String>());
						}
						assetsMap.get(fileType.getName(language)).put(
								entry.getClassUuid(), entry.getTitle());
					}
				}
			}
		} catch (SystemException e) {
			LOGGER.error("AssetMassManagementPortlet.setDLFileByType", e);
			e.printStackTrace();
			// throw new RuntimeException(e);
		}
	}

	/**
	 * Get all categories name in <i>language</i>
	 * 
	 * @return a Map with category.id and category.title
	 */
	private Map<Long, String> getCategories() {
		List<AssetCategory> assetsCategories = null;
		Map<Long, String> categories = null;

		try {
			assetsCategories = AssetCategoryLocalServiceUtil.getCategories();
			categories = new HashMap<>(assetsCategories.size());
			for (AssetCategory cat : assetsCategories) {
				categories.put(cat.getCategoryId(), cat.getTitle(language));
			}
		} catch (SystemException e) {
			LOGGER.error("AssetMassManagementPortlet.getCategories", e);
			throw new RuntimeException(e);
		}
		return categories;
	}

	/**
	 * Get all tags
	 * 
	 * @return a Map with tag.id and tag.name
	 */
	private Map<Long, String> getTags() {
		List<AssetTag> assetsTags = null;
		Map<Long, String> tags = null;
		try {
			assetsTags = AssetTagLocalServiceUtil.getTags();
			tags = new HashMap<>(assetsTags.size());
			for (AssetTag tag : assetsTags) {
				tags.put(tag.getTagId(), tag.getName());
			}
		} catch (SystemException e) {
			LOGGER.error("AssetMassManagementPortlet.getTags", e);
			throw new RuntimeException(e);
		}
		return tags;
	}

	@ProcessAction(name = "formAction")
	public void formAction(ActionRequest request, ActionResponse response) {
		// TODO
		ThemeDisplay themeDisplay = (ThemeDisplay) request
				.getAttribute(WebKeys.THEME_DISPLAY);
		long groupid = themeDisplay.getScopeGroupId();
		System.out.println(groupID);

		List<String> assetIds = fromParameterToList(request, "hiddenAssetId");
		List<String> tagIds = fromParameterToList(request, "hiddenTagId");
		List<String> categoryIds = fromParameterToList(request,
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
					idtag = parseToLong(id);
					tag = AssetTagLocalServiceUtil.getAssetTag(idtag);
					tags.add(tag);
				}
			}

			// Getting all categories that will be added
			if (categoryIds.size() > 0) {
				long idcategory;
				AssetCategory category;
				for (String id : categoryIds) {
					idcategory = parseToLong(id);
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

	private List<String> fromParameterToList(ActionRequest request,
			String parameter) {
		String stringParameter = request.getParameter(parameter);
		;
		String[] arrayParameter = stringParameter.split(",");
		List<String> returnList = new ArrayList<>();
		for (String string : arrayParameter) {
			if (!string.trim().isEmpty()) {
				returnList.add(string);
			}
		}
		return returnList;
	}

	private Long parseToLong(String param) {
		if (param != null && !param.isEmpty()) {
			Pattern patternElementPage = Pattern.compile(REGEX_INT);
			if (patternElementPage.matcher(param).matches()) {
				return Long.parseLong(param);
			}
		}
		return 0L;
	}
}
