package org.trophic.graph.data;

import org.trophic.graph.domain.Study;

public interface StudyImporter {

    Study importStudy() throws StudyImporterException;

}
