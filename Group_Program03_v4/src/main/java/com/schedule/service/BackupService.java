package com.schedule.service;

import com.schedule.model.Task;
import com.schedule.model.Project;
import com.schedule.model.Reminder;
import com.schedule.util.JsonUtil;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 备份服务类 - 负责数据备份和恢复
 * 对应功能5：数据备份与恢复
 */
public class BackupService {
    private TaskService taskService;
    private static final String BACKUP_DIR = "backups";
    private static final String BACKUP_FILE_PREFIX = "schedule_backup_";
    private static final String BACKUP_FILE_SUFFIX = ".json";
    private static final long AUTO_BACKUP_INTERVAL = 24 * 60 * 60 * 1000; // 24小时

    public BackupService(TaskService taskService) {
        this.taskService = taskService;
        // 确保备份目录存在
        File backupDir = new File(BACKUP_DIR);
        if (!backupDir.exists()) {
            backupDir.mkdirs();
        }
    }

    /**
     * 备份数据到文件
     * @return 备份文件路径，如果备份失败则返回null
     */
    public String backupData() {
        try {
            // 创建备份数据对象
            BackupData backupData = new BackupData();
            backupData.setTasks(taskService.getAllTasks());
            backupData.setProjects(taskService.getAllProjects());
            // 不单独备份提醒，因为TaskService在添加任务时会自动创建提醒
            backupData.setBackupTime(new Date());

            // 生成备份文件名
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String timestamp = dateFormat.format(new Date());
            String backupFileName = BACKUP_FILE_PREFIX + timestamp + BACKUP_FILE_SUFFIX;
            String backupFilePath = BACKUP_DIR + File.separator + backupFileName;

            // 保存为JSON文件（Java 8兼容方式）
            String jsonData = JsonUtil.toJson(backupData);
            Files.write(Paths.get(backupFilePath), jsonData.getBytes(StandardCharsets.UTF_8));

            return backupFilePath;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 从备份文件恢复数据
     * @param backupFilePath 备份文件路径
     * @return 是否恢复成功
     */
    public boolean restoreFromBackup(String backupFilePath) {
        try {
            // 读取备份文件（Java 8兼容方式）
            String jsonData = new String(Files.readAllBytes(Paths.get(backupFilePath)), StandardCharsets.UTF_8);
            BackupData backupData = JsonUtil.fromJson(jsonData, BackupData.class);

            if (backupData == null) {
                return false;
            }

            // 清空当前数据
            // 通过删除所有任务和项目来清空数据
            List<Task> allTasks = taskService.getAllTasks();
            for (Task task : allTasks) {
                taskService.deleteTask(task.getId());
            }
            
            // 恢复项目
            if (backupData.getProjects() != null) {
                for (Project project : backupData.getProjects()) {
                    taskService.addProject(project);
                }
            }

            // 恢复任务（TaskService会自动创建提醒）
            if (backupData.getTasks() != null) {
                for (Task task : backupData.getTasks()) {
                    taskService.addTask(task);
                }
            }

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 列出所有备份文件
     * @return 备份文件路径列表
     */
    public List<String> listBackupFiles() {
        File backupDir = new File(BACKUP_DIR);
        File[] files = backupDir.listFiles((dir, name) -> name.startsWith(BACKUP_FILE_PREFIX) && name.endsWith(BACKUP_FILE_SUFFIX));

        if (files == null) {
            return Collections.emptyList();
        }

        // 按修改时间排序，最新的在前面
        return Arrays.stream(files)
                .sorted(Comparator.comparingLong(File::lastModified).reversed())
                .map(File::getAbsolutePath)
                .collect(Collectors.toList());
    }

    /**
     * 安排自动备份
     */
    public void scheduleAutoBackup() {
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                backupData();
            }
        }, AUTO_BACKUP_INTERVAL, AUTO_BACKUP_INTERVAL);
    }

    /**
     * 备份数据类 - 用于序列化和反序列化备份数据
     */
    private static class BackupData {
        private List<Task> tasks;
        private List<Project> projects;
        private Date backupTime;

        public List<Task> getTasks() {
            return tasks;
        }

        public void setTasks(List<Task> tasks) {
            this.tasks = tasks;
        }

        public List<Project> getProjects() {
            return projects;
        }

        public void setProjects(List<Project> projects) {
            this.projects = projects;
        }

        public Date getBackupTime() {
            return backupTime;
        }

        public void setBackupTime(Date backupTime) {
            this.backupTime = backupTime;
        }
    }
}