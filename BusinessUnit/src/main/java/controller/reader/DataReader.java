package controller.reader;

import model.Product;

import java.util.List;
import java.util.Map;

public interface DataReader {
    public Map<String, List<String>> readLastDay();
}
