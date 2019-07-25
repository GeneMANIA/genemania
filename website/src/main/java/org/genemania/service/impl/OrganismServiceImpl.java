package org.genemania.service.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.genemania.dao.OrganismDao;
import org.genemania.domain.Organism;
import org.genemania.exception.DataStoreException;
import org.genemania.service.OrganismService;
import org.springframework.beans.factory.annotation.Autowired;

public class OrganismServiceImpl implements OrganismService {

	@Autowired
	private OrganismDao organismDao;

	@Override
	public Organism findOrganismById(Long id) throws DataStoreException {
		return organismDao.findOrganism(id);
	}

	@Override
	public Collection<Organism> getOrganisms() throws DataStoreException {
		List<Organism> orgs = organismDao.getAllOrganisms();
		Organism[] orgsArray = new Organism[orgs.size()];
		for (int i = 0; i < orgs.size(); i++) {
			Organism org = orgs.get(i);
			orgsArray[i] = org;

		}

		Comparator<Organism> nameComp = new Comparator<Organism>() {

			@Override
			public int compare(Organism org0, Organism org1) {
				return org0.getName().toLowerCase()
						.compareTo(org1.getName().toLowerCase());
			}

		};

		Arrays.sort(orgsArray, nameComp);

		List<Organism> sortedOrgs = new LinkedList<Organism>();
		for (Organism org : orgsArray) {
			sortedOrgs.add(org);
		}

		return sortedOrgs;
	}

	@Override
	public Organism getDefaultOrganism() throws DataStoreException {
		Organism human;

		try {
			human = this.findOrganismById(4L);

			if (human == null) {
				Iterator<Organism> iter = this.getOrganisms().iterator();
				return iter.next();
			}

			return human;

		} catch (DataStoreException e) { // if we can't get human, just get one
											// from the set
			Iterator<Organism> iter = this.getOrganisms().iterator();
			return iter.next();
		}
	}

	@Override
	public Organism findOrganismByString(String str) throws DataStoreException {
		Collection<Organism> organisms = this.getOrganisms();

		// matches id
		try {
			Long id = Long.parseLong(str);

			return this.findOrganismById(id);
		} catch (NumberFormatException e) {
			// then keep on trying, matching as name instead
		}

		// matches taxonomy id
		try {
			Long tid = Long.parseLong(str);

			for(Organism org : organisms){
				if( org.getTaxonomyId() == tid ){
					return org;
				}
			}
		} catch (NumberFormatException e) {
			// then keep on trying, matching as name instead
		}

		for (Organism org : organisms) {
			// matches name
			if (org.getName().toLowerCase().equals(str.toLowerCase())) {
				return org;
			}

			// matches alias
			if (org.getAlias().toLowerCase().equals(str.toLowerCase())) {
				return org;
			}

			// matches description
			if (org.getDescription().toLowerCase().equals(str.toLowerCase())) {
				return org;
			}
		}

		throw new DataStoreException("No organism matching `" + str
				+ "` could be found");
	}

	public OrganismDao getOrganismDao() {
		return organismDao;
	}

	public void setOrganismDao(OrganismDao organismDao) {
		this.organismDao = organismDao;
	}

}
