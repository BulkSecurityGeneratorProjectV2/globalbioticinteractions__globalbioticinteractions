package org.eol.globi.server;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eol.globi.domain.InteractType;
import org.eol.globi.domain.Location;
import org.eol.globi.domain.PropertyAndValueDictionary;
import org.eol.globi.domain.Specimen;
import org.eol.globi.domain.Study;
import org.eol.globi.server.util.InteractionTypeExternal;
import org.eol.globi.server.util.RequestHelper;
import org.eol.globi.server.util.ResultFields;
import org.eol.globi.util.CypherQuery;
import org.eol.globi.util.InteractUtil;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.eol.globi.server.util.ResultFields.*;

public class CypherQueryBuilder {
    private static final Log LOG = LogFactory.getLog(CypherQueryBuilder.class);

    public static final String INTERACTION_PREYS_ON = "preysOn";
    public static final String INTERACTION_PREYED_UPON_BY = "preyedUponBy";

    public static final String INTERACTION_PARASITE_OF = "parasiteOf";
    public static final String INTERACTION_HAS_PARASITE = "hasParasite";

    public static final String INTERACTION_POLLINATES = "pollinates";
    public static final String INTERACTION_POLLINATED_BY = "pollinatedBy";

    public static final String INTERACTION_PATHOGEN_OF = "pathogenOf";
    public static final String INTERACTION_HAS_PATHOGEN = "hasPathogen";

    public static final String INTERACTION_VECTOR_OF = "vectorOf";
    public static final String INTERACTION_HAS_VECTOR = "hasVector";

    public static final String INTERACTION_SYMBIONT_OF = "symbiontOf";
    public static final String INTERACTION_INTERACTS_WITH = "interactsWith";

    private static final String SOURCE_TAXON_HTTP_PARAM_NAME = "sourceTaxon";
    private static final String TARGET_TAXON_HTTP_PARAM_NAME = "targetTaxon";

    public static final String TAXON_HTTP_PARAM_NAME = "taxon";

    private static final Map<String, String> DIRECTIONAL_INTERACTION_TYPE_MAP = new TreeMap<String, String>() {
        {
            String preysOn = InteractType.ATE + "|" + InteractType.PREYS_UPON;
            put(INTERACTION_PREYS_ON, preysOn);
            put(INTERACTION_PREYED_UPON_BY, InteractType.EATEN_BY + "|" + InteractType.PREYED_UPON_BY);
            put(INTERACTION_PARASITE_OF, InteractType.PARASITE_OF.toString());
            put(INTERACTION_HAS_PARASITE, InteractType.HAS_PARASITE.toString());
            put(INTERACTION_POLLINATES, InteractType.POLLINATES.toString());
            put(INTERACTION_POLLINATED_BY, InteractType.POLLINATED_BY.toString());
            put(INTERACTION_PATHOGEN_OF, InteractType.PATHOGEN_OF.toString());
            put(INTERACTION_HAS_PATHOGEN, InteractType.HAS_PATHOGEN.toString());
            put(INTERACTION_HAS_VECTOR, InteractType.HAS_VECTOR.toString());
            put(INTERACTION_VECTOR_OF, InteractType.VECTOR_OF.toString());
            put(INTERACTION_SYMBIONT_OF, StringUtils.join(InteractType.values(), "|"));
            put(INTERACTION_INTERACTS_WITH, StringUtils.join(InteractType.values(), "|"));
        }
    };

