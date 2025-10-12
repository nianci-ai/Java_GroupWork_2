package com.schedule.model;

/**
 * 项目类 - 表示任务所属的项目
 */
public class Project {
    private String id;         // 项目ID
    private String name;       // 项目名称
    private String description; // 项目描述

    public Project(String id, String name) {
        this.id = id;
        this.name = name;
        this.description = "";
    }

    public Project(String id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Project project = (Project) obj;
        return id.equals(project.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}