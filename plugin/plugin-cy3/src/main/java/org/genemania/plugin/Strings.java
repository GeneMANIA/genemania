/**
 * This file is part of GeneMANIA.
 * Copyright (C) 2008-2011 University of Toronto.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package org.genemania.plugin;

import java.util.ResourceBundle;

import org.genemania.AbstractStrings;

public class Strings extends AbstractStrings {
	private static ResourceBundle resources;
	
	static {
		resources = load(Strings.class);
	}
	
	public static String retrieveRelatedGenes_menuLabel;
	public static String maniaResults_menuLabel;
	public static String maniaResults_menuLabel2;
	public static String maniaResults_title;
	public static String about_menuLabel;
	
	public static String dataUpdateCheck_menuLabel;
	public static String dataUpdateCheck_title;
	public static String dataUpdateDownload_menuLabel;
	public static String dataUpdateDownload_title;
	public static String dataUpdateDownload_label;
	
	public static String changeData_menuLabel;
	public static String changeData_title;
	
	public static String root_menuLabel;
	public static String loadData_title;
	public static String missingGeneName;
	public static String default_title;
	public static String openHyperlink_error;
	public static String downloadProgress_status;
	public static String downloadProgress2_status;
	public static String downloadDataSet_status;
	public static String unzip_status;
	public static String checkNetworkConsistency_format;
	public static String cacheDirectoryNotDirectory_error;
	public static String errorLogHeader;
	public static String dataSetFileFilter_description;
	public static String rebuildObjectCache_status;
	public static String rebuildObjectCache_status2;
	public static String rebuildObjectCache_description;
	public static String downloadNetwork_status;
	public static String adjacencyListParameter_error;
	public static String installTextNetwork_status;
	public static String dataSetConfiguration_title;
	public static String dataSetConfigurationButton_label;
	public static String closeButton_label;
	public static String unknownFileType_error;
	public static String installTextNetwork_status2;
	public static String installTextNetwork_status3;
	public static String installIndex_status;
	public static String deleteNetworkButton_label;
	public static String deleteNetwork_status;
	public static String editNetworkButton_label;
	public static String editNetwork_status;
	public static String editNetwork_title;
	public static String installedNetworkList_title;
	public static String importNetworkTab_label;
	public static String importNetwork_status;
	public static String importNetworkButton_label;
	public static String importNetworkDetecting_title;
	public static String importNetworkDetecting_status;
	public static String customNetworkNetworkColumn_name;
	public static String customNetworkOrganismColumn_name;
	public static String customNetworkGroupColumn_name;
	public static String importNetworkFile_title;
	public static String importNetworkBrowseButton_label;
	public static String importNetworkNetworkRadioButton_label;
	public static String importNetworkProfileRadioButton_label;
	public static String importNetworkFilePath_label;
	public static String importNetworkFileType_label;
	public static String importNetworkOrganism_label;
	public static String importNetworkGroup_label;
	public static String importNetworkName_label;
	public static String importNetworkDescription_label;
	public static String importNetworkOrganism_description;
	public static String importNetworkPanelTypeDescription_label;
	public static String importNetworkPanelUnrecognizedFile_error;
	public static String importNetworkDisambiguateMessage;
	public static String importNetworkDisambiguateTitle;
	
	public static String luceneConfig_error;
	public static String luceneConfig_title;
	public static String luceneConfigNetManiaTab_title;
	public static String luceneConfigNetManiaTab_label;
	public static String luceneConfigUserDefinedTab_title;
	public static String luceneConfigUserDefinedTab_label;
	public static String luceneConfigUserDefinedTab_label2;
	public static String luceneConfigCyNetworkTab_title;
	public static String netmaniaInstalledDataList_title;
	public static String deleteDataButton_label;
	public static String deleteData_status;
	public static String netmaniaAvailableDataList_title;
	public static String installDataButton_label;
	public static String installData_status;
	public static String convertProfileToNetwork_status;
	public static String convertProfileToNetwork_status2;
	public static String missingData_prompt;
	public static String incompatibleData_prompt;
	public static String incompatibleData2_prompt;
	public static String completionPanelGeneHint_label;
	public static String completionPanelTooManyGenes_status;
	public static String completionPanelTooManyGenes2_status;
	public static String completionPanelNoGenes_status;
	public static String completionPanelOneGene_status;
	public static String completionPanelManyGenes_status;
	public static String completionPanelUnknownSymbol_status;
	public static String completionPanelNameColumn_name;
	public static String completionPanelDescriptionColumn_name;
	public static String completionPanelDuplicateGene_status;
	public static String completionTransferHandler_title;
	public static String completionTransferHandler_title2;
	public static String completionTransferHandlerUnrecognizedGenes_label;
	public static String completionTransferHandlerSynonyms_label;
	public static String completionTransferHandlerDuplicateGenes_label;
	public static String completionTransferHandlerOkButton_label;
	public static String completionTransferHandler_status;
	public static String consoleTaskMonitorTimeRemaining_status;
	public static String consoleTaskMonitorException_status;
	public static String consoleTaskMonitorExceptionTip_status;
	public static String consoleTaskMonitorPercentCompleted_status;
	public static String consoleTaskMonitorStatus_status;
	public static String aboutDialogCloseButton_label;
	public static String aboutDialog_title;
	public static String geneDetailPanelDescription_label;
	public static String geneDetailPanelDescription2_label;
	public static String tableModelInvalidColumn_error;
	public static String maniaResultsPanelNetworkTab_label;
	public static String maniaResultsPanelGeneTab_label;
	public static String maniaResultsPanelFunctionTab_label;
	public static String maniaResultsPanelMergeNetworksButton_label;
	public static String maniaResultsPanelUnmergeNetworksButton_label;
	public static String maniaResultsPanelHideResultsButton_label;
	public static String maniaResultsPanelOrganism_label;
	public static String maniaResultsPanelExportButton_label;
	public static String maniaResultsPanelExport_title;
	public static String maniaResultsPanelExportFile_description;
	public static String networkScore_label;
	
	public static String networkDetailPanelComment_label;
	public static String networkDetailPanelSource_label;
	public static String networkDetailPanelSource_description;
	public static String networkDetailPanelTags_label;
	public static String networkDetailPanelDescription_label;
	
	public static String advancedOptionsPanel_title;
	public static String detailedSelection_label;
	public static String networkSelectionPanelDescription_label;
	public static String organismModelElementDescription_label;
	public static String taskDialogInitialization_status;
	public static String taskDialogCancelButton_label;
	public static String taskDialogProgress_label;
	public static String taskDialogMemory_label;
	public static String taskDialogTime_label;
	public static String retrieveRelatedGenesNoDataSet_label;
	public static String retrieveRelatedGenesOrganismComboBox_label;
	public static String retrieveRelatedGenesOrganism_label;
	public static String retrieveRelatedGenesGenePanel_label;
	public static String retrieveRelatedGenesRemoveGeneButton_label;
	public static String retrieveRelatedGenesClearGenesButton_label;
	public static String retrieveRelatedGenesNetworkPanel_label;
	public static String retrieveRelatedGenesNetworkPanel_tooltip;
	public static String retrieveRelatedGenes_label;
	public static String retrieveRelatedGenes_label2;
	public static String retrieveRelatedGenes_label3;
	public static String retrieveRelatedGenes_label4;
	public static String retrieveRelatedGenes_label5;
	public static String retrieveRelatedGenes_label6;
	public static String retrieveRelatedGenesStartButton_label;
	public static String retrieveRelatedGenes_status;
	public static String retrieveRelatedGenes_status2;
	public static String retrieveRelatedGenes_status3;
	public static String retrieveRelatedGenes_status4;
	public static String retrieveRelatedGenes_status5;
	public static String retrieveRelatedGenes_status6;
	public static String retrieveRelatedGenesNetworkName_label;
	public static String retrieveRelatedGenesStatistics_label;
	public static String retrieveRelatedGenesStatisticsOrganisms_label;
	public static String retrieveRelatedGenesStatisticsNetworks_label;
	public static String retrieveRelatedGenesStatisticsGenes_label;
	public static String retrieveRelatedGenesStatisticsInteractions_label;
	public static String retrieveRelatedGenesStatisticsVersion_label;
	public static String retrieveRelatedGenesNoResults;
	public static String retrieveRelatedGenesLoadParametersButton_label;
	public static String taskDialog_error;
	public static String taskDialogCloseButton_label;
	public static String taskDialogOutOfMemory_error;
	public static String reportValidInteractions;
	public static String reportInvalidInteractions;
	public static String reportUnrecognizedGenes;
	public static String reportPrompt_label;
	public static String yes_label;
	public static String no_label;
	public static String ok_label;
	public static String cancel_label;
	public static String save_label;
	public static String reportValidRows;
	public static String reportInvalidRows;
	public static String report_title;
	public static String reportInvalidProfileData;
	public static String reportInvalidNetworkData;
	public static String loadData_error;
	public static String checkForUpdatesOk_label;
	public static String checkForUpdatesNew_label;
	public static String checkForUpdates_title;
	public static String checkForUpdates_error;
	public static String checkForUpdates_error2;
	public static String sessionChangeListener_title;
	
	public static String heapSize_error;
	public static String heapSize_title;

	public static String default_combining_method;
	public static String automatic;
	public static String bp;
	public static String mf;
	public static String cc;
	public static String average;
	public static String average_category;
	public static String importCyNetworkSource_title;
	public static String importCyNetworkDestination_title;
	public static String importCyNetworkImport_label;
	public static String importCyNetworkOrganism_label;
	public static String importCyNetworkNetworkGroup_label;
	public static String importCyNetworkNetworkName_label;
	public static String importCyNetworkNetworkDescription_label;
	public static String importCyNetworkSourceNetwork_label;
	public static String importCyNetworkNodeIdentifier_label;
	public static String importCyNetworkTypeCoexpression_label;
	public static String importCyNetworkTypeUnweighted_label;
	public static String importCyNetworkTypeWeighted_label;
	public static String importCyNetworkTypeUnknown_label;
	public static String importCyNetworkType_label;
	public static String importCyNetworkWeight_label;
	public static String importCyNetworkExpressionValues_label;
	public static String importCyNetworkHelp_label;
	public static String importCyNetworkHelpEmpty_label;
	public static String importCyNetworkTask_title;
	public static String importCyNetworkExpressionNameColumn_label;
	
	public static String functionInfoPanelQValue_label;
	public static String functionInfoPanelCoverage_label;
	public static String functionInfoPanelGoAnnotation_label;
	
	public static String filteredLayout_title;
	public static String filteredLayout_groupsTunable;
	public static String layoutMenu_title;
	
	public static String downloadData_error;
	
	public static String reportMethod_label;
	public static String reportAuthors_label;
	public static String reportPubMed_label;
	public static String reportInteraction_label;
	public static String reportSource_label;
	public static String reportTags_label;
	
	public static String selectAllButton_label;
	public static String selectNoneButton_label;
	public static String selectDefaultButton_label;
	
	public static String attributesDialog_title;
	public static String attributesDialog_description;
	public static String attributesDialogAddButton_label;
	public static String attributesDialogCancelButton_label;
	public static String attributesDialogAuthors_label;
	public static String attributesDialogInteractions_label;
	public static String attributesDialogPubmedId_label;
	public static String attributesDialogProcessingMethod_label;
	public static String attributesDialogPublication_label;
	public static String attributesDialogPublicationYear_label;
	public static String attributesDialogSource_label;
	public static String attributesDialogSourceUrl_label;
	public static String attributesDialogTags_label;
	public static String attributesDialogTitle_label;
	public static String attributesDialogUrl_label;
	
	public static String maniaResultsAttributesButton_label;
	
	public static String downloadDialogDownloadButton_label;
	public static String downloadDialogCancelButton_label;
	public static String downloadDialogSelectButton_label;
	public static String downloadDialogInstalledColumn_label;
	public static String downloadDialogNameColumn_label;
	public static String downloadDialogDescriptionColumn_label;
	public static String downloadDialogOptionPanel_label;
	public static String downloadDialogOptionPanelInstalled_label;
	public static String downloadDialogOptionPanelActive_label;
	public static String downloadDialogOptionSize_label;
	public static String downloadControllerModelElement_label;
	public static String downloadControllerModelElementActive_label;
	
	public static String retrieveRelatedGenesChooseFile_title;
	public static String jsonDescription;
	public static String retrieveRelatedGenesChooseFile_status;
	public static String retrieveRelatedGenesChooseFile_error;
	
	public static String importOrganismNameColumn_name;
	public static String importOrganismDescriptionColumn_name;
	public static String installedOrganismList_title;
	public static String importOrganism_title;
	public static String importOrganismHelp_label;
	public static String importOrganismBrowseButton_label;
	public static String importOrganismFile_label;
	public static String importOrganismName_label;
	public static String importOrganismAlias_label;
	public static String importOrganismTaxonomyId_label;
	public static String importOrganismDescription_label;
	public static String importOrganismImportButton_label;
	
	public static String editOrganism_title;
	
	public static String importedDataPanelNetworkTab_title;
	public static String importedDataPanelOrganismTab_title;
	public static String importOrganismDelete_title;
	public static String importOrganismUpdate_title;
	public static String importOrganismImport_title;
	public static String importOrganismImport_status;
	
	public static String idFileParser_status;
	public static String networkGroupComboBoxCreateGroup_label;
	
	public static String processingMethodDirect_label;
	public static String processingMethodPearson_label;
	public static String processingMethodLogFrequency_label;
	public static String processingMethodUnknown_label;
	
	public static String networkDetailPanelMoreAt_label;
	public static String networkDetailPanelAttribute_description;
	public static String attributeGroup_label;
	public static String jsonQueryParser_error;
	public static String websiteQueryParser_error;
	public static String cy2_geneManiaPlugin_error;
	
	public static String paste_menuLabel;
	public static String edit_menuLabel;
	public static String resultReconstructor_status;
	public static String resultReconstructorViewState_description;
	public static String resultReconstructorEnrichmentCache_description;
	public static String resultReconstructorNetworkCache_description;
	public static String resultReconstructorSourceNetworks_description;
	public static String resultReconstructorAttributeCache_description;
	public static String resultReconstructorNodeCache_description;
	
	public static String get(String key) {
		return resources.getString(key);
	}
}
