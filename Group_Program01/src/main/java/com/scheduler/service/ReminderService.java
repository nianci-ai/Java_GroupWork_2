package com.scheduler.service;

import com.scheduler.model.Reminder;
import com.scheduler.model.Task;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 提醒服务类：提供提醒的设置、触发和管理等业务逻辑
 */
public class ReminderService {
    private Map<String, Reminder> reminderMap; // 存储所有提醒的Map，key为任务ID
    private TaskService taskService; // 任务服务，用于获取任务信息
    
    // 构造方法
    public ReminderService(TaskService taskService) {
        this.reminderMap = new HashMap<>();
        this.taskService = taskService;
    }
    
    // 为任务设置提醒
    public boolean setReminder(String taskId, int minutesBefore) {
        Task task = taskService.getTaskById(taskId);
        if (task == null || task.getStartTime() == null) {
            return false;
        }
        
        // 计算提醒时间（任务开始时间前minutesBefore分钟）
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(task.getStartTime());
        calendar.add(Calendar.MINUTE, -minutesBefore);
        Date reminderTime = calendar.getTime();
        
        // 创建提醒消息
        String message = "任务 '" + task.getName() + "' 即将开始！";
        
        // 创建并存储提醒
        Reminder reminder = new Reminder(taskId, reminderTime, message);
        reminderMap.put(taskId, reminder);
        
        // 安排提醒任务
        reminder.schedule();
        
        return true;
    }
    
    // 获取任务的提醒
    public Reminder getReminderByTaskId(String taskId) {
        return reminderMap.get(taskId);
    }
    
    // 取消任务的提醒
    public boolean cancelReminder(String taskId) {
        Reminder reminder = reminderMap.remove(taskId);
        if (reminder != null) {
            reminder.cancel();
            return true;
        }
        return false;
    }
    
    // 检查并触发所有应该触发的提醒
    public void checkAndTriggerReminders() {
        List<String> triggeredTaskIds = reminderMap.values().stream()
                .filter(Reminder::shouldTrigger)
                .map(Reminder::getTaskId)
                .collect(Collectors.toList());
        
        for (String taskId : triggeredTaskIds) {
            Reminder reminder = reminderMap.get(taskId);
            if (reminder != null) {
                reminder.trigger();
            }
        }
    }
    
    // 获取所有提醒
    public List<Reminder> getAllReminders() {
        return new ArrayList<>(reminderMap.values());
    }
    
    // 获取未触发的提醒
    public List<Reminder> getPendingReminders() {
        return reminderMap.values().stream()
                .filter(reminder -> !reminder.isTriggered())
                .collect(Collectors.toList());
    }
    
    // 获取已触发的提醒
    public List<Reminder> getTriggeredReminders() {
        return reminderMap.values().stream()
                .filter(Reminder::isTriggered)
                .collect(Collectors.toList());
    }
    
    // 获取提醒数量
    public int getReminderCount() {
        return reminderMap.size();
    }
}