package org.eol.globi.tool;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.eol.globi.Version;
import org.eol.globi.data.NodeFactoryNeo4j2;
import org.eol.globi.data.NodeFactoryNeo4j3;
import org.eol.globi.data.StudyImporterException;
import org.eol.globi.db.GraphServiceFactory;
import org.eol.globi.db.GraphServiceFactoryImpl;
import org.eol.globi.util.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Elton4N {
    private static final Logger LOG = LoggerFactory.getLogger(Elton4N.class);
    private static final String OPTION_HELP = "h";

    public static void main(final String[] args) throws StudyImporterException, ParseException {
        String o = Version.getVersionInfo(Elton4N.class);
        LOG.info(o);
        CommandLine cmdLine = parseOptions(args);
        if (cmdLine.hasOption(OPTION_HELP)) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("java -jar eol-globi-data-tool-[VERSION]-jar-with-dependencies.jar", getOptions());
        } else {
            try {
                new Elton4N().run(cmdLine);
            } catch (Throwable th) {
                LOG.error("failed to run GloBI indexer with [" + StringUtils.join(args, " ") + "]", th);
                throw th;
            }
        }
    }


    protected static CommandLine parseOptions(String[] args) throws ParseException {
        CommandLineParser parser = new BasicParser();
        return parser.parse(getOptions(), args);
    }

    private static Options getOptions() {
        Options options = new Options();
        options.addOption(CmdOptionConstants.OPTION_DATASET_DIR, true, "specifies location of dataset cache");
        options.addOption(CmdOptionConstants.OPTION_NEO4J_VERSION, true, "specifies version of Neo4j to use");

        Option helpOpt = new Option(OPTION_HELP, "help", false, "print this help information");
        options.addOption(helpOpt);
        return options;
    }

    public void run(CommandLine cmdLine) throws StudyImporterException {

        final String neo4jVersion = cmdLine == null
                ? "2"
                : cmdLine.getOptionValue(CmdOptionConstants.OPTION_NEO4J_VERSION, "2");


        Factories factoriesNeo4j = new Factories() {
            final GraphServiceFactory factory = new GraphServiceFactoryImpl("./");

            @Override
            public GraphServiceFactory getGraphServiceFactory() {
                return factory;
            }

            @Override
            public NodeFactoryFactory getNodeFactoryFactory() {
                return service -> StringUtils.equals("2", neo4jVersion)
                        ? new NodeFactoryNeo4j2(factory.getGraphService())
                        : new NodeFactoryNeo4j3(factory.getGraphService());
            }
        };

        GraphServiceFactory graphServiceFactory = factoriesNeo4j.getGraphServiceFactory();
        try {
            new CmdIndexDatasets(cmdLine, factoriesNeo4j.getNodeFactoryFactory(), graphServiceFactory)
                    .run();
        } finally {
            try {
                graphServiceFactory.close();
            } catch (Exception e) {
                LOG.error("failed to gracefully shutdown graphdb", e);
            }
            HttpUtil.shutdown();
        }
    }


}