package controller.store;

import java.util.List;
import java.util.Map;

public interface SearchQuery {
    public  Map<String, Map<String, List<String>>> searchQuery(String input);
}
