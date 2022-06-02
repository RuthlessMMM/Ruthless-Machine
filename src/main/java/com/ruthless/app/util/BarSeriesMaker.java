package com.ruthless.app.util;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

import org.ta4j.core.*;

import com.binance.api.client.domain.market.*;

public class BarSeriesMaker {

    BarSeriesMaker() {
    }

    // Symbol need to be captalized
    public static BarSeries make(String symbol, CandlestickInterval interval) {
        BarSeries series = new BaseBarSeriesBuilder().withName(symbol).build();
        // Create a Binance Bars Object
        CandlestickBars currencyPair = new CandlestickBars(symbol);
        // Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
        List<Candlestick> candlesticks = new ArrayList<Candlestick>();
        try {
            candlesticks = currencyPair.getCandlestickBars(interval);
        } catch (Exception e) {
            // internet error: NCKU internet not allow traffic from binance may be the
            // reason.
            System.out.println("Error: " + e.getMessage());
        }

        // Remove last tick from candlesticks if needed.
        candlesticks.remove(candlesticks.size() - 1);
        // addBar from binance to ta4j
        for (Candlestick tick : candlesticks) {
            Instant i = Instant.ofEpochSecond(tick.getCloseTime() / 1000);
            series.addBar(ZonedDateTime.ofInstant(i, ZoneId.of("Asia/Taipei")), tick.getOpen(), tick
                    .getHigh(),
                    tick.getLow(), tick.getClose(), tick.getVolume());
        }

        return series;
    }
}