    public static final Map<String, InteractionTypeExternal> INTERACTION_TYPE_INTERNAL_EXTERNAL_MAP = new TreeMap<String, InteractionTypeExternal>() {
        {
            put(InteractType.ATE.toString(), InteractionTypeExternal.PREYS_ON);
            put(InteractType.PREYS_UPON.toString(), InteractionTypeExternal.PREYS_ON);
            put(InteractType.EATEN_BY.toString(), InteractionTypeExternal.PREYED_UPON_BY);
            put(InteractType.PREYED_UPON_BY.toString(), InteractionTypeExternal.PREYED_UPON_BY);
            put(InteractType.PARASITE_OF.toString(), InteractionTypeExternal.PARASITE_OF);
            put(InteractType.HAS_PARASITE.toString(), InteractionTypeExternal.HAS_PARASITE);
            put(InteractType.POLLINATES.toString(), InteractionTypeExternal.POLLINATES);
            put(InteractType.POLLINATED_BY.toString(), InteractionTypeExternal.POLLINATED_BY);
            put(InteractType.PATHOGEN_OF.toString(), InteractionTypeExternal.PATHOGEN_OF);
            put(InteractType.HAS_PATHOGEN.toString(), InteractionTypeExternal.HAS_PATHOGEN);
            put(InteractType.VECTOR_OF.toString(), InteractionTypeExternal.VECTOR_OF);
            put(InteractType.HAS_VECTOR.toString(), InteractionTypeExternal.HAS_VECTOR);
            put(InteractType.SYMBIONT_OF.toString(), InteractionTypeExternal.SYMBIONT_OF);
            put(InteractType.INTERACTS_WITH.toString(), InteractionTypeExternal.INTERACTS_WITH);
        }
    };

    private static final Map<String, String> TRANSLATION_MAP = new TreeMap<String, String>() {
        {
            String preysOn = InteractType.ATE + "|" + InteractType.PREYS_UPON;
            put(INTERACTION_PREYS_ON, preysOn);
            put(INTERACTION_PREYED_UPON_BY, InteractType.EATEN_BY + "|" + InteractType.PREYED_UPON_BY);
            put(INTERACTION_PARASITE_OF, InteractType.PARASITE_OF.toString());
            put(INTERACTION_HAS_PARASITE, InteractType.HAS_PARASITE.toString());
            put(INTERACTION_POLLINATES, InteractType.POLLINATES.toString());
            put(INTERACTION_POLLINATED_BY, InteractType.POLLINATED_BY.toString());
            put(INTERACTION_PATHOGEN_OF, InteractType.PATHOGEN_OF.toString());
            put(INTERACTION_HAS_PATHOGEN, InteractType.HAS_PATHOGEN.toString());
            put(INTERACTION_INTERACTS_WITH, InteractType.INTERACTS_WITH.toString());
            put(INTERACTION_SYMBIONT_OF, InteractType.SYMBIONT_OF.toString());
        }
    };

    static final Map<String, String> EMPTY_PARAMS = new TreeMap<String, String>();
    public static final List<String> TAXON_FIELDS = Collections.unmodifiableList(new ArrayList<String>() {{
        add(TAXON_NAME);
        add(TAXON_COMMON_NAMES);
        add(TAXON_EXTERNAL_ID);
        add(TAXON_PATH);
        add(TAXON_PATH_IDS);
        add(TAXON_PATH_RANKS);
    }});

    public static final Map<String, String> FIELD_MAP = Collections.unmodifiableMap(new TreeMap<String, String>() {{
        put(TAXON_NAME, "taxon.name");
        put(TAXON_COMMON_NAMES, "taxon.commonNames");
        put(TAXON_EXTERNAL_ID, "taxon.externalId");
        put(TAXON_PATH, "taxon.path");
        put(TAXON_PATH_IDS, "taxon.pathIds");
        put(TAXON_PATH_RANKS, "taxon.pathNames");
    }});

    static public CypherQuery createDistinctTaxaInLocationQuery(Map<String, String[]> params) {
        StringBuilder builder = new StringBuilder();
        List<String> interactionTypes = collectParamValues(params, "interactionType");

        if (RequestHelper.isSpatialSearch(params)) {
            appendSpatialStartWhereWith(params, builder);
        } else {
            builder.append("START taxon = node:taxons('*:*') ");
        }

        if (RequestHelper.isSpatialSearch(params)) {
            builder.append("MATCH taxon<-[:CLASSIFIED_AS]-specimen-[:COLLECTED_AT]->loc");
            if (!interactionTypes.isEmpty()) {
                builder.append(", taxon-[:");
                builder.append(createInteractionTypeSelector(interactionTypes));
                builder.append("]->otherTaxon ");
            } else {
                builder.append(" ");
            }
        } else {
            if (!interactionTypes.isEmpty()) {
                builder.append("MATCH taxon-[:");
                builder.append(createInteractionTypeSelector(interactionTypes));
                builder.append("]->otherTaxon ");
            }
        }

        List<String> fields = collectParamValues(params, "field");

        List<String> returnFields = new ArrayList<String>();
        for (String defaultField : TAXON_FIELDS) {
            if (fields.contains(defaultField)) {
                returnFields.add(defaultField);
            }
        }
        if (returnFields.isEmpty()) {
            returnFields.addAll(TAXON_FIELDS);
        }
        for (int i = 0; i < returnFields.size(); i++) {
            String fieldName = returnFields.get(i);
            if (i == 0) {
                builder.append("RETURN distinct(").append(FIELD_MAP.get(fieldName)).append("?) as ").append(fieldName);
            } else {
                builder.append(", ").append(FIELD_MAP.get(fieldName)).append("? as ").append(fieldName);
            }
        }
        return new CypherQuery(builder.toString(), new HashMap<String, String>());
    }

