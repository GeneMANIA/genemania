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

package org.genemania.completion;

/**
 * Handles events from a CompletionProvider.  The typical lifecycle of a
 * completion operation consists of a tooManyCompletions() event, or
 * zero or more consume() events.  Finally, a finish() event indicates the
 * completion operation has concluded.
 */
public interface CompletionConsumer {
	/**
	 * Handles a completion proposal.  This method is called once for each
	 * candidate completion given by the CompletionProvider.
	 */
	void consume(String completion);
	
	/**
	 * Handles the event where the CompletionProvider cannot compute the
	 * requested completions because of some sort of limit.
	 */
	void tooManyCompletions();
	
	/**
	 * Handles any post-operation activities.
	 */
	void finish();
}
