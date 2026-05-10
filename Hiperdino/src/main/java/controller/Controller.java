package controller;
import model.Product;
import controller.reader.DataReader;
import controller.feeder.Feeder;
import controller.store.activemq.Subscriptor;

import java.util.List;
import java.util.Map;

public class Controller {
    private final DataReader eventReader;
    private final Feeder feeder;

    public Controller(DataReader eventReader, Feeder feeder){
        this.eventReader = eventReader;
        this.feeder = feeder;
    }

    public void init(){
        Map<String, List<String>> rawProducts = eventReader.readLastDay();
        List<Product> products = feeder.processData(rawProducts);
        System.out.println(products);
    }
}
