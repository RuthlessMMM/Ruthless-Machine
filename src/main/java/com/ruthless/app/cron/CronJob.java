package com.ruthless.app.cron;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.ta4j.core.BarSeries;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.request.*;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import com.ruthless.app.property.Reader;
import com.ruthless.app.strategy.Strategy86;
import com.ruthless.app.strategy.Strategy86Config;
import com.ruthless.app.util.BarSeriesMaker;
import com.ruthless.app.util.TradeRecord;
import com.ruthless.app.yamlmanager.UserData;
import com.ruthless.app.yamlmanager.YamlManager;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class CronJob implements Job {

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        // 就算建立再多的相依類別，也不能重建當年的那段光景
        // 卍無情 • 機器卍
        // 卍無情 • 打扣卍
        // 卍無情 • 拒絕卍
        // 卍無情 • 冷漠卍
        // 乂愛情 • 夢靨乂
        // 現在的我 已看清
        JobDataMap map = jobExecutionContext.getMergedJobDataMap();
        System.out.println("定時器任務執行，chatId: " + map.get("chatId"));

        String chatId = map.get("chatId").toString();
        int index = (Integer) map.get("subscriptionsIndex");

        String notificationMessage = seizeChance(chatId, index);

        if (!notificationMessage.equals("")) {
            System.out.println(notificationMessage);
            Reader r = new Reader("telegramConfig.properties");
            String token = r.getProperty("telegarm.token");
            TelegramBot bot = new TelegramBot(token);
            SendMessage request = new SendMessage(chatId,
                    notificationMessage)
                    .parseMode(ParseMode.HTML);

            // sync
            SendResponse sendResponse = bot.execute(request);
            boolean ok = sendResponse.isOk();
            Message message = sendResponse.message();

            if (ok) {
                System.out.println("成功！");
            } else {
                System.out.println("失敗！");
            }
        }
    }

    public String seizeChance(String chatId, int index) {
        DecimalFormat df = new DecimalFormat(".00");
        YamlManager manager = new YamlManager();
        UserData data = manager.read(chatId);
        BarSeries series = BarSeriesMaker.make(data.subscriptions.get(
                index).symbol,
                data.subscriptions.get(
                        index).interval);
        Strategy86Config config = data.subscriptions.get(index).config;
        String positionId = data.subscriptions.get(
                index).symbol +
                data.subscriptions.get(
                        index).interval.toString();
        System.out.println("positionId: " + positionId);
        ArrayList<TradeRecord> positionsHistory;
        if (data.allPositionsHistory.containsKey(positionId)) {
            positionsHistory = data.allPositionsHistory.get(positionId);
        } else {
            positionsHistory = null;
        }

        Strategy86 s86 = new Strategy86(series, config, positionsHistory);

        String notificationMessage = "";
        ArrayList<TradeRecord> ell = s86.exitLong();
        if (ell.size() > 0) {
            TradeRecord el = ell.get(0);
            notificationMessage += data.subscriptions.get(
                    index).symbol + " " +
                    data.subscriptions.get(
                            index).interval.toString().replaceAll("_", " ").toLowerCase()
                    + " 收線價格： " + df.format(el.getExitPrice()) + "，做多平倉信號：";

            if (el.getExitPrice() > el.getTakeProfitPrice())
                notificationMessage += "達到目標位 " + df.format(el.getTakeProfitPrice()) + '\n';
            else if (el.getExitPrice() < el.getStopLossPrice()) {
                notificationMessage += "達到止損位 " + df.format(el.getStopLossPrice()) + '\n';
            } else {
                notificationMessage += "RSI 指標觸及設定值！\n\n";
            }
        }

        ArrayList<TradeRecord> esl = s86.exitShort();
        if (esl.size() > 0) {
            TradeRecord es = esl.get(0);
            notificationMessage += data.subscriptions.get(
                    index).symbol + " " +
                    data.subscriptions.get(
                            index).interval.toString().replaceAll("_", " ").toLowerCase()
                    + " 收線價格： " + df.format(es.getExitPrice()) + "，做空平倉信號：";
            if (es.getExitPrice() < es.getTakeProfitPrice())
                notificationMessage += "達到目標位 " + df.format(es.getTakeProfitPrice()) + "\n\n";
            else if (es.getExitPrice() > es.getStopLossPrice()) {
                notificationMessage += "達到止損位 " + df.format(es.getStopLossPrice()) + "\n\n";
            } else {
                notificationMessage += "RSI 指標觸及設定值！\n\n";
            }
        }

        TradeRecord el = s86.entryLong();
        if (el.getType() == "long") {
            notificationMessage += data.subscriptions.get(
                    index).symbol + " " +
                    data.subscriptions.get(
                            index).interval.toString().replaceAll("_", " ").toLowerCase()
                    + " 收線價格： " + df.format(Double.parseDouble(series.getLastBar().getClosePrice().toString()))
                    + "，可以進場做多！";
        }

        TradeRecord es = s86.entryShort();
        if (es.getType() == "short") {
            try {
                notificationMessage += data.subscriptions.get(
                        index).symbol + " " + String
                                .valueOf(data.subscriptions.get(
                                        index).interval)
                                .replaceAll("_", " ").toLowerCase()
                        + " 收線價格： " + df.format(Double.parseDouble(series.getLastBar().getClosePrice().toString()))
                        + "，可以進場做空！";
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
        manager.updatePositionsHistory(chatId, positionId, s86.closeAccount());
        return notificationMessage;
    }
}
