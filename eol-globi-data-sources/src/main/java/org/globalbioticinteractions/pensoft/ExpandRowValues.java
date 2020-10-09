package org.globalbioticinteractions.pensoft;

import org.apache.commons.lang3.tuple.Pair;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.jsoup.select.Evaluator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ExpandRowValues implements TableProcessor {

    @Override
    public String process(String input) {
        Document doc = TableUtil.parseHtml(input);

        Elements rows = doc.select("tr");
        for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
            List<Node> toBeRemoved = new ArrayList<>();

            Map<Integer, Elements> toBeExpanded = new TreeMap<>();
            Element row = rows.get(rowIndex);
            Elements rowColumns = row.select("td");

            for (int i = 0; i < rowColumns.size(); i++) {
                Element rowColumn = rowColumns.get(i);
                final Elements names = rowColumn.select(new Evaluator.TagEndsWith("tp:taxon-name"));
                if (names.size() > 1) {
                    toBeExpanded.put(i, names);
                }

                final Elements references = rowColumn.select(new Evaluator.TagEndsWith("xref"));
                if (names.size() <= 1 && references.size() > 1) {
                    toBeExpanded.put(i, references);
                }

                if (names.size() > 1 || references.size() > 1) {
                    toBeRemoved.add(row);
                }
            }

            for (Map.Entry<Integer, Elements> expandedValues : toBeExpanded.entrySet()) {
                Elements names = expandedValues.getValue();
                List<Pair<Integer, Element>> expandedValueCandidates = new ArrayList<>();
                toBeExpanded.forEach((columnIndex, values) -> {
                    if (expandedValues.getKey().intValue() != columnIndex) {
                        values.forEach(x -> expandedValueCandidates.add(Pair.of(columnIndex, x)));
                    }
                });


                for (Element name : names) {
                    ArrayList<Pair<Integer, Element>> replacementValues = new ArrayList<>(expandedValueCandidates);
                    replacementValues.add(Pair.of(expandedValues.getKey(), name));
                    Element clonedRow = row.clone();

                    for (Pair<Integer, Element> replacementValue : replacementValues) {
                        Element clonedValue = clonedRow.child(replacementValue.getKey());
                        clonedValue.children().forEach(Node::remove);
                        clonedValue.appendChild(replacementValue.getValue());
                    }

                    row.before(clonedRow);

                }

            }


            toBeRemoved.forEach(Node::remove);
        }


        return doc.select("table").toString();
    }
}
