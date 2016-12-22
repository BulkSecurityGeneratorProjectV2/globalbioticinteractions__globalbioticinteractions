package org.eol.globi.service;

import org.junit.Test;

import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

public class DatasetFinderCachingIT {

    @Test
    public void zenodoTest() throws DatasetFinderException {
        DatasetFinder finder = new DatasetFinderCaching(new DatasetFinderZenodo());

        Dataset dataset = DatasetFactory.datasetFor("globalbioticinteractions/template-dataset", finder);

        assertThat(dataset.getArchiveURI().toString(), containsString("zenodo.org"));
        assertThat(dataset.getResourceURI("globi.json").toString(), startsWith("jar:file:/"));
        assertThat(dataset.getCitation(), is("Jorrit H. Poelen. 2014. Species associations manually extracted from literature."));

    }

    @Test
    public void gitHubTest() throws DatasetFinderException {
        DatasetFinder finder = new DatasetFinderCaching(new DatasetFinderGitHubArchive());

        Dataset dataset = DatasetFactory.datasetFor("globalbioticinteractions/Catalogue-of-Afrotropical-Bees", finder);

        assertThat(dataset.getArchiveURI().toString(), containsString("github.com"));
        assertThat(dataset.getResourceURI("globi.json").toString(), startsWith("jar:file:/"));
        assertThat(dataset.getCitation(), is("Catalogue of Afrotropical Bees. 2011. http://doi.org/10.15468/u9ezbh"));

    }


}