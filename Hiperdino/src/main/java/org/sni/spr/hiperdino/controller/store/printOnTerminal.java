package org.sni.spr.hiperdino.controller.store;

import org.sni.spr.hiperdino.model.HiperdinoProduct;

import java.util.List;

public class printOnTerminal implements Store{

    @Override
    public void storeAllData(List<HiperdinoProduct> productList) {
        for (HiperdinoProduct product : productList){
            System.out.println(product);
        }
    }
}
