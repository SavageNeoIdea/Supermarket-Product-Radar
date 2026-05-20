package org.sni.businessunit.store;
import org.sni.businessunit.model.Product;
import java.util.List;

public interface DatamartStore {
    public void storeAllData(List<Product> products);
}
