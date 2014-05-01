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

package org.genemania.plugin.controllers;

import java.util.ArrayList;
import java.util.List;

import org.genemania.plugin.Strings;
import org.genemania.plugin.cytoscape.CytoscapeUtils;
import org.genemania.plugin.model.AttributeEntry;

public class AttributesController {
	public List<AttributeEntry> createModel() {
		List<AttributeEntry> model = new ArrayList<AttributeEntry>();
		model.add(new AttributeEntry(Strings.attributesDialogAuthors_label, CytoscapeUtils.AUTHORS));
		model.add(new AttributeEntry(Strings.attributesDialogInteractions_label, CytoscapeUtils.INTERACTION_COUNT));
		model.add(new AttributeEntry(Strings.attributesDialogPubmedId_label, CytoscapeUtils.PUBMED_ID));
		model.add(new AttributeEntry(Strings.attributesDialogProcessingMethod_label, CytoscapeUtils.PROCESSING_DESCRIPTION));
		model.add(new AttributeEntry(Strings.attributesDialogPublication_label, CytoscapeUtils.PUBLICATION_NAME));
		model.add(new AttributeEntry(Strings.attributesDialogPublicationYear_label, CytoscapeUtils.YEAR_PUBLISHED));
		model.add(new AttributeEntry(Strings.attributesDialogSource_label, CytoscapeUtils.SOURCE));
		model.add(new AttributeEntry(Strings.attributesDialogSourceUrl_label, CytoscapeUtils.SOURCE_URL));
		model.add(new AttributeEntry(Strings.attributesDialogTags_label, CytoscapeUtils.TAGS));
		model.add(new AttributeEntry(Strings.attributesDialogTitle_label, CytoscapeUtils.TITLE));
		model.add(new AttributeEntry(Strings.attributesDialogUrl_label, CytoscapeUtils.URL));
		return model;
	}
}
