package com.scheduler.model;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 * 任务类：表示日程安排中的一个任务项
 */
public class Task implements Comparable<Task>, Serializable {
    private String id;             // 任务唯一标识
    private String name;           // 任务名称
    private String content;        // 任务内容
    private Date startTime;        // 开始时间
    private Date endTime;          // 截止时间
    private Priority priority;     // 优先级
    private TaskStatus status;     // 任务状态
    private String projectId;      // 所属项目ID
    private TaskType type;         // 任务类型（会议、截止日期、日常事务）
    private int reminderMinutes;   // 提前提醒时间（分钟）
    
    public enum Priority {
        LOW, MEDIUM, HIGH, URGENT
    }
    
    public enum TaskStatus {
        NOT_STARTED, IN_PROGRESS, COMPLETED
    }
    
    public enum TaskType {
        MEETING, DEADLINE, DAILY
    }
    
    // 无参构造方法
    public Task() {
        this.id = UUID.randomUUID().toString();
        this.status = TaskStatus.NOT_STARTED;
        this.priority = Priority.MEDIUM;
        this.reminderMinutes = 30; // 默认提前30分钟提醒
    }
    
    // 带参构造方法
    public Task(String name, String content, Date startTime, Date endTime, 
               Priority priority, String projectId, TaskType type) {
        this();
        this.name = name;
        this.content = content;
        this.startTime = startTime;
        this.endTime = endTime;
        this.priority = priority;
        this.projectId = projectId;
        this.type = type;
    }
    
    // Getter和Setter方法
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public Date getStartTime() { return startTime; }
    public void setStartTime(Date startTime) { this.startTime = startTime; }
    
    public Date getEndTime() { return endTime; }
    public void setEndTime(Date endTime) { this.endTime = endTime; }
    
    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }
    
    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }
    
    public String getProjectId() { return projectId; }
    public void setProjectId(String projectId) { this.projectId = projectId; }
    
    public TaskType getType() { return type; }
    public void setType(TaskType type) { this.type = type; }
    
    public int getReminderMinutes() { return reminderMinutes; }
    public void setReminderMinutes(int reminderMinutes) { this.reminderMinutes = reminderMinutes; }
    
    // 判断任务是否已过期
    public boolean isOverdue() {
        if (status == TaskStatus.COMPLETED) {
            return false;
        }
        Date now = new Date();
        return endTime != null && now.after(endTime);
    }
    
    // 计算剩余时间（毫秒）
    public long getRemainingTime() {
        if (status == TaskStatus.COMPLETED || endTime == null) {
            return 0;
        }
        Date now = new Date();
        return endTime.getTime() - now.getTime();
    }
    
    // 计算任务持续时间（毫秒）
    public long getDuration() {
        if (startTime == null || endTime == null) {
            return 0;
        }
        return endTime.getTime() - startTime.getTime();
    }
    
    // 根据截止时间和优先级进行排序
    @Override
    public int compareTo(Task other) {
        // 先按截止时间排序
        if (this.endTime != null && other.endTime != null) {
            int timeComparison = this.endTime.compareTo(other.endTime);
            if (timeComparison != 0) {
                return timeComparison;
            }
        }
        // 截止时间相同时按优先级排序
        return this.priority.compareTo(other.priority);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Task{id='").append(id).append("', name='").append(name)
          .append("', status='").append(status).append("', priority='").append(priority)
          .append("', startTime='").append(startTime).append("', endTime='").append(endTime)
          .append("', type='").append(type).append("'}");
        return sb.toString();
    }
}