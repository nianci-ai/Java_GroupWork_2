package com.scheduler.model;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 提醒类：负责根据任务时间设置和触发提醒
 */
public class Reminder {
    private String taskId;        // 关联的任务ID
    private Date reminderTime;    // 提醒时间
    private String message;       // 提醒消息
    private boolean isTriggered;  // 是否已触发
    
    // 无参构造方法
    public Reminder() {
        this.isTriggered = false;
    }
    
    // 带参构造方法
    public Reminder(String taskId, Date reminderTime, String message) {
        this();
        this.taskId = taskId;
        this.reminderTime = reminderTime;
        this.message = message;
    }
    
    // Getter和Setter方法
    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }
    
    public Date getReminderTime() { return reminderTime; }
    public void setReminderTime(Date reminderTime) { this.reminderTime = reminderTime; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public boolean isTriggered() { return isTriggered; }
    public void setTriggered(boolean triggered) { isTriggered = triggered; }
    
    // 设置提醒任务
    public void schedule() {
        if (reminderTime == null || isTriggered) {
            return;
        }
        
        Date now = new Date();
        long delay = reminderTime.getTime() - now.getTime();
        
        // 如果提醒时间在当前时间之前，立即触发提醒
        if (delay <= 0) {
            trigger();
            return;
        }
        
        // 否则，安排在指定时间触发提醒
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                trigger();
                timer.cancel(); // 任务完成后取消定时器
            }
        }, delay);
    }
    
    // 触发提醒
    public void trigger() {
        if (isTriggered) {
            return;
        }
        
        isTriggered = true;
        // 实际应用中，这里可以弹出对话框、发送通知等
        System.out.println("【提醒】 " + message + " (时间: " + reminderTime + ")");
    }
    
    // 取消提醒
    public void cancel() {
        isTriggered = true; // 标记为已触发，避免后续触发
    }
    
    // 检查提醒是否应该被触发
    public boolean shouldTrigger() {
        if (isTriggered || reminderTime == null) {
            return false;
        }
        return new Date().after(reminderTime);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Reminder{taskId='").append(taskId).append("', reminderTime='").append(reminderTime)
          .append("', isTriggered='").append(isTriggered).append("'}");
        return sb.toString();
    }
}