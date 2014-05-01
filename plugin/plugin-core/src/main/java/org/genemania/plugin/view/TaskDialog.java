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

package org.genemania.plugin.view;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import org.genemania.plugin.LogUtils;
import org.genemania.plugin.Strings;
import org.genemania.plugin.view.util.UiUtils;
import org.genemania.util.ProgressReporter;

@SuppressWarnings("serial")
public class TaskDialog extends JDialog implements ProgressReporter {
	private JEditorPane descriptionLabel;
	private JProgressBar progressBar;
	private JButton cancelButton;
	private boolean isCanceled;
	private int maximumProgress;
	private int progress;
	private String status;
	private JLabel statusLabel;
	private String description;
	private JButton closeButton;
	private JLabel statusImage;
	private JProgressBar memoryBar;
	private JLabel elapsedTimeLabel;
	private long startTime;
	private final UiUtils uiUtils;

	public TaskDialog(Frame owner, String title, boolean modal, boolean cancelable, UiUtils uiUtils) {
		super(owner, title, modal);
		this.uiUtils = uiUtils;
		initialize(cancelable);		
	}

	public TaskDialog(Dialog owner, String title, boolean modal, boolean cancelable, UiUtils uiUtils) {
		super(owner, title, modal);
		this.uiUtils = uiUtils;
		initialize(cancelable);
	}

	public void cancel() {
		isCanceled = true;
	}
	
	public void initialize(boolean cancelable) {
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		
		setLayout(new GridBagLayout());
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		panel.setDoubleBuffered(true);
		
		statusLabel = new JLabel();
		descriptionLabel = uiUtils.createLinkEnabledEditorPane(this, Strings.taskDialogInitialization_status);
		descriptionLabel.setDoubleBuffered(true);
		Dimension minimumSize = descriptionLabel.getMinimumSize();
		minimumSize.height = Math.max(minimumSize.height, descriptionLabel.getFontMetrics(descriptionLabel.getFont()).getHeight() * 3); 
		descriptionLabel.setMinimumSize(minimumSize);
		
		progressBar = new JProgressBar();
		progressBar.setMinimum(0);
		progressBar.setMaximum(100);
		progressBar.setValue(0);
		progressBar.setIndeterminate(true);

		memoryBar = new JProgressBar();
		memoryBar.setMinimum(0);
		memoryBar.setMaximum(100);
		memoryBar.setValue(0);
		
		cancelButton = new JButton(Strings.taskDialogCancelButton_label);
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				isCanceled = true;
			}
		});
		cancelButton.setVisible(cancelable);
		
		final TaskDialog dialog = this;
		closeButton = new JButton(Strings.taskDialogCloseButton_label);
		closeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dialog.setVisible(false);
			}
		});
		closeButton.setVisible(false);

		statusImage = new JLabel();

		JLabel progressLabel = new JLabel(Strings.taskDialogProgress_label);
		JLabel memoryLabel = new JLabel(Strings.taskDialogMemory_label);
		JLabel timeLabel = new JLabel(Strings.taskDialogTime_label);
		elapsedTimeLabel = new JLabel();
		
		Insets insets = new Insets(4, 4, 4, 4);
		panel.add(statusImage, new GridBagConstraints(0, 0, 1, 2, 0, 0, GridBagConstraints.PAGE_START, GridBagConstraints.NONE, insets, 0, 0));
		panel.add(statusLabel, new GridBagConstraints(1, 0, 2, 1, 1, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.HORIZONTAL, insets, 0, 0));
		panel.add(descriptionLabel, new GridBagConstraints(1, 1, 2, 1, 1, 1, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.HORIZONTAL, insets, 0, 0));
		panel.add(progressLabel, new GridBagConstraints(0, 2, 1, 1, 0, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, insets, 0, 0));
		panel.add(progressBar, new GridBagConstraints(1, 2, 2, 1, 1, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.HORIZONTAL, insets, 0, 0));
		panel.add(memoryLabel, new GridBagConstraints(0, 3, 1, 1, 0, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, insets, 0, 0));
		panel.add(memoryBar, new GridBagConstraints(1, 3, 2, 1, 1, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.HORIZONTAL, insets, 0, 0));
		panel.add(cancelButton, new GridBagConstraints(2, 4, 1, 1, 0, 0, GridBagConstraints.FIRST_LINE_END, GridBagConstraints.NONE, insets, 0, 0));
		panel.add(closeButton, new GridBagConstraints(2, 4, 1, 1, 0, 0, GridBagConstraints.FIRST_LINE_END, GridBagConstraints.NONE, insets, 0, 0));
		panel.add(timeLabel, new GridBagConstraints(0, 4, 1, 1, 0, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, insets, 0, 0));
		panel.add(elapsedTimeLabel, new GridBagConstraints(1, 4, 1, 1, 0, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, insets, 0, 0));
		
		startTime = System.currentTimeMillis();
		
		add(panel, new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(16, 16, 16, 16), 0, 0));
		new Thread(new Runnable() {
			public void run() {
				while (!isCanceled) {
					updateMemoryUsage();
					updateTime();
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
					}
				}
			}
		}).start();
	}
	
	public void setStatusIcon(Icon icon) {
		statusImage.setIcon(icon);
	}
	
	public void enableCloseButton(boolean enabled) {
		cancelButton.setVisible(!enabled);
		closeButton.setVisible(enabled);
		progressBar.setIndeterminate(false);
		progressBar.setValue(0);
		invalidate();
	}
	
	public int getMaximumProgress() {
		return maximumProgress;
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

	public void setMaximumProgress(final int maximum) {
		maximumProgress = maximum;
		progressBar.setMaximum(maximum);
	}

	public void setProgress(final int progress) {
		this.progress = progress;
		progressBar.setIndeterminate(false);
		progressBar.setValue(progress);
	}

	public void setStatus(final String status) {
		if (!SwingUtilities.isEventDispatchThread()) {
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					public void run() {
						setStatus(status);
					}
				});
			} catch (InterruptedException e) {
				LogUtils.log(getClass(), e);
			} catch (InvocationTargetException e) {
				LogUtils.log(getClass(), e);
			}
		}
		this.status = status;
		statusLabel.setText(status);
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(final String description) {
		if (!SwingUtilities.isEventDispatchThread()) {
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					public void run() {
						setDescription(description);
					}
				});
			} catch (InterruptedException e) {
				LogUtils.log(getClass(), e);
			} catch (InvocationTargetException e) {
				LogUtils.log(getClass(), e);
			}
		}
		this.description = description;
		descriptionLabel.setText(description);
	}
	
	void updateMemoryUsage() {
		Runtime runtime = Runtime.getRuntime();
		double free = runtime.freeMemory();
		double size = runtime.totalMemory();
		double used = size - free;
		double maxMemory = runtime.maxMemory();
		int usage = (int) (Math.round((maxMemory - used) / maxMemory * 100));
		memoryBar.setValue(usage);
	}
	
	void updateTime() {
		long elapsedTime = System.currentTimeMillis() - startTime;
		int seconds = (int) (elapsedTime / 1000.0);
		int minutes = seconds / 60;
		int hours = seconds / 3600;
		elapsedTimeLabel.setText(String.format("%d:%02d:%02d", hours, minutes % 60, seconds % 60)); //$NON-NLS-1$
	}
}
