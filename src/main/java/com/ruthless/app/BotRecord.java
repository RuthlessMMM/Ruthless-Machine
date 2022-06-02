package com.ruthless.app;

import com.binance.api.client.domain.market.CandlestickInterval;

public class BotRecord {
    public static int modeState = 0, configrationState = 0;
    public static String symbol, intervalString;
    public static CandlestickInterval interval;

    BotRecord() {

    }

    public static void deleteRecord() {
        modeState = 0;
        configrationState = 0;
    }
}
