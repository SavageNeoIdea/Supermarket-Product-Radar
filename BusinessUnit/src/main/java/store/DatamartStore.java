package store;
import model.Product;
import java.util.List;

public interface DatamartStore {
    public void storeAllData(List<Product> products);
}