    public enum QueryType {
        SINGLE_TAXON_DISTINCT, SINGLE_TAXON_ALL, MULTI_TAXON_DISTINCT, MULTI_TAXON_ALL
    }

    public static void addLocationClausesIfNecessary(StringBuilder query, Map parameterMap) {
        if (parameterMap != null) {
            RequestHelper.appendSpatialClauses(parameterMap, query);
        }
    }

    public static String lucenePathQuery(List<String> taxonNames) {
        int count = 0;
        StringBuilder lucenePathQuery = new StringBuilder();
        for (String taxonName : taxonNames) {
            if (count > 0) {
                lucenePathQuery.append(" OR ");
            }
            lucenePathQuery.append("path:\\\"").append(taxonName).append("\\\"");
            count++;
        }
        return lucenePathQuery.toString();
    }

    private static Map<String, String> getParams(List<String> sourceTaxonNames, List<String> targetTaxonNames) {
        Map<String, String> paramMap = new HashMap<String, String>();
        if (sourceTaxonNames != null && sourceTaxonNames.size() > 0) {
            paramMap.put(SOURCE_TAXON_NAME, lucenePathQuery(sourceTaxonNames));
        }

        if (targetTaxonNames != null && targetTaxonNames.size() > 0) {
            paramMap.put(TARGET_TAXON_NAME, lucenePathQuery(targetTaxonNames));
        }
        return paramMap;
    }

    static void appendTaxonSelectors(boolean includeSourceTaxon, boolean includeTargetTaxon, StringBuilder query) {
        if (includeSourceTaxon) {
            final String sourceTaxonSelector = "sourceTaxon = " + getTaxonPathSelector(SOURCE_TAXON_NAME);
            query.append(sourceTaxonSelector);
        }
        if (includeTargetTaxon) {
            if (includeSourceTaxon) {
                query.append(", ");
            }
            final String targetTaxonSelector = "targetTaxon = " + getTaxonPathSelector(TARGET_TAXON_NAME);
            query.append(targetTaxonSelector);
        }
    }

    private static String getTaxonPathSelector(String taxonParamName) {
        return "node:taxonPaths({" + taxonParamName + "})";
    }

    public static CypherQuery shortestPathQuery(final String startTaxon, final String endTaxon) {
        String query = "START startNode = node:taxons(name={startTaxon}),endNode = node:taxons(name={endTaxon}) " +
                "MATCH p = allShortestPaths(startNode-[:" + InteractUtil.allInteractionsCypherClause() + "|CLASSIFIED_AS*..100]-endNode) " +
                "RETURN extract(n in (filter(x in nodes(p) : has(x.name))) : " +
                "coalesce(n.name?)) as shortestPaths ";


        HashMap<String, String> params = new HashMap<String, String>() {{
            put("startTaxon", startTaxon);
            put("endTaxon", endTaxon);
        }};

        return new CypherQuery(query, params);
    }

    public static CypherQuery externalIdForStudy(final String studyTitle) {
        String query = "START study = node:studies(title={studyTitle}) " +
                " RETURN study.externalId? as study";

        HashMap<String, String> params = new HashMap<String, String>() {{
            put("studyTitle", studyTitle);
        }};

        return new CypherQuery(query, params);
    }

    public static CypherQuery externalIdForTaxon(final String taxonName) {
        String query = "START taxon = node:taxons(name={taxonName}) " +
                " RETURN taxon.externalId? as externalId";

        HashMap<String, String> taxonName1 = new HashMap<String, String>() {{
            put("taxonName", taxonName);
        }};

        return new CypherQuery(query, taxonName1);
    }

