package org.genemania.plugin.util;

import org.cytoscape.work.TaskMonitor;
import org.genemania.util.ProgressReporter;

/**
 * Implementation of GeneMANIA's {@link ProgressReporter} that delegate to a {@link TaskMonitor},
 * in order to use Cytoscape 3 Tasks to run the GeneMANIA engine.
 */
public class TaskMonitorProgressReporter implements ProgressReporter {

	private boolean cancelled;
	private int maximum;
	private int progress;
	private String status;
	private String description;
	
	private final TaskMonitor tm;
	
	public TaskMonitorProgressReporter(TaskMonitor tm, int maximum) {
		this.tm = tm;
		setMaximumProgress(maximum);
	}
	
	@Override
	public void setProgress(int progress) {
		if (progress != this.progress) {
			this.progress = progress;
			tm.setProgress(Math.min(1.0, (double) progress / maximum));
		}
	}

	@Override
	public int getProgress() {
		return progress;
	}

	@Override
	public void setMaximumProgress(int maximum) {
		this.maximum = maximum;
	}

	@Override
	public int getMaximumProgress() {
		return maximum;
	}

	@Override
	public void setStatus(String status) {
		this.status = status;
		
		if (status != null && !status.trim().isEmpty())
			tm.setStatusMessage(status);
	}

	@Override
	public String getStatus() {
		return status;
	}

	@Override
	public void setDescription(String description) {
		this.description = description;
		
		if (description != null && !description.trim().isEmpty())
			tm.setTitle(description);
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public void cancel() {
		cancelled = true;
		tm.setProgress(1.0);
	}

	@Override
	public boolean isCanceled() {
		return cancelled;
	}
}
