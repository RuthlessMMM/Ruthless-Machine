package com.ruthless.app;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.binance.api.client.domain.market.CandlestickInterval;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.request.*;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import com.ruthless.app.cron.Cron;
import com.ruthless.app.property.Reader;
import com.ruthless.app.strategy.Strategy86;
import com.ruthless.app.strategy.Strategy86Config;
import com.ruthless.app.util.BarSeriesMaker;
import com.ruthless.app.util.TradeRecord;
import com.ruthless.app.yamlmanager.SubscribeData;
import com.ruthless.app.yamlmanager.UserData;
import com.ruthless.app.yamlmanager.YamlManager;

import org.ta4j.core.BarSeries;

public class Bot {

    public static void main(String[] args) throws Exception {
        // Run all Crons which are stored in the resources folder

        Reader r = new Reader("telegramConfig.properties");
        String token = r.getProperty("telegarm.token");
        TelegramBot bot = new TelegramBot(token);
        Cron.init();
        runAllCrons();
        bot.setUpdatesListener(updates -> {
            try {
                Long chatId = updates.get(0).message().chat().id();
                String userInput = updates.get(0).message().text();
                String username = updates.get(0).message().chat().username();
                SendMessage request = new SendMessage(chatId, userInput);
                if (userInput.equals("取消")) {
                    BotRecord.modeState = 0;
                    BotRecord.configrationState = 0;
                } else if (userInput.equals("排程策略")) {
                    BotRecord.modeState = 1;
                    BotRecord.configrationState = 1;
                    request = new SendMessage(chatId, "請輸入品種代號 (ex: BTCUSDT)：")
                            .parseMode(ParseMode.HTML);
                } else if (userInput.equals("修改參數")) {
                    BotRecord.modeState = 2;
                    BotRecord.configrationState = 1;
                    String message = showAllCrons(String.valueOf(chatId));
                    request = new SendMessage(chatId, message);
                } else if (userInput.equals("顯示紀錄")) {
                    BotRecord.modeState = 3;
                    BotRecord.configrationState = 1;
                    String message = showAllCrons(String.valueOf(chatId));
                    request = new SendMessage(chatId, message);
                } else if (userInput.equals("回測")) {
                    BotRecord.modeState = 4;
                    BotRecord.configrationState = 1;
                    String message = showAllCrons(String.valueOf(chatId));
                    request = new SendMessage(chatId, message);
                } else if (userInput.equals("刪除排程")) {
                    BotRecord.modeState = 5;
                    BotRecord.configrationState = 1;
                    String message = showAllCrons(String.valueOf(chatId));
                    request = new SendMessage(chatId, message);
                } else {
                    if (BotRecord.modeState == 1) {
                        if (BotRecord.configrationState == 1) {
                            boolean hasSymbol = true;
                            try {
                                BarSeries series = BarSeriesMaker.make(
                                        userInput.toUpperCase(),
                                        CandlestickInterval.FOUR_HOURLY);
                                if (!series.isEmpty()) {
                                    BotRecord.symbol = userInput.toUpperCase();
                                    BotRecord.configrationState = 2;
                                    Keyboard replyKeyboardMarkup = new ReplyKeyboardMarkup(
                                            new String[] { "Fifteen minutes", "Hourly" },
                                            new String[] { "Four hourly", "Daily" })
                                            .oneTimeKeyboard(true) // optional
                                            .resizeKeyboard(true) // optional
                                            .selective(true); // optional
                                    request = new SendMessage(chatId, "請選擇時間級別")
                                            .parseMode(ParseMode.HTML)
                                            .replyMarkup(replyKeyboardMarkup);
                                } else {
                                    hasSymbol = false;
                                }
                            } catch (Exception e) {
                                System.out.println("ERROR:" + e.getMessage());
                                hasSymbol = false;
                            }
                            if (!hasSymbol) {
                                request = new SendMessage(chatId, "查無代號，請再輸入一次 (ex: BTCUSDT)，或輸入取消：")
                                        .parseMode(ParseMode.HTML);
                            }
                        } else if (BotRecord.configrationState == 2) {
                            ArrayList<String> timeUnits = new ArrayList<String>(
                                    Arrays.asList("Fifteen minutes", "Hourly", "Four hourly", "Daily"));
                            ArrayList<CandlestickInterval> timeIntervals = new ArrayList<CandlestickInterval>(
                                    Arrays.asList(CandlestickInterval.FIFTEEN_MINUTES,
                                            CandlestickInterval.HOURLY, CandlestickInterval.FOUR_HOURLY,
                                            CandlestickInterval.DAILY));
                            if (timeUnits.contains(userInput)) {
                                BotRecord.interval = timeIntervals.get(timeUnits.indexOf(userInput));
                                BotRecord.intervalString = userInput;
                                YamlManager manager = new YamlManager();
                                UserData data = manager.read(String.valueOf(chatId));
                                if (data != null) {
                                    manager.addSubscription(String.valueOf(chatId), BotRecord.interval,
                                            BotRecord.symbol,
                                            new Strategy86Config());
                                } else {
                                    manager.addUser(String.valueOf(chatId), username);
                                    manager.addSubscription(String.valueOf(chatId), BotRecord.interval,
                                            BotRecord.symbol,
                                            new Strategy86Config());
                                }
                                data = manager.read(String.valueOf(chatId));
                                // cron
                                try {
                                    Cron.schedule(String.valueOf(chatId), data.subscriptions.size() - 1);
                                    request = new SendMessage(chatId, "排程成功！");
                                } catch (Exception e) {
                                    System.out.println(e);
                                    request = new SendMessage(chatId, "排程失敗🥺");
                                }
                            } else {
                                request = new SendMessage(chatId, "請按下方鍵盤選擇時間級別，或者輸入取消！");
                            }
                            SendResponse sendResponse = bot.execute(request);
                            BotRecord.modeState = 0;
                            BotRecord.configrationState = 0;
                            // successfully
                            // update yaml
                            // call cron
                        }
                    } else if (BotRecord.modeState == 2) {
                        YamlManager manager = new YamlManager();
                        UserData data = manager.read(String.valueOf(chatId));
                        if (BotRecord.configrationState == 2) {
                            if (BotRecord.cronedStrategy.contains(userInput)) {
                                boolean successfullyEdit = false;
                                for (SubscribeData item : data.subscriptions) {
                                    if (userInput.equals(
                                            item.symbol + String.valueOf(item.interval))) {
                                        successfullyEdit = true;
                                        BotRecord.configrationState = 3;
                                        BotRecord.subscriptionsId = userInput;
                                        BotRecord.symbol = item.symbol;
                                        BotRecord.interval = item.interval;
                                        BotRecord.cronedStrategy = new ArrayList<String>();
                                        request = new SendMessage(chatId, "請輸入 SMA 指標參數：");
                                    }
                                }
                                if (!successfullyEdit) {
                                    request = new SendMessage(chatId, "找不到這個排程喔！請在輸入一次：");
                                }
                            } else {
                                request = new SendMessage(chatId, "找不到這個排程喔！請在輸入一次：");
                            }
                        } else if (BotRecord.configrationState == 3) {
                            request = new SendMessage(chatId,
                                    updateTechnicalIndicator(data, String.valueOf(chatId),
                                            userInput, "SMABarCount",
                                            "ATR 指標參數", 4));
                        } else if (BotRecord.configrationState == 4) {
                            request = new SendMessage(chatId,
                                    updateTechnicalIndicator(data, String.valueOf(chatId),
                                            userInput, "ATRBarCount",
                                            "ATR 止損百分比", 5));
                        } else if (BotRecord.configrationState == 5) {
                            request = new SendMessage(chatId,
                                    updateTechnicalIndicator(data, String.valueOf(chatId),
                                            userInput, "ATRStopLossPercent",
                                            "ATR 止贏百分比", 6));
                        } else if (BotRecord.configrationState == 6) {
                            request = new SendMessage(chatId,
                                    updateTechnicalIndicator(data, String.valueOf(chatId),
                                            userInput, "ATRTakeProfitPercent",
                                            "RSI 指標參數", 7));
                        } else if (BotRecord.configrationState == 7) {
                            request = new SendMessage(chatId,
                                    updateTechnicalIndicator(data, String.valueOf(chatId),
                                            userInput, "RSIBarCount",
                                            "RSI 超賣界線", 8));
                        } else if (BotRecord.configrationState == 8) {
                            request = new SendMessage(chatId,
                                    updateTechnicalIndicator(data, String.valueOf(chatId),
                                            userInput, "RSIoversold",
                                            "RSI 超買界線", 9));
                        } else if (BotRecord.configrationState == 9) {
                            request = new SendMessage(chatId,
                                    updateTechnicalIndicator(data, String.valueOf(chatId),
                                            userInput, "RSIoverbought",
                                            "RSI 做多止盈界線", 10));
                        } else if (BotRecord.configrationState == 10) {
                            request = new SendMessage(chatId,
                                    updateTechnicalIndicator(data, String.valueOf(chatId),
                                            userInput, "RSILongTakeProfit",
                                            "RSI 做空止盈界線", 11));
                        } else if (BotRecord.configrationState == 11) {
                            request = new SendMessage(chatId,
                                    updateTechnicalIndicator(data, String.valueOf(chatId),
                                            userInput, "RSIShortTakeProfit",
                                            null, 12));
                            BotRecord.configrationState = 0;
                            BotRecord.modeState = 0;
                            SendResponse sendResponse = bot.execute(request);
                        }
                    } else if (BotRecord.modeState == 3) {
                        // 顯示紀錄
                        if (BotRecord.configrationState == 2) {
                            YamlManager manager = new YamlManager();
                            UserData data = manager.read(String.valueOf(chatId));
                            if (BotRecord.cronedStrategy.contains(userInput)) {
                                boolean successfullyEdit = false;
                                for (SubscribeData item : data.subscriptions) {
                                    if (userInput.equals(
                                            item.symbol + String.valueOf(item.interval))) {
                                        if (data.allPositionsHistory.containsKey(userInput)) {
                                            BotRecord.configrationState = 0;
                                            BotRecord.modeState = 0;
                                            BotRecord.cronedStrategy = new ArrayList<String>();
                                            String message = "";
                                            Map<String, String> typeDict = Map.of(
                                                    "short", "做空",
                                                    "long", "做多",
                                                    "true", "開倉",
                                                    "false", "平倉");
                                            for (TradeRecord record : data.allPositionsHistory.get(userInput)) {
                                                message += "開倉時間：" + record.getEntryTime() + " "
                                                        + typeDict.get(record.getType()) + "，開倉價格："
                                                        + record.getEntryPrice()
                                                        + "\n";
                                                if (record.getExitTime() != null) {
                                                    message += "平倉時間：" + record.getExitTime() + "平倉價格"
                                                            + record.getExitPrice() + "收益率：" + record.getEarningsYield()
                                                            + "\n";
                                                }
                                                message += "\n";
                                            }
                                            request = new SendMessage(chatId, message);
                                            successfullyEdit = true;
                                            SendResponse sendResponse = bot.execute(request);
                                            break;
                                        } else {

                                        }
                                    }
                                }
                                if (!successfullyEdit) {
                                    request = new SendMessage(chatId, "這個項目還沒有交易紀錄喔！");
                                }
                            } else {
                                request = new SendMessage(chatId, "找不到這個排程喔！請在輸入一次：");
                            }
                        }
                    } else if (BotRecord.modeState == 4) {
                        // 回測
                        if (BotRecord.configrationState == 2) {
                            YamlManager manager = new YamlManager();
                            UserData data = manager.read(String.valueOf(chatId));
                            if (BotRecord.cronedStrategy.contains(userInput)) {
                                for (SubscribeData item : data.subscriptions) {
                                    if (userInput.equals(
                                            item.symbol + String.valueOf(item.interval))) {
                                        BotRecord.cronedStrategy = new ArrayList<String>();
                                        BarSeries series = BarSeriesMaker.make(item.symbol,
                                                item.interval);
                                        Strategy86Config config = item.config;
                                        Strategy86 s86 = new Strategy86(series, config, null);
                                        ArrayList<TradeRecord> history = s86.backTesting();
                                        int openingTrade = 0;
                                        double principal = 1000;
                                        for (TradeRecord record : history)
                                            if (record.isClosed()) {
                                                openingTrade++;
                                                principal *= 1 + record.getEarningsYield();
                                            }
                                        request = new SendMessage(chatId,
                                                String.format("總收益率: %.2f %%, 出現 %d 次交易機會", (principal - 1000) / 1000 *
                                                        100, openingTrade));

                                        BotRecord.configrationState = 0;
                                        BotRecord.modeState = 0;
                                        SendResponse sendResponse = bot.execute(request);
                                    }
                                }
                            } else {
                                request = new SendMessage(chatId, "找不到這個排程喔！請在輸入一次：");
                            }
                        }
                    } else if (BotRecord.modeState == 5) {
                        if (BotRecord.configrationState == 2) {
                            YamlManager manager = new YamlManager();
                            UserData data = manager.read(String.valueOf(chatId));
                            if (BotRecord.cronedStrategy.contains(userInput)) {
                                for (SubscribeData item : data.subscriptions) {
                                    if (userInput.equals(
                                            item.symbol + String.valueOf(item.interval))) {
                                        BotRecord.cronedStrategy = new ArrayList<String>();
                                        try {
                                            Cron.deleteScheduledJob(item.symbol + String.valueOf(item.interval),
                                                    String.valueOf(chatId));
                                            request = new SendMessage(chatId, "刪除成功！");
                                        } catch (Exception e) {
                                            // TODO Auto-generated catch block
                                            System.out.println(e.getMessage());
                                            request = new SendMessage(chatId, "刪除失敗！");
                                        }
                                        BotRecord.configrationState = 0;
                                        BotRecord.modeState = 0;
                                        SendResponse sendResponse = bot.execute(request);
                                        break;
                                    }
                                }
                            } else {
                                request = new SendMessage(chatId, "找不到這個排程喔！請在輸入一次：");
                            }
                        }
                    }
                }
                if (BotRecord.modeState == 0 &&
                        BotRecord.configrationState == 0) {
                    Keyboard replyKeyboardMarkup = new ReplyKeyboardMarkup(
                            new String[] { "排程策略", "修改參數" },
                            new String[] { "顯示紀錄", "回測", "刪除排程" })
                            .oneTimeKeyboard(true) // optional
                            .resizeKeyboard(true) // optional
                            .selective(true); // optional
                    request = new SendMessage(chatId, "<b>嗨👋</b> 今天開心嗎？")
                            .parseMode(ParseMode.HTML)
                            .replyMarkup(replyKeyboardMarkup);
                }

                System.out.println(updates.get(0).message().chat().id());
                System.out.println("中華路二段99號收到：" + updates.get(0).message().text());
                SendResponse sendResponse = bot.execute(request);
                boolean ok = sendResponse.isOk();
                // Message message = sendResponse.message();

                if (ok) {
                    System.out.println("成功！");
                } else {
                    System.out.println("失敗！");
                }

                // return id of last processed update or confirm them all
            } catch (Exception e) {
                System.out.println("e:" + e.getMessage());
            }
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }

    public static String updateTechnicalIndicator(UserData data, String chatId, String value, String fieldName,
            String nextFieldName, int nextConfigrationState) {
        String message = "";
        try {
            YamlManager manager = new YamlManager();
            Strategy86Config config;
            int valueInt = Integer.parseInt(value);
            if (data != null && data.subscriptions != null) {
                for (SubscribeData item : data.subscriptions) {
                    if (BotRecord.subscriptionsId.equals(
                            item.symbol + String.valueOf(item.interval))) {
                        config = item.config;
                        if (fieldName.equals("SMABarCount"))
                            config.SMABarCount = valueInt;
                        else if (fieldName.equals("ATRBarCount"))
                            config.ATRBarCount = valueInt;
                        else if (fieldName.equals("ATRStopLossPercent"))
                            config.ATRStopLossPercent = valueInt;
                        else if (fieldName.equals("ATRTakeProfitPercent"))
                            config.ATRTakeProfitPercent = valueInt;
                        else if (fieldName.equals("RSIBarCount"))
                            config.RSIBarCount = valueInt;
                        else if (fieldName.equals("RSIoversold"))
                            config.RSIoversold = valueInt;
                        else if (fieldName.equals("RSIoverbought"))
                            config.RSIoverbought = valueInt;
                        else if (fieldName.equals("RSILongTakeProfit"))
                            config.RSILongTakeProfit = valueInt;
                        else if (fieldName.equals("RSIShortTakeProfit"))
                            config.RSIShortTakeProfit = valueInt;
                        manager.updateSubscription(String.valueOf(chatId), BotRecord.interval,
                                BotRecord.symbol,
                                config);
                        if (nextFieldName != null)
                            message = "更新成功，請輸入 " + nextFieldName + "：";
                        else
                            message = "全部更新成功";
                        BotRecord.configrationState = nextConfigrationState;
                    }
                }
            }
        } catch (NumberFormatException e) {
            message = "請輸入正整數喔！請在輸入一次，或是取消：";
        }
        return message;
    }

    public static void runAllCrons() throws Exception {
        YamlManager manager = new YamlManager();
        final File folder = new File("./src/main/resources/");
        for (final File fileEntry : folder.listFiles()) {
            System.out.println(fileEntry.getName());
            if (fileEntry.isFile()) {
                UserData data = manager.read(String.valueOf(fileEntry.getName().split("\\.")[0]));
                data.allPositionsHistory = new HashMap<String, ArrayList<TradeRecord>>();
                manager.dump(data.chatId, data);
                for (int i = 0; i < data.subscriptions.size(); i++) {
                    Cron.schedule(data.chatId, i);
                }
            }
        }
    }

    public static String showAllCrons(String chatId) {
        String message = "";
        YamlManager manager = new YamlManager();
        ArrayList<String> cronedStrategy = new ArrayList<String>();
        UserData data = manager.read(chatId);
        if (data == null || data.subscriptions == null) {
            message = "找不到已排程的策略喔！請先排程策略！";
        } else {
            message = "請輸入以下排程名稱：\n";
            for (SubscribeData item : data.subscriptions) {
                cronedStrategy.add(
                        item.symbol + String.valueOf(item.interval));
                message += cronedStrategy.get(cronedStrategy.size() - 1) + '\n';
            }
            BotRecord.configrationState = 2;
            BotRecord.cronedStrategy = cronedStrategy;
        }
        return message;
    }
}
