/**
 * This file is part of GeneMANIA.
 * Copyright (C) 2010 University of Toronto.
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

package org.genemania.util;

public class NullProgressReporter implements ProgressReporter {
	boolean isCanceled;
	int maximum;
	int progress;
	String status;
	String description;
	
	private static Object mutex = new Object();
	private static NullProgressReporter instance; 

	public static ProgressReporter instance() {
		synchronized (mutex) {
			if (instance == null) {
				instance = new NullProgressReporter();
			}
			return instance;
		}
	}
	
	NullProgressReporter() {
	}
	
	public void cancel() {
		isCanceled = true;
	}

	public int getMaximumProgress() {
		return maximum;
	}

	public int getProgress() {
		return progress;
	}

	public String getStatus() {
		return status;
	}

	public boolean isCanceled() {
		return isCanceled;
	}

	public void setMaximumProgress(int maximum) {
		this.maximum = maximum;
	}

	public void setProgress(int progress) {
		this.progress = progress;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
