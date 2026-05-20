package org.sni.businessunit.store.reader;

import java.util.List;
import java.util.Map;

public interface DataReader {
    Map<String, List<String>> readLastDay();
}
