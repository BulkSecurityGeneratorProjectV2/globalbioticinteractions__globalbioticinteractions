package org.eol.globi.export;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.tdb.TDB;
import com.hp.hpl.jena.tdb.TDBFactory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eol.globi.data.StudyImporterException;
import org.eol.globi.domain.Study;
import org.eol.globi.util.NodeUtil;
import org.neo4j.graphdb.GraphDatabaseService;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

public class GraphExporter {
    private static final Log LOG = LogFactory.getLog(GraphExporter.class);

    public void export(GraphDatabaseService graphService, String baseDir) throws StudyImporterException {
        try {
            FileUtils.forceMkdir(new File(baseDir));
        } catch (IOException e) {
            throw new StudyImporterException("failed to create output dir [" + baseDir + "]", e);
        }
        List<Study> studies = NodeUtil.findAllStudies(graphService);
        try {
            FileUtils.forceMkdir(new File(baseDir + "taxa"));
        } catch (IOException e) {
            throw new StudyImporterException("failed to create output dir [" + baseDir + "]", e);
        }
        exportNames(studies, baseDir, new ExportTaxonMaps(), "taxa/taxonMap.csv.gz");
        exportNames(studies, baseDir, new ExportTaxonNames(), "taxa/taxonCache.csv.gz");
        exportNames(studies, baseDir, new ExportUnmatchedTaxonNames(), "taxa/taxonUnmatched.csv");
        exportDataOntology(studies, baseDir);
        exportDarwinCoreAggregatedByStudy(baseDir, studies);
        exportDarwinCoreAll(baseDir, studies);
    }

    private void exportNames(List<Study> studies, String baseDir, StudyExporter exporter, String filename) throws StudyImporterException {
        try {
            String filePath = baseDir + filename;
            OutputStreamWriter writer = openStream(filePath);
            for (Study study : studies) {
                boolean includeHeader = studies.indexOf(study) == 0;
                exporter.exportStudy(study, writer, includeHeader);
            }
            closeStream(filePath, writer);
        } catch (IOException e) {
            throw new StudyImporterException("failed to export unmatched source taxa", e);
        }
    }

    private void exportDarwinCoreAggregatedByStudy(String baseDir, List<Study> studies) throws StudyImporterException {
        exportDarwinCoreArchive(studies,
                baseDir + "aggregatedByStudy/", new HashMap<String, DarwinCoreExporter>() {
                    {
                        put("association.csv", new ExporterAssociationAggregates());
                        put("occurrence.csv", new ExporterOccurrenceAggregates());
                        put("references.csv", new ExporterReferences());
                        put("taxa.csv", new ExporterTaxaDistinct());
                    }
                }
        );
    }

    private void exportDarwinCoreAll(String baseDir, List<Study> studies) throws StudyImporterException {
        exportDarwinCoreArchive(studies, baseDir + "all/", new HashMap<String, DarwinCoreExporter>() {
            {
                put("association.csv", new ExporterAssociations());
                put("occurrence.csv", new ExporterOccurrences());
                put("references.csv", new ExporterReferences());
                put("taxa.csv", new ExporterTaxaDistinct());
                put("measurementOrFact.csv", new ExporterMeasurementOrFact());
            }
        });
    }

    private void exportDataOntology(List<Study> studies, String baseDir) throws StudyImporterException {
        try {
            String directory = baseDir + "jena-tdb-tmp";
            FileUtils.deleteQuietly(new File(directory));
            Dataset dataset = TDBFactory.createDataset(directory);
            Model model = dataset.getDefaultModel();
            LittleTurtleExporter studyExporter = new LittleTurtleExporter(model);
            OutputStreamWriter writer = openStream(baseDir + "globi.ttl.gz");
            int total = studies.size();
            int count = 1;
            for (Study study : studies) {
                studyExporter.exportStudy(study, writer, true);
                if (count % 50 == 0) {
                    LOG.info("added triples for [" + count + "] of [" + total + "] studies...");
                }
                count++;
            }
            LOG.info("adding triples for [" + total + "] of [" + total + "] studies.");

            TDB.sync(dataset);
            LOG.info("writing turtle archive...");
            studyExporter.exportDataOntology(writer);
            closeStream(baseDir + "globi.ttl.gz", writer);
            FileUtils.deleteQuietly(new File(directory));
        } catch (IOException e) {
            throw new StudyImporterException("failed to export as owl", e);
        }
    }

    private void exportDarwinCoreArchive(List<Study> studies, String pathPrefix, Map<String, DarwinCoreExporter> exporters) throws StudyImporterException {
        try {
            FileUtils.forceMkdir(new File(pathPrefix));
            FileWriter darwinCoreMeta = writeMetaHeader(pathPrefix);
            for (Map.Entry<String, DarwinCoreExporter> exporter : exporters.entrySet()) {
                export(studies, pathPrefix, exporter.getKey(), exporter.getValue(), darwinCoreMeta);
            }
            writeMetaFooter(darwinCoreMeta);
        } catch (IOException e) {
            throw new StudyImporterException("failed to export result to csv file", e);
        }
    }

    private void writeMetaFooter(FileWriter darwinCoreMeta) throws IOException {
        darwinCoreMeta.write("</archive>");
        darwinCoreMeta.flush();
        darwinCoreMeta.close();
    }

    private FileWriter writeMetaHeader(String pathPrefix) throws IOException {
        FileWriter darwinCoreMeta = new FileWriter(pathPrefix + "meta.xml", false);
        darwinCoreMeta.write("<?xml version=\"1.0\"?>\n" +
                "<archive xmlns=\"http://rs.tdwg.org/dwc/text/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://rs.tdwg.org/dwc/text/  http://services.eol.org/schema/dwca/tdwg_dwc_text.xsd\">\n");
        return darwinCoreMeta;
    }

    private void export(List<Study> importedStudies, String exportPath, String filename, DarwinCoreExporter studyExporter, FileWriter darwinCoreMeta) throws IOException {
        OutputStreamWriter writer = openStream(exportPath + filename);
        for (Study importedStudy : importedStudies) {
            boolean includeHeader = importedStudies.indexOf(importedStudy) == 0;
            studyExporter.exportStudy(importedStudy, writer, includeHeader);
        }
        closeStream(exportPath + filename, writer);
        LOG.info("darwin core meta file writing... ");
        studyExporter.exportDarwinCoreMetaTable(darwinCoreMeta, filename);
        LOG.info("darwin core meta file written. ");
    }

    private void closeStream(String exportPath, OutputStreamWriter writer) throws IOException {
        writer.flush();
        writer.close();
        LOG.info("export data to [" + new File(exportPath).getAbsolutePath() + "] complete.");
    }

    private OutputStreamWriter openStream(String exportPath) throws IOException {
        OutputStream fos = new BufferedOutputStream(new FileOutputStream(exportPath));
        if (exportPath.endsWith(".gz")) {
            fos = new GZIPOutputStream(fos);
        }
        OutputStreamWriter writer = new OutputStreamWriter(fos, "UTF-8");
        LOG.info("export data to [" + new File(exportPath).getAbsolutePath() + "] started...");
        return writer;
    }

}
