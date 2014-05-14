package org.genemania.adminweb.validators;

import org.genemania.adminweb.exception.DatamartException;
import org.genemania.adminweb.validators.stats.NetworkValidationStats;

public interface Validator {
    public NetworkValidationStats validate() throws DatamartException;
}
