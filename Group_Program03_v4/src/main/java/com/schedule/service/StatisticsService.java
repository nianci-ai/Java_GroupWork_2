package com.schedule.service;

import com.schedule.model.Task;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 统计服务类 - 提供数据统计功能
 * 对应功能4：数据统计
 */
public class StatisticsService {
    private TaskService taskService;

    public StatisticsService(TaskService taskService) {
        this.taskService = taskService;
    }

    // 获取本周完成的任务数量
    public int getCompletedTasksThisWeek() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date startOfWeek = cal.getTime();
        
        List<Task> tasks = taskService.getAllTasks();
        return (int) tasks.stream()
                .filter(task -> task.getStatus() == Task.TaskStatus.COMPLETED && task.getEndTime().after(startOfWeek))
                .count();
    }

    // 获取本月完成的任务数量
    public int getCompletedTasksThisMonth() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date startOfMonth = cal.getTime();
        
        List<Task> tasks = taskService.getAllTasks();
        return (int) tasks.stream()
                .filter(task -> task.getStatus() == Task.TaskStatus.COMPLETED && task.getEndTime().after(startOfMonth))
                .count();
    }

    // 获取本周按类型统计的完成任务数量
    public Map<Task.TaskType, Integer> getCompletedTasksByTypeThisWeek() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date startOfWeek = cal.getTime();
        
        List<Task> tasks = taskService.getAllTasks();
        return tasks.stream()
                .filter(task -> task.getStatus() == Task.TaskStatus.COMPLETED && task.getEndTime().after(startOfWeek))
                .collect(Collectors.groupingBy(Task::getType, Collectors.summingInt(task -> 1)));
    }

    // 获取本月按类型统计的完成任务数量
    public Map<Task.TaskType, Integer> getCompletedTasksByTypeThisMonth() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date startOfMonth = cal.getTime();
        
        List<Task> tasks = taskService.getAllTasks();
        return tasks.stream()
                .filter(task -> task.getStatus() == Task.TaskStatus.COMPLETED && task.getEndTime().after(startOfMonth))
                .collect(Collectors.groupingBy(Task::getType, Collectors.summingInt(task -> 1)));
    }

    // 计算任务延迟率（功能4：分析任务延迟率）
    public double getTaskDelayRate() {
        List<Task> tasks = taskService.getAllTasks();
        if (tasks.isEmpty()) {
            return 0.0;
        }
        
        // 计算已过期但未完成的任务数量
        int overdueTasks = (int) tasks.stream()
                .filter(Task::isOverdue)
                .count();
        
        // 计算应该已完成的任务数量（截止时间在当前时间之前的任务）
        Date now = new Date();
        int shouldCompletedTasks = (int) tasks.stream()
                .filter(task -> task.getEndTime().before(now))
                .count();
        
        if (shouldCompletedTasks == 0) {
            return 0.0;
        }
        
        // 计算延迟率
        return (double) overdueTasks / shouldCompletedTasks * 100;
    }

    // 计算本周任务完成率
    public double getTaskCompletionRateThisWeek() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date startOfWeek = cal.getTime();
        
        List<Task> tasks = taskService.getAllTasks();
        
        // 计算本周的任务数量
        int weeklyTasks = (int) tasks.stream()
                .filter(task -> task.getEndTime().after(startOfWeek))
                .count();
        
        if (weeklyTasks == 0) {
            return 0.0;
        }
        
        // 计算本周已完成的任务数量
        int completedWeeklyTasks = (int) tasks.stream()
                .filter(task -> task.getStatus() == Task.TaskStatus.COMPLETED && task.getEndTime().after(startOfWeek))
                .count();
        
        // 计算完成率
        return (double) completedWeeklyTasks / weeklyTasks * 100;
    }

    // 获取按优先级统计的任务数量
    public Map<Task.Priority, Integer> getTasksByPriority() {
        List<Task> tasks = taskService.getAllTasks();
        return tasks.stream()
                .collect(Collectors.groupingBy(Task::getPriority, Collectors.summingInt(task -> 1)));
    }

    // 获取即将到来的任务数量（24小时内）
    public int getUpcomingTasksCount() {
        List<Task> tasks = taskService.getAllTasks();
        return (int) tasks.stream()
                .filter(Task::isUpcoming)
                .count();
    }

    // 获取当前进行中的任务数量
    public int getInProgressTasksCount() {
        List<Task> tasks = taskService.getAllTasks();
        return (int) tasks.stream()
                .filter(task -> task.getStatus() == Task.TaskStatus.IN_PROGRESS)
                .count();
    }
}