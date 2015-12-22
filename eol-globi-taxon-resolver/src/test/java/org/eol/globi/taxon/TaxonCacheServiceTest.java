package org.eol.globi.taxon;

import org.apache.commons.io.FileUtils;
import org.eol.globi.domain.PropertyAndValueDictionary;
import org.eol.globi.domain.Taxon;
import org.eol.globi.service.PropertyEnricherException;
import org.eol.globi.service.TaxonUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class TaxonCacheServiceTest {

    private File mapdbDir;

    @Before
    public void createMapDBFolder() throws IOException {
        mapdbDir = new File("./target/mapdb" + new Random().nextLong());
    }

    @After
    public void deleteMapDBFolder() throws IOException {
        FileUtils.deleteQuietly(mapdbDir);
    }

    @Test
    public void enrichByName() throws PropertyEnricherException {
        Map<String, String> properties = new HashMap<String, String>() {
            {
                put(PropertyAndValueDictionary.NAME, "Green-winged teal");
            }
        };
        final TaxonCacheService cacheService = getTaxonCacheService();
        Map<String, String> enrich = cacheService.enrich(properties);
        Taxon enrichedTaxon = TaxonUtil.mapToTaxon(enrich);
        assertThat(enrichedTaxon.getName(), is("Anas crecca carolinensis"));
        assertThat(enrichedTaxon.getExternalId(), is("EOL:1276240"));
        assertThat(enrichedTaxon.getThumbnailUrl(), is("http://media.eol.org/content/2012/11/04/08/35791_98_68.jpg"));
    }

    @Test
    public void enrichByNameMissingThumbnail() throws PropertyEnricherException {
        Map<String, String> properties = new HashMap<String, String>() {
            {
                put(PropertyAndValueDictionary.NAME, "Acteocina inculcata");
            }
        };
        final TaxonCacheService cacheService = getTaxonCacheService();
        Map<String, String> enrich = cacheService.enrich(properties);
        Taxon enrichedTaxon = TaxonUtil.mapToTaxon(enrich);
        assertThat(enrichedTaxon.getName(), is("Acteocina inculta"));
        assertThat(enrichedTaxon.getExternalId(), is("EOL:455065"));
        assertThat(enrichedTaxon.getThumbnailUrl(), is(""));
    }

    public TaxonCacheService getTaxonCacheService() {
        final TaxonCacheService cacheService = new TaxonCacheService("taxonCache.csv", "taxonMap.csv");
        cacheService.setCacheDir(mapdbDir);
        return cacheService;
    }

    @Test
    public void enrichPassThrough() throws PropertyEnricherException {
        Map<String, String> properties = new HashMap<String, String>() {
            {
                put(PropertyAndValueDictionary.NAME, "some name");
                put(PropertyAndValueDictionary.EXTERNAL_ID, "some cached externalId");
            }
        };
        Map<String, String> enrich = getTaxonCacheService().enrich(properties);
        Taxon enrichedTaxon = TaxonUtil.mapToTaxon(enrich);
        assertThat(enrichedTaxon.getName(), is("some name"));
        assertThat(enrichedTaxon.getExternalId(), is("some cached externalId"));
    }

    @Test
    public void enrichPassThroughNoMatch() throws PropertyEnricherException {
        Map<String, String> properties = new HashMap<String, String>() {
            {
                put(PropertyAndValueDictionary.NAME, PropertyAndValueDictionary.NO_MATCH);
            }
        };
        Map<String, String> enrich = getTaxonCacheService().enrich(properties);
        Taxon enrichedTaxon = TaxonUtil.mapToTaxon(enrich);
        assertThat(enrichedTaxon.getName(), is(PropertyAndValueDictionary.NO_MATCH));
        assertThat(enrichedTaxon.getExternalId(), is(nullValue()));
    }

    @Test
    public void enrichById() throws PropertyEnricherException {
        Map<String, String> properties = new HashMap<String, String>() {
            {
                put(PropertyAndValueDictionary.EXTERNAL_ID, "EOL:1276240");
            }
        };
        final TaxonCacheService taxonCacheService = getTaxonCacheService();
        Map<String, String> enrich = taxonCacheService.enrich(properties);
        Taxon enrichedTaxon = TaxonUtil.mapToTaxon(enrich);
        assertThat(enrichedTaxon.getName(), is("Anas crecca carolinensis"));
        assertThat(enrichedTaxon.getExternalId(), is("EOL:1276240"));
        taxonCacheService.shutdown();
    }

    @Test
    public void enrichByIdGzip() throws PropertyEnricherException {
        Map<String, String> properties = new HashMap<String, String>() {
            {
                put(PropertyAndValueDictionary.EXTERNAL_ID, "EOL:1276240");
            }
        };
        Map<String, String> enrich = getTaxonCacheService().enrich(properties);
        Taxon enrichedTaxon = TaxonUtil.mapToTaxon(enrich);
        assertThat(enrichedTaxon.getName(), is("Anas crecca carolinensis"));
        assertThat(enrichedTaxon.getExternalId(), is("EOL:1276240"));
    }


}