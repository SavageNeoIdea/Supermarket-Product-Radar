package org.sni.spr.hiperdino;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UrlParser {

    public static List<String> getFormatedData(Matcher matcher, String url) {
        if (matcher.find()){
            String category = matcher.group(1).replace("-", " ");
            String subCategory = matcher.group(2).replace("-", " ");
            String name = category + " - " + subCategory;
            return new ArrayList<>(List.of(name, url, category, subCategory));
        }
        return new ArrayList<>();
    }

    public static Pattern initPattern(){
        return Pattern.compile("https://www\\.hiperdino\\.es/c\\d+/([^/]+)/([^/]+).*\\.html");
    }
}
