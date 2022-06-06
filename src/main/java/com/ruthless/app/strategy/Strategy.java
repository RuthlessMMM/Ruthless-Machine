package com.ruthless.app.strategy;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import com.ruthless.app.util.TradeRecord;

import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

public abstract class Strategy {
    // Strategy super Class
    protected BarSeries series;
    protected int endIndex;
    protected ClosePriceIndicator closePrices;
    protected double closePrice;
    protected String closeTime;

    protected ArrayList<TradeRecord> positionsHistory;
    // 一個 ArrayList 紀錄交易紀錄

    public Strategy(BarSeries series, ArrayList<TradeRecord> positionsHistory) {
        if (positionsHistory != null)
            this.positionsHistory = positionsHistory;
        else
            this.positionsHistory = new ArrayList<TradeRecord>();
    }

    public void update(BarSeries series) {
        this.series = series;
        this.endIndex = series.getEndIndex();
        // use the bar of index 498 which had become pass, 499 is still happening
        // Bar lastClosePrice = series.getBar(498);
        // Using ClosePrice to calculate the indicators
        this.closePrices = new ClosePriceIndicator(series);
        this.closePrice = Double.parseDouble(closePrices.getValue(this.endIndex).toString());
        this.closeTime = DateTimeFormatter.ofPattern("yyyy/MM/dd hh:mm").format(series.getLastBar().getEndTime());
        updateIndicators();
    }

    public ArrayList<TradeRecord> closeAccount() {
        // 結算
        return this.positionsHistory;
    }

    public abstract void updateIndicators();

    public abstract Double get(String indicatorName, Integer index);

    public abstract TradeRecord entryLong();

    public abstract TradeRecord entryShort();

    public abstract ArrayList<TradeRecord> exitShort();

    public abstract ArrayList<TradeRecord> exitLong();
}
