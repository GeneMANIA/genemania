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

package org.genemania.plugin.data;

import org.genemania.mediator.AttributeMediator;
import org.genemania.mediator.GeneMediator;
import org.genemania.mediator.NetworkMediator;
import org.genemania.mediator.NodeMediator;
import org.genemania.mediator.OntologyMediator;
import org.genemania.mediator.OrganismMediator;
import org.genemania.mediator.StatsMediator;

public interface IMediatorProvider {
	OrganismMediator getOrganismMediator();
	GeneMediator getGeneMediator();
	NodeMediator getNodeMediator();
	NetworkMediator getNetworkMediator();
	StatsMediator getStatsMediator();
	OntologyMediator getOntologyMediator();
	AttributeMediator getAttributeMediator();
}
