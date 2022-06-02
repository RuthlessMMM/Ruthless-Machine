package com.ruthless.app;

import java.util.ArrayList;
import java.util.Arrays;
import com.binance.api.client.domain.market.CandlestickInterval;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.request.*;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import com.ruthless.app.cron.Cron;
import com.ruthless.app.property.Reader;
import com.ruthless.app.strategy.Strategy86Config;
import com.ruthless.app.util.BarSeriesMaker;
import com.ruthless.app.yamlmanager.UserData;
import com.ruthless.app.yamlmanager.YamlManager;

import org.ta4j.core.BarSeries;

public class Bot {

    public static void main(String[] args) throws Exception {
        Reader r = new Reader("telegramConfig.properties");
        String token = r.getProperty("telegarm.token");
        TelegramBot bot = new TelegramBot(token);
        Cron.init();

        // System.out.println("token: " + token);
        bot.setUpdatesListener(updates -> {
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
            } else if (userInput.equals("顯示紀錄")) {
                BotRecord.modeState = 3;
                BotRecord.configrationState = 1;
            } else if (userInput.equals("回測")) {
                BotRecord.modeState = 4;
                BotRecord.configrationState = 1;
            } else if (userInput.equals("刪除排程")) {
                BotRecord.modeState = 5;
                BotRecord.configrationState = 1;
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
                                manager.updateSubscription(String.valueOf(chatId), BotRecord.interval, BotRecord.symbol,
                                        new Strategy86Config());
                            } else {
                                manager.addUser(String.valueOf(chatId), username);
                                manager.updateSubscription(String.valueOf(chatId), BotRecord.interval, BotRecord.symbol,
                                        new Strategy86Config());
                            }
                            // cron
                            try {
                                Cron.schedule(String.valueOf(chatId));
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
                }
            }
            System.out.println(BotRecord.configrationState);
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
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }
}
