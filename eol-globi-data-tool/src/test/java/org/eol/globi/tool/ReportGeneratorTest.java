package org.eol.globi.tool;

import org.eol.globi.data.GraphDBTestCase;
import org.eol.globi.data.NodeFactoryException;
import org.eol.globi.domain.PropertyAndValueDictionary;
import org.eol.globi.domain.Specimen;
import org.eol.globi.domain.Study;
import org.eol.globi.domain.StudyConstant;
import org.eol.globi.domain.StudyImpl;
import org.eol.globi.domain.TaxonImpl;
import org.junit.Test;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.IndexHits;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class ReportGeneratorTest extends GraphDBTestCase {


    @Test
    public void generateStudyReport() throws NodeFactoryException {
        createStudy(new StudyImpl("a second title", "a third source", null, null));

        StudyImpl studyWithDoi = new StudyImpl("a title", "a third source", "doi:12345", "citation");
        createStudy(studyWithDoi);
        resolveNames();

        new ReportGenerator(getGraphDb()).generateReportForStudies();

        IndexHits<Node> reports = getGraphDb().index().forNodes("reports").get(StudyConstant.TITLE, "a title");
        assertThat(reports.size(), is(1));
        Node reportNode = reports.getSingle();
        assertThat((String) reportNode.getProperty(StudyConstant.TITLE), is("a title"));
        assertThat((String) reportNode.getProperty(StudyConstant.SOURCE), is("a third source"));
        assertThat((String) reportNode.getProperty(StudyConstant.CITATION), is("citation:doi:citation"));
        assertThat((String) reportNode.getProperty(StudyConstant.DOI), is("doi:12345"));
        assertThat((String) reportNode.getProperty(PropertyAndValueDictionary.EXTERNAL_ID), is("http://dx.doi.org/citation"));
        assertThat((Integer) reportNode.getProperty(PropertyAndValueDictionary.NUMBER_OF_INTERACTIONS), is(4));
        assertThat((Integer) reportNode.getProperty(PropertyAndValueDictionary.NUMBER_OF_DISTINCT_TAXA), is(3));
        reports.close();

        reports = getGraphDb().index().forNodes("reports").get(StudyConstant.TITLE, "a second title");
        assertThat(reports.size(), is(1));
        reportNode = reports.getSingle();
        assertThat((String) reportNode.getProperty(StudyConstant.TITLE), is("a second title"));
        assertThat((String) reportNode.getProperty(StudyConstant.SOURCE), is("a third source"));
        assertThat(reportNode.hasProperty(StudyConstant.CITATION), is(false));
        assertThat(reportNode.hasProperty(StudyConstant.DOI), is(false));
        assertThat(reportNode.hasProperty(PropertyAndValueDictionary.EXTERNAL_ID), is(false));
        assertThat((Integer) reportNode.getProperty(PropertyAndValueDictionary.NUMBER_OF_INTERACTIONS), is(4));
        assertThat((Integer) reportNode.getProperty(PropertyAndValueDictionary.NUMBER_OF_DISTINCT_TAXA), is(3));
        reports.close();
    }


    @Test
    public void generateStudySourceReport() throws NodeFactoryException {
        createStudy(new StudyImpl("a title", "az source", null, "citation"));
        createStudy(new StudyImpl("another title", "az source", null, "citation"));
        createStudy(new StudyImpl("yet another title", "zother source", null, null));
        resolveNames();

        new ReportGenerator(getGraphDb()).generateReportForStudySources();

        IndexHits<Node> reports = getGraphDb().index().forNodes("reports").get(StudyConstant.SOURCE, "az source");
        Node reportNode = reports.getSingle();
        assertThat((Integer) reportNode.getProperty(PropertyAndValueDictionary.NUMBER_OF_STUDIES), is(2));
        assertThat((Integer) reportNode.getProperty(PropertyAndValueDictionary.NUMBER_OF_INTERACTIONS), is(8));
        assertThat((Integer) reportNode.getProperty(PropertyAndValueDictionary.NUMBER_OF_DISTINCT_TAXA), is(3));
        assertThat((String) reportNode.getProperty(StudyConstant.SOURCE), is("az source"));
        reports.close();

        IndexHits<Node> otherReports = getGraphDb().index().forNodes("reports").get(StudyConstant.SOURCE, "zother source");
        Node otherReport = otherReports.getSingle();
        assertThat((String) otherReport.getProperty(StudyConstant.SOURCE), is("zother source"));
        assertThat((Integer) otherReport.getProperty(PropertyAndValueDictionary.NUMBER_OF_STUDIES), is(1));
        assertThat((Integer) otherReport.getProperty(PropertyAndValueDictionary.NUMBER_OF_INTERACTIONS), is(4));
        assertThat((Integer) otherReport.getProperty(PropertyAndValueDictionary.NUMBER_OF_DISTINCT_TAXA), is(3));
    }

    @Test
    public void generateCollectionReport() throws NodeFactoryException {
        createStudy(new StudyImpl("a title", "source", null, "citation"));
        createStudy(new StudyImpl("another title", "another source", null, "citation"));
        resolveNames();

        new ReportGenerator(getGraphDb()).generateReportForCollection();

        IndexHits<Node> reports = getGraphDb().index().forNodes("reports").query("*:*");
        assertThat(reports.size(), is(1));
        Node reportNode = reports.getSingle();
        assertThat((Integer) reportNode.getProperty(PropertyAndValueDictionary.NUMBER_OF_SOURCES), is(2));
        assertThat((Integer) reportNode.getProperty(PropertyAndValueDictionary.NUMBER_OF_STUDIES), is(2));
        assertThat((Integer) reportNode.getProperty(PropertyAndValueDictionary.NUMBER_OF_INTERACTIONS), is(8));
        assertThat((Integer) reportNode.getProperty(PropertyAndValueDictionary.NUMBER_OF_DISTINCT_TAXA), is(3));
    }

    protected Study createStudy(Study study1) throws NodeFactoryException {
        Study study = nodeFactory.getOrCreateStudy(study1);
        Specimen monkey = nodeFactory.createSpecimen(study, new TaxonImpl("Monkey", null));
        monkey.ate(nodeFactory.createSpecimen(study, new TaxonImpl("Banana", null)));
        monkey.ate(nodeFactory.createSpecimen(study, new TaxonImpl("Banana", null)));
        monkey.ate(nodeFactory.createSpecimen(study, new TaxonImpl("Banana", null)));
        monkey.ate(nodeFactory.createSpecimen(study, new TaxonImpl("Apple", null)));
        return study;
    }

}