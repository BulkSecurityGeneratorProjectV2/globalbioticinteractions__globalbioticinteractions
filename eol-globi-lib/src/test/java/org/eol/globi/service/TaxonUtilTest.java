package org.eol.globi.service;

import org.eol.globi.domain.PropertyAndValueDictionary;
import org.eol.globi.domain.Taxon;
import org.eol.globi.domain.TaxonImage;
import org.eol.globi.domain.TaxonImpl;
import org.eol.globi.domain.Term;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.Map;
import java.util.TreeMap;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class TaxonUtilTest {

    @Test
    public void homonym() {
        TaxonImpl taxon = new TaxonImpl();
        taxon.setName("Lestes");
        taxon.setPath("Insecta|Lestidae|Lestes");
        taxon.setPathNames("class|family|genus");

        TaxonImpl otherTaxon = new TaxonImpl();
        otherTaxon.setName("Lestes");
        otherTaxon.setPath("Mammalia|Mesonychidae|Lestes");
        otherTaxon.setPathNames("class|family|genus");

        assertThat(TaxonUtil.likelyHomonym(taxon, otherTaxon), is(true));
    }

    @Test
    public void homonymBacteria() {
        TaxonImpl taxon = new TaxonImpl();

        taxon.setName("Bacteria");
        taxon.setPath(" | Eukaryota | Opisthokonta | Metazoa | Eumetazoa | Bilateria | Protostomia | Ecdysozoa | Panarthropoda | Arthropoda | Mandibulata | Pancrustacea | Hexapoda | Insecta | Dicondylia | Pterygota | Neoptera | Orthopteroidea | Phasmatodea | Verophasmatodea | Anareolatae | Diapheromeridae | Diapheromerinae | Diapheromerini | Bacteria");
        taxon.setPathNames(" | superkingdom |  | kingdom |  |  |  |  |  | phylum |  |  | superclass | class |  |  | subclass | infraclass | order | suborder | infraorder | family | subfamily | tribe | genus");

        TaxonImpl otherTaxon = new TaxonImpl();
        otherTaxon.setName("Bacteria");
        otherTaxon.setPath("Bacteria");
        otherTaxon.setPathNames("kingdom");

        assertThat(TaxonUtil.likelyHomonym(taxon, otherTaxon), is(true));
    }

    @Test
    public void copyTaxon() {
        Taxon src = new TaxonImpl("name", "id");
        src.setStatus(new Term("statusId", "statusLabel"));
        Taxon target = new TaxonImpl();
        TaxonUtil.copy(src, target);
        assertThat(target.getStatus().getId(), is("statusId"));
        assertThat(target.getStatus().getName(), is("statusLabel"));
    }

    @Test
    public void copyTaxonPrefillExternalURL() {
        Taxon src = new TaxonImpl("name", "GBIF:123");
        src.setStatus(new Term("statusId", "statusLabel"));
        Taxon target = new TaxonImpl();
        TaxonUtil.copy(src, target);
        assertThat(target.getStatus().getId(), is("statusId"));
        assertThat(target.getStatus().getName(), is("statusLabel"));
        assertThat(target.getExternalUrl(), is("http://www.gbif.org/species/123"));
    }

    @Test
    public void notHomonym() {
        TaxonImpl taxon = new TaxonImpl();
        taxon.setName("Lestes");
        taxon.setPath("Insecta|Lestidae|Lestes");
        taxon.setPathNames("class|family|genus");

        TaxonImpl otherTaxon = new TaxonImpl();
        otherTaxon.setName("Lestes");
        otherTaxon.setPath("Insecta|Lestidae|Lestes");
        otherTaxon.setPathNames("class|family|genus");

        assertThat(TaxonUtil.likelyHomonym(taxon, otherTaxon), is(false));
    }

    @Test
    public void toTaxonImage() {
        TaxonImage image = new TaxonImage();

        Taxon taxon = new TaxonImpl("Donald duckus", "EOL:123");
        taxon.setCommonNames("bla @en | boo @de");
        taxon.setPath("one | two | three");
        Map<String, String> taxonMap = new TreeMap<String, String>(TaxonUtil.taxonToMap(taxon));
        taxonMap.put(PropertyAndValueDictionary.THUMBNAIL_URL, "http://foo/bar/thumb");
        taxonMap.put(PropertyAndValueDictionary.EXTERNAL_URL, "http://foo/bar");
        TaxonImage enrichedImage = TaxonUtil.enrichTaxonImageWithTaxon(taxonMap, image);

        assertThat(enrichedImage.getCommonName(), is("bla"));
        assertThat(enrichedImage.getTaxonPath(), is("one | two | three"));
        assertThat(enrichedImage.getInfoURL(), is("http://foo/bar"));
        assertThat(enrichedImage.getThumbnailURL(), is("http://foo/bar/thumb"));
        assertThat(enrichedImage.getPageId(), is("123"));
        assertThat(enrichedImage.getImageURL(), is(nullValue()));
    }

    @Test
    public void toTaxonImageNonEOL() {
        TaxonImage image = new TaxonImage();

        Taxon taxon = new TaxonImpl("Donald duckus", "ZZZ:123");
        taxon.setCommonNames("bla @en | boo @de");
        taxon.setPath("one | two | three");
        Map<String, String> taxonMap = new TreeMap<String, String>(TaxonUtil.taxonToMap(taxon));
        taxonMap.put(PropertyAndValueDictionary.THUMBNAIL_URL, "http://foo/bar/thumb");
        taxonMap.put(PropertyAndValueDictionary.EXTERNAL_URL, "http://foo/bar");
        TaxonImage enrichedImage = TaxonUtil.enrichTaxonImageWithTaxon(taxonMap, image);

        assertThat(enrichedImage.getCommonName(), is("bla"));
        assertThat(enrichedImage.getTaxonPath(), is("one | two | three"));
        assertThat(enrichedImage.getInfoURL(), is("http://foo/bar"));
        assertThat(enrichedImage.getThumbnailURL(), is("http://foo/bar/thumb"));
        assertThat(enrichedImage.getPageId(), is(nullValue()));
        assertThat(enrichedImage.getImageURL(), is(nullValue()));
    }

}