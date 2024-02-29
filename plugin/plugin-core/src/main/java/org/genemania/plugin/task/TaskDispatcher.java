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

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Window;

import javax.swing.UIManager;

import org.genemania.exception.ApplicationException;
import org.genemania.plugin.LogUtils;
import org.genemania.plugin.Strings;
import org.genemania.plugin.view.TaskDialog;
import org.genemania.plugin.view.util.UiUtils;

public class TaskDispatcher {
	
	private final UiUtils uiUtils;
	private final Object mutex = new Object();
	private final Object executionMutex = new Object();

	private Thread taskThread;
	private TaskDialog taskDialog;

	public TaskDispatcher(UiUtils uiUtils) {
		this.uiUtils = uiUtils;
	}
	
	public void executeTask(GeneManiaTask task, Window owner, boolean modal, boolean cancelable) {
		synchronized (mutex) {
			if (taskDialog != null) {
				throw new RuntimeException();
			}
			if (owner instanceof Frame) {
				taskDialog = new TaskDialog((Frame) owner, task.getTitle(), modal, cancelable, uiUtils);
			} else if (owner instanceof Dialog) {
				taskDialog = new TaskDialog((Dialog) owner, task.getTitle(), modal, cancelable, uiUtils);
			} else {
				throw new RuntimeException();
			}
			taskDialog.setMinimumSize(new Dimension(500, 220));
			taskDialog.setLocationByPlatform(true);
			taskDialog.setStatusIcon(UIManager.getIcon("OptionPane.informationIcon")); //$NON-NLS-1$
			taskDialog.pack();
		}
		final Object executionMutex = new Object();
		taskThread = new Thread(createTaskHandler(task));
		taskThread.start();
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
		}
		
		synchronized (executionMutex) {
			if (taskDialog == null) {
				return;
			}
			if (!taskThread.isAlive() && task.getLastError() != null) {
				return;
			}
			taskDialog.setVisible(true);
		}
	}

	protected Runnable createTaskHandler(GeneManiaTask task) {
		return new TaskHandler(task);
	}

	public void joinTask() throws ApplicationException {
		synchronized (executionMutex) {
			if (taskThread == null) {
				return;
			}
		}
		try {
			taskThread.join();
		} catch (InterruptedException e) {
			throw new ApplicationException(e);
		}
	}
	
	public TaskDialog getTaskDialog() {
		return taskDialog;
	}
	
	public class TaskHandler implements Runnable {
		private GeneManiaTask task;

		public TaskHandler(GeneManiaTask task) {
			this.task = task;
		}
		
		@Override
		public void run() {
			task.setProgressReporter(taskDialog);
			task.run();
			Throwable lastError = task.getLastError();
			try {
				if (lastError != null) {
					LogUtils.log(getClass(), lastError);
					taskDialog.setStatusIcon(UIManager.getIcon("OptionPane.errorIcon")); //$NON-NLS-1$
					taskDialog.setStatus(Strings.taskDialog_error);
					if (lastError instanceof OutOfMemoryError) {
						taskDialog.setDescription(Strings.taskDialogOutOfMemory_error);
					} else {
						taskDialog.setDescription(lastError.getMessage());
					}
					taskDialog.enableCloseButton(true);
				} else {
					taskDialog.setVisible(false);
				}
			} finally {
				synchronized (executionMutex) {
					taskDialog = null;
					taskThread = null;
				}
			}
		}
	}
}
