package com.schedule.service;

import com.schedule.model.Task;
import com.schedule.model.Project;
import com.schedule.model.Reminder;

import java.util.*;
import java.util.stream.Collectors;
import com.schedule.model.Task.TaskStatus;
import com.schedule.model.Task.TaskType;
import com.schedule.model.Task.Priority;

/**
 * 任务服务类 - 管理任务的核心功能
 * 对应功能3：任务管理
 */
public class TaskService {
    private Map<String, Task> taskMap;          // 存储所有任务
    private Map<String, Project> projectMap;    // 存储所有项目
    private List<Reminder> reminders;           // 存储所有提醒

    public TaskService() {
        this.taskMap = new HashMap<>();
        this.projectMap = new HashMap<>();
        this.reminders = new ArrayList<>();
        // 生成100条9月份的随机任务数据
        com.schedule.util.GenerateTestData.generateSeptemberTasks(this);
    }

    // 初始化示例数据
    private void initSampleData() {
        // 创建示例项目
        Project project1 = new Project("proj1", "项目A", "第一个示例项目");
        Project project2 = new Project("proj2", "项目B", "第二个示例项目");
        projectMap.put(project1.getId(), project1);
        projectMap.put(project2.getId(), project2);

        // 创建示例任务
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        
        // 今天的任务
        cal.setTime(now);
        cal.set(Calendar.HOUR_OF_DAY, 10);
        cal.set(Calendar.MINUTE, 0);
        Date task1Start = cal.getTime();
        cal.add(Calendar.HOUR_OF_DAY, 2);
        Date task1End = cal.getTime();
        Task task1 = new Task("task1", "团队会议", task1Start, task1End, Task.Priority.HIGH, Task.TaskType.MEETING, project1);
        task1.setContent("讨论项目进度和下一步计划");
        
        // 明天的任务
        cal.setTime(now);
        cal.add(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 14);
        cal.set(Calendar.MINUTE, 30);
        Date task2Start = cal.getTime();
        cal.add(Calendar.HOUR_OF_DAY, 1);
        Date task2End = cal.getTime();
        Task task2 = new Task("task2", "客户访谈", task2Start, task2End, Task.Priority.MEDIUM, Task.TaskType.MEETING, project2);
        task2.setContent("了解客户需求和反馈");
        
        // 即将截止的任务
        cal.setTime(now);
        cal.add(Calendar.HOUR_OF_DAY, 3);
        Date task3Start = cal.getTime();
        cal.add(Calendar.HOUR_OF_DAY, 1);
        Date task3End = cal.getTime();
        Task task3 = new Task("task3", "周报提交", task3Start, task3End, Task.Priority.URGENT, Task.TaskType.DEADLINE, project1);
        task3.setContent("提交本周工作进展和下周计划");
        
        // 添加任务
        addTask(task1);
        addTask(task2);
        addTask(task3);
    }

    // 生成唯一ID
    private String generateId(String prefix) {
        return prefix + "_" + UUID.randomUUID().toString().substring(0, 8);
    }

    // 添加任务（功能1：日程录入）
    public Task addTask(Task task) {
        if (task.getId() == null || task.getId().isEmpty()) {
            task.setId(generateId("task"));
        }
        taskMap.put(task.getId(), task);
        // 为任务创建提醒
        Reminder reminder = new Reminder(generateId("reminder"), task);
        reminders.add(reminder);
        return task;
    }

    // 更新任务
    public Task updateTask(Task task) {
        if (!taskMap.containsKey(task.getId())) {
            return null;
        }
        taskMap.put(task.getId(), task);
        // 更新关联的提醒
        for (Reminder reminder : reminders) {
            if (reminder.getTask().getId().equals(task.getId())) {
                reminder.updateReminderTime();
                break;
            }
        }
        return task;
    }

    // 删除任务
    public boolean deleteTask(String taskId) {
        if (!taskMap.containsKey(taskId)) {
            return false;
        }
        taskMap.remove(taskId);
        // 删除关联的提醒
        reminders.removeIf(reminder -> reminder.getTask().getId().equals(taskId));
        return true;
    }

    // 获取所有任务
    public List<Task> getAllTasks() {
        return new ArrayList<>(taskMap.values());
    }

    // 根据ID获取任务
    public Task getTaskById(String taskId) {
        return taskMap.get(taskId);
    }

    // 根据日期获取任务（功能2：支持按日/周/月视图展示）
    public List<Task> getTasksByDate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date startOfDay = cal.getTime();
        
        cal.add(Calendar.DAY_OF_MONTH, 1);
        Date endOfDay = cal.getTime();
        
