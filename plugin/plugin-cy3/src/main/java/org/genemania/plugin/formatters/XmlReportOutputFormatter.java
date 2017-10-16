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

package org.genemania.plugin.formatters;

import java.io.OutputStream;

import org.genemania.plugin.controllers.IGeneProvider;
import org.genemania.plugin.data.DataSet;
import org.genemania.plugin.model.ViewState;
import org.genemania.plugin.report.ManiaReport;
import org.genemania.plugin.report.ReportExporter;
import org.genemania.plugin.report.XmlReportExporter;

public class XmlReportOutputFormatter implements IOutputFormatter {
	private final DataSet fData;
	private final IGeneProvider fProvider;
	
	public XmlReportOutputFormatter(DataSet data, IGeneProvider provider) {
		fData = data;
		fProvider = provider;
	}

	public void format(OutputStream out, ViewState viewState) {
		ReportExporter exporter = new XmlReportExporter(fProvider);
		ManiaReport report = new ManiaReport(viewState, fData);
		exporter.export(report, out);
	}

	public String getExtension() {
		return "report.xml"; //$NON-NLS-1$
	}

}
