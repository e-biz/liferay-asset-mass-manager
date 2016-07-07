package com.excilys.util;

import com.liferay.portlet.asset.service.persistence.AssetEntryQuery;

import java.util.List;
import java.util.Map;

import javax.portlet.ActionRequest;

public interface AssetMassManagement {

    /**
     * Fill <i>assetsMap</i> with Assets Entries sorted by types.
     * 
     * @param query
     *            query to pass at AssetEntryService for request
     */
    void setAssetEntriesByDefaultTypes(AssetEntryQuery query,
            Map<String, Map<String, String>> map);

    /**
     * Add AssetEntries of <i>entryClassName</i> type to <i>map</i> if there is
     * AssetEntry.
     * 
     * @param query
     *            query to pass at AssetEntryService for request
     * @param entryClassName
     *            type of wanted AssetEntries
     * @param entryName
     *            name of type of AssetEntries
     * @param map
     *            map to set as attribute of the request in render() filled with
     *            all assets
     */
    void addToAssetsMap(AssetEntryQuery query, String entryClassName,
            String entryName, Map<String, Map<String, String>> map);

    /**
     * Return an ArrayList filled with the entries'title of type
     * <i>classname</i>.
     * 
     * @param query
     *            query to pass at AssetEntryService for request
     * @param className
     *            the type of wanted Asset entries
     * @return an ArrayList of all entries titles instance of <i>className</i>
     */
    Map<String, String> getAssetEntriesTitles(AssetEntryQuery query,
            String className);

    /**
     * Fill <i>map</i> with DLFileEntries sorted by types.
     * 
     * @param query
     *            query to pass at AssetEntryService for request
     * @param map
     *            map to set as attribute of the request in render() filled with
     *            all assets
     */
    void setDLFileByType(AssetEntryQuery query,
            Map<String, Map<String, String>> map);

    /**
     * Get all categories name in <i>language</i>.
     * 
     * @return a Map with category.id and category.title
     */
    Map<Long, String> getCategories();

    /**
     * Get all tags.
     * 
     * @return a Map with tag.id and tag.name
     */
    Map<Long, String> getTags();

    /**
     * Map the request parameter in a List.
     * 
     * @param request
     *            request sent by form
     * @param parameter
     *            name of parameter
     * @return List of request parameter object
     */
    List<String> fromParameterToList(ActionRequest request, String parameter);

    /**
     * Parse <i>param</i> to Long without dirty checking.
     * 
     * @param param
     *            string to convert in Long
     * @return the corresponding Long if parsable, 0L else
     */
    Long parseToLong(String param);

}
