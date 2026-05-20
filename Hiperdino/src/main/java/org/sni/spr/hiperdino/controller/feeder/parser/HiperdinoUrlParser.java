package org.sni.spr.hiperdino.controller.feeder.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HiperdinoUrlParser {
    private static final Pattern pattern = Pattern.compile("https://www\\.hiperdino\\.es/c\\d+/([^/]+)/([^/]+).*\\.html");

    public static String getCategory(String url) {
        return getCategorySubcategory(url).getFirst();
    }

    public static String getSubcategory(String url) {
        return getCategorySubcategory(url).getLast();
    }

    public static List<String> getCategorySubcategory(String url) {
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            String category = matcher.group(1).replace("-", " ");
            String subCategory = matcher.group(2).replace("-", " ");
            return new ArrayList<>(List.of(category, subCategory));
        }
        return new ArrayList<>();
    }
}