    public static CypherQuery references(final String source) {
        String whereClause = StringUtils.isBlank(source) ? "" : " AND study.source = {source}";
        Map<String, String> params = StringUtils.isBlank(source) ? EMPTY_PARAMS : new HashMap<String, String>() {{
            put("source", source);
        }};
        String cypherQuery = "START study=node:studies('*:*')" +
                " MATCH study-[:COLLECTED]->sourceSpecimen-[interact:" + InteractUtil.allInteractionsCypherClause() + "]->targetSpecimen-[:CLASSIFIED_AS]->targetTaxon, sourceSpecimen-[:CLASSIFIED_AS]->sourceTaxon " +
                " WHERE not(has(interact." + PropertyAndValueDictionary.INVERTED + "))" + whereClause +
                " RETURN study.institution?, study.period?, study.description?, study.contributor?, count(interact), count(distinct(sourceTaxon.name)), count(distinct(targetTaxon.name)), study.title, study.citation?, study.doi?, study.source, study.externalId?";

        return new CypherQuery(cypherQuery, params);
    }

    public static CypherQuery locations() {
        String query = "START loc = node:locations('*:*') RETURN loc.latitude, loc.longitude";
        return new CypherQuery(query);
    }

    public static CypherQuery buildInteractionQuery(final String sourceTaxonName, final String interactionType, final String targetTaxonName, Map parameterMap, QueryType queryType) {
        List<String> sourceTaxa = new ArrayList<String>() {{
            if (sourceTaxonName != null) {
                add(sourceTaxonName);
            }
        }};
        List<String> targetTaxa = new ArrayList<String>() {{
            if (targetTaxonName != null) {
                add(targetTaxonName);
            }
        }};
        return buildInteractionQuery(sourceTaxa, interactionType, targetTaxa, parameterMap, queryType);
    }

    public static CypherQuery buildInteractionQuery(List<String> sourceTaxonName, final String interactionType, List<String> targetTaxonName, Map parameterMap, QueryType queryType) {
        List<String> interactionTypes = new ArrayList<String>() {{
            add(interactionType);
        }};
        return interactionObservations(sourceTaxonName, interactionTypes, targetTaxonName, parameterMap, queryType);
    }

    protected static CypherQuery interactionObservations(List<String> sourceTaxa, List<String> interactionTypes, List<String> targetTaxa, Map parameterMap, QueryType queryType) {
        StringBuilder query = appendStartMatchWhereClauses(sourceTaxa, interactionTypes, targetTaxa, parameterMap);
        appendReturnClause(interactionTypes, query, queryType);
        return new CypherQuery(query.toString(), getParams(sourceTaxa, targetTaxa));
    }


    public static CypherQuery buildInteractionTypeQuery(Map parameterMap) {
        final List<String> taxa = collectParamValues(parameterMap, TAXON_HTTP_PARAM_NAME);
        String query = "START taxon = " + getTaxonPathSelector(TAXON_NAME)
                + " MATCH taxon-[rel:" + InteractUtil.allInteractionsCypherClause() + "]->otherTaxon RETURN distinct(type(rel)) as " + INTERACTION_TYPE;
        return new CypherQuery(query
                , new HashMap<String, String>() {
            {
                put(TAXON_NAME, lucenePathQuery(taxa));
            }
        });
    }


    public static CypherQuery buildInteractionQuery(Map parameterMap, QueryType queryType) {
        List<String> sourceTaxa = collectParamValues(parameterMap, SOURCE_TAXON_HTTP_PARAM_NAME);
        List<String> targetTaxa = collectParamValues(parameterMap, TARGET_TAXON_HTTP_PARAM_NAME);
        List<String> interactionTypeSelectors = collectParamValues(parameterMap, "interactionType");
        return interactionObservations(sourceTaxa, interactionTypeSelectors, targetTaxa, parameterMap, queryType);
    }


    private static void appendReturnClause(List<String> interactionType, StringBuilder query, QueryType returnType) {
        switch (returnType) {
            case SINGLE_TAXON_DISTINCT:
                appendReturnClauseDistinct(interactionType.get(0), query);
                break;
            case SINGLE_TAXON_ALL:
                appendReturnClause(interactionType.get(0), query);
                break;
            case MULTI_TAXON_ALL:
                appendReturnClause(query);
                break;
            case MULTI_TAXON_DISTINCT:
                appendReturnClauseDistinct(query);
                break;
            default:
                throw new IllegalArgumentException("invalid option [" + returnType + "]");
        }
    }


