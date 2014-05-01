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

/**
 * AbstractTest: JUnit base test class
 * Created Jul 22, 2008
 * @author Ovi Comes
 */
package org.genemania;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import junit.framework.TestCase;

import org.apache.log4j.Logger;

public abstract class AbstractTest extends TestCase {

	// __[static]______________________________________________________________
	private static Logger LOG = Logger.getLogger(AbstractTest.class);

	// __[attributes]__________________________________________________________
	// protected OrganismMediator organismMediator;
	// protected NetworkMediator networkMediator;
	// protected GeneMediator geneMediator;
	// protected NodeMediator nodeMediator;

	// __[constructors]________________________________________________________
	public AbstractTest() {
		// super();
		// this.setAutowireMode(AUTOWIRE_BY_NAME);
		// this.setDependencyCheck(false);
		// sessionFactory =
		// (SessionFactory)this.getApplicationContext().getBean("sessionFactory");
	}

	// __[framework methods]___________________________________________________
	@Override
	protected void setUp() throws Exception {
		// super.onSetUp();
		// session = SessionFactoryUtils.getSession(this.sessionFactory, true);
		// TransactionSynchronizationManager.bindResource(this.sessionFactory,
		// new SessionHolder(session));
	}

	@Override
	protected void tearDown() throws Exception {
		// TransactionSynchronizationManager.unbindResource(this.sessionFactory);
		// SessionFactoryUtils.releaseSession(this.session,
		// this.sessionFactory);
		// super.onTearDown();
	}

	// __[Spring requirements]_________________________________________________
	// @Override
	// protected String[] getConfigLocations(){
	// return new String[] {"classpath:/TestApplicationContext.xml"};
	// }
	//	
	// public void setOrganismMediator(OrganismMediator organismMediator) {
	// this.organismMediator = organismMediator;
	// }
	//
	// public OrganismMediator getOrganismMediator() {
	// return organismMediator;
	// }
	//
	// public void setNetworkMediator(NetworkMediator networkMediator) {
	// this.networkMediator = networkMediator;
	// }
	//
	// public NetworkMediator getNetworkMediator() {
	// return networkMediator;
	// }
	//
	// public void setGeneMediator(GeneMediator geneMediator) {
	// this.geneMediator = geneMediator;
	// }
	//
	// public GeneMediator getGeneMediator() {
	// return geneMediator;
	// }
	//
	// public void setNodeMediator(NodeMediator NodeMediator) {
	// this.nodeMediator = NodeMediator;
	// }
	//
	// public NodeMediator getNodeMediator() {
	// return nodeMediator;
	// }

	// __[public helpers]______________________________________________________
	public String readTestFile(String filename) throws IOException {
		String ret = "";
		File networkFile = new File(filename);
		assertTrue(networkFile.getAbsolutePath() + " exists", networkFile
				.exists());
		FileInputStream fis = new FileInputStream(networkFile);
		byte[] buf = new byte[(int) networkFile.length()];
		fis.read(buf);
		ret = new String(buf);
		return ret;
	}

}
