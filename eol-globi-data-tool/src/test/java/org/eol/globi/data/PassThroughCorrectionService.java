package org.eol.globi.data;

import org.eol.globi.taxon.CorrectionService;

public class PassThroughCorrectionService implements CorrectionService {
    @Override
    public String correct(String taxonName) {
        return taxonName;
    }
}
