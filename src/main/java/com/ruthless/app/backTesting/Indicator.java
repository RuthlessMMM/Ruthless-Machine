package com.ruthless.app.backTesting;

import org.ta4j.core.*;
import org.ta4j.core.indicators.*;
import org.ta4j.core.indicators.helpers.*;

import com.binance.api.client.domain.market.*;
import com.ruthless.app.util.BarSeriesMaker;

public class Indicator {
    public static void main(String[] args) {

        BarSeries series = BarSeriesMaker.make("BTCUSDT", CandlestickInterval.HOURLY);

        // use the bar of index 498 which had become pass, 499 is still happening
        // Bar lastClosePrice = series.getBar(499);

        // Using ClosePrice to calculate the indicators
        ClosePriceIndicator closePrices = new ClosePriceIndicator(series);
        SMAIndicator sma200 = new SMAIndicator(closePrices, 200);
        RSIIndicator rsi10 = new RSIIndicator(closePrices, 10);
        System.out.println("ma200: " + sma200.getValue(499));
        System.out.println("rsi10: " + rsi10.getValue(498));
        // CrossedDownIndicatorRule
    }
}
