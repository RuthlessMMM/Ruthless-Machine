package com.ruthless.app.strategy;

import java.time.format.DateTimeFormatter;
import java.util.*;

import com.binance.api.client.domain.market.CandlestickInterval;
import com.ruthless.app.util.*;

import org.ta4j.core.*;
import org.ta4j.core.indicators.*;

public class Strategy86 extends Strategy {
    // Strategy 86 發又發戰法(86)

    ////////////////////////////////////////////////////////////////////////
    // Var extends form Strategy Super Class
    // protected BarSeries series;
    // protected int endIndex;
    // protected ClosePriceIndicator closePrices;
    // protected double closePrice;
    // protected ArrayList<TradeRecord> positionsHistory = new ArrayList<>();
    ///////////////////////////////////////////////////////////////////////

    private Strategy86Config config;
    private SMAIndicator sma;
    private RSIIndicator rsi;
    private ATRIndicator atr;
    private double smaValue, rsiValue, atrValue;

    public Strategy86(BarSeries series, Strategy86Config config, ArrayList<TradeRecord> positionsHistory) {
        super(series, positionsHistory);
        this.config = config;
        super.update(series);
    }

    public void update(BarSeries series) {
        super.update(series);
    }

    public void updateIndicators() {
        this.sma = new SMAIndicator(closePrices, this.config.SMABarCount);
        this.rsi = new RSIIndicator(closePrices, this.config.RSIBarCount);
        this.atr = new ATRIndicator(series, this.config.ATRBarCount);
        this.smaValue = Double.parseDouble(this.sma.getValue(this.endIndex).toString());
        this.rsiValue = Double.parseDouble(this.rsi.getValue(this.endIndex).toString());
        this.atrValue = Double.parseDouble(this.atr.getValue(this.endIndex).toString());
    }

    public Double get(String indicatorName, Integer index) {
        if (index == null || index < 0 || index >= this.endIndex)
            index = this.endIndex;
        if (indicatorName.equals("SMA"))
            return Double.parseDouble(sma.getValue(this.endIndex).toString());
        else if (indicatorName.equals("RSI"))
            return Double.parseDouble(rsi.getValue(this.endIndex).toString());
        else if (indicatorName.equals("ATR"))
            return Double.parseDouble(atr.getValue(this.endIndex).toString());

        return null;
    }

    public TradeRecord entryLong() {

        for (TradeRecord r : positionsHistory)
            if (!r.isClosed())
                return new TradeRecord("none");
        if (closePrice > this.smaValue
                && this.rsiValue < this.config.RSIoversold) {
            positionsHistory.add(new TradeRecord(closePrice,
                    closeTime, "long"));
            return this.positionsHistory.get(positionsHistory.size() - 1);
            // last one
        } else
            return new TradeRecord("none");
    }

    public TradeRecord entryShort() {
        for (TradeRecord r : positionsHistory)
            if (!r.isClosed())
                return new TradeRecord("none");
        if (closePrice < this.smaValue
                && this.rsiValue > this.config.RSIoverbought) {
            positionsHistory.add(new TradeRecord(closePrice, closeTime, "short"));
            return positionsHistory.get(positionsHistory.size() - 1);
            // last one
        } else
            return new TradeRecord("none");
    }

    public ArrayList<TradeRecord> exitShort() {
        ArrayList<TradeRecord> closePositions = new ArrayList<TradeRecord>();
        for (TradeRecord r : positionsHistory) {
            if (!r.isClosed() && r.getType().equals("short")) {
                double takeProfitPrice = r.getEntryPrice() - this.config.ATRTakeProfitPercent * atrValue / 100;
                double stopLossPrice = r.getEntryPrice() + this.config.ATRStopLossPercent * atrValue / 100;
                if (closePrice < takeProfitPrice || // take profit
                        this.rsiValue < this.config.RSIShortTakeProfit || // take profit: RSI
                        closePrice > stopLossPrice) { // stop loss
                    r.setExitPrice(closePrice);
                    r.setStopLossPrice(stopLossPrice);
                    r.setTakeProfitPrice(takeProfitPrice);
                    r.setExitTime(closeTime);
                    r.setEarningsYield((r.getEntryPrice() - closePrice) / r.getEntryPrice());
                    r.closePosition();
                    closePositions.add(r);
                }
            }
        }
        return closePositions;
    }

