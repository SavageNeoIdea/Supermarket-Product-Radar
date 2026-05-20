package org.sni.businessunit.store;

import java.util.List;
import java.util.Map;

public interface SearchQuery {
    Map<String, Map<String, List<String>>> searchQuery(String input);
}
