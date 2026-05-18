package Controller;
import store.Subscriptor;

public class Controller {
    private final Subscriptor suscriptor;

    public Controller(Subscriptor suscriptor) {
        this.suscriptor = suscriptor;
    }

    public void init(){
        suscriptor.start();
    }

}
