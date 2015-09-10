package org.eol.globi.data;

import com.Ostermiller.util.CSVParser;
import com.Ostermiller.util.LabeledCSVParser;
import com.Ostermiller.util.MD5;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.eol.globi.domain.Study;
import org.eol.globi.domain.TaxonomyProvider;
import org.eol.globi.geo.LatLng;
import org.eol.globi.util.ResourceUtil;
import org.geotools.data.FeatureReader;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.eol.globi.data.StudyImporterForTSV.DECIMAL_LATITUDE;
import static org.eol.globi.data.StudyImporterForTSV.DECIMAL_LONGITUDE;
import static org.eol.globi.data.StudyImporterForTSV.INTERACTION_TYPE_ID;
import static org.eol.globi.data.StudyImporterForTSV.INTERACTION_TYPE_NAME;
import static org.eol.globi.data.StudyImporterForTSV.LOCALITY_NAME;
import static org.eol.globi.data.StudyImporterForTSV.REFERENCE_CITATION;
import static org.eol.globi.data.StudyImporterForTSV.REFERENCE_ID;
import static org.eol.globi.data.StudyImporterForTSV.SOURCE_TAXON_ID;
import static org.eol.globi.data.StudyImporterForTSV.SOURCE_TAXON_NAME;
import static org.eol.globi.data.StudyImporterForTSV.STUDY_SOURCE_CITATION;
import static org.eol.globi.data.StudyImporterForTSV.TARGET_TAXON_ID;
import static org.eol.globi.data.StudyImporterForTSV.TARGET_TAXON_NAME;

public class StudyImporterForSzoboszlai extends BaseStudyImporter {
    private String shapeArchiveURL;
    private String linkArchiveURL;

    public StudyImporterForSzoboszlai(ParserFactory parserFactory, NodeFactory nodeFactory) {
        super(parserFactory, nodeFactory);
        setSourceCitation("Szoboszlai AI, Thayer JA, Wood SA, Sydeman WJ, Koehn LE (2015) Data from: Forage species in predator diets: synthesis of data from the California Current. Dryad Digital Repository. http://dx.doi.org/10.5061/dryad.nv5d2");
        setSourceDOI("http://dx.doi.org/10.5061/dryad.nv5d2");
        setShapeArchiveURL("http://datadryad.org/bitstream/handle/10255/dryad.94535/CCPDDlocationdata_v1.zip");
        setLinkArchiveURL("http://datadryad.org/bitstream/handle/10255/dryad.94536/CCPDDlinkdata_v1.csv");
    }

    @Override
    public Study importStudy() throws StudyImporterException {
        try {
            Map<Integer, LatLng> localeMap = importShapes();
            importLinks(ResourceUtil.asInputStream(getLinkArchiveURL(), null)
                    , new InteractionListenerNeo4j(nodeFactory, getGeoNamesService(), getLogger())
                    , localeMap);
        } catch (IOException e) {
            throw new StudyImporterException("failed to find: [" + getLinkArchiveURL() + "]");
        }
        return null;
    }

    protected void importLinks(InputStream is, InteractionListener interactionListener, Map<Integer, LatLng> localeMap) throws IOException, StudyImporterException {
        LabeledCSVParser parser = new LabeledCSVParser(new CSVParser(is));
        while (parser.getLine() != null) {
            Map<String, String> e = importLink(parser, localeMap);
            if (e != null) {
                interactionListener.newLink(e);
            }
        }
    }

