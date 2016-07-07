package com.excilys.util;

import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portlet.asset.model.AssetCategory;
import com.liferay.portlet.asset.model.AssetEntry;
import com.liferay.portlet.asset.model.AssetTag;
import com.liferay.portlet.asset.service.AssetCategoryLocalServiceUtil;
import com.liferay.portlet.asset.service.AssetEntryLocalServiceUtil;
import com.liferay.portlet.asset.service.AssetTagLocalServiceUtil;
import com.liferay.portlet.asset.service.persistence.AssetEntryQuery;
import com.liferay.portlet.documentlibrary.model.DLFileEntryType;
import com.liferay.portlet.documentlibrary.service.DLFileEntryLocalServiceUtil;
import com.liferay.portlet.documentlibrary.service.DLFileEntryTypeLocalServiceUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import javax.portlet.ActionRequest;

public class AssetMassManagementUtil implements AssetMassManagement {
    private static final Log LOGGER = LogFactoryUtil
            .getLog(AssetMassManagementUtil.class);

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

    private Locale language;
    private Long groupID;

    public void setLanguage(Locale locale) {
        this.language = locale;
    }

    public Locale getLanguage() {
        return language;
    }

    public void setGroupID(Long groupID) {
        this.groupID = groupID;
    }

    public Long getGroupID() {
        return groupID;
    }

    @Override
    public void setAssetEntriesByDefaultTypes(AssetEntryQuery query,
            Map<String, Map<String, String>> map) {
        // Get Blog entries
        addToAssetsMap(query, BLOG_ENTRY_CLASSNAME, BLOG_ENTRY_NAME, map);

        // Get Bookmarks entries
        addToAssetsMap(query, BOOKMARKS_ENTRY_CLASSNAME, BOOKMARK_NAME, map);

        // Get Calendar events //FIXME CalendarBooking
        addToAssetsMap(query, CALENDAR_EVENT_CLASSNAME, CALENDAR_EVENT_NAME,
                map);

        // Get Web content articles
        addToAssetsMap(query, WEB_CONTENT_ARTICLE_CLASSNAME, WEB_CONTENT_NAME,
                map);
    }

    @Override
    public void addToAssetsMap(AssetEntryQuery query, String entryClassName,
            String entryName, Map<String, Map<String, String>> map) {
        Map<String, String> entries = getAssetEntriesTitles(query,
                entryClassName);
        if (!entries.isEmpty()) {
            map.put(entryName, getAssetEntriesTitles(query, entryClassName));
        }

    }

    @Override
    public Map<String, String> getAssetEntriesTitles(AssetEntryQuery query,
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
            LOGGER.error("AssetMassManagementUtil.getAssetEntriesTitles", e);
            throw new RuntimeException(e);
        }
        return entriesTitles;
    }

    @Override
    public void setDLFileByType(AssetEntryQuery query,
            Map<String, Map<String, String>> map) {
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
                    if (entry.getClassTypeId() == fileType.getFileEntryTypeId()) {
                        if (map.get(fileType.getName(language)) == null) {
                            map.put(fileType.getName(language),
                                    new HashMap<String, String>());
                        }
                        map.get(fileType.getName(language)).put(
                                entry.getClassUuid(), entry.getTitle());
                    }
                }
            }
        } catch (SystemException e) {
            LOGGER.error("AssetMassManagementUtil.setDLFileByType", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<Long, String> getCategories() {
        List<AssetCategory> assetsCategories = null;
        Map<Long, String> categories = null;

        try {
            assetsCategories = AssetCategoryLocalServiceUtil.getCategories();
            categories = new HashMap<>(assetsCategories.size());
            for (AssetCategory cat : assetsCategories) {
                categories.put(cat.getCategoryId(), cat.getTitle(language));
            }
        } catch (SystemException e) {
            LOGGER.error("AssetMassManagementUtil.getCategories", e);
            throw new RuntimeException(e);
        }
        return categories;
    }

    @Override
    public Map<Long, String> getTags() {
        List<AssetTag> assetsTags = null;
        Map<Long, String> tags = null;
        try {
            assetsTags = AssetTagLocalServiceUtil.getTags();
            tags = new HashMap<>(assetsTags.size());
            for (AssetTag tag : assetsTags) {
                tags.put(tag.getTagId(), tag.getName());
            }
        } catch (SystemException e) {
            LOGGER.error("AssetMassManagementUtil.getTags", e);
            throw new RuntimeException(e);
        }
        return tags;
    }

    @Override
    public List<String> fromParameterToList(ActionRequest request,
            String parameter) {
        String stringParameter = request.getParameter(parameter);
        String[] arrayParameter = stringParameter.split(",");

        List<String> returnList = new ArrayList<>(); // FIXME asList
        for (String string : arrayParameter) {
            if (!string.trim().isEmpty()) {
                returnList.add(string);
            }
        }
        return returnList;
    }

    @Override
    public Long parseToLong(String param) {
        if (param != null && !param.isEmpty()) {
            Pattern patternElementPage = Pattern.compile(REGEX_INT);
            if (patternElementPage.matcher(param).matches()) {
                return Long.parseLong(param);
            }
        }
        return 0L;
    }

}
