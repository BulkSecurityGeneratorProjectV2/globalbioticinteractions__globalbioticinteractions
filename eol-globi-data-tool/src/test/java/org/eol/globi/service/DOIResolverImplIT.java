package org.eol.globi.service;

import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.internal.matchers.StringContains.containsString;

public class DOIResolverImplIT {

    @Test
    public void resolveDOIByReferenceNoMatch() throws IOException {
        String doi = new DOIResolverImpl().findDOIForReference("James D. Simons Food habits and trophic structure of the demersal fish assemblages on the Mississippi-Alabama continental shelf");
        assertThat(doi, is(nullValue()));
    }

    @Test
    public void resolveDOIByReferenceMatch() throws IOException {
        String doi = new DOIResolverImpl().findDOIForReference("J. N. Kremer and S. W. Nixon, A Coastal Marine Ecosystem:  Simulation and Analysis, Vol. 24 of Ecol. Studies (Springer-Verlag, Berlin, 1978), from p. 12.");
        assertThat(doi, is("http://dx.doi.org/10.1002/bimj.4710230217"));
    }

    @Test
    public void resolveDOIByReferenceTamarins() throws IOException {
        String doi = new DOIResolverImpl().findDOIForReference("Raboy, Becky E., and James M. Dietz. Diet, Foraging, and Use of Space in Wild Golden-headed Lion Tamarins. American Journal of Primatology, 63(1):, 2004, 1-15. Accessed April 20, 2015. http://hdl.handle.net/10088/4251.");
        assertThat(doi, is("http://dx.doi.org/10.1002/ajp.20032"));
    }

    @Test
    public void resolveDOIByReferenceMatch2() throws IOException {
        String doi = new DOIResolverImpl().findDOIForReference("Medan, D., N. H. Montaldo, M. Devoto, A. Mantese, V. Vasellati, and N. H. Bartoloni. 2002. Plant-pollinator relationships at two altitudes in the Andes of Mendoza, Argentina. Arctic Antarctic and Alpine Research 34:233-241.");
        assertThat(doi, is("http://dx.doi.org/10.2307/1552480"));
    }

    @Test
    public void resolveDOIByReferenceMatch3() throws IOException {
        String doi = new DOIResolverImpl().findDOIForReference("Hocking, B. 1968. Insect-flower associations in the high Arctic with special reference to nectar. Oikos 19:359-388.");
        assertThat(doi, is("http://dx.doi.org/10.2307/3565022"));
    }


    @Test
    public void resolveDOIBugFixedServerError() throws IOException {
        String citation = new DOIResolverImpl().findCitationForDOI("http://dx.doi.org/10.2307/4070736");
        assertThat(citation, is("Anon. McAtee’s “Food Habits of the Grosbeaks” Food Habits of the Grosbeaks W. L. McAtee. The Auk [Internet]. 1908 April;25(2):245–246. Available from: http://dx.doi.org/10.2307/4070736"));
    }

    @Test
    public void resolveBioInfoCitation() throws IOException {
        String doi = new DOIResolverImpl().findDOIForReference("Galea, V.J. & Price, T.V.. 1988. Infection of Lettuce by Microdochium panattonianum. Transactions of the British Mycological Society. Vol Vol 91 (3). pp 419-425");
        String expectedDoi = "http://dx.doi.org/10.1016/s0007-1536(88)80117-7";
        assertThat(doi, is(expectedDoi));
        String citation = new DOIResolverImpl().findCitationForDOI(doi);
        assertThat(citation, containsString("Galea VJ, Price TV"));
    }

    @Test
    public void findCitationForDOIStrangeCharacters() throws IOException {
        String citation = new DOIResolverImpl().findCitationForDOI("http://dx.doi.org/10.1007/s00300-004-0645-x");
        assertThat(citation, is("La Mesa M, Dalú M, Vacchi M. Trophic ecology of the emerald notothen Trematomus bernacchii (Pisces, Nototheniidae) from Terra Nova Bay, Ross Sea, Antarctica. Polar Biology [Internet]. 2004 July 27;27(11):721–728. Available from: http://dx.doi.org/10.1007/s00300-004-0645-x"));
    }

    @Test
    public void findCitationForDOI() throws IOException {
        String citationForDOI = new DOIResolverImpl().findCitationForDOI("http://dx.doi.org/10.1086/283073");
        assertThat(citationForDOI, is("Menge BA, Sutherland JP. Species Diversity Gradients: Synthesis of the Roles of Predation, Competition, and Temporal Heterogeneity. American Naturalist, The [Internet]. 1976 January;110(973):351. Available from: http://dx.doi.org/10.1086/283073"));
    }

    @Test
    public void findCitationForDOIEscaped() throws IOException {
        String citationForDOI = new DOIResolverImpl().findCitationForDOI("http://dx.doi.org/10.1577/1548-8659(1973)102<511:fhojmf>2.0.co;2");
        assertThat(citationForDOI, is("Carr WES, Adams CA. Food Habits of Juvenile Marine Fishes Occupying Seagrass Beds in the Estuarine Zone near Crystal River, Florida. Transactions of the American Fisheries Society [Internet]. 1973 July;102(3):511–540. Available from: http://dx.doi.org/10.1577/1548-8659(1973)102<511:fhojmf>2.0.co;2"));
    }

    @Test
    public void findCitationForDOI2() throws IOException {
        String citationForDOI = new DOIResolverImpl().findCitationForDOI("http://dx.doi.org/10.1371/journal.pone.0052967");
        assertThat(citationForDOI, is("García-Robledo C, Erickson DL, Staines CL, Erwin TL, Kress WJ. Tropical Plant–Herbivore Networks: Reconstructing Species Interactions Using DNA Barcodes Heil M, editor. PLoS ONE [Internet]. 2013 January 8;8(1):e52967. Available from: http://dx.doi.org/10.1371/journal.pone.0052967"));
    }

    @Test
    public void findCitationForDOI3() throws IOException {
        String citationForDOI = new DOIResolverImpl().findCitationForDOI("http://dx.doi.org/10.2307/177149");
        assertThat(citationForDOI, is("Yodzis P. DIFFUSE EFFECTS IN FOOD WEBS. Ecology [Internet]. 2000 January;81(1):261–266. Available from: http://dx.doi.org/10.1890/0012-9658(2000)081[0261:DEIFW]2.0.CO;2"));
    }

    @Test
    public void findMalformedCitationWithMalformedDOIURL() throws IOException {
        String citationForDOI = new DOIResolverImpl().findCitationForDOI("this ain't no uRL");
        assertThat(citationForDOI, nullValue());
    }

    @Test(expected = IOException.class)
    public void findNotResponding() throws IOException {
        new DOIResolverImpl("http://google.com").findDOIForReference("some reference");
    }

    @Test
    public void findCitationForDOIWithUnescapeChars() throws IOException, URISyntaxException {
        String citationForDOI = new DOIResolverImpl().findCitationForDOI("http://dx.doi.org/10.1642/0004-8038(2005)122[1182:baemfa]2.0.co;2");
        assertThat(citationForDOI, is(notNullValue()));
    }

    @Test
    public void findCitationForShortDOI() throws IOException, URISyntaxException {
        String citationForDOI = new DOIResolverImpl().findCitationForDOI("doi:10.1007/s13127-011-0039-1");
        assertThat(citationForDOI, is(notNullValue()));
    }

    @Test
    public void findCitationForShortDOIUpperCase() throws IOException, URISyntaxException {
        String citationForDOI = new DOIResolverImpl().findCitationForDOI("DOI:10.5962/bhl.title.2633");
        assertThat(citationForDOI, is(notNullValue()));
    }

}
