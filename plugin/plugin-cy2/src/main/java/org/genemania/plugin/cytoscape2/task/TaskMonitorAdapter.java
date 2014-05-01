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

package org.genemania.plugin.cytoscape2.task;

import org.genemania.util.ProgressReporter;

import cytoscape.task.TaskMonitor;

public class TaskMonitorAdapter implements ProgressReporter {
	int progress;
	int maximumProgress;
	boolean isCanceled;
	String status;
	TaskMonitor monitor;

	public void cancel() {
		isCanceled = true;
	}

	public int getMaximumProgress() {
		return maximumProgress;
	}

	public int getProgress() {
		return 0;
	}

	public String getStatus() {
		return status;
	}

	public boolean isCanceled() {
		return isCanceled;
	}

	public void setMaximumProgress(int maximum) {
		maximumProgress = maximum; 
	}

	public void setProgress(int progress) {
		this.progress = progress;
		monitor.setPercentCompleted((int) (100.0 * progress / maximumProgress));
	}

	public void setStatus(String status) {
		this.status = status;
		monitor.setStatus(status);
	}

	public void setTaskMonitor(TaskMonitor monitor) {
		this.monitor = monitor;
	}

	public TaskMonitor getTaskMonitor() {
		return monitor;
	}

	public String getDescription() {
		return null;
	}

	public void setDescription(String text) {
	}
}
