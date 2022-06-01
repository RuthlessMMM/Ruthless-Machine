package com.ruthless.app.yamlmanager;

import java.util.ArrayList;

public class UserData {

    public String username;
    public String chatId;
    public ArrayList<SubscribeData> subscriptions;

    UserData() {

    }

    UserData(String username, String chatId, ArrayList<SubscribeData> subscriptions) {
        this.username = username;
        this.chatId = chatId;
        this.subscriptions = subscriptions;
    }

}