    protected Map<String, String> importLink(LabeledCSVParser parser, Map<Integer, LatLng> localeMap) throws IOException, StudyImporterException {
        TreeMap<String, String> link = new TreeMap<String, String>();

        link.put(STUDY_SOURCE_CITATION, getSourceCitation() + " " + ReferenceUtil.createLastAccessedString(getLinkArchiveURL()));

        String predNum = StringUtils.trim(parser.getValueByLabel("PredatorSciNameTSN"));
        if (StringUtils.isNotBlank(predNum)) {
            link.put(SOURCE_TAXON_ID, TaxonomyProvider.ITIS.getIdPrefix() + predNum);
        }

        String predName = StringUtils.trim(parser.getValueByLabel("PredatorSciName"));
        if (StringUtils.isNotBlank(predName)) {
            link.put(SOURCE_TAXON_NAME, predName);
        }

        String preyNum = StringUtils.trim(parser.getValueByLabel("PreySciNameTSN"));
        if (StringUtils.isNotBlank(preyNum)) {
            link.put(TARGET_TAXON_ID, TaxonomyProvider.ITIS.getIdPrefix() + preyNum);
        }

        String preyName = StringUtils.trim(parser.getValueByLabel("PreySciName"));
        if (StringUtils.isNotBlank(preyName)) {
            link.put(TARGET_TAXON_NAME, preyName);
        }

        String[] citeFields = {"CiteAuth", "CiteYear", "CiteTitle", "CiteSource", "CiteVolume", "CitePages"};
        List<String> citeValues = new ArrayList<String>();
        for (String citeField : citeFields) {
            String value = StringUtils.trim(parser.getValueByLabel(citeField));
            if (StringUtils.isNotBlank(value)) {
                String prefix;
                if ("CiteVolume".equals(citeField)) {
                    prefix = "v.";
                } else if ("CitePages".equals(citeField)) {
                    prefix = "pp.";
                } else {
                    prefix = "";
                }
                citeValues.add(prefix + value);
            }
        }
        String referenceCitation = StringUtils.join(citeValues, ". ");
        link.put(REFERENCE_ID, getSourceDOI() + '/' + MD5.getHashString(referenceCitation));
        link.put(REFERENCE_CITATION, referenceCitation);
        link.put(INTERACTION_TYPE_NAME, "preysOn");
        link.put(INTERACTION_TYPE_ID, "RO:0002439");
        link.put(LOCALITY_NAME, StringUtils.trim(parser.getValueByLabel("LocatName")));
        String locatNum = StringUtils.trim(parser.getValueByLabel("LocatNum"));
        if (StringUtils.isNotBlank(locatNum)) {
            try {
                LatLng latLng = localeMap.get(Integer.parseInt(locatNum));
                if (latLng != null) {
                    link.put(DECIMAL_LATITUDE, Double.toString(latLng.getLat()));
                    link.put(DECIMAL_LONGITUDE, Double.toString(latLng.getLng()));
                }
            } catch (NumberFormatException ex) {
                throw new StudyImporterException("found invalid LocalNum [" + locatNum + "] in [" + getLinkArchiveURL() + "]:" + parser.lastLineNumber(), ex);
            }
        }
        return link;
    }

    public void setShapeArchiveURL(String shapeArchiveURL) {
        this.shapeArchiveURL = shapeArchiveURL;
    }

    public void setLinkArchiveURL(String linkArchiveURL) {
        this.linkArchiveURL = linkArchiveURL;
    }

    public String getLinkArchiveURL() {
        return linkArchiveURL;
    }

    public String getShapeArchiveURL() {
        return shapeArchiveURL;
    }

    protected Map<Integer, LatLng> importShapes() throws StudyImporterException {
        Map<Integer, LatLng> localityMap = new TreeMap<Integer, LatLng>();
        FileDataStore dataStore = null;
        try {
            InputStream shapeZipArchive = ResourceUtil.asInputStream(getShapeArchiveURL(), getClass());
            File tmpFolder = new File(FileUtils.getTempDirectory(), UUID.randomUUID().toString());
            tmpFolder.deleteOnExit();
            unpackZip(shapeZipArchive, tmpFolder);
            dataStore = FileDataStoreFinder.getDataStore(new File(tmpFolder, "LocatPolygonsPoints.shp"));
            if (dataStore == null) {
                throw new StudyImporterException("failed to parse shapefiles [" + getShapeArchiveURL() + "]");
            }
            FeatureReader<SimpleFeatureType, SimpleFeature> featureReader = dataStore.getFeatureReader();
            while (featureReader.hasNext()) {
                SimpleFeature feature = featureReader.next();
                Object geom = feature.getAttribute("the_geom");
                if (geom instanceof Point) {
                    Coordinate coordinate = ((Point) geom).getCoordinate();
                    Object localNum = feature.getAttribute("LocatNum");
                    if (localNum instanceof Integer) {
                        localityMap.put((Integer) localNum
                                , new LatLng(coordinate.y, coordinate.x));
                    }
                }
            }
            featureReader.close();
        } catch (IOException e) {
            throw new StudyImporterException("failed to import [" + getShapeArchiveURL() + "]", e);
        } finally {
            if (dataStore != null) {
                dataStore.dispose();
            }
        }
        return localityMap;
    }

    private void unpackZip(InputStream is, File outputDir) throws IOException {
        ZipInputStream zipStream = new ZipInputStream(is);
        try {
            ZipEntry entry;
            while ((entry = zipStream.getNextEntry()) != null) {
                File entryDestination = new File(outputDir, entry.getName());
                if (entry.isDirectory())
                    entryDestination.mkdirs();
                else {
                    entryDestination.getParentFile().mkdirs();
                    OutputStream out = new FileOutputStream(entryDestination);
                    IOUtils.copy(zipStream, out);
                    out.flush();
                    zipStream.closeEntry();
                    IOUtils.closeQuietly(out);
                }
            }
        } finally {
            IOUtils.closeQuietly(zipStream);
        }
    }
}