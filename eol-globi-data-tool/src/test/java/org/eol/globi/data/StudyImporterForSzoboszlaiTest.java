package org.eol.globi.data;

import org.apache.commons.io.IOUtils;
import org.eol.globi.domain.Location;
import org.eol.globi.domain.Specimen;
import org.eol.globi.domain.Study;
import org.eol.globi.geo.LatLng;
import org.eol.globi.util.NodeUtil;
import org.junit.Test;
import org.neo4j.graphdb.Relationship;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.eol.globi.data.StudyImporterForTSV.DECIMAL_LATITUDE;
import static org.eol.globi.data.StudyImporterForTSV.DECIMAL_LONGITUDE;
import static org.eol.globi.data.StudyImporterForTSV.INTERACTION_TYPE_ID;
import static org.eol.globi.data.StudyImporterForTSV.INTERACTION_TYPE_NAME;
import static org.eol.globi.data.StudyImporterForTSV.LOCALITY_ID;
import static org.eol.globi.data.StudyImporterForTSV.LOCALITY_NAME;
import static org.eol.globi.data.StudyImporterForTSV.REFERENCE_CITATION;
import static org.eol.globi.data.StudyImporterForTSV.REFERENCE_DOI;
import static org.eol.globi.data.StudyImporterForTSV.REFERENCE_URL;
import static org.eol.globi.data.StudyImporterForTSV.SOURCE_TAXON_ID;
import static org.eol.globi.data.StudyImporterForTSV.SOURCE_TAXON_NAME;
import static org.eol.globi.data.StudyImporterForTSV.STUDY_SOURCE_CITATION;
import static org.eol.globi.data.StudyImporterForTSV.TARGET_TAXON_ID;
import static org.eol.globi.data.StudyImporterForTSV.TARGET_TAXON_NAME;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.internal.matchers.StringContains.containsString;

public class StudyImporterForSzoboszlaiTest extends GraphDBTestCase {

    @Test
    public void importAll() throws StudyImporterException, NodeFactoryException {
        StudyImporter importer = new StudyImporterForSzoboszlai(new ParserFactoryImpl(), nodeFactory);
        importStudy(importer);

        List<Study> allStudies = NodeUtil.findAllStudies(getGraphDb());
        assertThat(allStudies.size(), is(not(0)));
        Study firstStudy = allStudies.get(0);
        Iterable<Relationship> specimens = firstStudy.getSpecimens();
        for (Relationship specimen : specimens) {
            Specimen specimenNode = new Specimen(specimen.getEndNode());
            Location sampleLocation = specimenNode.getSampleLocation();
            assertThat(sampleLocation, is(notNullValue()));
            assertThat(sampleLocation.getLatitude(), is(notNullValue()));
            assertThat(sampleLocation.getLongitude(), is(notNullValue()));
        }

        assertThat(taxonIndex.findTaxonByName("Thunnus thynnus"), is(notNullValue()));
        assertThat(nodeFactory.findLocation(34.00824202376044, -120.72716166720323, null), is(notNullValue()));
    }

    @Test
    public void importLines() throws IOException, StudyImporterException {
        StudyImporterForSzoboszlai studyImporterForSzoboszlai = new StudyImporterForSzoboszlai(new ParserFactoryImpl(), nodeFactory);
        final List<Map<String, String>> maps = new ArrayList<Map<String, String>>();
        HashMap<Integer, LatLng> localeMap = new HashMap<Integer, LatLng>();
        localeMap.put(2361, new LatLng(34.00824202376044, -120.72716166720323));
        studyImporterForSzoboszlai.importLinks(IOUtils.toInputStream(firstFewLines()), new InteractionListener() {
            @Override
            public void newLink(Map<String, String> properties) {
                maps.add(properties);
            }
        }, localeMap);
        assertThat(maps.size(), is(4));
        Map<String, String> firstLink = maps.get(0);
        assertThat(firstLink.get(SOURCE_TAXON_ID), is(nullValue()));
        assertThat(firstLink.get(SOURCE_TAXON_NAME), is("Thunnus thynnus"));
        assertThat(firstLink.get(TARGET_TAXON_ID), is("ITIS:161828"));
        assertThat(firstLink.get(TARGET_TAXON_NAME), is("Engraulis mordax"));
        assertThat(firstLink.get(STUDY_SOURCE_CITATION), containsString("Szoboszlai AI, Thayer JA, Wood SA, Sydeman WJ, Koehn LE (2015) Data from: Forage species in predator diets: synthesis of data from the California Current. Dryad Digital Repository. http://dx.doi.org/10.5061/dryad.nv5d2"));
        assertThat(firstLink.get(STUDY_SOURCE_CITATION), containsString("Accessed at"));
        assertThat(firstLink.get(REFERENCE_CITATION), is("Blunt, CE. 1958. California bluefin tuna-wary wanderer of the Pacific. Outdoor California. v.19. pp.14"));
        assertThat(firstLink.get(REFERENCE_DOI), is(nullValue()));
        assertThat(firstLink.get(REFERENCE_URL), is(nullValue()));
        assertThat(firstLink.get(LOCALITY_NAME), is("USA, CA"));
        assertThat(firstLink.get(DECIMAL_LONGITUDE), is("-120.72716166720323"));
        assertThat(firstLink.get(DECIMAL_LATITUDE), is("34.00824202376044"));
        assertThat(firstLink.get(INTERACTION_TYPE_ID), is("RO:0002439"));
        assertThat(firstLink.get(INTERACTION_TYPE_NAME), is("preysOn"));
    }

