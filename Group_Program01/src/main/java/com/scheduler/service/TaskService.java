package com.scheduler.service;

import com.scheduler.model.Task;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 任务服务类：提供任务的增删改查等业务逻辑
 */
public class TaskService {
    private Map<String, Task> taskMap; // 存储所有任务的Map，key为任务ID
    private Map<String, List<String>> tasksByProject; // 按项目分组的任务ID列表
    private Map<Date, List<String>> tasksByDate; // 按日期分组的任务ID列表
    
    // 构造方法
    public TaskService() {
        this.taskMap = new HashMap<>();
        this.tasksByProject = new HashMap<>();
        this.tasksByDate = new HashMap<>();
    }
    
    // 添加任务
    public boolean addTask(Task task) {
        if (task == null || task.getId() == null || taskMap.containsKey(task.getId())) {
            return false;
        }
        
        taskMap.put(task.getId(), task);
        
        // 更新按项目分组的任务列表
        if (task.getProjectId() != null) {
            tasksByProject.computeIfAbsent(task.getProjectId(), k -> new ArrayList<>()).add(task.getId());
        }
        
        // 更新按日期分组的任务列表
        if (task.getStartTime() != null) {
            Date dateKey = getDateKey(task.getStartTime());
            tasksByDate.computeIfAbsent(dateKey, k -> new ArrayList<>()).add(task.getId());
        }
        
        return true;
    }
    
    // 更新任务
    public boolean updateTask(Task updatedTask) {
        if (updatedTask == null || updatedTask.getId() == null || !taskMap.containsKey(updatedTask.getId())) {
            return false;
        }
        
        Task oldTask = taskMap.get(updatedTask.getId());
        
        // 移除旧任务的项目关联
        if (oldTask.getProjectId() != null) {
            List<String> taskIds = tasksByProject.get(oldTask.getProjectId());
            if (taskIds != null) {
                taskIds.remove(oldTask.getId());
            }
        }
        
        // 移除旧任务的日期关联
        if (oldTask.getStartTime() != null) {
            Date oldDateKey = getDateKey(oldTask.getStartTime());
            List<String> taskIds = tasksByDate.get(oldDateKey);
            if (taskIds != null) {
                taskIds.remove(oldTask.getId());
            }
        }
        
        // 更新任务
        taskMap.put(updatedTask.getId(), updatedTask);
        
        // 更新新的项目关联
        if (updatedTask.getProjectId() != null) {
            tasksByProject.computeIfAbsent(updatedTask.getProjectId(), k -> new ArrayList<>()).add(updatedTask.getId());
        }
        
        // 更新新的日期关联
        if (updatedTask.getStartTime() != null) {
            Date newDateKey = getDateKey(updatedTask.getStartTime());
            tasksByDate.computeIfAbsent(newDateKey, k -> new ArrayList<>()).add(updatedTask.getId());
        }
        
        return true;
    }
    
    // 删除任务
    public boolean deleteTask(String taskId) {
        if (taskId == null || !taskMap.containsKey(taskId)) {
            return false;
        }
        
        Task task = taskMap.remove(taskId);
        
        // 从项目关联中移除
        if (task.getProjectId() != null) {
            List<String> taskIds = tasksByProject.get(task.getProjectId());
            if (taskIds != null) {
                taskIds.remove(taskId);
            }
        }
        
        // 从日期关联中移除
        if (task.getStartTime() != null) {
            Date dateKey = getDateKey(task.getStartTime());
            List<String> taskIds = tasksByDate.get(dateKey);
            if (taskIds != null) {
                taskIds.remove(taskId);
            }
        }
        
        return true;
    }
    
    // 根据ID获取任务
    public Task getTaskById(String taskId) {
        return taskMap.get(taskId);
    }
    
    // 获取所有任务
    public List<Task> getAllTasks() {
        return new ArrayList<>(taskMap.values());
    }
    
    // 获取指定项目的任务
    public List<Task> getTasksByProject(String projectId) {
        List<String> taskIds = tasksByProject.getOrDefault(projectId, Collections.emptyList());
        return taskIds.stream()
                .map(taskMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
    
    // 获取指定日期的任务
    public List<Task> getTasksByDate(Date date) {
        Date dateKey = getDateKey(date);
        List<String> taskIds = tasksByDate.getOrDefault(dateKey, Collections.emptyList());
        return taskIds.stream()
                .map(taskMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
    
    // 按优先级排序获取任务
    public List<Task> getTasksSortedByPriority() {
        return taskMap.values().stream()
                .sorted(Comparator.comparing(Task::getPriority).reversed())
                .collect(Collectors.toList());
    }
    
    // 按截止时间排序获取任务
    public List<Task> getTasksSortedByEndTime() {
        return taskMap.values().stream()
                .sorted(Comparator.comparing(Task::getEndTime))
                .collect(Collectors.toList());
    }
    
    // 获取已过期的任务
    public List<Task> getOverdueTasks() {
        return taskMap.values().stream()
                .filter(task -> task.isOverdue())
                .collect(Collectors.toList());
    }
    
    // 标记任务完成
    public boolean markTaskAsCompleted(String taskId) {
        Task task = getTaskById(taskId);
        if (task != null) {
            task.setStatus(Task.TaskStatus.COMPLETED);
            return true;
        }
        return false;
    }
    
    // 标记任务进行中
    public boolean markTaskAsInProgress(String taskId) {
        Task task = getTaskById(taskId);
        if (task != null) {
            task.setStatus(Task.TaskStatus.IN_PROGRESS);
            return true;
        }
        return false;
    }
    
    // 标记任务未开始
    public boolean markTaskAsNotStarted(String taskId) {
        Task task = getTaskById(taskId);
        if (task != null) {
            task.setStatus(Task.TaskStatus.NOT_STARTED);
            return true;
        }
        return false;
    }
    
    // 获取任务数量
    public int getTaskCount() {
        return taskMap.size();
    }
    
    // 辅助方法：获取日期的关键部分（年月日）作为Map的键
    private Date getDateKey(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }
}