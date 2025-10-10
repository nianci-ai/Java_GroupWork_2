package com.scheduler.service;

import com.scheduler.model.Task;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 统计服务类：提供任务完成率、延迟率等统计功能
 */
public class StatisticsService {
    private TaskService taskService; // 任务服务，用于获取任务数据
    private SimpleDateFormat weekFormat; // 用于格式化周的日期格式
    private SimpleDateFormat monthFormat; // 用于格式化月的日期格式
    
    // 构造方法
    public StatisticsService(TaskService taskService) {
        this.taskService = taskService;
        this.weekFormat = new SimpleDateFormat("yyyy-'W'ww"); // 格式：2023-W45
        this.monthFormat = new SimpleDateFormat("yyyy-MM"); // 格式：2023-11
    }
    
    // 获取本周完成的任务数量
    public int getCompletedTasksThisWeek() {
        Date now = new Date();
        String currentWeek = weekFormat.format(now);
        
        return taskService.getAllTasks().stream()
                .filter(task -> task.getStatus() == Task.TaskStatus.COMPLETED)
                .filter(task -> {
                    if (task.getEndTime() == null) {
                        return false;
                    }
                    String taskWeek = weekFormat.format(task.getEndTime());
                    return currentWeek.equals(taskWeek);
                })
                .collect(Collectors.toList())
                .size();
    }
    
    // 获取本月完成的任务数量
    public int getCompletedTasksThisMonth() {
        Date now = new Date();
        String currentMonth = monthFormat.format(now);
        
        return taskService.getAllTasks().stream()
                .filter(task -> task.getStatus() == Task.TaskStatus.COMPLETED)
                .filter(task -> {
                    if (task.getEndTime() == null) {
                        return false;
                    }
                    String taskMonth = monthFormat.format(task.getEndTime());
                    return currentMonth.equals(taskMonth);
                })
                .collect(Collectors.toList())
                .size();
    }
    
    // 按周统计完成的任务数量
    public Map<String, Integer> getCompletedTasksByWeek(int weeks) {
        Map<String, Integer> result = new TreeMap<>();
        Calendar calendar = Calendar.getInstance();
        
        // 获取最近指定周数的数据
        for (int i = 0; i < weeks; i++) {
            calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
            Date weekStart = calendar.getTime();
            calendar.add(Calendar.DAY_OF_MONTH, 6);
            Date weekEnd = calendar.getTime();
            
            String weekKey = weekFormat.format(weekStart);
            
            // 计算该周完成的任务数量
            int count = taskService.getAllTasks().stream()
                    .filter(task -> task.getStatus() == Task.TaskStatus.COMPLETED)
                    .filter(task -> {
                        if (task.getEndTime() == null) {
                            return false;
                        }
                        return !task.getEndTime().before(weekStart) && !task.getEndTime().after(weekEnd);
                    })
                    .collect(Collectors.toList())
                    .size();
            
            result.put(weekKey, count);
            
            // 移动到上一周
            calendar.add(Calendar.DAY_OF_MONTH, -7);
        }
        
        return result;
    }
    
    // 按周统计延迟的任务数量
    public Map<String, Integer> getOverdueTasksByWeek(int weeks) {
        Map<String, Integer> result = new TreeMap<>();
        Calendar calendar = Calendar.getInstance();
        
        // 获取最近指定周数的数据
        for (int i = 0; i < weeks; i++) {
            calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
            Date weekStart = calendar.getTime();
            calendar.add(Calendar.DAY_OF_MONTH, 6);
            Date weekEnd = calendar.getTime();
            
            String weekKey = weekFormat.format(weekStart);
            
            // 计算该周延迟的任务数量
            int count = taskService.getAllTasks().stream()
                    .filter(Task::isOverdue)
                    .filter(task -> {
                        if (task.getEndTime() == null) {
                            return false;
                        }
                        return !task.getEndTime().before(weekStart) && !task.getEndTime().after(weekEnd);
                    })
                    .collect(Collectors.toList())
                    .size();
            
            result.put(weekKey, count);
            
            // 移动到上一周
            calendar.add(Calendar.DAY_OF_MONTH, -7);
        }
        
        return result;
    }
    
    // 获取任务完成率
    public double getTaskCompletionRate() {
        List<Task> allTasks = taskService.getAllTasks();
        if (allTasks.isEmpty()) {
            return 0.0;
        }
        
        long completedCount = allTasks.stream()
                .filter(task -> task.getStatus() == Task.TaskStatus.COMPLETED)
                .count();
        
        return (double) completedCount / allTasks.size() * 100;
    }
    
    // 获取任务延迟率
    public double getTaskOverdueRate() {
        List<Task> allTasks = taskService.getAllTasks();
        if (allTasks.isEmpty()) {
            return 0.0;
        }
        
        long overdueCount = allTasks.stream()
                .filter(Task::isOverdue)
                .count();
        
        return (double) overdueCount / allTasks.size() * 100;
    }
    
    // 按任务类型统计任务数量
    public Map<Task.TaskType, Integer> getTaskCountByType() {
        Map<Task.TaskType, Integer> result = new EnumMap<>(Task.TaskType.class);
        
        // 初始化所有类型的计数为0
        for (Task.TaskType type : Task.TaskType.values()) {
            result.put(type, 0);
        }
        
        // 统计各类型的任务数量
        for (Task task : taskService.getAllTasks()) {
            if (task.getType() != null) {
                result.put(task.getType(), result.get(task.getType()) + 1);
            }
        }
        
        return result;
    }
    
    // 按优先级统计任务数量
    public Map<Task.Priority, Integer> getTaskCountByPriority() {
        Map<Task.Priority, Integer> result = new EnumMap<>(Task.Priority.class);
        
        // 初始化所有优先级的计数为0
        for (Task.Priority priority : Task.Priority.values()) {
            result.put(priority, 0);
        }
        
        // 统计各优先级的任务数量
        for (Task task : taskService.getAllTasks()) {
            result.put(task.getPriority(), result.get(task.getPriority()) + 1);
        }
        
        return result;
    }
    
    // 兼容方法：获取本周完成的任务数量
    public int getWeeklyCompletedTasks() {
        return getCompletedTasksThisWeek();
    }
    
    // 兼容方法：获取本月完成的任务数量
    public int getMonthlyCompletedTasks() {
        return getCompletedTasksThisMonth();
    }
    
    // 兼容方法：获取任务完成率
    public double getCompletionRate() {
        return getTaskCompletionRate() / 100.0; // MainFrame期望返回的是小数形式（0-1）
    }
    
    // 兼容方法：获取任务延迟率
    public double getDelayRate() {
        return getTaskOverdueRate() / 100.0; // MainFrame期望返回的是小数形式（0-1）
    }
}