    @Test
    public void importShapes() throws StudyImporterException {
        StudyImporterForSzoboszlai studyImporterForSzoboszlai = new StudyImporterForSzoboszlai(new ParserFactoryImpl(), nodeFactory);
        studyImporterForSzoboszlai.setShapeArchiveURL("szoboszlai/CCPDDlocationdata_test.zip");

        Map<Integer, LatLng> localityMap = studyImporterForSzoboszlai.importShapes();

        LatLng centroid = localityMap.get(2361);
        assertThat(centroid, is(notNullValue()));
        assertThat(centroid.getLat(), is(34.00824202376044));
        assertThat(centroid.getLng(), is(-120.72716166720323));
    }


    private String firstFewLines() {
        return "PredPreyNum,PredatorClassName,Predator Family Name,PredatorSciName,PredatorCommName,PreyGroup,PreyGroupTSN,PreySciName,PreySciNameTSN,CiteAuth,CiteYear,CiteTitle,CiteSource,CiteVolume,CitePages,LocatName,LocatNum,LocatSpan,DateName,DateName,Length,ObsTypeName,adjusted sample size,\"Annual=1, Seasonal (<6 mo, 2=summer april-sept, 3=winter oct-ma\",#,% #,FO,% FO,mass or vol,% mass or vol,Index,% Index\n" +
                "47636,Actinopterygii,Scombridae,Thunnus thynnus,Pacific bluefin tuna,Engraulidae,553173,Engraulis mordax,161828,\"Blunt, CE\",1958,California bluefin tuna-wary wanderer of the Pacific,Outdoor California,19,14,\"USA, CA\",2361,span CA-N to CA-S,1957,1957,4,Stomach Content,168,1,,,,x,,,,\n" +
                "39811,Mammalia,Otariidae,Zalophus californianus,California sea lion,Sebastes,166705,Sebastes,166705,\"Weise, MJ, JT Harvey\",2005,\"California Sea Lion (Zalophus californianus) impacts on salmonids near A�o Nuevo Island, California. \",NOAA Report,0,1-31,\"USA, CA, Ano Nuevo Island\",,CA-C,2002,2002,4,Scat,,1,,x,,x,x,x,x,\n" +
                "39814,Mammalia,Otariidae,Zalophus californianus,California sea lion,Merlucciidae,164789,Merluccius productus,164792,\"Weise, MJ, JT Harvey\",2005,\"California Sea Lion (Zalophus californianus) impacts on salmonids near A�o Nuevo Island, California. \",NOAA Report,0,1-31,\"USA, CA, Ano Nuevo Island\",,CA-C,2002,2002,4,Scat,,1,,x,,x,x,x,x,\n" +
                "39815,Mammalia,Otariidae,Zalophus californianus,California sea lion,Loliginidae,82369,Loligo opalescens,82371,\"Weise, MJ, JT Harvey\",2005,\"California Sea Lion (Zalophus californianus) impacts on salmonids near A�o Nuevo Island, California. \",NOAA Report,0,1-31,\"USA, CA, Ano Nuevo Island\",,CA-C,2002,2002,4,Scat,,1,,x,,x,,x,x,\n";
    }
}