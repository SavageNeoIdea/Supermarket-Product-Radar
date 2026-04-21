package org.sni.spr.controller;

import org.sni.spr.model.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CategoryPathBuilder {

    public static String build(List<CategoryNode> categories) {
        if (categories == null || categories.isEmpty()) return null;
        List<String> path = new ArrayList<>();
        CategoryNode node = categories.getFirst();

        while (node != null) {
            path.add(node.getName());
            if (node.getCategories() == null || node.getCategories().isEmpty()) break;
            node = node.getCategories().getFirst();
        }
        Collections.reverse(path);
        return String.join(" > ", path);
    }
}
