package com.scheduler.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 项目类：表示一个项目，包含多个任务
 */
public class Project implements Serializable {
    private String id;            // 项目唯一标识
    private String name;          // 项目名称
    private String description;   // 项目描述
    private List<String> taskIds; // 关联的任务ID列表
    
    // 无参构造方法
    public Project() {
        this.id = UUID.randomUUID().toString();
        this.taskIds = new ArrayList<>();
    }
    
    // 带参构造方法
    public Project(String name, String description) {
        this();
        this.name = name;
        this.description = description;
    }
    
    // Getter和Setter方法
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public List<String> getTaskIds() { return taskIds; }
    public void setTaskIds(List<String> taskIds) { this.taskIds = taskIds; }
    
    // 添加任务到项目
    public void addTask(String taskId) {
        if (!taskIds.contains(taskId)) {
            taskIds.add(taskId);
        }
    }
    
    // 从项目中移除任务
    public boolean removeTask(String taskId) {
        return taskIds.remove(taskId);
    }
    
    // 检查项目是否包含指定任务
    public boolean containsTask(String taskId) {
        return taskIds.contains(taskId);
    }
    
    // 获取项目中任务数量
    public int getTaskCount() {
        return taskIds.size();
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Project{id='").append(id).append("', name='").append(name)
          .append("', taskCount='").append(getTaskCount()).append("'}");
        return sb.toString();
    }
}