package org.eol.globi.tool;

import org.apache.commons.lang3.StringUtils;
import org.eol.globi.data.StudyImporterException;
import org.eol.globi.db.GraphServiceFactory;
import org.eol.globi.db.GraphServiceFactoryImpl;
import org.eol.globi.util.ResourceServiceLocal;
import org.globalbioticinteractions.dataset.DatasetRegistry;
import picocli.CommandLine;

import java.io.File;

@CommandLine.Command(
        name = "compile",
        aliases = {"import"},
        description = "compile and import datasets into Neo4J"
)
public abstract class CmdNeo4J implements Cmd {


    private NodeFactoryFactory nodeFactoryFactory = null;

    private static GraphServiceFactory graphServiceFactory = null;

    @CommandLine.Option(
            names = {CmdOptionConstants.OPTION_GRAPHDB_DIR},
            defaultValue = "./graph.db",
            description = "location of neo4j graph.db"
    )
    private String graphDbDir;

    @CommandLine.Option(
            names = {CmdOptionConstants.OPTION_NEO4J_VERSION},
            description = "version neo4j index to use",
            defaultValue = "2",
            hidden = true
    )
    private String neo4jVersion;

    @CommandLine.Option(
            names = {CmdOptionConstants.OPTION_TAXON_CACHE_PATH},
            description = "location of taxonCache.tsv.gz"
    )
    private String taxonCachePath = "./taxonCache.tsv.gz";

    @CommandLine.Option(
            names = {CmdOptionConstants.OPTION_TAXON_MAP_PATH},
            description = "location of taxonCache.tsv.gz"
    )
    private String taxonMapPath = "./taxonMap.tsv.gz";


    private static NodeFactoryFactory getNodeFactoryFactory(String neo4jVersion, GraphServiceFactory graphServiceFactory) {
        return StringUtils.equals("2", neo4jVersion)
                ? new NodeFactoryFactoryTransactingOnDatasetNeo4j2(graphServiceFactory)
                : new NodeFactoryFactoryTransactingOnDatasetNeo4j3(graphServiceFactory);
    }

    private static GraphServiceFactoryImpl getGraphServiceFactory(String graphDbDir) {
        return new GraphServiceFactoryImpl(
                new File(graphDbDir));
    }

    protected NodeFactoryFactory getNodeFactoryFactory() {
        if (this.nodeFactoryFactory == null) {
            this.nodeFactoryFactory = getNodeFactoryFactory(neo4jVersion, getGraphServiceFactory());
        }
        return nodeFactoryFactory;
    }

    protected GraphServiceFactory getGraphServiceFactory() {
        if (this.graphServiceFactory == null) {
            this.graphServiceFactory =
                    getGraphServiceFactory(graphDbDir);
        }
        return graphServiceFactory;
    }

    public void setNodeFactoryFactory(NodeFactoryFactory nodeFactoryFactory) {
        this.nodeFactoryFactory = nodeFactoryFactory;
    }

    public void setGraphServiceFactory(GraphServiceFactory graphServiceFactory) {
        this.graphServiceFactory = graphServiceFactory;
    }

    protected void configureAndRun(CmdNeo4J cmd) {
        cmd.setTaxonCachePath(getTaxonCachePath());
        cmd.setTaxonMapPath(getTaxonMapPath());
        cmd.setGraphServiceFactory(getGraphServiceFactory());
        cmd.setNodeFactoryFactory(getNodeFactoryFactory());
        cmd.run();
    }

    public String getTaxonCachePath() {
        return taxonCachePath;
    }

    public String getTaxonMapPath() {
        return taxonMapPath;
    }

    public void setTaxonCachePath(String taxonCachePath) {
        this.taxonCachePath = taxonCachePath;
    }

    public void setTaxonMapPath(String taxonMapPath) {
        this.taxonMapPath = taxonMapPath;
    }


}