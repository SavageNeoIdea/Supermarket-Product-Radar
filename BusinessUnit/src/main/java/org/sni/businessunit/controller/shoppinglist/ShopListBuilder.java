package org.sni.businessunit.controller.shoppinglist;
import org.sni.businessunit.model.DefinitiveOptimizedItem;
import org.sni.businessunit.model.OptimizedItem;
import java.util.List;

public interface ShopListBuilder {
    OptimizedItem findCandidates(String input);
    void saveDefinitiveItem(DefinitiveOptimizedItem item);
    List<DefinitiveOptimizedItem> getShoppingList();
    void clearList();
}