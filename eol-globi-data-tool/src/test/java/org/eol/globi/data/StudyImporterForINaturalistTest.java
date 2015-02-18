package org.eol.globi.data;

import org.eol.globi.domain.InteractType;
import org.eol.globi.domain.RelTypes;
import org.eol.globi.domain.Specimen;
import org.eol.globi.domain.Study;
import org.eol.globi.domain.TaxonNode;
import org.eol.globi.domain.TaxonomyProvider;
import org.eol.globi.service.PropertyEnricherException;
import org.eol.globi.util.NodeUtil;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.any;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

public class StudyImporterForINaturalistTest extends GraphDBTestCase {
    private StudyImporterForINaturalist importer;

    @Before
    public void setup() {
        importer = new StudyImporterForINaturalist(null, nodeFactory);
    }

    @Test
    public void importUsingINatAPI() throws StudyImporterException, PropertyEnricherException {
        importer.importStudy();
        assertThat(NodeUtil.findAllStudies(getGraphDb()).size() > 150, is(true));
    }

    @Ignore(value = "see https://github.com/jhpoelen/eol-globi-data/issues/56")
    @Test
    public void importTestResponseWithEcologicalInteraction() throws StudyImporterException, NodeFactoryException {
        Study study = nodeFactory.createStudy("testing123");
        importer.parseJSON(getClass().getResourceAsStream("inaturalist/response_with_ecological_interaction_field.json"));
        assertThat(countSpecimen(study) > 86, is(true));
    }

    @Test
    public void importTestResponse() throws IOException, NodeFactoryException, StudyImporterException {
        importer.parseJSON(getClass().getResourceAsStream("inaturalist/sample_inaturalist_response.json"));

        assertThat(NodeUtil.findAllStudies(getGraphDb()).size(), is(30));

        Study anotherStudy = nodeFactory.findStudy("INAT:831");
        assertThat(anotherStudy, is(notNullValue()));
        assertThat(anotherStudy.getCitation(), containsString("Ken-ichi Ueda. 2008. Argiope eating Orthoptera. iNaturalist.org. Accessed at http://inaturalist.org/observations/831 on "));
        assertThat(anotherStudy.getExternalId(), is("http://inaturalist.org/observations/831"));

        anotherStudy = nodeFactory.findStudy("INAT:97380");
        assertThat(anotherStudy, is(notNullValue()));
        assertThat(anotherStudy.getCitation(), containsString("annetanne. 2012. Misumena vatia eating Eristalis nemorum."));
        assertThat(anotherStudy.getExternalId(), is("http://inaturalist.org/observations/97380"));


        TaxonNode sourceTaxonNode = nodeFactory.findTaxonByName("Arenaria interpres");

        assertThat(sourceTaxonNode, is(not(nullValue())));
        Iterable<Relationship> relationships = sourceTaxonNode.getUnderlyingNode().getRelationships(Direction.INCOMING, RelTypes.CLASSIFIED_AS);
        for (Relationship relationship : relationships) {
            Node sourceSpecimen = relationship.getStartNode();

            assertThat(new Specimen(sourceSpecimen).getExternalId(), containsString(TaxonomyProvider.ID_PREFIX_INATURALIST));
            Relationship ateRel = sourceSpecimen.getSingleRelationship(InteractType.ATE, Direction.OUTGOING);
            Node preySpecimen = ateRel.getEndNode();
            assertThat(preySpecimen, is(not(nullValue())));
            Relationship preyClassification = preySpecimen.getSingleRelationship(RelTypes.CLASSIFIED_AS, Direction.OUTGOING);
            String actualPreyName = (String) preyClassification.getEndNode().getProperty("name");
            assertThat(actualPreyName, is("Crepidula fornicata"));

            Relationship locationRel = sourceSpecimen.getSingleRelationship(RelTypes.COLLECTED_AT, Direction.OUTGOING);
            assertThat((Double) locationRel.getEndNode().getProperty("latitude"), is(41.249813));
            assertThat((Double) locationRel.getEndNode().getProperty("longitude"), is(-72.542556));

            Relationship collectedRel = sourceSpecimen.getSingleRelationship(RelTypes.COLLECTED, Direction.INCOMING);
            assertThat((Long) collectedRel.getProperty(Specimen.DATE_IN_UNIX_EPOCH), is(any(Long.class)));

        }
    }

    private int countSpecimen(Study study) {
        Iterable<Relationship> specimens = study.getSpecimens();
        int count = 0;
        for (Relationship specimen : specimens) {
            count++;
        }
        return count;
    }

}
