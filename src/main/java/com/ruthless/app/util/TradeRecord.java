package com.ruthless.app.util;

public class TradeRecord {
    private double entryPrice;
    private double exitPrice;
    private double takeProfitPrice;
    private double stopLossPrice;
    private String type;
    private String entryTime;
    private String exitTime;
    private double earningsYield;
    private boolean isClose = false;

    public TradeRecord(double entryPrice, String entryTime, String type) {
        this.entryPrice = entryPrice;
        this.entryTime = entryTime;
        this.type = type;
    }

    public TradeRecord(String type) {
        this.type = type;
    }

    public double getEntryPrice() {
        return entryPrice;
    }

    public double getTakeProfitPrice() {
        return takeProfitPrice;
    }

    public double getStopLossPrice() {
        return stopLossPrice;
    }

    public double getExitPrice() {
        return exitPrice;
    }

    public void setEntryPrice(double entryPrice) {
        this.entryPrice = entryPrice;
    }

    public void setTakeProfitPrice(double takeProfitPrice) {
        this.takeProfitPrice = takeProfitPrice;
    }

    public void setStopLossPrice(double stopLossPrice) {
        this.stopLossPrice = stopLossPrice;
    }

    public void setExitPrice(double exitPrice) {
        this.exitPrice = exitPrice;
    }

    public void setEntryTime(String entryTime) {
        this.entryTime = entryTime;
    }

    public void setExitTime(String exitTime) {
        this.exitTime = exitTime;
    }

    public void setEarningsYield(double earningsYield) {
        this.earningsYield = earningsYield;
    }

    public String getEntryTime() {
        return this.entryTime;
    }

    public String getExitTime() {
        return this.exitTime;
    }

    public double getEarningsYield() {
        return this.earningsYield;
    }

    public void closePosition() {
        this.isClose = true;
    }

    public boolean isClosed() {
        return isClose;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return this.type;
    }
}
