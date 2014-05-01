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

import org.genemania.plugin.Strings;

import cytoscape.task.TaskMonitor;

public class ConsoleTaskMonitor implements TaskMonitor {

	public void setEstimatedTimeRemaining(long timeRemaining) throws IllegalThreadStateException {
		System.err.printf(Strings.consoleTaskMonitorTimeRemaining_status, timeRemaining);
	}
	
	public void setException(Throwable t, String message) throws IllegalThreadStateException {
		System.err.printf(Strings.consoleTaskMonitorException_status, message);
		t.printStackTrace(System.err);
	}

	public void setException(Throwable t, String message, String recoveryTip) throws IllegalThreadStateException {
		System.err.printf(Strings.consoleTaskMonitorExceptionTip_status, message, recoveryTip);
		t.printStackTrace(System.err);
	}

	public void setPercentCompleted(int completed) throws IllegalThreadStateException, IllegalArgumentException {
		System.err.printf(Strings.consoleTaskMonitorPercentCompleted_status, completed);
	}
	
	public void setStatus(String message) throws IllegalThreadStateException, NullPointerException {
		System.err.printf(Strings.consoleTaskMonitorStatus_status, message);
	}

}
