package org.sni.spr;

import java.util.*;


public class Product {

    public int id;
    public String display_name;
    public String thumbnail;
    public List<CategoryNode> categories;
    public PriceInstructions price_instructions;

    public String getFullCategory() {
        if (categories == null || categories.isEmpty()) return null;
        List<String> path = new ArrayList<>();
        CategoryNode node = categories.getFirst();
        while (node != null) {
            path.add(node.name);
            if (node.categories == null || node.categories.isEmpty()) break;
            node = node.categories.getFirst();
        }

        Collections.reverse(path);

        return String.join(" > ", path);
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + display_name + '\'' +
                ", category='" + getFullCategory() + '\'' +
                ", price=" + (price_instructions != null ? price_instructions.unit_price : null) +
                '}';
    }
}


class PriceInstructions {
    public double unit_price;
    public double reference_price;
    public String unit_name;
    public double unit_size;
}

class Category {
    int id;
    String name;
    Integer parentId;

    public Category(int id, String name, Integer parentId) {
        this.id = id;
        this.name = name;
        this.parentId = parentId;
    }
}

class CategoryNode {
    public int id;
    public String name;
    public List<CategoryNode> categories;
}
