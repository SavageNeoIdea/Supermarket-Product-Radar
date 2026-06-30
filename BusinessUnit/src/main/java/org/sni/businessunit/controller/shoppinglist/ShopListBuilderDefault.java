package org.sni.businessunit.controller.shoppinglist;

import org.sni.businessunit.model.DefinitiveOptimizedItem;
import org.sni.businessunit.model.OptimizedItem;

import java.util.ArrayList;
import java.util.List;

public class ShopListBuilderDefault implements ShopListBuilder{

    private final SearchQuery searchQuery;
    private final List<DefinitiveOptimizedItem> shoppingList = new ArrayList<>();

    public ShopListBuilderDefault(SearchQuery searchQuery) {
        this.searchQuery = searchQuery;
    }
    @Override
    public OptimizedItem findCandidates(String input) {
        return searchQuery.searchQuery(input);
    }
    @Override
    public void saveDefinitiveItem(DefinitiveOptimizedItem item) {
        shoppingList.add(item);
    }
    @Override
    public List<DefinitiveOptimizedItem> getShoppingList() {
        return shoppingList;
    }
    @Override
    public void clearList() {
        shoppingList.clear();
    }
}
