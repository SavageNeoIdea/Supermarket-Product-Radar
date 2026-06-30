package org.sni.businessunit.controller.feeder;
import org.sni.businessunit.model.Product;
import java.util.List;
import java.util.Map;

public interface Feeder {
    List<Product> processData(Map<String, List<String>> rawEventsPerSource);
    Product processData(String source, String rawEvent);
    String extractSourceFromJson(String rawEventString);
}
