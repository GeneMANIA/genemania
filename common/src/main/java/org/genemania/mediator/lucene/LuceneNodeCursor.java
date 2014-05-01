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

package org.genemania.mediator.lucene;

import java.util.Iterator;
import java.util.List;

import org.apache.lucene.document.Document;
import org.genemania.exception.ApplicationException;
import org.genemania.mediator.NodeCursor;

public class LuceneNodeCursor implements NodeCursor {

	private Iterator<ObjectProvider<Document>> iterator;
	private Document document;

	public LuceneNodeCursor(List<ObjectProvider<Document>> documents) {
		iterator = documents.iterator();
	}

	public void close() throws ApplicationException {
	}

	public long getId() throws ApplicationException {
		return Long.parseLong(document.get(LuceneMediator.NODE_ID));
	}

	public String getName() throws ApplicationException {
		return document.get(LuceneMediator.NODE_NAME);
	}

	public boolean next() throws ApplicationException {
		if (!iterator.hasNext()) {
			document = null;
			return false;
		}
		document = iterator.next().getObject();
		return true;
	}
}
