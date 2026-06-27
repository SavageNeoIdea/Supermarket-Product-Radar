package org.sni.businessunit.controller.shoppinglist;
import org.sni.businessunit.model.OptimizedItem;
import java.util.ArrayList;
import java.util.List;

public class ShopListBuilder {
    private final SearchQuery searchQuery;
    private List<OptimizedItem> shoppingList = new ArrayList<>();

    public ShopListBuilder(SearchQuery searchQuery) {
        this.searchQuery = searchQuery;
    }

    public void processCustomerInput(String input){
        OptimizedItem optimizedItem = searchQuery.searchQuery(input);
        shoppingList.add(optimizedItem);
    }

    public List<OptimizedItem> getShoppingList() {
        return shoppingList;
    }
}
