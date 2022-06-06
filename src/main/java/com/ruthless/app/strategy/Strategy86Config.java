package com.ruthless.app.strategy;

public class Strategy86Config implements StrategyConfig {
    public int SMABarCount = 200,
            ATRBarCount = 25,
            ATRStopLossPercent = 50,
            ATRTakeProfitPercent = 200,
            RSIBarCount = 10,
            RSIoversold = 30,
            RSIoverbought = 70,
            RSILongTakeProfit = 50, // 40
            RSIShortTakeProfit = 50; // 60

    public Strategy86Config() {
    };

    Strategy86Config(int SMABarCount,
            int ATRBarCount,
            int ATRStopLossPercent,
            int ATRTakeProfitPercent,
            int RSIBarCount,
            int RSIoversold,
            int RSIoverbought,
            int RSILongTakeProfit,
            int RSIShortTakeProfit) {
        this.SMABarCount = SMABarCount;
        this.ATRBarCount = ATRBarCount;
        this.ATRStopLossPercent = ATRStopLossPercent;
        this.ATRTakeProfitPercent = ATRTakeProfitPercent;
        this.RSIBarCount = RSIBarCount;
        this.RSIoversold = RSIoversold;
        this.RSIoverbought = RSIoverbought;
        this.RSILongTakeProfit = RSILongTakeProfit;
        this.RSIShortTakeProfit = RSIShortTakeProfit;
    }
}