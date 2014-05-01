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

package org.genemania.data.profile;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * A ProfileCursor for reading Simple Omnibus Format in Text-formatted
 * expression profile data.  This cursor handles both the annotated expression
 * data format and raw SOFT submissions to GEO.
 */
public class SoftProfileCursor extends ReaderProfileCursor {
	private static final Pattern METADATA = Pattern.compile("^[!^#].*"); //$NON-NLS-1$
	private static final Pattern GDS_HEADER = Pattern.compile("GDS\\d+"); //$NON-NLS-1$
	private static final Pattern GSM_HEADER = Pattern.compile("GSM\\d+"); //$NON-NLS-1$
	
	String[] current;
	String next;
	
	int idColumn;
	List<Integer> dataColumns;
	List<String> headers;
	
	public SoftProfileCursor(Reader reader) throws IOException {
		super(reader);
		dataColumns = new ArrayList<Integer>();
		headers = new ArrayList<String>();
		findDataLine();
		if (next != null) {
			if (GDS_HEADER.matcher(next).matches()) {
				handleAnnotatedFormat();
			} else {
				handleSoftFormat();
			}
		}
	}

	private boolean isIdColumn(String data) {
		return "gene symbol".equalsIgnoreCase(data) || "identifier".equalsIgnoreCase(data);  //$NON-NLS-1$//$NON-NLS-2$
	}
	
	private void handleSoftFormat() {
		headers.add("ID"); //$NON-NLS-1$
		idColumn = 0;
		String[] header = next.split("\t"); //$NON-NLS-1$
		for (int i = 0; i < header.length; i++) {
			if (isIdColumn(header[i])) {
				idColumn = i;
			} else if (GSM_HEADER.matcher(header[i]).matches()) {
				dataColumns.add(i);
				headers.add(header[i]);
			}
		}
		
		// This isn't Soft format.  Assume all non-id columns are data.
		if (headers.size() == 1) {
			for (int i = 0; i < header.length; i++) {
				if (i != idColumn) {
					dataColumns.add(i);
					headers.add(header[i]);
				}
			}
		}
		findDataLine();
	}

	private void handleAnnotatedFormat() throws IOException {
		headers.add("ID"); //$NON-NLS-1$
		String header = reader.readLine();
		if (header == null) {
			return;
		}
		String[] parts = header.split("\t"); //$NON-NLS-1$
		for (int i = 0; i < parts.length; i++) {
			if (isIdColumn(parts[i])) {
				idColumn = i;
			} else if (GSM_HEADER.matcher(parts[i]).matches()) {
				dataColumns.add(i);
				headers.add(parts[i]);
			}
		}
		// Skip over series summary
		reader.readLine();
		next = reader.readLine();
	}

	public String getId() {
		return current[idColumn];
	}

	public int getTotalValues() {
		return dataColumns.size();
	}

	public double getValue(int index) {
		try {
			return Double.parseDouble(current[dataColumns.get(index)]);
		} catch (NumberFormatException e) {
			return Double.NaN;
		}
	}

	public boolean next() {
		if (next == null) {
			return false;
		}
		if (next.trim().length() == 0) {
			next = null;
			return false;
		}
		current = next.split("\t"); //$NON-NLS-1$
		findDataLine();
		return true;
	}

	void findDataLine() {
		try {
			while ((next = reader.readLine()) != null) {
				if (!METADATA.matcher(next).matches()) {
					break;
				}
			}
		} catch (IOException e) {
			next = null;
		}
	}

	public String getHeader(int index) {
		return headers.get(index);
	}

	public int getTotalHeaders() {
		return headers.size();
	}
}