    public ArrayList<TradeRecord> exitLong() {

        // System.out.printf(
        // "config ATRStopLossPercent: %d ,ATRTakeProfitPercent: %d, RSIoversold: %d,
        // \nRSIoverbought: %d, RSILongTakeProfit: %d, RSIShortTakeProfit: %d\n",
        // this.config.ATRStopLossPercent, this.config.ATRTakeProfitPercent,
        // this.config.RSIoversold,
        // this.config.RSIoverbought, this.config.RSILongTakeProfit,
        // this.config.RSIShortTakeProfit);
        // System.out.printf("時間：%s, 收盤價：%.2f, SMA: %.2f, RSI: %.2f, ATR: %.2f\n",
        // DateTimeFormatter.ofPattern("yyyy/MM/dd
        // hh:mm").format(this.series.getLastBar().getEndTime()),
        // Double.parseDouble(this.series.getLastBar().getClosePrice().toString()),
        // this.get("SMA", null),
        // this.get("RSI", null), this.get("ATR", null));

        ArrayList<TradeRecord> closePositions = new ArrayList<TradeRecord>();

        for (TradeRecord r : positionsHistory) {
            if (!r.isClosed() && r.getType().equals("long")) {
                double takeProfitPrice = r.getEntryPrice() + this.config.ATRTakeProfitPercent * atrValue / 100;
                double stopLossPrice = r.getEntryPrice() - this.config.ATRStopLossPercent * atrValue / 100;
                if (closePrice > takeProfitPrice || // take profit
                        this.rsiValue > this.config.RSILongTakeProfit || // take profit: RSI
                        closePrice < stopLossPrice) { // stop loss
                    r.setExitPrice(closePrice);
                    r.setStopLossPrice(stopLossPrice);
                    r.setTakeProfitPrice(takeProfitPrice);
                    r.setExitTime(closeTime);
                    r.setEarningsYield((closePrice - r.getEntryPrice()) / r.getEntryPrice());
                    r.closePosition();
                    closePositions.add(r);
                }
            }
        }
        return closePositions;
    }

    public ArrayList<TradeRecord> backTesting() {

        BarSeries backtestingSeries = new BaseBarSeriesBuilder().build();

        for (int i = 0; i < 200; i++) {
            backtestingSeries.addBar(series.getBar(i));
        }
        Strategy86 s86 = new Strategy86(backtestingSeries, config, null);

        for (int i = 200; i <= series.getEndIndex(); i++) {
            backtestingSeries.addBar(series.getBar(i));
            s86.update(backtestingSeries);
            /*
             * System.out.printf("時間：%s, 收盤價：%.2f, SMA: %.2f, RSI: %.2f, ATR: %.2f\n",
             * DateTimeFormatter.ofPattern("yyyy/MM/dd hh:mm").format(backtestingSeries.
             * getLastBar()
             * .getEndTime()),
             * Double.parseDouble(backtestingSeries.getLastBar().getClosePrice().toString())
             * ,
             * s86.get("SMA", null), s86.get("RSI", null), s86.get("ATR", null));
             */
            // Exit first
            s86.exitLong();
            s86.exitShort();
            s86.entryLong();
            s86.entryShort();
        }

        return s86.closeAccount();

    }

    public static void main(String[] args) {
        BarSeries series = BarSeriesMaker.make("BTCUSDT",
                CandlestickInterval.DAILY);
        Strategy86Config config = new Strategy86Config();
        Strategy86 s86 = new Strategy86(series, config, null);
        ArrayList<TradeRecord> history = s86.backTesting();
        int openingTrade = 0;
        double principal = 1000;
        for (TradeRecord r : history)
            if (r.isClosed()) {
                openingTrade++;
                principal *= 1 + r.getEarningsYield();
            }

        System.out.printf("總收益率: %.2f %%, 出現 %d 次交易機會", (principal - 1000) / 1000 *
                100, openingTrade);
    }
}