        return taskMap.values().stream()
                .filter(task -> !task.getEndTime().before(startOfDay) && !task.getStartTime().after(endOfDay))
                .collect(Collectors.toList());
    }

    // 根据周获取任务
    public List<Task> getTasksByWeek(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date startOfWeek = cal.getTime();
        
        cal.add(Calendar.WEEK_OF_YEAR, 1);
        Date endOfWeek = cal.getTime();
        
        return taskMap.values().stream()
                .filter(task -> !task.getEndTime().before(startOfWeek) && !task.getStartTime().after(endOfWeek))
                .collect(Collectors.toList());
    }

    // 根据月获取任务
    public List<Task> getTasksByMonth(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date startOfMonth = cal.getTime();
        
        cal.add(Calendar.MONTH, 1);
        Date endOfMonth = cal.getTime();
        
        return taskMap.values().stream()
                .filter(task -> !task.getEndTime().before(startOfMonth) && !task.getStartTime().after(endOfMonth))
                .collect(Collectors.toList());
    }

    // 根据优先级排序任务（功能3：支持按优先级排序）
    public List<Task> getTasksSortedByPriority() {
        return taskMap.values().stream()
                .sorted(Comparator.comparing(Task::getPriority).reversed())
                .collect(Collectors.toList());
    }

    // 根据截止时间排序任务（功能3：支持按截止时间排序）
    public List<Task> getTasksSortedByEndTime() {
        return taskMap.values().stream()
                .sorted(Comparator.comparing(Task::getEndTime))
                .collect(Collectors.toList());
    }

    // 根据状态过滤任务（功能3：标记任务状态）
    public List<Task> getTasksByStatus(Task.TaskStatus status) {
        return taskMap.values().stream()
                .filter(task -> task.getStatus() == status)
                .collect(Collectors.toList());
    }

    // 自动更新过期任务的状态
    public void updateOverdueTasksStatus() {
        Date now = new Date();
        for (Task task : taskMap.values()) {
            // 如果任务未完成且已过结束时间，标记为已延迟
            if (task.getStatus() != TaskStatus.COMPLETED && now.after(task.getEndTime())) {
                task.setStatus(TaskStatus.DELAYED);
                // 确保不再提醒已延迟的任务
                for (Reminder reminder : reminders) {
                    if (reminder.getTask().getId().equals(task.getId())) {
                        reminder.setNotified(true);
                        break;
                    }
                }
            }
        }
    }

    // 计算九月份的任务延迟率
    public double calculateSeptemberDelayRate() {
        Calendar cal = Calendar.getInstance();
        int currentYear = cal.get(Calendar.YEAR); // 获取当前年份
        cal.set(Calendar.YEAR, currentYear); 
        cal.set(Calendar.MONTH, Calendar.SEPTEMBER);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date septemberStart = cal.getTime();
        
        cal.add(Calendar.MONTH, 1);
        Date septemberEnd = cal.getTime();
        
        // 获取九月份所有任务
        List<Task> septemberTasks = taskMap.values().stream()
                .filter(task -> !task.getEndTime().before(septemberStart) && !task.getStartTime().after(septemberEnd))
                .collect(Collectors.toList());
        
        if (septemberTasks.isEmpty()) {
            return 0.0;
        }
        
        // 计算已到截止日期的任务数量（截止日期在当前时间之前的任务）
        Date now = new Date();
        long dueTasksCount = septemberTasks.stream()
                .filter(task -> task.getEndTime().before(now))
                .count();
        
        if (dueTasksCount == 0) {
            return 0.0;
        }
        
        // 计算已延迟的任务数量（已到截止日期但未完成的任务）
        long delayedTasksCount = septemberTasks.stream()
                .filter(task -> task.getEndTime().before(now) && task.getStatus() != TaskStatus.COMPLETED)
                .count();
        
        // 正确计算延迟率：已延迟任务数/已到截止日期的任务数
        return (double) delayedTasksCount / dueTasksCount * 100;
    }

    // 批量导入任务时调用的方法，避免重复添加提醒
    public Task addTaskWithoutReminder(Task task) {
        if (task.getId() == null || task.getId().isEmpty()) {
            task.setId(generateId("task"));
        }
        taskMap.put(task.getId(), task);
        return task;
    }

    // 添加项目
    public Project addProject(Project project) {
        if (project.getId() == null || project.getId().isEmpty()) {
            project.setId(generateId("project"));
        }
        projectMap.put(project.getId(), project);
        return project;
    }

    // 获取所有项目
    public List<Project> getAllProjects() {
        return new ArrayList<>(projectMap.values());
    }

    // 获取所有需要提醒的任务（功能2：提醒功能）
    public List<Reminder> getActiveReminders() {
        return reminders.stream()
                .filter(Reminder::shouldRemind)
                .collect(Collectors.toList());
    }

    // 标记提醒为已通知
    public void markReminderAsNotified(String reminderId) {
        for (Reminder reminder : reminders) {
            if (reminder.getId().equals(reminderId)) {
                reminder.setNotified(true);
                break;
            }
        }
    }
}