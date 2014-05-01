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

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.genemania.plugin.task.TaskDispatcher;
import org.genemania.plugin.view.util.UiUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.uispec4j.ListBox;
import org.uispec4j.Trigger;
import org.uispec4j.UISpec4J;
import org.uispec4j.Window;
import org.uispec4j.interception.WindowHandler;
import org.uispec4j.interception.WindowInterceptor;

import cytoscape.CyMain;

@SuppressWarnings("nls")
public class PluginUiTests {
	private static Window cytoscape;
	protected static final WindowHandler debugHandler = new WindowHandler() {
		@Override
		public Trigger process(Window window) throws Exception {
			System.out.println(window.getTitle());
			return Trigger.DO_NOTHING;
		}
	};
	private static File settingsFile;
	private static FileUtils fileUtils;

	static {
        UISpec4J.init();
    }

	@AfterClass
	public static void tearDown() throws IOException {
		fileUtils.delete(settingsFile);
	}
	
	public static Window getMainWindow(Trigger trigger) {
		final Window[] result = new Window[1];
		WindowInterceptor.init(trigger).processTransientWindow().process(new WindowHandler() {
			@Override
			public Trigger process(Window window) throws Exception {
				result[0] = window;
				return Trigger.DO_NOTHING;
			}
		}).run();
		return result[0];
	};
	
	@BeforeClass
	public static void setUp() throws IOException {
		fileUtils = new FileUtils();
		
		settingsFile = File.createTempFile("genemaniaSettings", "");
		settingsFile.delete();
		settingsFile.mkdirs();
		
		System.setProperty(GeneMania.SETTINGS_PROPERTY, settingsFile.getCanonicalPath());
		final String corePath = System.getProperty("core.plugin.path");
		final String pluginPath = System.getProperty("genemania.plugin.path");

		cytoscape = getMainWindow(new Trigger() {
			public void run() throws Exception {
				new CyMain(new String[] {"-p", corePath, "-p", pluginPath});
			}
		});
	}

	private TaskDispatcher taskDispatcher;
	
	@Before
	public void setUpInstance() {
		UiUtils uiUtils = new UiUtils();
		taskDispatcher = new TaskDispatcher(uiUtils);
	}
	
	@Test
	public void testDataLoad() throws Exception {
		List<Window> windows = TestUtils.getWindows(new Trigger() {
			public void run() throws Exception {
				cytoscape.getMenuBar().getMenu("Plugins").getSubMenu(Strings.root_menuLabel).getSubMenu(Strings.retrieveRelatedGenes_menuLabel).click();
				
				Window taskWindow = WindowInterceptor.getModalDialog(Trigger.DO_NOTHING);
				System.out.println(taskWindow.getTitle());
			}
		}, new boolean[] {false, true});

		final Window downloadConfirmDialog = windows.get(0);
		
		windows = TestUtils.getWindows(new Trigger() {
			public void run() throws Exception {
				downloadConfirmDialog.getButton("Yes").click();
			}
		}, new boolean[] {true});
		
		final Window manageDataDialog = windows.get(0);
		manageDataDialog.titleEquals(Strings.dataSetConfiguration_title).isTrue();
		
		ListBox availableDataListBox = manageDataDialog.getListBox(Strings.netmaniaAvailableDataList_title);
		availableDataListBox.contains("C. elegans (worm)").isTrue();
		availableDataListBox.select("C. elegans (worm)");
		windows = TestUtils.getWindows(new Trigger() {
			public void run() throws Exception {
				manageDataDialog.getButton(Strings.installDataButton_label).click();
			}
		}, new boolean[] {true});
		taskDispatcher.joinTask();
		
		windows = TestUtils.getWindows(new Trigger() {
			public void run() throws Exception {
				manageDataDialog.getButton(Strings.closeButton_label).click();
			}
		}, new boolean[] {false});
		
		Window searchWindow = windows.get(0);
		searchWindow.getTextBox(Strings.retrieveRelatedGenesStatisticsOrganisms_label).textEquals("1").isTrue();
		searchWindow.getTextBox(Strings.retrieveRelatedGenesStatisticsNetworks_label).textEquals("14").isTrue();
		searchWindow.getTextBox(Strings.retrieveRelatedGenesStatisticsInteractions_label).textEquals("3382762").isTrue();
		
		System.out.println(searchWindow.getDescription());
	}
}
