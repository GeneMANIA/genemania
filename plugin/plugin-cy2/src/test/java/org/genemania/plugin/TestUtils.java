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

import java.util.ArrayList;
import java.util.List;

import org.uispec4j.Trigger;
import org.uispec4j.Window;
import org.uispec4j.interception.handlers.InterceptionHandler;
import org.uispec4j.interception.toolkit.UISpecDisplay;
import org.uispec4j.utils.TriggerRunner;

public class TestUtils {
	public static List<Window> getWindows(Trigger trigger, final boolean[] shouldBeModal) throws InterruptedException {
		final Object barrier = new Object();
		final List<Window> windows = new ArrayList<Window>();
	    UISpecDisplay.instance().rethrowIfNeeded();
	    
	    InterceptionHandler handler = new InterceptionHandler() {
			public void process(Window window) {
				System.out.println(window.getDescription());
				windows.add(window);
				if (windows.size() == shouldBeModal.length) {
					synchronized (barrier) {
						barrier.notifyAll();
					}
				}
			}
		};
		UISpecDisplay.instance().add(handler);
		
		for (boolean modal : shouldBeModal) {
			if (modal) {
				UISpecDisplay.instance().add(handler);
			}
		}
	    try {
	    	TriggerRunner.runInUISpecThread(trigger);
	    	synchronized (barrier) {
	    		barrier.wait();
	    	}
	    } finally {
	      UISpecDisplay.instance().remove(handler);
	      UISpecDisplay.instance().rethrowIfNeeded();
	    }
	    return windows;
	}
}
