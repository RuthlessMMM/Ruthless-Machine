package com.ruthless.app.yamlmanager;

import java.util.ArrayList;
import java.util.Map;

import com.ruthless.app.util.TradeRecord;

public class UserData {

    public String username;
    public String chatId;
    public ArrayList<SubscribeData> subscriptions;
    public Map<String, ArrayList<TradeRecord>> allPositionsHistory;

    UserData() {

    }

    UserData(String username, String chatId, ArrayList<SubscribeData> subscriptions,
            Map<String, ArrayList<TradeRecord>> allPositionsHistory) {
        this.username = username;
        this.chatId = chatId;
        this.subscriptions = subscriptions;
        this.allPositionsHistory = allPositionsHistory;
    }

}
