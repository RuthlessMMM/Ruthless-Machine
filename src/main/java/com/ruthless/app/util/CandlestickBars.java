package com.ruthless.app.util;

import java.util.*;

import com.binance.api.client.*;
import com.binance.api.client.domain.market.*;

import com.google.gson.*;

public class CandlestickBars {
    private String symbol;
    private BinanceApiClientFactory factory;
    private BinanceApiRestClient client;

    CandlestickBars(String symbol) {
        this.symbol = symbol;
        this.factory = BinanceApiClientFactory.newInstance();
        this.client = factory.newRestClient();
    }

    // symbol need to be capitalized
    public List<Candlestick> getCandlestickBars(CandlestickInterval tickInterval) {
        return this.client.getCandlestickBars(symbol, tickInterval);
    }

    public static void main(String[] args) {
        CandlestickBars currencyPair = new CandlestickBars("BTCUSDT");
        Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
        List<Candlestick> candlesticks15min = currencyPair.getCandlestickBars(CandlestickInterval.HOURLY);
        System.out.println(gson.toJson(candlesticks15min));
        System.out.println("length: " + candlesticks15min.size());
    }
}
