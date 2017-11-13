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

package org.genemania.plugin.task;

import org.genemania.util.ProgressReporter;

@Deprecated
public abstract class GeneManiaTask {
	
	private final String title;
	protected ProgressReporter progress;
	private Throwable lastError;
	
	public GeneManiaTask(String title) {
		this.title = title;
	}
	
	public String getTitle() {
		return title;
	}

	public void setProgressReporter(ProgressReporter progress) {
		this.progress = progress;
	}
	
	public void run() {
		try {
			runTask();
		} catch (Throwable t) {
			lastError = t;
		}
	}
	
	public Throwable getLastError() {
		return lastError;
	}
	
	protected abstract void runTask() throws Throwable;
}
