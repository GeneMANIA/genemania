package org.genemania.service;

import java.util.Collection;

import org.genemania.domain.Organism;
import org.genemania.exception.DataStoreException;

/**
 * Used to access organism domain data
 */
public interface OrganismService {

	/**
	 * Gets all organisms
	 * 
	 * @return The set of all organisms
	 */
	public Collection<Organism> getOrganisms() throws DataStoreException;

	/**
	 * Gets a particular organism by its ID
	 * 
	 * @param id
	 *            The organism ID
	 * @return The organism corresponding to the ID
	 */
	public Organism findOrganismById(Long id) throws DataStoreException;

	/**
	 * Gets the default query organism
	 * 
	 * @return The default query organism
	 */
	public Organism getDefaultOrganism() throws DataStoreException;

}
