package org.eol.globi.data;

import com.Ostermiller.util.LabeledCSVParser;
import org.apache.commons.collections.SetUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eol.globi.domain.Specimen;
import org.eol.globi.domain.Study;
import org.eol.globi.domain.Term;
import org.neo4j.graphdb.Relationship;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class StudyImporterForHurlbert extends BaseStudyImporter {

    public static final String RESOURCE = "https://raw.githubusercontent.com/hurlbertlab/dietdatabase/master/AvianDietDatabase.txt";

    private static final Log LOG = LogFactory.getLog(StudyImporterForHurlbert.class);

    public StudyImporterForHurlbert(ParserFactory parserFactory, NodeFactory nodeFactory) {
        super(parserFactory, nodeFactory);
    }

    @Override
    public Study importStudy() throws StudyImporterException {
        Set<String> regions = new HashSet<String>();
        Set<String> locales = new HashSet<String>();
        Set<String> habitats = new HashSet<String>();


        try {
            LabeledCSVParser parser = parserFactory.createParser(RESOURCE, "UTF-8");
            parser.changeDelimiter('\t');
            while (parser.getLine() != null) {
                String sourceCitation = parser.getValueByLabel("Source");
                Study study = nodeFactory.getOrCreateStudy(sourceCitation, null, null, null, sourceCitation, null, "Avian Diet Database. Unpublished data provided by Allen Hurlbert. For more info see http://labs.bio.unc.edu/Hurlbert/ .", null);
                study.setCitationWithTx(sourceCitation);

                //ID,Common_Name,Scientific_Name,,,,Prey_Common_Name,Fraction_Diet_By_Wt_or_Vol,Fraction_Diet_By_Items,Fraction_Occurrence,Fraction_Diet_Unspecified,Item Sample Size,Bird Sample size,Sites,Study Type,Notes,Source

                String preyLabels[] = {"Prey_Kingdom", "Prey_Phylum", "Prey_Class", "Prey_Order", "Prey_Suborder", "Prey_Family", "Prey_Genus", "Prey_Scientific_Name"};
                ArrayUtils.reverse(preyLabels);
                String preyTaxonName = null;
                for (String preyLabel : preyLabels) {
                    preyTaxonName = parser.getValueByLabel(preyLabel);
                    if (StringUtils.isNotBlank(preyTaxonName)) {
                        break;
                    }
                }

                String predatorTaxonName = StringUtils.trim(parser.getValueByLabel("Scientific_Name"));
                if (StringUtils.isNotBlank(predatorTaxonName) || StringUtils.isNotBlank(preyTaxonName)) {
                    importInteraction(regions, locales, habitats, parser, study, preyTaxonName, predatorTaxonName);
                }
            }
        } catch (IOException e) {
            throw new StudyImporterException("failed to import [" + RESOURCE + "]", e);
        }

        LOG.info("unmapped habitats [" + StringUtils.join(habitats.iterator(), ";") + "]");
        LOG.info("unmapped locales [" + StringUtils.join(locales.iterator(), ";") + "]");
        LOG.info("unmapped regions [" + StringUtils.join(regions.iterator(), ";") + "]");
        return null;
    }

    protected void importInteraction(Set<String> regions, Set<String> locales, Set<String> habitats, LabeledCSVParser parser, Study study, String preyTaxonName, String predatorName) throws StudyImporterException {
        try {
            Specimen predatorSpecimen = nodeFactory.createSpecimen(predatorName);
            //Longitude,Latitude,Altitude_min_m,Altitude_mean_m,Altitude_max_m,

            Specimen preySpecimen = nodeFactory.createSpecimen(preyTaxonName);

            String preyStage = StringUtils.trim(parser.getValueByLabel("Prey_Stage"));
            if (StringUtils.isNotBlank(preyStage)) {
                Term lifeStage = nodeFactory.getOrCreateLifeStage("HULBERT:" + StringUtils.replace(preyStage, " ", "_"), preyStage);
                preySpecimen.setLifeStage(lifeStage);
            }

            String preyPart = StringUtils.trim(parser.getValueByLabel("Prey_Part"));
            if (StringUtils.isNotBlank(preyPart)) {
                Term term = nodeFactory.getOrCreateBodyPart("HULBERT:" + StringUtils.replace(preyPart, " ", "_"), preyPart);
                preySpecimen.setBodyPart(term);
            }

            predatorSpecimen.ate(preySpecimen);
            // Observation_Season,,
            Relationship collected = study.collected(predatorSpecimen);
            addCollectionDate(parser, study, collected);
        } catch (NodeFactoryException e) {
            throw new StudyImporterException("failed to create interaction between [" + predatorName + "] and [" + preyTaxonName + "] on line [" + parser.lastLineNumber() + "] in [" + RESOURCE + "]", e);
        }


        //Location_Region,Location_Specific
        regions.add(parser.getValueByLabel("Location_Region"));
        locales.add(parser.getValueByLabel("Location_Specific"));
        habitats.add(parser.getValueByLabel("Habitat_type"));
    }

    private void addCollectionDate(LabeledCSVParser parser, Study study, Relationship collected) {
        //Observation_Month_Begin,Observation_Year_Begin,Observation_Month_End,Observation_Year_End
        String dateString = null;
        String year = parser.getValueByLabel("Observation_Year_Begin");
        if (StringUtils.isNotBlank(year)) {
            dateString = StringUtils.trim(year);
        }
        String month = parser.getValueByLabel("Observation_Month_Begin");
        if (StringUtils.isNotBlank(month)) {
            dateString += "-" + StringUtils.trim(month);
        }

        if (StringUtils.isNotBlank(dateString)) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM");
            try {
                Date date = dateFormat.parse(dateString);
                nodeFactory.setUnixEpochProperty(collected, date);
            } catch (ParseException e) {
                getLogger().warn(study, "not setting collection date, because [" + dateString + "] on line [" + parser.getLastLineNumber() + "] could not be read as date.");
            }
        }
    }

}
