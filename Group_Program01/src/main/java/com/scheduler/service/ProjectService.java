package com.scheduler.service;

import com.scheduler.model.Project;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 项目服务类：提供项目的增删改查等业务逻辑
 */
public class ProjectService {
    private Map<String, Project> projectMap; // 存储所有项目的Map，key为项目ID
    
    // 构造方法
    public ProjectService() {
        this.projectMap = new HashMap<>();
    }
    
    // 添加项目
    public boolean addProject(Project project) {
        if (project == null || project.getId() == null || projectMap.containsKey(project.getId())) {
            return false;
        }
        
        projectMap.put(project.getId(), project);
        return true;
    }
    
    // 更新项目
    public boolean updateProject(Project updatedProject) {
        if (updatedProject == null || updatedProject.getId() == null || !projectMap.containsKey(updatedProject.getId())) {
            return false;
        }
        
        projectMap.put(updatedProject.getId(), updatedProject);
        return true;
    }
    
    // 删除项目
    public boolean deleteProject(String projectId) {
        if (projectId == null || !projectMap.containsKey(projectId)) {
            return false;
        }
        
        projectMap.remove(projectId);
        return true;
    }
    
    // 根据ID获取项目
    public Project getProjectById(String projectId) {
        return projectMap.get(projectId);
    }
    
    // 获取所有项目
    public List<Project> getAllProjects() {
        return new ArrayList<>(projectMap.values());
    }
    
    // 根据名称搜索项目
    public List<Project> searchProjectsByName(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllProjects();
        }
        
        String lowerKeyword = keyword.toLowerCase();
        return projectMap.values().stream()
                .filter(project -> project.getName().toLowerCase().contains(lowerKeyword))
                .collect(Collectors.toList());
    }
    
    // 获取项目数量
    public int getProjectCount() {
        return projectMap.size();
    }
    
    // 获取指定项目的任务数量
    public int getTaskCountInProject(String projectId) {
        Project project = getProjectById(projectId);
        if (project != null) {
            return project.getTaskCount();
        }
        return 0;
    }
}