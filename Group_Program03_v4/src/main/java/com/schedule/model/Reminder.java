package com.schedule.model;

import java.util.Date;

/**
 * 提醒类 - 处理任务的提醒功能
 */
public class Reminder {
    private String id;          // 提醒ID
    private Task task;          // 关联的任务
    private Date reminderTime;  // 提醒时间
    private boolean notified;   // 是否已通知

    public Reminder(String id, Task task) {
        this.id = id;
        this.task = task;
        // 计算提醒时间（任务开始时间减去提前提醒的分钟数）
        long reminderMillis = task.getStartTime().getTime() - (task.getReminderMinutes() * 60 * 1000);
        this.reminderTime = new Date(reminderMillis);
        this.notified = false;
    }

    // getter和setter方法
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
        // 更新提醒时间
        long reminderMillis = task.getStartTime().getTime() - (task.getReminderMinutes() * 60 * 1000);
        this.reminderTime = new Date(reminderMillis);
    }

    public Date getReminderTime() {
        return reminderTime;
    }

    public void setReminderTime(Date reminderTime) {
        this.reminderTime = reminderTime;
    }

    public boolean isNotified() {
        return notified;
    }

    public void setNotified(boolean notified) {
        this.notified = notified;
    }

    // 检查是否需要提醒
    public boolean shouldRemind() {
        Date now = new Date();
        return !notified && now.after(reminderTime) && task.getStatus() != Task.TaskStatus.COMPLETED;
    }

    // 更新提醒时间（根据任务的提前提醒分钟数）
    public void updateReminderTime() {
        long reminderMillis = task.getStartTime().getTime() - (task.getReminderMinutes() * 60 * 1000);
        this.reminderTime = new Date(reminderMillis);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Reminder reminder = (Reminder) obj;
        return id.equals(reminder.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "提醒: " + task.getName() + " (" + reminderTime + ")";
    }
}