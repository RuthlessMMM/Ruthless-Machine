package com.ruthless.app.cron;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import com.ruthless.app.yamlmanager.SubscribeData;
import com.ruthless.app.yamlmanager.UserData;
import com.ruthless.app.yamlmanager.YamlManager;

public class Cron {
    static SchedulerFactory sf;
    static Scheduler scheduler;

    Cron() {
        try {
            Cron.init();
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    public static void init() throws Exception {
        sf = new StdSchedulerFactory();
        scheduler = sf.getScheduler();
    }

    public static void schedule(String chatId, int subscriptionsIndex) throws Exception {

        YamlManager manager = new YamlManager();
        UserData data = manager.read(chatId);
        SubscribeData theOne = data.subscriptions.get(subscriptionsIndex);
        String intervalString = theOne.interval.toString();
        String symbol = theOne.symbol;
        // 3.建立JobDetail(作業資訊)
        JobDetail jb = JobBuilder.newJob(CronJob.class)
                .withDescription("RuthlessMMM") // job的描述
                .withIdentity(symbol + intervalString, chatId) // job 的name和group
                .build();
        // 向任務傳遞資料
        JobDataMap jobDataMap = jb.getJobDataMap();
        jobDataMap.put("chatId", chatId);
        jobDataMap.put("subscriptionsIndex", subscriptionsIndex);

        int[] timeUnit = { 1, 60, 3600, 86400, 604800 }; // sec min hour day week
        int[] targetUnit = { 0, 0, 0, 0 };
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);// 跟交易所一樣的開盤時間
        int[] timeNow = { now.getSecond(), now.getMinute(), now.getHour(), now.getDayOfMonth() };
        if (intervalString.equals("FIFTEEN_MINUTES"))
            targetUnit[1] = 15;
        else if (intervalString.equals("HOURLY"))
            targetUnit[2] = 1;
        else if (intervalString.equals("FOUR_HOURLY"))
            targetUnit[2] = 4;
        else
            targetUnit[3] = 1;
        int needSec = 0;
        for (int i = 0; i < 4; i++) {
            if (targetUnit[i] != 0) {
                needSec = targetUnit[i] * timeUnit[i];
                needSec -= (timeNow[i] % targetUnit[i]) * timeUnit[i];
                for (int j = i - 1; j >= 0; j--)
                    needSec -= timeNow[j] * timeUnit[j];
            }
        }
        System.out.println("needSec: " + needSec);
        SimpleDateFormat dateFormat = new SimpleDateFormat("mm - ss");
        System.out.println("currentTimeSecond： " + dateFormat.format(new Date()));
        // 任務執行的時間，SimpleSchedle型別觸發器有效
        long time = System.currentTimeMillis() + needSec * 1000L; // 3秒後啟動任務
        Date statTime = new Date(time);
        ArrayList<String> intervalMap = new ArrayList<String>(
                Arrays.asList("FIFTEEN_MINUTES", "HOURLY", "FOUR_HOURLY", "DAILY"));
        ArrayList<String> cronExpressions = new ArrayList<String>(
                Arrays.asList("0 * * * * ?", "0 0 * * * ?", "0 0 0/4 * * ?", "0 0 0 * * ?"));
        // System.out.println(cronExpressions.get(intervalMap.indexOf(intervalString)));
        // 4.建立Trigger
        // 使用SimpleScheduleBuilder或者CronScheduleBuilder
        Trigger t = TriggerBuilder.newTrigger()
                .withDescription("")
                .withIdentity(symbol + intervalString, chatId)
                // .startAt(statTime) // 預設當前時間啟動
                // 普通計時器
                // .withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(3).withRepeatCount(3))//間隔3秒，重複3次
                // 表示式計時器
                .withSchedule(
                        CronScheduleBuilder.cronSchedule(cronExpressions.get(intervalMap.indexOf(intervalString))))

                .build();

        // 5.註冊任務和定時器
        Cron.scheduler.scheduleJob(jb, t);
        // 6.啟動 排程器
        Cron.scheduler.start();
    }

    public static void deleteScheduledJob(String jobName, String chatId) throws Exception {
        Cron.scheduler.deleteJob(new JobKey(jobName, chatId));
    }

    public static void main(String[] args) throws Exception {
        Cron.init();
        Cron.schedule("ff", 0);
    }
}
