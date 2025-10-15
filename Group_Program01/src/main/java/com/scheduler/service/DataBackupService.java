package com.scheduler.service;

import com.scheduler.storage.DataStorage;
import com.scheduler.model.Task;
import com.scheduler.model.Project;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 数据备份服务类：负责数据的定期备份和手动备份
 */
public class DataBackupService {
    private DataStorage dataStorage;
    private TaskService taskService;
    private ProjectService projectService;
    private String backupDir; // 备份文件目录
    private SimpleDateFormat dateFormat; // 日期格式化对象
    
    /**
     * 构造方法
     * @param dataStorage 数据存储对象
     * @param taskService 任务服务对象
     * @param projectService 项目服务对象
     * @param backupDir 备份文件目录
     */
    public DataBackupService(DataStorage dataStorage, TaskService taskService, 
                           ProjectService projectService, String backupDir) {
        this.dataStorage = dataStorage;
        this.taskService = taskService;
        this.projectService = projectService;
        this.backupDir = backupDir;
        this.dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        
        // 确保备份目录存在
        File dir = new File(backupDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }
    
    /**
     * 执行手动备份
     * @return 备份文件的路径
     */
    public String performManualBackup() {
        try {
            // 创建备份文件名
            String timestamp = dateFormat.format(new Date());
            String taskBackupFile = backupDir + File.separator + "tasks_backup_" + timestamp + ".dat";
            String projectBackupFile = backupDir + File.separator + "projects_backup_" + timestamp + ".dat";
            
            // 备份任务数据
            List<Task> tasks = taskService.getAllTasks();
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(taskBackupFile))) {
                oos.writeObject(tasks);
            }
            
            // 备份项目数据
            List<Project> projects = projectService.getAllProjects();
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(projectBackupFile))) {
                oos.writeObject(projects);
            }
            
            return "任务备份: " + taskBackupFile + "\n项目备份: " + projectBackupFile;
        } catch (IOException e) {
            e.printStackTrace();
            return "备份失败: " + e.getMessage();
        }
    }
    
    /**
     * 自动定期备份（可以在定时任务中调用）
     */
    public void performAutoBackup() {
        performManualBackup();
    }
    
    /**
     * 从备份文件恢复数据
     * @param taskBackupFile 任务备份文件路径
     * @param projectBackupFile 项目备份文件路径
     * @return 是否恢复成功
     */
    @SuppressWarnings("unchecked")
    public boolean restoreFromBackup(String taskBackupFile, String projectBackupFile) {
        try {
            // 恢复项目数据
            if (projectBackupFile != null && !projectBackupFile.isEmpty()) {
                try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(projectBackupFile))) {
                    List<Project> projects = (List<Project>) ois.readObject();
                    
                    // 清空现有项目数据
                    for (Project project : projectService.getAllProjects()) {
                        projectService.deleteProject(project.getId());
                    }
                    
                    // 恢复项目数据
                    for (Project project : projects) {
                        projectService.addProject(project);
                    }
                }
            }
            
            // 恢复任务数据
            if (taskBackupFile != null && !taskBackupFile.isEmpty()) {
                try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(taskBackupFile))) {
                    List<Task> tasks = (List<Task>) ois.readObject();
                    
                    // 清空现有任务数据
                    for (Task task : taskService.getAllTasks()) {
                        taskService.deleteTask(task.getId());
                    }
                    
                    // 恢复任务数据
                    for (Task task : tasks) {
                        taskService.addTask(task);
                    }
                }
            }
            
            // 保存恢复的数据到主文件
            dataStorage.saveAllData(taskService, projectService);
            
            return true;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 获取备份目录
     * @return 备份目录路径
     */
    public String getBackupDir() {
        return backupDir;
    }
}