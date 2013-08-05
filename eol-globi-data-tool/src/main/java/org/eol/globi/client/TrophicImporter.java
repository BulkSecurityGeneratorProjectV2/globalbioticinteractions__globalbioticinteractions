package org.eol.globi.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eol.globi.export.ExporterAssociationAggregates;
import org.eol.globi.export.ExporterAssociations;
import org.eol.globi.export.ExporterMeasurementOrFact;
import org.eol.globi.export.ExporterOccurrenceAggregates;
import org.eol.globi.export.ExporterOccurrences;
import org.eol.globi.export.ExporterTaxa;
import org.eol.globi.export.GlobiOWLExporter;
import org.eol.globi.export.StudyExportUnmatchedSourceTaxaForStudies;
import org.eol.globi.export.StudyExportUnmatchedTargetTaxaForStudies;
import org.eol.globi.service.TaxonPropertyEnricher;
import org.eol.globi.service.TaxonPropertyEnricherImpl;
import org.neo4j.graphdb.GraphDatabaseService;
import org.eol.globi.data.NodeFactory;
import org.eol.globi.data.ParserFactory;
import org.eol.globi.data.ParserFactoryImpl;
import org.eol.globi.data.StudyImporter;
import org.eol.globi.data.StudyImporterException;
import org.eol.globi.data.StudyImporterFactory;
import org.eol.globi.db.GraphService;
import org.eol.globi.domain.Study;
import org.eol.globi.export.StudyExporter;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TrophicImporter {
    private static final Log LOG = LogFactory.getLog(TrophicImporter.class);

    public static void main(final String[] commandLineArguments) throws StudyImporterException {
        new TrophicImporter().importExport();
    }

    public void importExport() throws StudyImporterException {
        final GraphDatabaseService graphService = GraphService.getGraphService();
        TaxonPropertyEnricherImpl taxonEnricher = new TaxonPropertyEnricherImpl(graphService);
        importData(graphService, taxonEnricher);

        exportData(graphService);
        graphService.shutdown();
    }

    private void exportData(GraphDatabaseService graphService) throws StudyImporterException {
        List<Study> studies = findAllStudies(graphService);
        try {
            FileWriter darwinCoreMeta = writeMetaHeader();
            export(studies, "./unmatchedSourceTaxa.csv", new StudyExportUnmatchedSourceTaxaForStudies(GraphService.getGraphService()), darwinCoreMeta);
            export(studies, "./unmatchedTargetTaxa.csv", new StudyExportUnmatchedTargetTaxaForStudies(GraphService.getGraphService()), darwinCoreMeta);
            export(studies, "./associations.csv", new ExporterAssociations(), darwinCoreMeta);
            export(studies, "./associationAggregates.csv", new ExporterAssociationAggregates(), darwinCoreMeta);
            export(studies, "./occurrences.csv", new ExporterOccurrences(), darwinCoreMeta);
            export(studies, "./occurrenceAggregates.csv", new ExporterOccurrenceAggregates(), darwinCoreMeta);
            export(studies, "./taxa.csv", new ExporterTaxa(), darwinCoreMeta);
            export(studies, "./measurementOrFact.csv", new ExporterMeasurementOrFact(), darwinCoreMeta);
            writeMetaFooter(darwinCoreMeta);
        } catch (IOException e) {
            throw new StudyImporterException("failed to export result to csv file", e);
        }

        try {
            export(studies, "./globi.owl", new GlobiOWLExporter(), null);
        } catch (OWLOntologyCreationException e) {
            throw new StudyImporterException("failed to export as owl", e);
        } catch (IOException e) {
            throw new StudyImporterException("failed to export as owl", e);
        }
    }

    protected List<Study> findAllStudies(GraphDatabaseService graphService) {
        List<Study> studies = new ArrayList<Study>();
        Index<Node> studyIndex = graphService.index().forNodes("studies");
        IndexHits<Node> hits = studyIndex.query("title", "*");
        for (Node hit : hits) {
            studies.add(new Study(hit));
        }
        return studies;
    }

    private void importData(GraphDatabaseService graphService, TaxonPropertyEnricher taxonEnricher) throws StudyImporterException {
        for (Class importer : StudyImporterFactory.getAvailableImporters()) {
            importData(graphService, taxonEnricher, importer);
        }
    }

    protected void importData(GraphDatabaseService graphService, TaxonPropertyEnricher taxonEnricher, Class importer) throws StudyImporterException {
        StudyImporter studyImporter = createStudyImporter(graphService, importer, taxonEnricher);
        LOG.info("[" + importer + "] importing ...");
        studyImporter.importStudy();
        LOG.info("[" + importer + "] imported.");
    }

    private void writeMetaFooter(FileWriter darwinCoreMeta) throws IOException {
        darwinCoreMeta.write("</archive>");
        darwinCoreMeta.flush();
        darwinCoreMeta.close();
    }

    private FileWriter writeMetaHeader() throws IOException {
        FileWriter darwinCoreMeta = new FileWriter("./meta.xml", false);
        darwinCoreMeta.write("<?xml version=\"1.0\"?>\n" +
                "<archive xmlns=\"http://rs.tdwg.org/dwc/text/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://rs.tdwg.org/dwc/text/  http://services.eol.org/schema/dwca/tdwg_dwc_text.xsd\">\n");
        return darwinCoreMeta;
    }

    private void export(List<Study> importedStudies, String exportPath, StudyExporter studyExporter, FileWriter darwinCoreMeta) throws IOException {
        FileWriter writer = new FileWriter(exportPath, false);
        LOG.info("export data to [" + new File(exportPath).getAbsolutePath() + "] started...");
        for (Study importedStudy : importedStudies) {
            boolean includeHeader = importedStudies.indexOf(importedStudy) == 0;
            studyExporter.exportStudy(importedStudy, writer, includeHeader);
        }
        studyExporter.exportDarwinCoreMetaTable(darwinCoreMeta, exportPath);
        writer.flush();
        writer.close();
        LOG.info("export data to [" + new File(exportPath).getAbsolutePath() + "] complete.");
    }

    private StudyImporter createStudyImporter(GraphDatabaseService graphService, Class<StudyImporter> studyImporter, TaxonPropertyEnricher taxonEnricher) throws StudyImporterException {
        NodeFactory factory = new NodeFactory(graphService, taxonEnricher);
        ParserFactory parserFactory = new ParserFactoryImpl();
        return new StudyImporterFactory(parserFactory, factory).instantiateImporter(studyImporter);
    }

}