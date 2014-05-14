package org.genemania.adminweb.dao;

import java.util.List;

import org.genemania.adminweb.entity.Group;
import org.genemania.adminweb.entity.Network;
import org.genemania.adminweb.entity.Organism;

import com.j256.ormlite.dao.Dao;

public interface NetworkDao extends Dao<Network, Integer> {

    public List<Network> getNetworks(Organism organism);
    public List<Network> getNetworks(Organism organism, Group group);
    public List<Network> getNetworks(Organism organism, String name);
}
