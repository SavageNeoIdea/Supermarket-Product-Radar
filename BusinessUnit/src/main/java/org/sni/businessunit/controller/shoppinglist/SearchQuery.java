package org.sni.businessunit.controller.shoppinglist;

import org.sni.businessunit.model.OptimizedItem;

public interface SearchQuery {
    OptimizedItem searchQuery(String input);
}
