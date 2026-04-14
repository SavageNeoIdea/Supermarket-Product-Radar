package org.sni.spr.hiperdino.view;
import org.sni.spr.hiperdino.model.Product;
import java.util.List;
import java.util.Map;

public interface ProductFeeder {
    public Map<String, List<Product>> getProducts(Map<String, List<Map<String, String>>> productsRawDataMap);
}