    protected static StringBuilder appendTargetTaxonWhereClause(Map parameterMap, List<String> sourceTaxa, List<String> targetTaxa, StringBuilder query) {
        if (sourceTaxa.size() > 0) {
            appendTaxonFilter(query, RequestHelper.isSpatialSearch(parameterMap), "targetTaxon", targetTaxa);
        }
        return query;
    }

    protected static StringBuilder appendStartClause2(Map parameterMap, List<String> sourceTaxa, List<String> targetTaxa, StringBuilder query) {
        if (noSearchCriteria(RequestHelper.isSpatialSearch(parameterMap), sourceTaxa, targetTaxa)) {
            // sensible default
            sourceTaxa.add("Homo sapiens");
        }

        query.append("START");
        if (sourceTaxa.size() == 0 && targetTaxa.size() == 0) {
            query.append(" loc = node:locations('*:*')");
        } else {
            if (sourceTaxa.size() > 0) {
                query.append(" ");
                appendTaxonSelectors(true, false, query);
            } else if (targetTaxa.size() > 0) {
                query.append(" ");
                appendTaxonSelectors(false, true, query);
            }
        }
        return query;
    }

    private static StringBuilder appendStartMatchWhereClauses(List<String> sourceTaxa, List<String> interactionTypes, List<String> targetTaxa, Map parameterMap) {
        StringBuilder query = new StringBuilder();
        appendStartClause2(parameterMap, sourceTaxa, targetTaxa, query);
        appendMatchAndWhereClause(interactionTypes, parameterMap, query);
        return appendTargetTaxonWhereClause(parameterMap, sourceTaxa, targetTaxa, query);
    }

    protected static void appendReturnClause(String interactionType, StringBuilder query) {
        query.append(" RETURN ")
                .append("sourceTaxon.name as ").append(SOURCE_TAXON_NAME)
                .append(",'").append(interactionType).append("' as ").append(INTERACTION_TYPE)
                .append(",targetTaxon.name as ").append(TARGET_TAXON_NAME).append(", ");
        query.append("loc.").append(Location.LATITUDE).append("? as ").append(LATITUDE)
                .append(",loc.").append(Location.LONGITUDE).append("? as ").append(LONGITUDE)
                .append(",loc.").append(Location.ALTITUDE).append("? as ").append(ALTITUDE);
        query.append(",study.").append(Study.TITLE).append(" as ").append(STUDY_TITLE)
                .append(",collected_rel.dateInUnixEpoch? as ").append(COLLECTION_TIME_IN_UNIX_EPOCH)
                .append(",ID(sourceSpecimen) as tmp_and_unique_")
                .append(PREFIX_SOURCE_SPECIMEN).append("_id,")
                .append("ID(targetSpecimen) as tmp_and_unique_")
                .append(PREFIX_TARGET_SPECIMEN).append("_id,")
                .append("sourceSpecimen.").append(Specimen.LIFE_STAGE_LABEL).append("? as ").append(PREFIX_SOURCE_SPECIMEN).append(SUFFIX_LIFE_STAGE).append(",")
                .append("targetSpecimen.").append(Specimen.LIFE_STAGE_LABEL).append("? as ").append(PREFIX_TARGET_SPECIMEN).append(SUFFIX_LIFE_STAGE).append(",")
                .append("sourceSpecimen.").append(Specimen.BODY_PART_LABEL).append("? as ").append(PREFIX_SOURCE_SPECIMEN).append(SUFFIX_BODY_PART).append(",")
                .append("targetSpecimen.").append(Specimen.BODY_PART_LABEL).append("? as ").append(PREFIX_TARGET_SPECIMEN).append(SUFFIX_BODY_PART).append(",")
                .append("sourceSpecimen.").append(Specimen.PHYSIOLOGICAL_STATE_LABEL).append("? as ").append(PREFIX_SOURCE_SPECIMEN).append(SUFFIX_PHYSIOLOGICAL_STATE).append(",")
                .append("targetSpecimen.").append(Specimen.PHYSIOLOGICAL_STATE_LABEL).append("? as ").append(PREFIX_TARGET_SPECIMEN).append(SUFFIX_PHYSIOLOGICAL_STATE).append(",")
                .append("targetSpecimen.").append(Specimen.TOTAL_COUNT).append("? as ").append(PREFIX_TARGET_SPECIMEN).append("_total_count").append(",")
                .append("targetSpecimen.").append(Specimen.TOTAL_VOLUME_IN_ML).append("? as ").append(PREFIX_TARGET_SPECIMEN).append("_total_volume_ml").append(",")
                .append("targetSpecimen.").append(Specimen.FREQUENCY_OF_OCCURRENCE).append("? as ").append(PREFIX_TARGET_SPECIMEN).append("_frequency_of_occurrence");
    }

