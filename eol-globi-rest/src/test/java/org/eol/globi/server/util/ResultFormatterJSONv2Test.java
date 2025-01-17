package org.eol.globi.server.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eol.globi.server.CypherTestUtil;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.core.AnyOf.anyOf;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class ResultFormatterJSONv2Test {

    @Test
    public void formatSingleResult() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String format = new ResultFormatterJSONv2().format(CypherTestUtil.CYPHER_RESULT);

        JsonNode jsonNode = mapper.readTree(format);
        assertThat(jsonNode.isArray(), is(true));
        for (JsonNode interaction : jsonNode) {
            assertThat(interaction.get("type").asText(), is("preyedUponBy"));
            JsonNode taxon = interaction.get("source");
            assertThat(taxon.get("name").asText(), is("Ariopsis felis"));

            taxon = interaction.get("target");
            assertThat(taxon.get("name").asText(),
                    anyOf(is("Pomatomus saltatrix"), is("Lagodon rhomboides"), is("Centropomus undecimalis")));

            assertThat(interaction.get("latitude").asDouble(), is(notNullValue()));
            assertThat(interaction.get("longitude").asDouble(), is(notNullValue()));
            if (interaction.has("altitude")) {
                assertThat(interaction.get("altitude").asDouble(), is(notNullValue()));
            }
            if (interaction.has("time")) {
                assertThat(interaction.get("time").asLong() > 0, is(true));
            }

            assertThat(interaction.get("study").asText(),
                    anyOf(is("SPIRE"), is("Akin et al 2006"), is("Blewett 2006")));
            assertThat(interaction.get("study_title").asText(), anyOf(is("SPIRE"), is("Blewett 2006"), is("Akin et al 2006")));
            assertThat(interaction.get("source_taxon_name").asText(), is("Ariopsis felis"));
            assertThat(interaction.get("latitude").asText(), is(notNullValue()));
            assertThat(interaction.get("longitude").asText(), is(notNullValue()));
            assertThat(interaction.get("interaction_type").asText(), is("preyedUponBy"));
        }

    }

    @Test
    public void formatInteractionResult() throws IOException {
        String result = ResultFormatterJSONTestUtil.oldInteractionJsonResult();

        ObjectMapper mapper = new ObjectMapper();
        String format = new ResultFormatterJSONv2().format(result);

        JsonNode jsonNode = mapper.readTree(format);
        assertThat(jsonNode.isArray(), is(true));
        int count = 0;
        for (JsonNode interaction : jsonNode) {
            assertThat(interaction.get("type").asText(), is("ATE"));
            JsonNode taxon = assertNodePropertiesExist(interaction, "source");

            if (count == 0) {
                assertThat(taxon.get("name").asText(), is("Todus mexicanus"));
                assertThat(taxon.get("path").asText(), is("Animalia Chordata Aves Coraciiformes Todidae Todus"));
                assertThat(taxon.get("id").asText(), is("EOL:917146"));
            }

            taxon = assertNodePropertiesExist(interaction, "target");
            if (count == 0) {
                assertThat(taxon.get("name").asText(), is("Coleoptera"));
                assertThat(taxon.get("path").asText(), is("Animalia Arthropoda Insecta"));
                assertThat(taxon.get("id").asText(), is("EOL:345"));
            }

            assertThat(interaction.has("source"), is(true));

            if (count == 0) {
                assertThat(interaction.get("source_taxon_name").asText(), is("Todus mexicanus"));
                assertThat(interaction.get("source_taxon_path").asText(), is("Animalia Chordata Aves Coraciiformes Todidae Todus"));
                assertThat(interaction.get("source_taxon_external_id").asText(), is("EOL:917146"));
            }

            if (count == 0) {
                assertThat(interaction.get("target_taxon_name").asText(), is("Coleoptera"));
                assertThat(interaction.get("target_taxon_path").asText(), is("Animalia Arthropoda Insecta"));
                assertThat(interaction.get("target_taxon_external_id").asText(), is("EOL:345"));
            }
            count++;
        }

    }

    @Test
    public void formatTaxonResult() throws IOException {
        String result = ResultFormatterJSONTestUtil.oldTaxonQueryResult();

        ObjectMapper mapper = new ObjectMapper();
        String format = new ResultFormatterJSONv2().format(result);

        JsonNode jsonNode = mapper.readTree(format);
        assertThat(jsonNode.isArray(), is(true));

        JsonNode isopoda = jsonNode.get(0);
        assertThat(isopoda.get("taxon_name").asText(), is("Isopoda"));
        assertThat(isopoda.get("taxon_common_names").asText(), is("Asseln @de | isopods @en | Siirat @fi | Isopoda @fr | zeepissebedden @nl | gråsuggor och tånglöss @sv | "));
        assertThat(isopoda.get("taxon_external_id").asText(), is("EOL:7230"));
        assertThat(isopoda.get("taxon_path").asText(), is("Animalia | Arthropoda | Malacostraca | Isopoda"));
        assertThat(isopoda.get("taxon_path_ids").asText(), is("EOL:1 | EOL:164 | EOL:1157 | EOL:7230"));
        assertThat(isopoda.get("taxon_path_ranks").asText(), is("kingdom | phylum | class | order"));

    }

    @Test
    public void formatTaxonNewResult() throws IOException {
        String result = ResultFormatterJSONTestUtil.newTaxonQueryResult();

        ObjectMapper mapper = new ObjectMapper();
        String format = new ResultFormatterJSONv2().format(result);

        JsonNode jsonNode = mapper.readTree(format);
        assertThat(jsonNode.isArray(), is(true));

        JsonNode isopoda = jsonNode.get(0);
        assertThat(isopoda.get("taxon_name").asText(), is("Isopoda"));
        assertThat(isopoda.get("taxon_common_names").asText(), is("Asseln @de | isopods @en | Siirat @fi | Isopoda @fr | zeepissebedden @nl | gråsuggor och tånglöss @sv | "));
        assertThat(isopoda.get("taxon_external_id").asText(), is("EOL:7230"));
        assertThat(isopoda.get("taxon_path").asText(), is("Animalia | Arthropoda | Malacostraca | Isopoda"));
        assertThat(isopoda.get("taxon_path_ids").asText(), is("EOL:1 | EOL:164 | EOL:1157 | EOL:7230"));
        assertThat(isopoda.get("taxon_path_ranks").asText(), is("kingdom | phylum | class | order"));

    }


    @Test
    public void formatInteractionsNewResult() throws IOException {
        String result = ResultFormatterJSONTestUtil.getNewInteractionResults();

        ObjectMapper mapper = new ObjectMapper();
        String format = new ResultFormatterJSONv2().format(result);

        JsonNode jsonNode = mapper.readTree(format);
        assertThat(jsonNode.isArray(), is(true));

        JsonNode isopoda = jsonNode.get(0);
        assertThat(isopoda.get("source_taxon_external_id").asText(), is("GBIF:9203090"));
        assertThat(isopoda.get("source_taxon_name").asText(), is("Glypthelmins pennsylvaniensis"));
        assertThat(isopoda.get("source").get("id").asText(), is("GBIF:9203090"));
        assertThat(isopoda.get("source").get("name").asText(), is("Glypthelmins pennsylvaniensis"));
        assertThat(isopoda.get("target_taxon_external_id").asText(), is("EOL:1048370"));
        assertThat(isopoda.get("target_taxon_name").asText(), is("Pseudacris triseriata"));
        assertThat(isopoda.get("target").get("id").asText(), is("EOL:1048370"));
        assertThat(isopoda.get("target").get("name").asText(), is("Pseudacris triseriata"));
        assertThat(isopoda.get("type").asText(), is("parasiteOf"));
        assertThat(isopoda.get("interaction_type").asText(), is("parasiteOf"));

    }

    @Test
    public void formatAnyResult() throws IOException {
        String result = "{\n" +
                "  \"columns\" : [ \"col1\", \"col2\", \"col3\", \"col4\", \"col5\", \"col6\" ],\n" +
                "  \"data\" : [ [ \"Isopoda\", \"Asseln @de | isopods @en | Siirat @fi | Isopoda @fr | zeepissebedden @nl | gråsuggor och tånglöss @sv | \", \"EOL:7230\", \"Animalia | Arthropoda | Malacostraca | Isopoda\", \"EOL:1 | EOL:164 | EOL:1157 | EOL:7230\", \"kingdom | phylum | class | order\" ], [ \"Hymenoptera\", \"Hautflügler @de | ants, bees, and wasps @en | hyménoptères @fr | Vliesvleugeligen @nl | Vespa @pt | Перепончатокрылые @ru | steklar @sv | Zar kanatlılar @tr | Перетинчастокрилі @uk | \", \"EOL:648\", \"Cellular organisms | Eukaryota | Opisthokonta | Metazoa | Eumetazoa | Bilateria | Protostomia | Ecdysozoa | Panarthropoda | Arthropoda | Mandibulata | Pancrustacea | Hexapoda | Insecta | Dicondylia | Pterygota | Neoptera | Endopterygota | Hymenoptera\", \"EOL:6061725 | EOL:2908256 | EOL:2910700 | EOL:1 | EOL:10380067 | EOL:3014411 | EOL:10459935 | EOL:8880788 | EOL:12008312 | EOL:164 | EOL:5003390 | EOL:10578120 | EOL:2634370 | EOL:344 | EOL:2765371 | EOL:12024878 | EOL:1327472 | EOL:3016961 | EOL:648\", \" | superkingdom |  | kingdom |  |  |  |  |  | phylum |  |  | superclass | class |  |  | subclass | infraclass | order\" ], [ \"Coleoptera\", \"Твърдокрили @bg | Brouci @cs | Biller @da | Käfer @de | Κολεόπτερα @el | beetles @en | Koleopteroj @eo | Coleoptera @es | Kovakuoriaiset @fi | coléoptères @fr | חיפושיות @he | Bogarak @hu | Coleoptera @it | コウチュウ目 @ja | Vabalai @lt | Papaka @mi | Kevers @nl | Biller @no | Chrząszcze @pl | besouro @pt | жуки @ru | hrošči @sl | Skalbaggar @sv | kınkanatlılar @tr | твердокрилі @uk | \", \"EOL:345\", \"Animalia | Bilateria | Protostomia | Ecdysozoa | Arthropoda | Hexapoda | Insecta | Pterygota | Neoptera | Holometabola | Coleoptera\", \"EOL:1 | EOL:3014411 | EOL:10459935 | EOL:8880788 | EOL:164 | EOL:2634370 | EOL:344 | EOL:12024878 | EOL:1327472 | EOL:38309901 | EOL:345\", \"kingdom | subkingdom | infrakingdom | superphylum | division | subdivision | class | subclass | infraclass | superorder | order\" ], [ \"Hypostomus alatus\", \"Catfish @en | Vieja de agua @es | Cascudo @pt | \", \"EOL:214679\", \"Animalia | Chordata | Actinopterygii | Siluriformes | Loricariidae | Hypostomus | Hypostomus alatus\", \"EOL:1 | EOL:694 | EOL:1905 | EOL:5083 | EOL:5097 | EOL:23909 | EOL:214679\", \"kingdom | phylum | class | order | family | genus | species\" ], [ \"Lepidoptera\", \"Schmetterlinge @de | Butterflies and moths @en | Perhoset @fi | Hétérocères @fr | Mariposa @pt | \", \"EOL:747\", \"Animalia | Arthropoda | Insecta | Lepidoptera\", \"EOL:1 | EOL:164 | EOL:344 | EOL:747\", \"kingdom | phylum | class | order\" ], [ \"Copepoda\", \"Ruderfußkrebse @de | copepods @en | Hankajalkaiset @fi | copépodes @fr | roeipootkreeften @nl | hoppkräftor @sv | \", \"EOL:2625033\", \"Animalia | Bilateria | Protostomia | Ecdysozoa | Arthropoda | Crustacea | Maxillopoda | Copepoda\", \"EOL:1 | EOL:3014411 | EOL:10459935 | EOL:8880788 | EOL:164 | EOL:2598871 | EOL:1353 | EOL:2625033\", \"kingdom | subkingdom | infrakingdom | superphylum | division | subdivision | class | subclass\" ], [ \"Fungi\", \"فطر @ar | Гъби @bg | Fungo @br | Gljive @bs | Fongs @ca | Houby @cs | Svampe @da | Hefen @de | Μύκητες @el | Fungi @en | Fungo @eo | Levadura @es | Sienet @fi | Soppar @fo | Deuteromycota @fr | Fungas @ga | Fungos @gl | פטריה @he | Gljive @hr | Gombák @hu | Sveppir @is | Funghi @it | 酵母 @ja | 균류, 균계 @ko | Grybai @lt | Sēnes @lv | Печурки @mk | Schimmels @nl | Deuteromycota @pt | Ciuperci @ro | Дрожжи @ru | Huby @sk | Svampar @sv | Mantar @tr | tchampion @wa | 真菌界 @zh | \", \"EOL:5559\", \"Fungi\", \"EOL:5559\", \"kingdom\" ], [ \"Bacteria\", \"بيكتيريا @ar | Bakteria @bg | Bakteri @br | Bakterije @bs | Bacteris @ca | Bakterie @cs | Bakterie @da | Bakterien @de | Bακτήρια @el | Bacteria @en | Bakterioj @eo | باکتری @fa | Bakteerit @fi | Bacteria @fr | בקטריה @he | Bakterije @hr | Baktérium @hu | Batteri @it | 真正細菌 @ja | 세균 @ko | Bakterijos @lt | Bacteriën @nl | Bakterier @no | Bactèris @oc | Bakterie @pl | Bacterii @ro | Бактерии @ru | Baktérie @sk | Bakterije @sl | Bakterier @sv | Bakteri @tr | 细菌 @zh | \", \"EOL:288\", \"Bacteria\", \"EOL:288\", \"kingdom\" ], [ \"Mesocoelium\", null, \"EOL:60756\", \"Animalia | Platyhelminthes | Trematoda | Plagiorchiida | Mesocoeliidae | Mesocoelium\", \"EOL:1 | EOL:2884 | EOL:2894 | EOL:2976 | EOL:2999 | EOL:60756\", \"kingdom | phylum | class | order | family | genus\" ] ]\n" +
                "}";

        ObjectMapper mapper = new ObjectMapper();
        String format = new ResultFormatterJSONv2().format(result);

        JsonNode jsonNode = mapper.readTree(format);
        assertThat(jsonNode.isArray(), is(true));

        JsonNode isopoda = jsonNode.get(0);
        assertThat(isopoda.get("col1").asText(), is("Isopoda"));
        assertThat(isopoda.get("col2").asText(), is("Asseln @de | isopods @en | Siirat @fi | Isopoda @fr | zeepissebedden @nl | gråsuggor och tånglöss @sv | "));
        assertThat(isopoda.get("col3").asText(), is("EOL:7230"));
        assertThat(isopoda.get("col4").asText(), is("Animalia | Arthropoda | Malacostraca | Isopoda"));
        assertThat(isopoda.get("col5").asText(), is("EOL:1 | EOL:164 | EOL:1157 | EOL:7230"));
        assertThat(isopoda.get("col6").asText(), is("kingdom | phylum | class | order"));

    }

    @Test
    public void formatTaxonResultSelectedFields() throws IOException {
        String result = "{\n" +
                "  \"columns\" : [ \"taxon_common_names\", \"taxon_external_id\" ],\n" +
                "  \"data\" : [ [ \"Isopoda\", \"123\"] ]\n" +
                "}";

        ObjectMapper mapper = new ObjectMapper();
        String format = new ResultFormatterJSONv2().format(result);

        JsonNode jsonNode = mapper.readTree(format);
        assertThat(jsonNode.isArray(), is(true));

        JsonNode isopoda = jsonNode.get(0);
        assertThat(isopoda.get("taxon_common_names").asText(), is("Isopoda"));
        assertThat(isopoda.get("taxon_external_id").asText(), is("123"));

    }

    @Test
    public void formatSourceReportResult() throws IOException {
        String result = "{\n" +
                "  \"columns\" : [ \"study_citation\", \"study_url\", \"study_doi\", \"study_source_citation\", \"number_of_interactions\", \"number_of_distinct_taxa\", \"number_of_studies\", \"number_of_sources\", \"number_of_distinct_taxa_no_match\", \"study_source_id\", \"study_source_doi\", \"study_source_format\", \"study_source_archive_uri\", \"study_source_last_seen_at\" ],\n" +
                "  \"data\" : [ [ null, null, null, null, 1154, 419, 1, 1, 32, \"globi:millerse/Classified-List-of-Hosts-and-Parasites\", \"https://doi.org/10.5281/zenodo.258224\", \"globi\", \"https://zenodo.org/record/258224/files/millerse/Classified-List-of-Hosts-and-Parasites-v1.0.zip\", \"1489545754794\" ], [ null, null, null, null, 435, 359, 18, 1, 1, \"globi:millerse/Smithsonian-Repository-Interactions\", \"\", \"globi\", \"https://github.com/millerse/Smithsonian-Repository-Interactions/archive/102e55d6f6a73e5181188068f904198f01fc9451.zip\", \"1489545489153\" ], [ null, null, null, null, 13, 14, 1, 1, 0, \"globi:millerse/Diseases-of-Coffee-in-Porto-Rico\", \"https://doi.org/10.5281/zenodo.258221\", \"globi\", \"https://zenodo.org/record/258221/files/millerse/Diseases-of-Coffee-in-Porto-Rico-v1.0.zip\", \"1489545758912\" ], [ null, null, null, null, 469, 525, 1, 1, 0, \"globi:millerse/The-Butterflies-of-North-America\", \"https://doi.org/10.5281/zenodo.258190\", \"globi\", \"https://zenodo.org/record/258190/files/millerse/The-Butterflies-of-North-America-v1.0.zip\", \"1489545524947\" ], [ null, null, null, null, 13966, 251, 1, 1, 19, \"globi:globalbioticinteractions/hechinger2011\", \"  https://doi.org/10.1890/10-1383.1\", \"hechinger\", \"https://github.com/globalbioticinteractions/hechinger2011/archive/2e3f57339677fd1959aad9e4fb78fcdad13cf14b.zip\", \"1489545493525\" ], [ null, null, null, null, 1577, 101, 1, 1, 5, \"globi:globalbioticinteractions/zander2011\", \"https://doi.org/10.1890/11-0374.1\", \"hechinger\", \"https://github.com/globalbioticinteractions/zander2011/archive/0a9f807ef9e0a84b78ea8d972bd4bd7bc007711a.zip\", \"1489545756495\" ], [ null, null, null, null, 1924, 126, 1, 1, 14, \"globi:globalbioticinteractions/mouritsen2011\", \"https://doi.org/10.1890/11-0371.1\", \"hechinger\", \"https://github.com/globalbioticinteractions/mouritsen2011/archive/916f12ffd6904c1687aba2d7a47b43765fad9b9f.zip\", \"1489545485081\" ], [ null, null, null, null, 17, 31, 1, 1, 0, \"globi:globalbioticinteractions/hafner\", \"\", \"hafner\", \"https://github.com/globalbioticinteractions/hafner/archive/4f6ff1d51fbce4323eea9dc4be016eb2ec6b1d60.zip\", \"1489545488615\" ], [ null, null, null, null, 63886, 1717, 131, 1, 121, \"globi:GoMexSI/interaction-data\", \"\", \"gomexsi\", \"https://github.com/GoMexSI/interaction-data/archive/2fba3631b0676659c9a0d9898dcb4bfb41795b59.zip\", \"1489545527682\" ], [ null, null, null, null, 1, 2, 1, 1, 0, \"globi:millerse/Seton-1929\", \"https://doi.org/10.5281/zenodo.258192\", \"globi\", \"https://zenodo.org/record/258192/files/millerse/Seton-1929-v1.0.zip\", \"1489545483217\" ], [ null, null, null, null, 2, 3, 1, 1, 0, \"globi:globalbioticinteractions/jsonld-template-dataset\", \"\", \"globi\", \"https://github.com/globalbioticinteractions/jsonld-template-dataset/archive/5d31a27594656ba947e6d33f59e0c0774a115b51.zip\", \"1489564642490\" ], [ null, null, null, null, 43645, 8698, 1, 1, 658, \"globi:globalbioticinteractions/ferrer-paris\", \"\", \"ferrer-paris\", \"https://github.com/globalbioticinteractions/ferrer-paris/archive/3b0482c491539f3efa430772ff03d7dff6380cdd.zip\", \"1489564232384\" ], [ null, null, null, null, 2651, 1018, 1, 1, 106, \"globi:millerse/Flowers-and-insects-lists-of-visitors-to-four-hundred-and-fifty-three-flowers\", \"https://doi.org/10.5281/zenodo.258216\", \"globi\", \"https://zenodo.org/record/258216/files/millerse/Flowers-and-insects-lists-of-visitors-to-four-hundred-and-fifty-three-flowers-v1.0.zip\", \"1489545882286\" ], [ null, null, null, null, 32, 20, 1, 1, 0, \"globi:millerse/Greystock-et-al.-2015\", \"https://doi.org/10.5281/zenodo.258213\", \"globi\", \"https://zenodo.org/record/258213/files/millerse/Greystock-et-al.-2015-v1.0.zip\", \"1489545886386\" ], [ null, null, null, null, 225564, 1256, 191, 1, 44, \"globi:millerse/Dapstrom-integrated-database-and-portal-for-fish-stomach-records\", \"https://doi.org/10.5281/zenodo.258222\", \"globi\", \"https://zenodo.org/record/258222/files/millerse/Dapstrom-integrated-database-and-portal-for-fish-stomach-records-v1.0.zip\", \"1489563231138\" ], [ null, null, null, null, 5275, 1440, 257, 1, 13, \"globi:globalbioticinteractions/Catalogue-of-Afrotropical-Bees\", \"https://doi.org/10.5281/zenodo.229519\", \"coetzer\", \"https://zenodo.org/record/229519/files/globalbioticinteractions/Catalogue-of-Afrotropical-Bees-v0.1.zip\", \"1489545880273\" ], [ null, null, null, null, 350213, 8698, 4, 1, 95, \"globi:globalbioticinteractions/arthropodEasyCaptureAMNH\", \"\", \"arthropodEasyCapture\", \"https://github.com/globalbioticinteractions/arthropodEasyCaptureAMNH/archive/ef9963efce497f7c545cc59e9bf905d78d854984.zip\", \"1489545889367\" ], [ null, null, null, null, 34931, 244, 24, 1, 6, \"globi:globalbioticinteractions/barnes\", \"\", \"barnes\", \"https://github.com/globalbioticinteractions/barnes/archive/79f1139ecee05fe3809298170ffb07abd2d6c1a1.zip\", \"1489545759328\" ], [ null, null, null, null, 5673, 622, 1, 1, 11, \"globi:GoMexSI/simons\", \"\", \"simons\", \"https://github.com/GoMexSI/simons/archive/f7890fdee09fad388625a1d26761280cb4d95cd5.zip\", \"1489545856547\" ], [ null, null, null, null, 256, 120, 1, 1, 0, \"globi:millerse/Hummingbird-and-Flower-Interactions\", \"https://doi.org/10.5281/zenodo.258212\", \"globi\", \"https://zenodo.org/record/258212/files/millerse/Hummingbird-and-Flower-Interactions-v1.0.zip\", \"1489570636508\" ], [ null, null, null, null, 11, 19, 1, 1, 0, \"globi:globalbioticinteractions/template-dataset\", \"https://doi.org/10.5281/zenodo.207958\", \"globi\", \"https://zenodo.org/record/207958/files/globalbioticinteractions/template-dataset-0.0.2.zip\", \"1489570607945\" ], [ null, null, null, null, 49642, 6707, 82, 1, 876, \"globi:globalbioticinteractions/web-of-life\", \"\", \"web-of-life\", \"https://github.com/globalbioticinteractions/web-of-life/archive/dec58e5bb1ebab61687f98f6c005024b777537e6.zip\", \"1489570637851\" ], [ null, null, null, null, 255266, 19470, 3689, 1, 4834, \"globi:globalbioticinteractions/fishbase\", \"\", \"fishbase\", \"https://github.com/globalbioticinteractions/fishbase/archive/7e67537c10ab6def64e0e7aa7953af28f6198d2a.zip\", \"1489564646787\" ], [ null, null, null, null, 747, 549, 1, 1, 2, \"globi:millerse/Plant-Disease-Survey\", \"https://doi.org/10.5281/zenodo.258204\", \"globi\", \"https://zenodo.org/record/258204/files/millerse/Plant-Disease-Survey-v1.0.zip\", \"1489570608555\" ], [ null, null, null, null, 29, 34, 1, 1, 0, \"globi:millerse/Bird-Parasite\", \"https://doi.org/10.5281/zenodo.258228\", \"globi\", \"https://zenodo.org/record/258228/files/millerse/Bird-Parasite-v1.0.zip\", \"1489570604606\" ], [ null, null, null, null, 2156, 534, 1, 1, 15, \"globi:millerse/A-Host-parasite-Catalog-of-North-American-Tachinidae-Diptera-\", \"https://doi.org/10.5281/zenodo.258186\", \"globi\", \"https://zenodo.org/record/258186/files/millerse/A-Host-parasite-Catalog-of-North-American-Tachinidae-Diptera--v1.0.zip\", \"1489570606512\" ], [ null, null, null, null, 1130, 52, 1, 1, 0, \"globi:GoMexSI/blewett\", \"\", \"blewett\", \"https://github.com/GoMexSI/blewett/archive/fffe7b473f699653b66bad790b31085ce6c16024.zip\", \"1489571067697\" ], [ null, null, null, null, 16331, 120, 1, 1, 0, \"globi:GoMexSI/akin\", \"\", \"akin\", \"https://github.com/GoMexSI/akin/archive/d7c309bc61ef9cfff277a5f99d2f025cc031a5ea.zip\", \"1489570616623\" ], [ null, null, null, null, 34179, 5136, 2206, 1, 330, \"globi:millerse/Ecological-Database-of-the-World-s-Insect-Pathogens\", \"https://doi.org/10.5281/zenodo.258220\", \"globi\", \"https://zenodo.org/record/258220/files/millerse/Ecological-Database-of-the-World-s-Insect-Pathogens-v1.0.zip\", \"1489574728168\" ], [ null, null, null, null, 104, 87, 1, 1, 0, \"globi:millerse/Insect-Herbivores-on-Goldenrods-Solidago-altissima\", \"https://doi.org/10.5281/zenodo.258211\", \"globi\", \"https://zenodo.org/record/258211/files/millerse/Insect-Herbivores-on-Goldenrods-Solidago-altissima-v1.0.zip\", \"1489574710045\" ], [ null, null, null, null, 3293, 269, 1, 1, 2, \"globi:millerse/Bascompte-J.-Meli-n-C.J.-and-Sala-E.-2005\", \"\", \"globi\", \"https://github.com/millerse/Bascompte-J.-Meli-n-C.J.-and-Sala-E.-2005/archive/722735d8e972fafdb3535da9a8582ac73ee97967.zip\", \"1489574774368\" ], [ null, null, null, null, 793, 176, 146, 1, 0, \"globi:globalbioticinteractions/life-watch-greece\", \"\", \"life-watch-greece\", \"https://github.com/globalbioticinteractions/life-watch-greece/archive/3fa2336e1b699f117163291a41219de28f262c01.zip\", \"1489574706153\" ], [ null, null, null, null, 3338, 152, 1, 1, 5, \"globi:globalbioticinteractions/thieltges2011\", \"https://doi.org/10.1890/11-0351.1\", \"hechinger\", \"https://zenodo.org/record/229587/files/globalbioticinteractions/thieltges2011-v0.2.zip\", \"1489574768859\" ], [ null, null, null, null, 2450, 228, 237, 1, 6, \"globi:globalbioticinteractions/planque2014\", \"https://doi.org/10.1890/13-1062.1\", \"planque\", \"https://github.com/globalbioticinteractions/planque2014/archive/7846fbeddc9391e7ab2a94455d9ef0bde18c49d3.zip\", \"1489574706903\" ], [ null, null, null, null, 331, 144, 10, 1, 2, \"globi:diatomsRcool/greenland_interactions\", \"\", \"globi\", \"https://github.com/diatomsRcool/greenland_interactions/archive/8fc8e5d082e9b94ca49e7f1e9361eafa3bf92a21.zip\", \"1489574776809\" ], [ null, null, null, null, 100122, 309, 3, 1, 6, \"globi:globalbioticinteractions/noaa-reem\", \"https://doi.org/10.5281/zenodo.229505\", \"globi\", \"https://zenodo.org/record/229505/files/globalbioticinteractions/noaa-reem-v0.1.zip\", \"1489571165253\" ], [ null, null, null, null, 1018, 44, 1, 1, 0, \"globi:GoMexSI/baremore\", \"\", \"baremore\", \"https://github.com/GoMexSI/baremore/archive/31d7692de9337b670d43ddaa2a0eab5e915e4226.zip\", \"1489574773509\" ], [ null, null, null, null, 11620, 5087, 99, 1, 3115, \"globi:globalbioticinteractions/apsnet-common-names-plant-diseases\", \"\", \"globi\", \"https://github.com/globalbioticinteractions/apsnet-common-names-plant-diseases/archive/d21982a5f79a998e67ba71e0b0c0b9f6d26d9c55.zip\", \"1489575074783\" ], [ null, null, null, null, 50, 47, 1, 1, 0, \"globi:millerse/Host-plant-and-distribution-records\", \"https://doi.org/10.5281/zenodo.259822\", \"globi\", \"https://zenodo.org/record/259822/files/millerse/Host-plant-and-distribution-records-v1.0.zip\", \"1489575057874\" ], [ null, null, null, null, 77, 39, 1, 1, 0, \"globi:holmesjtg/okaloosa-county-coastal-uplands\", \"\", \"globi\", \"https://github.com/holmesjtg/okaloosa-county-coastal-uplands/archive/04071fa674aff8e0b61fafc639566a964849cad7.zip\", \"1489575055804\" ], [ null, null, null, null, 38008, 10430, 1, 1, 659, \"globi:globalbioticinteractions/strona\", \"\", \"strona\", \"https://github.com/globalbioticinteractions/strona/archive/3a0ae9b4aa3da1272d03d4f1a48a05e126d2741d.zip\", \"1489574786923\" ], [ null, null, null, null, 762, 27, 6, 1, 0, \"globi:globalbioticinteractions/bell\", \"\", \"bell\", \"https://github.com/globalbioticinteractions/bell/archive/e14c06196f6a5cde7ab859bf932b7b79b1e02be0.zip\", \"1489575056578\" ], [ null, null, null, null, 14896, 8119, 6217, 1, 0, \"globi:globalbioticinteractions/siad\", \"\", \"siad\", \"https://github.com/globalbioticinteractions/siad/archive/2d1141cbaae0156a5bab97e0f4cd4e89d7b623af.zip\", \"1489575059038\" ], [ null, null, null, null, 42, 5, 2, 1, 0, \"globi:millerse/Zika-Virus\", \"https://doi.org/10.5281/zenodo.258187\", \"globi\", \"https://zenodo.org/record/258187/files/millerse/Zika-Virus-v1.0.zip\", \"1489575073704\" ], [ null, null, null, null, 31, 10, 1, 1, 0, \"globi:millerse/Bat-flies\", \"https://doi.org/10.5281/zenodo.258230\", \"globi\", \"https://zenodo.org/record/258230/files/millerse/Bat-flies-v1.0.zip\", \"1489575058526\" ], [ null, null, null, null, 996, 93, 1, 1, 2, \"globi:GoMexSI/wrast\", \"\", \"wrast\", \"https://github.com/GoMexSI/wrast/archive/af62c0c5ee8b1f0cf27f85a3e1e8d66876a301fc.zip\", \"1489574777403\" ], [ null, null, null, null, 7720, 361, 193, 1, 0, \"globi:globalbioticinteractions/szoboszlai2015\", \"https://doi.org/10.5061/dryad.nv5d2\", \"szoboszlai\", \"https://github.com/globalbioticinteractions/szoboszlai2015/archive/88e7ee7c745b5d2dfb437808fc749a0739fc36ac.zip\", \"1489577482561\" ], [ null, null, null, null, 923, 297, 1, 1, 20, \"globi:millerse/Clements-R.-E.-and-F.-L.-Long\", \"https://doi.org/10.5281/zenodo.258223\", \"globi\", \"https://zenodo.org/record/258223/files/millerse/Clements-R.-E.-and-F.-L.-Long-v1.0.zip\", \"1489577479882\" ], [ null, null, null, null, 14965, 6173, 13026, 1, 1, \"globi:globalbioticinteractions/inaturalist\", \"\", \"inaturalist\", \"https://github.com/globalbioticinteractions/inaturalist/archive/78e4104ceb9a1f7b3327a54373722529e2b82504.zip\", \"1489575158744\" ], [ null, null, null, null, 61195, 18762, 1302, 1, 108, \"globi:globalbioticinteractions/bioinfo\", \"https://doi.org/10.5281/zenodo.293019\", \"bioinfo\", \"https://zenodo.org/record/293019/files/globalbioticinteractions/bioinfo-v1.0.zip\", \"1489575079842\" ], [ null, null, null, null, 16865, 1449, 20, 1, 70, \"globi:globalbioticinteractions/brose\", \"\", \"brose\", \"https://github.com/globalbioticinteractions/brose/archive/c8ce6b62696491ad8741824a8a67b9c936ce01e7.zip\", \"1489577565745\" ], [ null, null, null, null, 474, 198, 2, 1, 0, \"globi:millerse/Plant-Herbivore-Web\", \"https://doi.org/10.5281/zenodo.258203\", \"globi\", \"https://zenodo.org/record/258203/files/millerse/Plant-Herbivore-Web-v1.0.zip\", \"1489577564390\" ], [ null, null, null, null, 227, 104, 1, 1, 1, \"globi:millerse/Coccidae-of-Egypt\", \"https://doi.org/10.5281/zenodo.259823\", \"globi\", \"https://zenodo.org/record/259823/files/millerse/Coccidae-of-Egypt-v1.0.zip\", \"1489577481232\" ], [ null, null, null, null, 41, 42, 1, 1, 0, \"globi:millerse/Bald-Eagle-Diet\", \"https://doi.org/10.5281/zenodo.258233\", \"globi\", \"https://zenodo.org/record/258233/files/millerse/Bald-Eagle-Diet-v1.0.zip\", \"1489577563675\" ], [ null, null, null, null, 592, 144, 1, 1, 0, \"globi:millerse/Serengeti\", \"https://doi.org/10.5281/zenodo.258197\", \"globi\", \"https://zenodo.org/record/258197/files/millerse/Serengeti-v1.0.zip\", \"1489577479089\" ], [ null, null, null, null, 191, 61, 1, 1, 1, \"globi:millerse/Feeding-Niches-of-Hummingbirds-in-a-Trinidad-Valley\", \"https://doi.org/10.5281/zenodo.258219\", \"globi\", \"https://zenodo.org/record/258219/files/millerse/Feeding-Niches-of-Hummingbirds-in-a-Trinidad-Valley-v1.0.zip\", \"1489577610663\" ], [ null, null, null, null, 1804, 234, 1, 1, 2, \"globi:globalbioticinteractions/dunne2016SanakIntertidal\", \"https://doi.org/10.1038/srep21179\", \"dunne\", \"https://zenodo.org/record/229521/files/globalbioticinteractions/dunne2016SanakIntertidal-v0.1.zip\", \"1489577604846\" ], [ null, null, null, null, 16, 25, 12, 1, 0, \"globi:KatjaSchulz/dinosaur-biotic-interactions\", \"\", \"globi\", \"https://github.com/KatjaSchulz/dinosaur-biotic-interactions/archive/e744bef6ff47d57b86b81fe767ad983ef5b7f460.zip\", \"1489577607488\" ], [ null, null, null, null, 2290, 126, 1, 1, 7, \"globi:millerse/Carpinteria-Salt-Marsh-Web\", \"https://doi.org/10.5281/zenodo.258225\", \"globi\", \"https://zenodo.org/record/258225/files/millerse/Carpinteria-Salt-Marsh-Web-v1.0.zip\", \"1489581257387\" ], [ null, null, null, null, 183935, 834, 1, 1, 0, \"globi:globalbioticinteractions/ices\", \"\", \"ices\", \"https://github.com/globalbioticinteractions/ices/archive/99077c13bd8286e8ccf57b3e81b5de898f0eefe7.zip\", \"1489577612289\" ], [ null, null, null, null, 5244, 1668, 22, 1, 64, \"globi:millerse/Plant-Pollinator-Web\", \"https://doi.org/10.5281/zenodo.258201\", \"globi\", \"https://zenodo.org/record/258201/files/millerse/Plant-Pollinator-Web-v1.0.zip\", \"1489581259535\" ], [ null, null, null, null, 96, 65, 1, 1, 0, \"globi:millerse/Fly-parasites\", \"https://doi.org/10.5281/zenodo.258215\", \"globi\", \"https://zenodo.org/record/258215/files/millerse/Fly-parasites-v1.0.zip\", \"1489577610219\" ], [ null, null, null, null, 41, 13, 1, 1, 0, \"globi:millerse/Lara-C.-2006\", \"\", \"globi\", \"https://github.com/millerse/Lara-C.-2006/archive/0c7538cb8674d9fa937ecb68a769abb3e8d459ef.zip\", \"1489577606911\" ], [ null, null, null, null, 4167, 971, 1, 1, 1, \"globi:millerse/Lice\", \"https://doi.org/10.5281/zenodo.258207\", \"globi\", \"https://zenodo.org/record/258207/files/millerse/Lice-v1.0.zip\", \"1489581266458\" ], [ null, null, null, null, 25919, 131, 1, 1, 2, \"globi:globalbioticinteractions/wood2015\", \"https://doi.org/10.5061/dryad.g1qr6\", \"wood\", \"https://github.com/globalbioticinteractions/wood2015/archive/3c0f3183dd72c252474198aecbf705640ea92532.zip\", \"1489581347208\" ], [ null, null, null, null, 203, 244, 1, 1, 0, \"globi:globalbioticinteractions/cruaud\", \"\", \"cruaud\", \"https://github.com/globalbioticinteractions/cruaud/archive/2774167bee6bcc4a1187ccb606d855edd646f19a.zip\", \"1489581480931\" ], [ null, null, null, null, 3343, 955, 30, 1, 14, \"globi:millerse/Pollination-Collection\", \"https://doi.org/10.5281/zenodo.258199\", \"globi\", \"https://zenodo.org/record/258199/files/millerse/Pollination-Collection-v1.0.zip\", \"1489581482369\" ], [ null, null, null, null, 30167, 4073, 194, 1, 390, \"globi:globalbioticinteractions/spire\", \"\", \"spire\", \"https://github.com/globalbioticinteractions/spire/archive/f9a1ba09085173a5bf56736456947d5a787d00de.zip\", \"1489581297912\" ], [ null, null, null, null, 335, 2, 1, 1, 0, \"globi:globalbioticinteractions/cook\", \"\", \"cook\", \"https://github.com/globalbioticinteractions/cook/archive/dba9272d77a1e7f9a104716bf4bcf856dbd2cd85.zip\", \"1489581345808\" ], [ null, null, null, null, 367, 310, 18, 1, 0, \"globi:millerse/Jstor-Collecton\", \"https://doi.org/10.5281/zenodo.258209\", \"globi\", \"https://zenodo.org/record/258209/files/millerse/Jstor-Collecton-v1.0.zip\", \"1489581289676\" ], [ null, null, null, null, 746, 205, 7, 1, 0, \"globi:millerse/Arctic-food-web\", \"https://doi.org/10.5281/zenodo.258202\", \"globi\", \"https://zenodo.org/record/258202/files/millerse/Arctic-food-web-v1.0.zip\", \"1489581477040\" ], [ null, null, null, null, 48, 11, 7, 1, 0, \"globi:millerse/Fossil-snake\", \"https://doi.org/10.5281/zenodo.258214\", \"globi\", \"https://zenodo.org/record/258214/files/millerse/Fossil-snake-v1.0.zip\", \"1489581346798\" ], [ null, null, null, null, 1832, 206, 1, 1, 0, \"globi:millerse/Seaweed\", \"https://doi.org/10.5281/zenodo.258198\", \"globi\", \"https://zenodo.org/record/258198/files/millerse/Seaweed-v1.0.zip\", \"1489581475625\" ], [ null, null, null, null, 33, 34, 2, 1, 1, \"globi:millerse/Birds-Consumed-by-the-Invasive-Burmese-Python-Python-molurus-bivittatus-\", \"https://doi.org/10.5281/zenodo.258227\", \"globi\", \"https://zenodo.org/record/258227/files/millerse/Birds-Consumed-by-the-Invasive-Burmese-Python-Python-molurus-bivittatus--v1.0.zip\", \"1489581733568\" ], [ null, null, null, null, 147, 138, 1, 1, 0, \"globi:millerse/Amphibians-and-Reptiles-Predators-and-Prey.-Amphibians-and-Birds\", \"https://doi.org/10.5281/zenodo.258194\", \"globi\", \"https://zenodo.org/record/258194/files/millerse/Amphibians-and-Reptiles-Predators-and-Prey.-Amphibians-and-Birds-v1.0.zip\", \"1489581732330\" ], [ null, null, null, null, 1183, 213, 6, 1, 4, \"globi:millerse/Canadian-freshwater-fish-and-their-metazoan-parasites\", \"https://doi.org/10.5281/zenodo.258226\", \"globi\", \"https://zenodo.org/record/258226/files/millerse/Canadian-freshwater-fish-and-their-metazoan-parasites-v1.0.zip\", \"1489581727382\" ], [ null, null, null, null, 97, 45, 45, 1, 1, \"globi:millerse/Fishes-of-Basrah-Province-Iraq\", \"https://doi.org/10.5281/zenodo.258218\", \"globi\", \"https://zenodo.org/record/258218/files/millerse/Fishes-of-Basrah-Province-Iraq-v1.0.zip\", \"1489581730018\" ], [ null, null, null, null, 22827, 376, 1, 1, 6, \"globi:millerse/Buprestidae-of-North-America\", \"https://doi.org/10.5281/zenodo.259795\", \"globi\", \"https://zenodo.org/record/259795/files/millerse/Buprestidae-of-North-America-v1.0.zip\", \"1489581503524\" ], [ null, null, null, null, 1905, 62, 1, 1, 5, \"globi:globalbioticinteractions/preston2012\", \"https://doi.org/10.1890/11-2194.1\", \"hechinger\", \"https://github.com/globalbioticinteractions/preston2012/archive/74ca9d4c94e0beee02f8a197bc532bcb7ae5be68.zip\", \"1489581734043\" ], [ null, null, null, null, 74, 36, 1, 1, 0, \"globi:globalbioticinteractions/robledo\", \"\", \"robledo\", \"https://github.com/globalbioticinteractions/robledo/archive/34387216749990802735c887a153a0bf581a2382.zip\", \"1489581731671\" ], [ null, null, null, null, 62, 36, 1, 1, 0, \"globi:millerse/Weidinger-et-al.-2009\", \"https://doi.org/10.5281/zenodo.258188\", \"globi\", \"https://zenodo.org/record/258188/files/millerse/Weidinger-et-al.-2009-v1.0.zip\", \"1489581732896\" ], [ null, null, null, null, 2534, 727, 3, 1, 9, \"globi:millerse/BHL-Interactions\", \"https://doi.org/10.5281/zenodo.258229\", \"globi\", \"https://zenodo.org/record/258229/files/millerse/BHL-Interactions-v1.0.zip\", \"1489581489557\" ], [ null, null, null, null, 31, 25, 1, 1, 0, \"globi:jhammock/Layman-and-Allgeier-Lionfish\", \"\", \"globi\", \"https://github.com/jhammock/Layman-and-Allgeier-Lionfish/archive/23fcd6031013f7957071bf0a0fb391346b26bf01.zip\", \"1489582503616\" ], [ null, null, null, null, 215, 276, 18, 1, 2, \"globi:millerse/parasitic-plant-connection\", \"https://doi.org/10.5281/zenodo.258206\", \"globi\", \"https://zenodo.org/record/258206/files/millerse/parasitic-plant-connection-v1.0.zip\", \"1489581740970\" ], [ null, null, null, null, 96647, 736, 1, 1, 1, \"globi:globalbioticinteractions/roopnarine\", \"\", \"roopnarine\", \"https://github.com/globalbioticinteractions/roopnarine/archive/8800239a980ab7de1ea0f435e13a21a90072c9d6.zip\", \"1489581788757\" ], [ null, null, null, null, 395, 358, 2, 1, 3, \"globi:millerse/Interaction-data-by-SEM\", \"https://doi.org/10.5281/zenodo.258210\", \"globi\", \"https://zenodo.org/record/258210/files/millerse/Interaction-data-by-SEM-v1.0.zip\", \"1489582505725\" ], [ null, null, null, null, 12610, 10893, 1, 1, 17, \"globi:globalbioticinteractions/gemina\", \"\", \"gemina\", \"https://github.com/globalbioticinteractions/gemina/archive/74f37dd442b95f95b601953128f37a07c99d7fab.zip\", \"1489581768329\" ], [ null, null, null, null, 2090, 204, 121, 1, 0, \"globi:globalbioticinteractions/byrnes\", \"\", \"byrnes\", \"https://github.com/globalbioticinteractions/byrnes/archive/1fbb70a39c0fe2d5f62ce820a84f7f578667d602.zip\", \"1489582504516\" ], [ null, null, null, null, 271904, 14880, 229079, 1, 12, \"globi:millerse/Wardeh-et-al.-2015\", \"https://doi.org/10.5281/zenodo.258189\", \"globi\", \"https://zenodo.org/record/258189/files/millerse/Wardeh-et-al.-2015-v1.0.zip\", \"1489582343173\" ], [ null, null, null, null, 362, 124, 4, 1, 0, \"globi:millerse/Plant-ant-webs\", \"https://doi.org/10.5281/zenodo.258205\", \"globi\", \"https://zenodo.org/record/258205/files/millerse/Plant-ant-webs-v1.0.zip\", \"1489581766430\" ], [ null, null, null, null, 178, 24, 2, 1, 0, \"globi:millerse/Anemonefish\", \"https://doi.org/10.5281/zenodo.258196\", \"globi\", \"https://zenodo.org/record/258196/files/millerse/Anemonefish-v1.0.zip\", \"1489581765849\" ], [ null, null, null, null, 82644, 12608, 490, 1, 152, \"globi:globalbioticinteractions/natural-history-museum-london-interactions-bank\", \"\", \"globi\", \"https://github.com/globalbioticinteractions/natural-history-museum-london-interactions-bank/archive/810618ef85863b6ec4ed8b7e8776a370f6e446ab.zip\", \"1489582645931\" ], [ null, null, null, null, 6774, 511, 1, 1, 2, \"globi:globalbioticinteractions/dunne2016SanakNearshore\", \"https://doi.org/10.1038/srep21179\", \"dunne\", \"https://zenodo.org/record/229522/files/globalbioticinteractions/dunne2016SanakNearshore-v0.1.zip\", \"1489582639222\" ], [ null, null, null, null, 7694, 1757, 245, 1, 117, \"globi:globalbioticinteractions/AfricaTreeDatabase\", \"https://doi.org/10.5281/zenodo.229547\", \"globi\", \"https://zenodo.org/record/229547/files/globalbioticinteractions/AfricaTreeDatabase-v0.1.zip\", \"1489582518210\" ], [ null, null, null, null, 16, 25, 12, 1, 0, \"globi:cmungall/dinosaur-biotic-interactions\", \"\", \"globi\", \"https://github.com/cmungall/dinosaur-biotic-interactions/archive/e744bef6ff47d57b86b81fe767ad983ef5b7f460.zip\", \"1489582522679\" ], [ null, null, null, null, 22498, 6400, 1332, 1, 1131, \"globi:globalbioticinteractions/sealifebase\", \"\", \"fishbase\", \"https://github.com/globalbioticinteractions/sealifebase/archive/6e93023088855bf66ea9241fb7d2d837375afe59.zip\", \"1489582620036\" ], [ null, null, null, null, 0, 0, 1, 1, 0, \"globi:globalbioticinteractions/kelpforest\", \"\", \"kelpforest\", \"https://github.com/globalbioticinteractions/kelpforest/archive/0d4ae5a8a4d68b048c38535cefad32f6601df194.zip\", \"1489582515001\" ], [ null, null, null, null, 851, 222, 6, 1, 0, \"globi:millerse/PLANT-SEED-DISPERSER-WEBS\", \"https://doi.org/10.5281/zenodo.258200\", \"globi\", \"https://zenodo.org/record/258200/files/millerse/PLANT-SEED-DISPERSER-WEBS-v1.0.zip\", \"1489582637774\" ], [ null, null, null, null, 26462, 1064, 324, 1, 23, \"globi:globalbioticinteractions/raymond\", \"\", \"raymond\", \"https://github.com/globalbioticinteractions/raymond/archive/f46c29e304b50188f1c2f8f18470e7dd72158c55.zip\", \"1489582523668\" ], [ null, null, null, null, 337, 5, 1, 1, 0, \"globi:millerse/Adams-et-al.-2016\", \"https://doi.org/10.5281/zenodo.258193\", \"globi\", \"https://zenodo.org/record/258193/files/millerse/Adams-et-al.-2016-v1.0.zip\", \"1489582610587\" ], [ null, null, null, null, 254, 236, 42, 1, 0, \"globi:diatomsRcool/yellowstone_grizzly\", \"\", \"globi\", \"https://github.com/diatomsRcool/yellowstone_grizzly/archive/b0daae49f01058a5520780614bb2c1e546da0873.zip\", \"1489583920736\" ], [ null, null, null, null, 2, 3, 1, 1, 0, \"globi:jhpoelen/geosymbio\", \"\", \"globi\", \"https://github.com/jhpoelen/geosymbio/archive/a9cb473a202ac656271c04e3ab2b7233cdf5c522.zip\", \"1489583921199\" ], [ null, null, null, null, 464, 179, 5, 1, 5, \"globi:millerse/Flea-Collection\", \"https://doi.org/10.5281/zenodo.258217\", \"globi\", \"https://zenodo.org/record/258217/files/millerse/Flea-Collection-v1.0.zip\", \"1489583921721\" ], [ null, null, null, null, 183772, 34193, 1, 1, 6, \"globi:EOL/pseudonitzchia\", \"\", \"globi\", \"https://raw.githubusercontent.com/EOL/pseudonitzchia/e5838965a186fba4b7215cd0d179c4526773bad5\", \"1489583922645\" ] ]\n" +
                "}";

        ObjectMapper mapper = new ObjectMapper();
        String format = new ResultFormatterJSONv2().format(result);

        JsonNode jsonNode = mapper.readTree(format);
        assertThat(jsonNode.isArray(), is(true));

        JsonNode firstRow = jsonNode.get(0);
        assertThat(firstRow.get("study_source_id").asText(), is("globi:millerse/Classified-List-of-Hosts-and-Parasites"));
        assertThat(firstRow.get("study_source_last_seen_at").asText(), is("1489545754794"));

    }

    private JsonNode assertNodePropertiesExist(JsonNode interaction, String nodeLabel) {
        JsonNode taxon = interaction.get(nodeLabel);
        assertThat(taxon.has("name"), is(true));
        assertThat(taxon.has("path"), is(true));
        assertThat(taxon.has("id"), is(true));
        return taxon;
    }
}
