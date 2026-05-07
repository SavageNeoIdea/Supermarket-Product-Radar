import java.util.List;
import java.util.Map;

public class Controller {
    private final Subscriptor subscription;
    private final DataReader eventReader;
    private final Feeder feeder;

    public Controller(Subscriptor subscription, DataReader eventReader, Feeder feeder){
        this.subscription = subscription;
        this.eventReader = eventReader;
        this.feeder = feeder;
    }
    public void init(){
        Map<String, List<String>> rawProducts = eventReader.readLastDay();
        List<Product> products = feeder.process(rawProducts);
    }
}
