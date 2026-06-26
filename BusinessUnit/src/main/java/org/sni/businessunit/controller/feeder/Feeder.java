package org.sni.businessunit.controller.feeder;
import org.sni.businessunit.model.Product;
import java.util.List;
import java.util.Map;

public interface Feeder {
    List<Product> processData(Map<String, List<String>> json);
    Product processData(String source, String event);
    String extractSourceFromJson(String eventString);
}
