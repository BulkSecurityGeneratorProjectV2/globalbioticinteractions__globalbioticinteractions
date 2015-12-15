package org.eol.globi.data;

import org.eol.globi.domain.Study;
import org.hamcrest.core.Is;
import org.junit.Ignore;
import org.junit.Test;
import org.neo4j.graphdb.Relationship;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class StudyImporterForJRFerrerParisTest extends GraphDBTestCase {

    @Ignore(value = "too slow for regular use")
    @Test
    public void testFullImport() throws StudyImporterException {
        StudyImporterForJRFerrerParis studyImporterForJRFerrerParis = new StudyImporterForJRFerrerParis(new ParserFactoryImpl(), nodeFactory);
        Study study = studyImporterForJRFerrerParis.importStudy();
        Iterable<Relationship> specimens = study.getSpecimens();
        int count = 0;
        for (Relationship specimen : specimens) {
            count++;
        }
        assertTrue(count > 0);
    }

    @Test
    public void testSomeLine() throws StudyImporterException, NodeFactoryException {
        String csvContent = "\"\",\"Lepidoptera Family\",\"Lepidoptera Name\",\"Hostplant Family\",\"Hostplant Name\",\"Country\",\"reference\"\n" +
                "\"27385\",\"Pieridae\",\"Hesperocharis anguitia\",\"Santalales\",\"'Loranthus'\",\"Brazil\",\"Braby & Nishida 2007\"\n" +
                "\"27386\",\"Pieridae\",\"Mathania carrizoi\",\"Santalales\",\"'Loranthus'\",\"Argentina\",\"Braby & Nishida 2007\"\n" +
                "\"27387\",\"Pieridae\",\"Mylothris agathina\",\"Santalales\",\"?Loranthus? spp.\",\"Kenya, Tanzania, South Africa\",\"Braby 2005\"\n" +
                "\"27388\",\"Pieridae\",\"Mylothris chloris\",\"Santalales\",\"?Loranthus? spp.\",\"Kenya\",\"Braby 2005\"";

        StudyImporterForJRFerrerParis importer = new StudyImporterForJRFerrerParis(new TestParserFactory(csvContent), nodeFactory);

        Study study = importStudy(importer);
        assertNotNull(taxonIndex.findTaxonByName("Hesperocharis anguitia"));

        Iterable<Relationship> collectedRels = study.getSpecimens();
        int totalRels = 0;
        for (Relationship collectedRel : collectedRels) {
            totalRels++;
        }

        assertThat(totalRels, Is.is(8));
    }
}