    protected static void appendReturnClauseDistinct(String interactionType, StringBuilder query) {
        query.append(" RETURN sourceTaxon.name as ")
                .append(SOURCE_TAXON_NAME).append(", '")
                .append(interactionType).append("' as ")
                .append(INTERACTION_TYPE)
                .append(", collect(distinct(targetTaxon.name)) as ")
                .append(TARGET_TAXON_NAME);
    }

    protected static StringBuilder appendMatchAndWhereClause(List<String> interactionTypes, Map parameterMap, StringBuilder query) {
        String interactionMatch = getInteractionMatch(createInteractionTypeSelector(interactionTypes));
        query.append(" ")
                .append(interactionMatch);
        addLocationClausesIfNecessary(query, parameterMap);
        return query;
    }

    public static StringBuilder appendStartClause(boolean includeSourceTaxon, boolean includeTargetTaxon, StringBuilder query) {
        query.append("START ");
        appendTaxonSelectors(includeSourceTaxon, includeTargetTaxon, query);
        return query;
    }

    private static String getInteractionMatch(String interactionTypeSelector) {
        return "MATCH sourceTaxon<-[:CLASSIFIED_AS]-sourceSpecimen-[interactionType:" + interactionTypeSelector + "]->targetSpecimen-[:CLASSIFIED_AS]->targetTaxon, sourceSpecimen<-[collected_rel:COLLECTED]-study";
    }

    public static CypherQuery sourcesQuery() {
        String cypherQuery = "START study=node:studies('*:*')" +
                " RETURN distinct(study.source)";
        return new CypherQuery(cypherQuery, EMPTY_PARAMS);
    }


    protected static void appendReturnClause(StringBuilder query) {
        String[] fields = {SOURCE_TAXON_EXTERNAL_ID, SOURCE_TAXON_NAME, SOURCE_TAXON_PATH,
                PREFIX_SOURCE_SPECIMEN + SUFFIX_LIFE_STAGE, INTERACTION_TYPE,
                TARGET_TAXON_EXTERNAL_ID, TARGET_TAXON_NAME, TARGET_TAXON_PATH,
                PREFIX_TARGET_SPECIMEN + SUFFIX_LIFE_STAGE, LATITUDE, LONGITUDE, STUDY_TITLE};

        query.append("RETURN ");

        Map<String, String> selectors = new HashMap<String, String>() {
            {
                put(SOURCE_TAXON_EXTERNAL_ID, "sourceTaxon.externalId?");
                put(SOURCE_TAXON_NAME, "sourceTaxon.name");
                put(SOURCE_TAXON_PATH, "sourceTaxon.path?");
                put(PREFIX_SOURCE_SPECIMEN + SUFFIX_LIFE_STAGE, "sourceSpecimen.lifeStage?");
                StringBuilder interactionBuilder = new StringBuilder();
                appendInteractionTypeReturn(interactionBuilder, "type(interactionType)");
                put(INTERACTION_TYPE, interactionBuilder.toString());
                put(TARGET_TAXON_EXTERNAL_ID, "targetTaxon.externalId?");
                put(TARGET_TAXON_NAME, "targetTaxon.name");
                put(TARGET_TAXON_PATH, "targetTaxon.path?");
                put(PREFIX_TARGET_SPECIMEN + SUFFIX_LIFE_STAGE, "targetSpecimen.lifeStage?");
                put(LATITUDE, "loc.latitude?");
                put(LONGITUDE, "loc.longitude?");
                put(STUDY_TITLE, "study.title");
            }
        };

        List<String> returnFields = new ArrayList<String>();
        for (String field : fields) {
            returnFields.add(selectors.get(field) + " as " + field);
        }

        query.append(StringUtils.join(returnFields, ","));
    }

