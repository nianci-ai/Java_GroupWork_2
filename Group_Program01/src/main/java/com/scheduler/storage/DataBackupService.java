package com.scheduler.storage;

import com.scheduler.service.ProjectService;
import com.scheduler.service.TaskService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 数据备份服务类：负责定期自动备份和手动导出日程数据
 */
public class DataBackupService {
    private DataStorage dataStorage; // 数据存储服务
    private TaskService taskService; // 任务服务，用于获取任务数据
    private ProjectService projectService; // 项目服务，用于获取项目数据
    private String backupDirectory; // 备份文件目录
    private Timer backupTimer; // 备份定时器
    private SimpleDateFormat dateFormat; // 日期格式化对象，用于生成备份文件名
    
    // 构造方法
    public DataBackupService(DataStorage dataStorage, TaskService taskService, 
                            ProjectService projectService, String backupDirectory) {
        this.dataStorage = dataStorage;
        this.taskService = taskService;
        this.projectService = projectService;
        this.backupDirectory = backupDirectory;
        this.dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        
        // 创建备份目录（如果不存在）
        File dir = new File(backupDirectory);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }
    
    // 开始定期自动备份（默认每天凌晨2点）
    public void startAutoBackup() {
        if (backupTimer != null) {
            stopAutoBackup();
        }
        
        backupTimer = new Timer(true); // 守护线程，程序结束时自动停止
        
        // 设置备份时间：每天凌晨2点
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 2);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        Date firstBackupTime = calendar.getTime();
        
        // 如果今天的备份时间已过，则设置为明天
        if (firstBackupTime.before(new Date())) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            firstBackupTime = calendar.getTime();
        }
        
        // 安排定期备份任务，间隔24小时
        backupTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    performBackup(taskService, projectService);
                    System.out.println("自动备份完成: " + new Date());
                } catch (Exception e) {
                    System.err.println("自动备份失败: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }, firstBackupTime, 24 * 60 * 60 * 1000); // 24小时
        
        System.out.println("定期自动备份已启动，下次备份时间: " + firstBackupTime);
    }
    
    // 停止定期自动备份
    public void stopAutoBackup() {
        if (backupTimer != null) {
            backupTimer.cancel();
            backupTimer = null;
            System.out.println("定期自动备份已停止");
        }
    }
    
    // 手动执行备份
    public boolean performBackup(TaskService taskService, ProjectService projectService) {
        try {
            // 获取当前时间作为备份文件名的一部分
            String timestamp = dateFormat.format(new Date());
            
            // 创建备份文件路径
            String taskBackupPath = backupDirectory + File.separator + "tasks_backup_" + timestamp + ".dat";
            String projectBackupPath = backupDirectory + File.separator + "projects_backup_" + timestamp + ".dat";
            
            // 备份数据
            DataStorage backupStorage = new DataStorage(taskBackupPath, projectBackupPath);
            boolean success = backupStorage.saveAllData(taskService, projectService);
            
            if (success) {
                System.out.println("手动备份完成，文件保存在: " + backupDirectory);
            }
            
            return success;
        } catch (Exception e) {
            System.err.println("手动备份失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // 从备份文件恢复数据
    public boolean restoreFromBackup(String taskBackupFile, String projectBackupFile, 
                                    TaskService taskService, ProjectService projectService) {
        try {
            // 验证备份文件是否存在
            File taskFile = new File(taskBackupFile);
            File projectFile = new File(projectBackupFile);
            
            if (!taskFile.exists() || !projectFile.exists()) {
                System.err.println("备份文件不存在");
                return false;
            }
            
            // 从备份文件恢复数据
            DataStorage restoreStorage = new DataStorage(taskBackupFile, projectBackupFile);
            boolean success = restoreStorage.loadAllData(taskService, projectService);
            
            if (success) {
                System.out.println("数据已从备份文件恢复");
            }
            
            return success;
        } catch (Exception e) {
            System.err.println("从备份文件恢复数据失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // 获取备份目录中的所有备份文件
    public String[] getBackupFiles() {
        File dir = new File(backupDirectory);
        return dir.list((d, name) -> name.endsWith(".dat"));
    }
    
    // 清理旧的备份文件（保留指定数量的最新备份）
    public void cleanupOldBackups(int keepCount) {
        try {
            File dir = new File(backupDirectory);
            File[] files = dir.listFiles((d, name) -> name.endsWith(".dat"));
            
            if (files == null || files.length <= keepCount) {
                return; // 不需要清理
            }
            
            // 按修改时间排序（最新的在前）
            java.util.Arrays.sort(files, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
            
            // 删除多余的旧备份文件
            for (int i = keepCount; i < files.length; i++) {
                Files.delete(Paths.get(files[i].getPath()));
                System.out.println("已删除旧备份文件: " + files[i].getName());
            }
        } catch (Exception e) {
            System.err.println("清理旧备份文件失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}