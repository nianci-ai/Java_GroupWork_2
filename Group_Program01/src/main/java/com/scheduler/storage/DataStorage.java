package com.scheduler.storage;

import com.scheduler.model.Project;
import com.scheduler.model.Task;
import com.scheduler.service.ProjectService;
import com.scheduler.service.TaskService;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * 数据存储类：负责任务和项目数据的保存和加载
 */
public class DataStorage {
    private String taskFilePath; // 任务数据文件路径
    private String projectFilePath; // 项目数据文件路径
    
    // 构造方法
    public DataStorage(String taskFilePath, String projectFilePath) {
        this.taskFilePath = taskFilePath;
        this.projectFilePath = projectFilePath;
    }
    
    // 保存任务数据到文件
    public boolean saveTasks(TaskService taskService) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(taskFilePath))) {
            // 将任务数据转换为可序列化的Map
            Map<String, Task> taskMap = new HashMap<>();
            for (Task task : taskService.getAllTasks()) {
                taskMap.put(task.getId(), task);
            }
            oos.writeObject(taskMap);
            return true;
        } catch (IOException e) {
            System.err.println("保存任务数据失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // 从文件加载任务数据
    public boolean loadTasks(TaskService taskService) {
        File file = new File(taskFilePath);
        if (!file.exists()) {
            return false;
        }
        
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(taskFilePath))) {
            Map<String, Task> taskMap = (Map<String, Task>) ois.readObject();
            
            // 清空现有任务数据
            for (Task task : taskService.getAllTasks()) {
                taskService.deleteTask(task.getId());
            }
            
            // 加载新的任务数据
            for (Task task : taskMap.values()) {
                taskService.addTask(task);
            }
            
            return true;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("加载任务数据失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // 保存项目数据到文件
    public boolean saveProjects(ProjectService projectService) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(projectFilePath))) {
            // 将项目数据转换为可序列化的Map
            Map<String, Project> projectMap = new HashMap<>();
            for (Project project : projectService.getAllProjects()) {
                projectMap.put(project.getId(), project);
            }
            oos.writeObject(projectMap);
            return true;
        } catch (IOException e) {
            System.err.println("保存项目数据失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // 从文件加载项目数据
    public boolean loadProjects(ProjectService projectService) {
        File file = new File(projectFilePath);
        if (!file.exists()) {
            return false;
        }
        
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(projectFilePath))) {
            Map<String, Project> projectMap = (Map<String, Project>) ois.readObject();
            
            // 清空现有项目数据
            for (Project project : projectService.getAllProjects()) {
                projectService.deleteProject(project.getId());
            }
            
            // 加载新的项目数据
            for (Project project : projectMap.values()) {
                projectService.addProject(project);
            }
            
            return true;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("加载项目数据失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // 同时保存任务和项目数据
    public boolean saveAllData(TaskService taskService, ProjectService projectService) {
        boolean tasksSaved = saveTasks(taskService);
        boolean projectsSaved = saveProjects(projectService);
        return tasksSaved && projectsSaved;
    }
    
    // 同时加载任务和项目数据
    public boolean loadAllData(TaskService taskService, ProjectService projectService) {
        boolean projectsLoaded = loadProjects(projectService);
        boolean tasksLoaded = loadTasks(taskService);
        return projectsLoaded && tasksLoaded;
    }
}