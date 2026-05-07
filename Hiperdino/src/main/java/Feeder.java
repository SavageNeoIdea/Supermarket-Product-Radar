import java.util.List;
import java.util.Map;

public interface Feeder {
    public List<Product> process(Map<String, List<String>> json);

}
