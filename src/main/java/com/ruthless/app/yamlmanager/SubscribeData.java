package com.ruthless.app.yamlmanager;

import java.util.Map;

import com.binance.api.client.domain.market.CandlestickInterval;
import com.ruthless.app.strategy.Strategy86Config;

public class SubscribeData {
    // | symbol | interval | config |
    public String symbol;
    public CandlestickInterval interval;

    public Strategy86Config config;

    SubscribeData() {

    }

    SubscribeData(String symbol, CandlestickInterval interval, Strategy86Config config) {
        this.symbol = symbol;
        this.interval = interval;
        this.config = config;
    }
}