package com.ruthless.app.yamlmanager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.binance.api.client.domain.market.CandlestickInterval;
import com.ruthless.app.strategy.Strategy86Config;
import com.ruthless.app.util.TradeRecord;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

public class YamlManager {
    private String path = "./src/main/resources/";

    public YamlManager() {

    }

    public UserData read(String chatId) {
        try {
            InputStream inputStream = new FileInputStream(new File(path + chatId + ".yml"));
            Yaml yaml = new Yaml();
            UserData yamlData = yaml.load(inputStream);
            return yamlData;
        } catch (FileNotFoundException e) {
            System.err.println(e);
            return null;
        }
    }

    public void addSubscription(String chatId, CandlestickInterval interval, String symbol,
            Strategy86Config config) {
        UserData yamlData = this.read(chatId);
        if (yamlData != null) {
            ArrayList<SubscribeData> subData = yamlData.subscriptions;
            subData.add(new SubscribeData(symbol, interval, config));
            yamlData.subscriptions = subData;
            try {
                dump(chatId, yamlData);
            } catch (Exception e) {
                System.err.println(e);
            }
        } else {
            System.out.println("ERROR: Could not read " + chatId);
        }
    }

    public void updateSubscription(String chatId, CandlestickInterval interval, String symbol,
            Strategy86Config config) {
        UserData yamlData = this.read(chatId);
        if (yamlData != null) {
            for (int i = 0; i < yamlData.subscriptions.size(); i++) {
                if ((symbol + String.valueOf(interval))
                        .equals(yamlData.subscriptions.get(i).symbol + String.valueOf(
                                yamlData.subscriptions.get(i).interval))) {
                    try {
                        yamlData.subscriptions.get(i).config = config;
                        dump(chatId, yamlData);
                    } catch (Exception e) {
                        System.err.println(e);
                    }
                    break;
                }
            }
        } else {
            System.out.println("ERROR: Could not read " + chatId);
        }
    }

    // positionid : symbol+interval ex: BTCUSDTFIFTEEN_MINUTES
    public void updatePositionsHistory(String chatId, String positionId, ArrayList<TradeRecord> positionsHistory) {
        UserData yamlData = this.read(chatId);
        if (yamlData != null && yamlData.allPositionsHistory != null) {
            ArrayList<TradeRecord> itemsList = positionsHistory;
            yamlData.allPositionsHistory.put(positionId, itemsList);
            try {
                dump(chatId, yamlData);
            } catch (Exception e) {
                System.err.println(e);
            }
        } else {
            System.out.println("ERROR: Could not read " + chatId);
        }
    }

    public void addUser(String chatId, String username) {
        try {
            ArrayList<SubscribeData> subData = new ArrayList<SubscribeData>();
            Map<String, ArrayList<TradeRecord>> allPositionsHistory = new HashMap<>();
            UserData data = new UserData(username, chatId, subData, allPositionsHistory);
            dump(chatId, data);
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    public void dump(String chatId, UserData data) throws Exception {
        PrintWriter writer = new PrintWriter(new File(path + chatId + ".yml"));
        Yaml yaml = new Yaml();
        yaml.dump(data, writer);
    }

    public static void main(String[] args) {
        YamlManager m = new YamlManager();
        m.addUser("ff", "ncc");
        UserData u = m.read("ff");
        m.updateSubscription("ff", CandlestickInterval.DAILY, "BTCUSDT", new Strategy86Config());

    }
}
