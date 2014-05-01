package org.genemania.service;

import java.io.IOException;
import java.io.InputStream;

import org.genemania.domain.InteractionNetwork;
import org.genemania.exception.ApplicationException;
import org.genemania.exception.DataStoreException;
import org.genemania.exception.SystemException;
import org.genemania.exception.ValidationException;

public interface UploadNetworkService {

	/**
	 * Uploads a network to the engine
	 * 
	 * @param name
	 *            The network name
	 * @param stream
	 *            The input stream containing the network file
	 * @param organismId
	 *            The organismId corresponding to the network
	 * @param sessionId
	 *            The user's sessionId
	 * @return The uploaded network
	 */
	public InteractionNetwork upload(String name, InputStream stream,
			long organismId, String sessionId) throws ApplicationException,
			IOException, DataStoreException, ValidationException,
			SystemException;

	/**
	 * Deletes a user network from the engine
	 * 
	 * @param organismId
	 *            The organism for the network
	 * @param networkId
	 *            The network ID
	 * @param sessionId
	 *            The user's session ID
	 * @throws ApplicationException
	 *             if the network can't be deleted
	 */
	public void delete(Integer organismId, Long networkId, String sessionId)
			throws DataStoreException;

}