    protected static StringBuilder appendInteractionTypeReturn(StringBuilder query, String interactionTypeValue) {
        int terms = 0;
        StringBuilder suffix = new StringBuilder();
        for (Map.Entry<String, String> interactMap : TRANSLATION_MAP.entrySet()) {
            String externalType = interactMap.getKey();
            String[] internalTypes = interactMap.getValue().split("\\|");

            for (String internalType : internalTypes) {
                suffix.append(",'").append(internalType).append("','").append(externalType).append("')");
                terms++;
            }

        }

        query.append(StringUtils.repeat("replace(", terms));
        query.append(interactionTypeValue);
        query.append(suffix);
        return query;
    }

    protected static void appendReturnClauseDistinct(StringBuilder query) {
        query.append("WITH distinct targetTaxon as tTaxon, type(interactionType) as iType, sourceTaxon as sTaxon ");
        query.append("RETURN sTaxon.externalId? as ").append(SOURCE_TAXON_EXTERNAL_ID)
                .append(",sTaxon.name as ").append(SOURCE_TAXON_NAME)
                .append(",sTaxon.path? as ").append(SOURCE_TAXON_PATH)
                .append(",NULL as ").append(PREFIX_SOURCE_SPECIMEN).append(SUFFIX_LIFE_STAGE)
                .append(",");
        appendInteractionTypeReturn(query, "iType").append(" as ").append(INTERACTION_TYPE);
        query.append(",tTaxon.externalId? as ").append(TARGET_TAXON_EXTERNAL_ID)
                .append(",tTaxon.name as ").append(TARGET_TAXON_NAME)
                .append(",tTaxon.path? as ").append(TARGET_TAXON_PATH)
                .append(",NULL as ").append(PREFIX_TARGET_SPECIMEN).append(SUFFIX_LIFE_STAGE)
                .append(",NULL as ").append(LATITUDE)
                .append(",NULL as ").append(LONGITUDE)
                .append(",NULL as ").append(STUDY_TITLE);
    }

    protected static String createInteractionTypeSelector(List<String> interactionTypeSelectors) {
        List<String> cypherTypes = new ArrayList<String>();
        for (String type : interactionTypeSelectors) {
            if (DIRECTIONAL_INTERACTION_TYPE_MAP.containsKey(type)) {
                cypherTypes.add(DIRECTIONAL_INTERACTION_TYPE_MAP.get(type));
            } else if (StringUtils.isNotBlank(type)) {
                throw new IllegalArgumentException("unsupported interaction type [" + type + "]");
            }
        }
        return cypherTypes.isEmpty() ? InteractUtil.allInteractionsCypherClause() : StringUtils.join(cypherTypes, "|");
    }

    private static boolean noSearchCriteria(boolean spatialSearch, List<String> sourceTaxaSelectors, List<String> targetTaxaSelectors) {
        return !spatialSearch && sourceTaxaSelectors.size() == 0 && targetTaxaSelectors.size() == 0;
    }

    private static boolean appendTaxonFilter(StringBuilder query, boolean hasWhereClause, String taxonLabel, List<String> taxonNames) {
        if (taxonNames.size() > 0) {
            if (hasWhereClause) {
                query.append("AND ");
            } else {
                query.append(" WHERE ");
                hasWhereClause = true;
            }
            query.append("has(").append(taxonLabel).append(".path) AND ").append(taxonLabel).append(".path =~ '(.*(");
            query.append(StringUtils.join(taxonNames, "|"));
            query.append(").*)' ");
        }
        return hasWhereClause;
    }

    private static List<String> collectParamValues(Map parameterMap, String taxonSearchKey) {
        List<String> taxa = new ArrayList<String>();
        if (parameterMap.containsKey(taxonSearchKey)) {
            Object paramObject = parameterMap.get(taxonSearchKey);
            if (paramObject instanceof String[]) {
                Collections.addAll(taxa, (String[]) paramObject);
            } else if (paramObject instanceof String) {
                taxa.add((String) paramObject);
            }
        }
        return taxa;
    }


