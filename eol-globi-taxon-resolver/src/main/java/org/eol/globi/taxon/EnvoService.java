package org.eol.globi.taxon;

import org.apache.commons.lang3.StringUtils;
import org.eol.globi.service.PropertyEnricherException;

import java.util.HashMap;
import java.util.Map;

import static org.eol.globi.domain.TaxonomyProvider.ID_PREFIX_ENVO;

public class EnvoService extends BasePropertyEnricherService {

    public static final String SEDIMENT = ID_PREFIX_ENVO + "00002007";
    public static final String SOIL = ID_PREFIX_ENVO + "00001998";
    public static final String ORGANIC_MATERIAL = ID_PREFIX_ENVO + "01000155";
    public static final String FECES = ID_PREFIX_ENVO + "00002003";
    public static final String WOOD = ID_PREFIX_ENVO + "00002040";
    public static final String PLASTIC = ID_PREFIX_ENVO + "01000404";
    public static final String ROCK = ID_PREFIX_ENVO + "00001995";
    public static final String PIECE_OF_ROCK = ID_PREFIX_ENVO + "00000339";

    private Map<String, String> mapping = new HashMap<String, String>() {{
        put("organic material", ORGANIC_MATERIAL);
        put("detritus", ORGANIC_MATERIAL);
        put("organic detritus", ORGANIC_MATERIAL);
        put("organic matter", ORGANIC_MATERIAL);
        put("Unidentified remains", ORGANIC_MATERIAL);
        put("suspended organic matter   ", ORGANIC_MATERIAL);
        put("dissolved organic carbon", ORGANIC_MATERIAL);
        put("organic matter in mud", ORGANIC_MATERIAL);
        put("dung", FECES);
        put("animal dung", FECES);
        put("bovine or equine dung", FECES);
        put("plastic", PLASTIC);
        put("rock", ROCK);
        put("organic matter", ORGANIC_MATERIAL);
        put("wood", WOOD);
        put("rotting wood", WOOD);
        put("sediment POC", SEDIMENT);
        put("sediment", SEDIMENT);
        put("soil", SOIL);
        put("stones", PIECE_OF_ROCK);
    }};

    private Map<String, String> pathLookup = new HashMap<String, String>() {{
        put(SEDIMENT, "environmental material | sediment");
        put(SOIL, "environmental material | soil");
        put(ORGANIC_MATERIAL, "environmental material | organic material");
        put(FECES, "environmental material | organic material | bodily fluid | excreta | feces");
        put(WOOD, "environmental material | organic material | wood");
        put(PLASTIC, "environmental material | anthropogenic environmental material");
        put(ROCK, "environmental material");
        put(PIECE_OF_ROCK, "environmental feature | mesoscopic physical object | abiotic mesoscopic physical object | piece of rock");
    }};

    public String lookupIdByName(String taxonName) throws PropertyEnricherException {
        String id = null;
        String lowerCaseName = StringUtils.lowerCase(taxonName);
        if (StringUtils.isNotBlank(lowerCaseName)) {
            id = mapping.get(lowerCaseName);
        }
        return id;
    }

    @Override
    public String lookupTaxonPathById(String id) throws PropertyEnricherException {
        return pathLookup.get(id);
    }
}
