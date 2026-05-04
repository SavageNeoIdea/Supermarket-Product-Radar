package org.sni.spr.hiperdino.controller.store;

import org.sni.spr.hiperdino.model.HiperdinoProduct;

import javax.jms.JMSException;
import java.util.List;
import java.util.Map;

public interface Store {
    public void storeAllData(List<HiperdinoProduct> productList) throws JMSException;
}