    public static CypherQuery createPagedQuery(HttpServletRequest request, CypherQuery query) {
        long defaultValue = 0L;
        long offset = getValueOrDefault(request, "offset", defaultValue);
        if (offset == defaultValue) {
            offset = getValueOrDefault(request, "skip", defaultValue);
        }
        long limit = getValueOrDefault(request, "limit", 1024L);
        return new CypherQuery(query.getQuery() + " SKIP " + offset + " LIMIT " + limit, query.getParams());
    }

    private static long getValueOrDefault(HttpServletRequest request, String paramName, long defaultValue) {
        long offset = defaultValue;
        if (request != null) {
            String offsetValue = request.getParameter(paramName);
            if (org.apache.commons.lang.StringUtils.isNotBlank(offsetValue)) {
                try {
                    offset = Long.parseLong(offsetValue);
                } catch (NumberFormatException ex) {
                    LOG.warn("malformed " + paramName + " found [" + offsetValue + "]", ex);
                }
            }
        }
        return offset;
    }

    public static CypherQuery spatialInfo(Map<String, String[]> parameterMap) {
        final String interactionLabel = "sourceTaxon.name + type(interact) + targetTaxon.name";
        StringBuilder query = new StringBuilder();

        if (RequestHelper.isSpatialSearch(parameterMap)) {
            appendSpatialStartWhereWith(parameterMap, query);
        } else {
            query.append("START study = node:studies('*:*') ");
        }

        query.append("MATCH sourceTaxon<-[:CLASSIFIED_AS]-sourceSpecimen<-[c:COLLECTED]-study")
                .append(", sourceSpecimen-[interact]->targetSpecimen-[:CLASSIFIED_AS]->targetTaxon");
        if (RequestHelper.isSpatialSearch(parameterMap)) {
            query.append(", sourceSpecimen-[:COLLECTED_AT]->loc");
        }
        query.append(" WHERE not(has(interact.");
        query.append(PropertyAndValueDictionary.INVERTED);
        query.append("))");

        Map<String, String> params = addSourceWhereClause(parameterMap, query);

        query.append(" RETURN count(distinct(study)) as `number of distinct studies`")
                .append(", count(interact) as `number of interactions`")
                .append(", count(distinct(sourceTaxon.name)) as `number of distinct source taxa (e.g. predators)`")
                .append(", count(distinct(targetTaxon.name)) as `number of distinct target taxa (e.g. prey)`")
                .append(", count(distinct(study.source)) as `number of distinct study sources`")
                .append(", count(c." + Specimen.DATE_IN_UNIX_EPOCH + "?) as `number of interactions with timestamp`")
        ;
        if (RequestHelper.isSpatialSearch(parameterMap)) {
            query.append(", count(distinct(loc))");
        } else {
            query.append(", NULL");
        }
        query.append(" as `number of distinct locations`")
                .append(", count(distinct(").append(interactionLabel).append(")) as `number of distinct interactions`");
        return new CypherQuery(query.toString(), params);
    }

    public static void appendSpatialStartWhereWith(Map<String, String[]> parameterMap, StringBuilder query) {
        query.append("START loc = node:locations('*:*') WHERE ");
        RequestHelper.addSpatialWhereClause(RequestHelper.parseSpatialSearchParams(parameterMap), query);
        query.append("WITH loc ");
    }

    protected static Map<String, String> addSourceWhereClause(Map<String, String[]> parameterMap, StringBuilder query) {
        String[] sourceList = parameterMap == null ? null : parameterMap.get("source");
        final String source = sourceList != null && sourceList.length > 0 ? sourceList[0] : null;
        String sourceWhereClause = StringUtils.isBlank(source) ? "" : " study.source = {source}";
        Map<String, String> params = StringUtils.isBlank(source) ? EMPTY_PARAMS : new HashMap<String, String>() {{
            put("source", source);
        }};

        if (StringUtils.isNotBlank(sourceWhereClause)) {
            query.append(" AND");
            query.append(sourceWhereClause);
        }
        return params;
    }

    public static CypherQuery stats(final String source) {
        HashMap<String, String[]> paramMap = new HashMap<String, String[]>();
        if (StringUtils.isNotBlank(source)) {
            paramMap.put("source", new String[]{source});
        }
        return spatialInfo(paramMap);
    }
}
