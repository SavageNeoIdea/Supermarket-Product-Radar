import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class ProductFeeder implements Feeder {

    @Override
    public List<Product> process(Map<String, List<String>> rawJson) {
        Set<String> sources = rawJson.keySet();
        for (String source : sources) {
            for (String event : rawJson.get(source)) {
                JsonObject json = JsonParser.parseString(event).getAsJsonObject();
            }
        }
        return List.of();
    }
}
