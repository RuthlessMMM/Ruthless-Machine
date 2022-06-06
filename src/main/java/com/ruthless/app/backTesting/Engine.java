package com.ruthless.app.backTesting;

import java.time.format.DateTimeFormatter;
import java.util.*;

import com.binance.api.client.domain.market.CandlestickInterval;
import com.ruthless.app.strategy.Strategy86;
import com.ruthless.app.strategy.Strategy86Config;
import com.ruthless.app.strategy.StrategyConfig;
import com.ruthless.app.util.*;

import org.ta4j.core.*;

public class Engine {

        public static void main(String args[]) {
                Strategy86Config config = new Strategy86Config();
                BarSeries series = BarSeriesMaker.make("BNBUSDT",
                                CandlestickInterval.FOUR_HOURLY);
                BarSeries backtestingSeries = new BaseBarSeriesBuilder().build();

                for (int i = 0; i < 200; i++) {
                        backtestingSeries.addBar(series.getBar(i));
                }
                Strategy86 s86 = new Strategy86(backtestingSeries, config, null);
                double principal = 1000;

                for (int i = 200; i <= series.getEndIndex(); i++) {
                        backtestingSeries.addBar(series.getBar(i));
                        s86.update(backtestingSeries);
                        System.out.printf("時間：%s, 收盤價：%.2f, SMA: %.2f, RSI: %.2f, ATR: %.2f\n",
                                        DateTimeFormatter.ofPattern("yyyy/MM/dd -hh:mm")
                                                        .format(backtestingSeries.getLastBar()
                                                                        .getEndTime()),
                                        Double.parseDouble(backtestingSeries.getLastBar().getClosePrice().toString()),
                                        s86.get("SMA", null), s86.get("RSI", null), s86.get("ATR", null));

                        // Exit first
                        ArrayList<TradeRecord> exitLong = s86.exitLong();
                        ArrayList<TradeRecord> exitShort = s86.exitShort();

                        if (exitLong.size() != 0)
                                for (int j = 0; j < exitLong.size(); j++) {
                                        System.out.println("CloseLong ==> entryPrice: "
                                                        + exitLong.get(j).getEntryPrice()
                                                        + ", exitPrice: "
                                                        + exitLong.get(j).getExitPrice() + "\n");
                                        principal = exitLong.get(j).getExitPrice() * (principal /
                                                        exitLong.get(j).getEntryPrice());
                                }

                        if (exitShort.size() != 0)
                                for (int j = 0; j < exitShort.size(); j++) {
                                        System.out
                                                        .println(
                                                                        "CloseShort==> entryPrice: "
                                                                                        + exitShort.get(j)
                                                                                                        .getEntryPrice()
                                                                                        +
                                                                                        ",exitPrice: "
                                                                                        + exitShort.get(j)
                                                                                                        .getExitPrice()
                                                                                        + "\n");
                                        principal = 2 * principal
                                                        - exitShort.get(j).getExitPrice() * (principal /
                                                                        exitShort.get(j).getEntryPrice());
                                }
                        TradeRecord entryLong = s86.entryLong();
                        TradeRecord entryShort = s86.entryShort();
                        if (entryLong.getType() != "none")
                                System.out.println("entryLong: " + entryLong.getEntryPrice() + ", 本金：" +
                                                principal);
                        if (entryShort.getType() != "none")
                                System.out.println("entryShort! price: " + entryShort.getEntryPrice() +
                                                ",本金：" + principal);
                }

                ArrayList<TradeRecord> closeAccount = s86.closeAccount();
                int openingTrade = 0;
                for (TradeRecord r : closeAccount)
                        if (r.isClosed())
                                openingTrade++;
                System.out.printf("總收益率: %.2f %%, 出現 %d 次交易機會", (principal - 1000) / 1000 *
                                100, openingTrade);

        }

        // public static void main(String[] args) {
        // BarSeries series = BarSeriesMaker.make("SANDUSDT",
        // CandlestickInterval.DAILY);
        // Strategy86Config c = new Strategy86Config();
        // Strategy86 s86 = new Strategy86(series, c);
        // Engine e = new Engine(s86, c);
        // }
}
