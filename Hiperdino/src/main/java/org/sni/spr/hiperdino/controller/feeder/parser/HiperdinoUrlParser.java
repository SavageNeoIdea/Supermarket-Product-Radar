package org.sni.spr.hiperdino.controller.feeder.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HiperdinoUrlParser {
    private static final Pattern pattern = Pattern.compile("https://www\\.hiperdino\\.es/c\\d+/([^/]+)/([^/]+).*\\.html");

    public static List<String> getCategorySubcategoryName(String url){
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()){
            String category = matcher.group(1).replace("-", " ");
            String subCategory = matcher.group(2).replace("-", " ");
            String name = category + " - " + subCategory;
            return new ArrayList<>(List.of(name, category, subCategory));
        }
        return new ArrayList<>();

    }

}
