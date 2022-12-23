package pet.diploma.sitesearchengine.repositories;

import java.io.IOException;
import java.util.Map;

public interface MorphologyService {
    Map<String, Integer> getNormalFormsList(String text) throws IOException;
}
