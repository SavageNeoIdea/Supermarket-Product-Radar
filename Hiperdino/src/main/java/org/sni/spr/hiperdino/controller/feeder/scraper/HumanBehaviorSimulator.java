package org.sni.spr.hiperdino.controller.feeder.scraper;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;

import java.util.concurrent.ThreadLocalRandom;

public class HumanBehaviorSimulator {

    public void simulateHumanScroll(Page page) {
        int totalScroll = ThreadLocalRandom.current().nextInt(400, 1201);
        int currentScroll = 0;

        while (currentScroll < totalScroll) {
            int step = ThreadLocalRandom.current().nextInt(50, 151);
            page.mouse().wheel(0, step);
            currentScroll += step;
            page.waitForTimeout(ThreadLocalRandom.current().nextInt(50, 151));
        }
        stochasticWait(page);
    }

    private void stochasticWait(Page page) {
        page.waitForLoadState(LoadState.NETWORKIDLE);
        if (ThreadLocalRandom.current().nextDouble() > 0.7) {
            long waitTime = ThreadLocalRandom.current().nextLong(5000, 12001);
            try {
                Thread.sleep(waitTime);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}