package com.schedule.model;

import java.util.Date;

/**
 * 任务类 - 表示日程安排中的任务
 */
public class Task {
    // 任务状态枚举
    public enum TaskStatus {
        NOT_STARTED("未开始"),
        IN_PROGRESS("进行中"),
        COMPLETED("已完成"),
        DELAYED("已延迟");

        private String description;

        TaskStatus(String description) {
            this.description = description;
        }

        @Override
        public String toString() {
            return description;
        }
    }

    // 任务优先级枚举
    public enum Priority {
        LOW("低"),
        MEDIUM("中"),
        HIGH("高"),
        URGENT("紧急");

        private String description;

        Priority(String description) {
            this.description = description;
        }

        @Override
        public String toString() {
            return description;
        }
    }

    // 任务类型枚举
    public enum TaskType {
        MEETING("会议"),
        DEADLINE("截止日期"),
        DAILY("日常事务");

        private String description;

        TaskType(String description) {
            this.description = description;
        }

        @Override
        public String toString() {
            return description;
        }
    }

    private String id;              // 任务ID
    private String name;            // 任务名称
    private String content;         // 任务内容
    private Date startTime;         // 开始时间
    private Date endTime;           // 截止时间
    private Priority priority;      // 优先级
    private TaskStatus status;      // 任务状态
    private TaskType type;          // 任务类型
    private Project project;        // 所属项目
    private int reminderMinutes;    // 提前提醒分钟数（如30分钟）

    public Task(String id, String name, Date startTime, Date endTime, Priority priority, TaskType type, Project project) {
        this.id = id;
        this.name = name;
        this.content = "";
        this.startTime = startTime;
        this.endTime = endTime;
        this.priority = priority;
        this.status = TaskStatus.NOT_STARTED;
        this.type = type;
        this.project = project;
        this.reminderMinutes = 30; // 默认提前30分钟提醒
    }

    // getter和setter方法
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public TaskType getType() {
        return type;
    }

    public void setType(TaskType type) {
        this.type = type;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public int getReminderMinutes() {
        return reminderMinutes;
    }

    public void setReminderMinutes(int reminderMinutes) {
        this.reminderMinutes = reminderMinutes;
    }

    // 判断任务是否过期
    public boolean isOverdue() {
        Date now = new Date();
        return status != TaskStatus.COMPLETED && now.after(endTime);
    }

    // 判断任务是否即将开始（在提醒时间范围内）
    public boolean isUpcoming() {
        Date now = new Date();
        long diff = startTime.getTime() - now.getTime();
        long reminderTime = reminderMinutes * 60 * 1000; // 转换为毫秒
        return status != TaskStatus.COMPLETED && diff > 0 && diff <= reminderTime;
    }

    // 计算剩余时间（以毫秒为单位）
    public long getRemainingTime() {
        Date now = new Date();
        return endTime.getTime() - now.getTime();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Task task = (Task) obj;
        return id.equals(task.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Task{" +
                "name='" + name + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", priority=" + priority +
                ", status=" + status +
                ", project=" + (project != null ? project.getName() : "无") +
                '}';
    }
}