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

/**
 * Subdivides a single unit of progress from a ProgressReporter into an
 * entirely new ProgressReporter range.   
 */
public class ChildProgressReporter implements ProgressReporter {
	private ProgressReporter parent;
	private State initialState;
	private State current;

	public ChildProgressReporter(ProgressReporter parent) {
		this.parent = parent;
		initialState = new State(parent);
		current = new State();
	}
	
	public void close() {
		initialState.progress++;
		initialState.apply(parent);
	}
	
	public void cancel() {
		parent.cancel();
	}

	public String getDescription() {
		return parent.getDescription();
	}

	public int getMaximumProgress() {
		return current.maximum;
	}

	public int getProgress() {
		return current.progress;
	}

	public String getStatus() {
		return current.status;
	}

	public boolean isCanceled() {
		return parent.isCanceled();
	}

	public void setDescription(String description) {
		current.description = description;
		parent.setDescription(description);
	}

	public void setMaximumProgress(int maximum) {
		current.maximum = maximum;
		parent.setMaximumProgress(initialState.maximum * maximum);
	}

	public void setProgress(int progress) {
		current.progress = progress;
		parent.setProgress(initialState.progress * current.maximum + current.progress);
	}

	public void setStatus(String status) {
		current.status = status;
		parent.setStatus(status);
	}

	static class State {
		String status;
		String description;
		int maximum;
		int progress;
		
		public State(ProgressReporter reporter) {
			status = reporter.getStatus();
			description = reporter.getDescription();
			maximum = reporter.getMaximumProgress();
			progress = reporter.getProgress();
		}
		
		public State() {
			maximum = 100;
			progress = 0;
		}
		
		public void apply(ProgressReporter reporter) {
			reporter.setStatus(status);
			reporter.setDescription(description);
			reporter.setMaximumProgress(maximum);
			reporter.setProgress(progress);
		}
	}
}
