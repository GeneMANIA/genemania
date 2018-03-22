/**
 * This file is part of GeneMANIA.
 * Copyright (C) 2017 University of Toronto.
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
package org.genemania.plugin.cytoscape3.task;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.cytoscape.application.CyUserLog;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TaskMonitor.Level;
import org.genemania.domain.InteractionNetworkGroup;
import org.genemania.plugin.LogUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Fetch all interaction networks for all organisms available on the GeneMANIA server.
 */
public class LoadRemoteNetworksTask extends AbstractTask {

	// TODO Make it a CyProperty?
	protected static final String URL = "http://genemania.org/json/network_groups";
	private static final String TAG = "network_groups";
	
	private Map<Integer, Collection<InteractionNetworkGroup>> networkGroups = new HashMap<>();
	private String errorMessage;
	
	private final OkHttpClient httpClient; // Avoid creating several instances
	
	private final Logger logger = Logger.getLogger(CyUserLog.NAME);

	public LoadRemoteNetworksTask(OkHttpClient httpClient) {
		this.httpClient = httpClient;
	}
	
	@Override
	public void run(TaskMonitor tm) throws Exception {
		tm.setTitle("GeneMANIA");
		tm.setStatusMessage("Loading networks from server...");
		
		try {
			Request request = new Request.Builder()
					.url(URL)
					.get()
					.tag(TAG)
					.build();
			Response response = httpClient.newCall(request).execute();
			String json = response.body().string();
			
			if (cancelled)
				return;
			
			Gson gson = new Gson();
			Map<Integer, Collection<InteractionNetworkGroup>> ngMap = gson.fromJson(json,
					new TypeToken<Map<Integer, List<InteractionNetworkGroup>>>() { }.getType());

			if (ngMap != null)
				networkGroups.putAll(ngMap);
		} catch (Throwable e) {
			// Don't throw an exception here, we don't want to block the Cytoscape UI.
			errorMessage = "GeneMANIA cannot load networs from the server: " + e.getMessage();
			tm.showMessage(Level.ERROR, errorMessage);
			logger.error("GeneMANIA cannot load networks from the server.", e);
		}
	}
	
	public Map<Integer, Collection<InteractionNetworkGroup>> getNetworkGroups() {
		return networkGroups;
	}
	
	public String getErrorMessage() {
		return errorMessage;
	}
	
	@Override
	public void cancel() {
		super.cancel();
		
		try {
			for (Call call : httpClient.dispatcher().queuedCalls()) {
				if (call.request().tag().equals(TAG))
					call.cancel();
			}
			for (Call call : httpClient.dispatcher().runningCalls()) {
				if (call.request().tag().equals(TAG))
					call.cancel();
			}
		} catch (Exception e) {
			LogUtils.log(getClass(), e);
		}
	}
}
