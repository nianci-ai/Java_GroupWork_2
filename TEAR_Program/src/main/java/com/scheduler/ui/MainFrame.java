package com.scheduler.ui;

import com.scheduler.model.Project;
import com.scheduler.model.Task;
import com.scheduler.service.ProjectService;
import com.scheduler.service.ReminderService;
import com.scheduler.service.StatisticsService;
import com.scheduler.service.TaskService;
import com.scheduler.storage.DataBackupService;
import com.scheduler.storage.DataStorage;
import com.scheduler.Main;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * 主界面类：应用程序的主窗口，集成所有服务和UI组件
 */
public class MainFrame extends JFrame {
    // 服务层
    private TaskService taskService;
    private ProjectService projectService;
    private ReminderService reminderService;
    private StatisticsService statisticsService;
    private DataStorage dataStorage;
    private DataBackupService dataBackupService;
    
    // UI组件
    private GanttChart ganttChart;
    private JTabbedPane tabbedPane;
    private JTable taskTable;
    private TaskTableModel taskTableModel;
    private JPanel statisticsPanel;
    private JTable projectTable;
    private ProjectTableModel projectTableModel;
    private JPanel projectPanel;
    
    // 构造方法
    public MainFrame() {
        // 初始化服务层
        initServices();
        
        // 初始化UI
        initUI();
        
        // 加载数据
        loadData();
        
        // 启动自动备份
        dataBackupService.startAutoBackup();
        
        // 启动提醒检查线程
        startReminderChecker();
    }
    
    // 初始化服务层
    private void initServices() {
        // 创建服务实例
        taskService = new TaskService();
        projectService = new ProjectService();
        reminderService = new ReminderService(taskService);
        statisticsService = new StatisticsService(taskService);
        
        // 创建数据存储服务
        String userHome = System.getProperty("user.home");
        String dataDir = userHome + File.separator + ".scheduler_data";
        new File(dataDir).mkdirs(); // 创建数据目录
        
        String taskFilePath = dataDir + File.separator + "tasks.dat";
        String projectFilePath = dataDir + File.separator + "projects.dat";
        dataStorage = new DataStorage(taskFilePath, projectFilePath);
        
        // 创建备份服务
        String backupDir = dataDir + File.separator + "backups";
        dataBackupService = new DataBackupService(dataStorage, taskService, projectService, backupDir);
    }
    
    // 初始化UI
    private void initUI() {
        // 设置窗口属性
        setTitle("个人日程安排与提醒系统");
        setSize(1000, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // 创建菜单
        createMenuBar();
        
        // 创建选项卡面板
        tabbedPane = new JTabbedPane();
        
        // 创建甘特图选项卡
        ganttChart = new GanttChart(taskService);
        JScrollPane ganttScrollPane = new JScrollPane(ganttChart);
        ganttScrollPane.setBorder(BorderFactory.createEmptyBorder());
        tabbedPane.addTab("甘特图", ganttScrollPane);
        
        // 创建任务列表选项卡
        createTaskTablePanel();
        
        // 创建项目管理选项卡
        createProjectPanel();
        
        // 创建统计信息选项卡
        createStatisticsPanel();
        
        // 添加选项卡面板到窗口
        add(tabbedPane);
        
        // 添加一些示例数据
        addSampleData();
        
        // 更新UI显示
        updateUI();
    }
    
    // 创建菜单栏
    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        // 文件菜单
        JMenu fileMenu = new JMenu("文件");
        
        JMenuItem saveItem = new JMenuItem("保存");
        saveItem.addActionListener(e -> saveData());
        
        JMenuItem backupItem = new JMenuItem("手动备份");
        backupItem.addActionListener(e -> performBackup());
        
        JMenuItem exitItem = new JMenuItem("退出");
        exitItem.addActionListener(e -> System.exit(0));
        
        fileMenu.add(saveItem);
        fileMenu.add(backupItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        
        // 任务菜单
        JMenu taskMenu = new JMenu("任务");
        
        JMenuItem addTaskItem = new JMenuItem("添加任务");
        addTaskItem.addActionListener(e -> showAddTaskDialog());
        
        JMenuItem editTaskItem = new JMenuItem("编辑任务");
        editTaskItem.addActionListener(e -> showEditTaskDialog());
        
        JMenuItem deleteTaskItem = new JMenuItem("删除任务");
        deleteTaskItem.addActionListener(e -> deleteSelectedTask());
        
        taskMenu.add(addTaskItem);
        taskMenu.add(editTaskItem);
        taskMenu.add(deleteTaskItem);
        
        // 项目菜单
        JMenu projectMenu = new JMenu("项目");
        
        JMenuItem addProjectItem = new JMenuItem("添加项目");
        addProjectItem.addActionListener(e -> showAddProjectDialog());
        
        projectMenu.add(addProjectItem);
        
        // 查看菜单
        JMenu viewMenu = new JMenu("查看");
        
        JMenuItem todayItem = new JMenuItem("今天");
        todayItem.addActionListener(e -> ganttChart.goToToday());
        
        ButtonGroup viewGroup = new ButtonGroup();
        JRadioButtonMenuItem weekViewItem = new JRadioButtonMenuItem("周视图", true);
        weekViewItem.addActionListener(e -> ganttChart.setDaysVisible(7));
        
        JRadioButtonMenuItem monthViewItem = new JRadioButtonMenuItem("月视图");
        monthViewItem.addActionListener(e -> ganttChart.setDaysVisible(30));
        
        JRadioButtonMenuItem dayViewItem = new JRadioButtonMenuItem("日视图");
        dayViewItem.addActionListener(e -> ganttChart.setDaysVisible(1));
        
        viewGroup.add(weekViewItem);
        viewGroup.add(monthViewItem);
        viewGroup.add(dayViewItem);
        
        viewMenu.add(todayItem);
        viewMenu.addSeparator();
        viewMenu.add(weekViewItem);
        viewMenu.add(monthViewItem);
        viewMenu.add(dayViewItem);
        
        // 添加菜单到菜单栏
        menuBar.add(fileMenu);
        menuBar.add(taskMenu);
        menuBar.add(projectMenu);
        menuBar.add(viewMenu);
        
        setJMenuBar(menuBar);
    }
    
    // 创建任务列表面板
    private void createTaskTablePanel() {
        // 创建表格模型
        taskTableModel = new TaskTableModel(taskService);
        
        // 创建表格
        taskTable = new JTable(taskTableModel);
        taskTable.setRowHeight(30);
        taskTable.getTableHeader().setFont(new Font("宋体", Font.BOLD, 14));
        
        // 创建滚动面板
        JScrollPane scrollPane = new JScrollPane(taskTable);
        tabbedPane.addTab("任务列表", scrollPane);
    }
    
    // 创建项目管理面板
    private void createProjectPanel() {
        // 创建表格模型
        projectTableModel = new ProjectTableModel(projectService);
        
        // 创建表格
        projectTable = new JTable(projectTableModel);
        projectTable.setRowHeight(30);
        projectTable.getTableHeader().setFont(new Font("宋体", Font.BOLD, 14));
        
        // 创建滚动面板
        JScrollPane scrollPane = new JScrollPane(projectTable);
        
        // 创建按钮面板
        JPanel buttonPanel = new JPanel();
        JButton addProjectButton = new JButton("添加项目");
        JButton editProjectButton = new JButton("编辑项目");
        JButton deleteProjectButton = new JButton("删除项目");
        
        addProjectButton.addActionListener(e -> showAddProjectDialog());
        editProjectButton.addActionListener(e -> showEditProjectDialog());
        deleteProjectButton.addActionListener(e -> deleteSelectedProject());
        
        buttonPanel.add(addProjectButton);
        buttonPanel.add(editProjectButton);
        buttonPanel.add(deleteProjectButton);
        
        // 创建项目面板
        projectPanel = new JPanel(new BorderLayout());
        projectPanel.add(scrollPane, BorderLayout.CENTER);
        projectPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // 添加到选项卡
        tabbedPane.addTab("项目管理", projectPanel);
    }
    
    // 创建统计信息面板
    private void createStatisticsPanel() {
        statisticsPanel = new JPanel(new GridLayout(4, 2, 20, 20));
        statisticsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        tabbedPane.addTab("统计信息", statisticsPanel);
    }
    
    // 添加示例数据
    private void addSampleData() {
        // 添加示例项目
        Project project1 = new Project("项目A", "这是一个示例项目");
        projectService.addProject(project1);
        
        Project project2 = new Project("项目B", "这是另一个示例项目");
        projectService.addProject(project2);
        
        // 添加示例任务
        Calendar calendar = Calendar.getInstance();
        
        // 今天的任务
        Task task1 = new Task("团队周会", "讨论本周工作进度", calendar.getTime(), 
                addHours(calendar.getTime(), 2), Task.Priority.HIGH, project1.getId(), Task.TaskType.MEETING);
        taskService.addTask(task1);
        
        // 明天的任务
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        Task task2 = new Task("项目规划文档", "完成项目的详细规划", calendar.getTime(), 
                addHours(calendar.getTime(), 4), Task.Priority.MEDIUM, project1.getId(), Task.TaskType.DEADLINE);
        taskService.addTask(task2);
        
        // 后天的任务
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        Task task3 = new Task("代码审查", "审查团队成员的代码", calendar.getTime(), 
                addHours(calendar.getTime(), 3), Task.Priority.HIGH, project2.getId(), Task.TaskType.DAILY);
        taskService.addTask(task3);
        
        // 设置提醒
        reminderService.setReminder(task1.getId(), 30); // 提前30分钟提醒
        reminderService.setReminder(task2.getId(), 60); // 提前1小时提醒
        reminderService.setReminder(task3.getId(), 15); // 提前15分钟提醒
    }
    
    // 工具方法：为日期添加指定小时数
    private Date addHours(Date date, int hours) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.HOUR_OF_DAY, hours);
        return calendar.getTime();
    }
    
    // 更新UI显示
    private void updateUI() {
        // 更新甘特图
        List<Task> allTasks = taskService.getAllTasks();
        ganttChart.setTasks(allTasks);
        
        // 更新任务表格
        updateTaskTable();
        
        // 更新项目表格
        updateProjectTable();
        
        // 更新统计信息
        updateStatisticsPanel();
    }
    
    // 更新任务表格
    private void updateTaskTable() {
        taskTableModel.updateData();
    }
    
    // 更新项目表格
    private void updateProjectTable() {
        projectTableModel.updateData();
    }
    
    // 更新统计信息面板
    private void updateStatisticsPanel() {
        statisticsPanel.removeAll();
        
        // 获取统计数据
        int completedThisWeek = statisticsService.getCompletedTasksThisWeek();
        int completedThisMonth = statisticsService.getCompletedTasksThisMonth();
        double completionRate = statisticsService.getTaskCompletionRate();
        double overdueRate = statisticsService.getTaskOverdueRate();
        Map<Task.TaskType, Integer> tasksByType = statisticsService.getTaskCountByType();
        Map<Task.Priority, Integer> tasksByPriority = statisticsService.getTaskCountByPriority();
        
        // 添加统计项
        addStatisticItem("本周完成任务", completedThisWeek + " 个");
        addStatisticItem("本月完成任务", completedThisMonth + " 个");
        addStatisticItem("任务完成率", String.format("%.1f%%", completionRate));
        addStatisticItem("任务延迟率", String.format("%.1f%%", overdueRate));
        
        // 添加任务类型统计
        StringBuilder typeStats = new StringBuilder();
        for (Map.Entry<Task.TaskType, Integer> entry : tasksByType.entrySet()) {
            typeStats.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        addStatisticItem("任务类型分布", typeStats.toString());
        
        // 添加优先级统计
        StringBuilder priorityStats = new StringBuilder();
        for (Map.Entry<Task.Priority, Integer> entry : tasksByPriority.entrySet()) {
            priorityStats.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        addStatisticItem("优先级分布", priorityStats.toString());
        
        // 刷新面板
        statisticsPanel.revalidate();
        statisticsPanel.repaint();
    }
    
    // 添加统计项
    private void addStatisticItem(String label, String value) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        
        JLabel labelLabel = new JLabel(label);
        labelLabel.setFont(new Font("宋体", Font.BOLD, 14));
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("宋体", Font.PLAIN, 14));
        
        panel.add(labelLabel, BorderLayout.NORTH);
        panel.add(valueLabel, BorderLayout.CENTER);
        
        statisticsPanel.add(panel);
    }
    
    // 显示添加任务对话框
    private void showAddTaskDialog() {
        TaskDialog dialog = new TaskDialog(this, projectService, reminderService);
        dialog.setDefaultToToday(); // 设置默认时间为今天
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            Task newTask = dialog.getTask();
            taskService.addTask(newTask);
            saveData(); // 自动保存数据
            updateUI(); // 更新界面显示
        }
    }
    
    // 显示编辑任务对话框
    private void showEditTaskDialog() {
        int selectedRow = taskTable.getSelectedRow();
        if (selectedRow >= 0) {
            // 获取选中任务的名称
            String taskName = (String) taskTableModel.getValueAt(selectedRow, 0);
            
            // 查找对应的任务对象
            Task taskToEdit = null;
            for (Task task : taskService.getAllTasks()) {
                if (task.getName().equals(taskName)) {
                    taskToEdit = task;
                    break;
                }
            }
            
            if (taskToEdit != null) {
                TaskDialog dialog = new TaskDialog(this, taskToEdit, projectService, reminderService);
                dialog.setVisible(true);
                
                if (dialog.isConfirmed()) {
                    taskService.updateTask(taskToEdit);
                    saveData(); // 自动保存数据
                    updateUI(); // 更新界面显示
                }
            } else {
                JOptionPane.showMessageDialog(this, "找不到选中的任务", "错误", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "请先选择要编辑的任务");
        }
    }
    
    // 删除选中的任务
    private void deleteSelectedTask() {
        int selectedRow = taskTable.getSelectedRow();
        if (selectedRow >= 0) {
            // 获取选中任务的名称
            String taskName = (String) taskTableModel.getValueAt(selectedRow, 0);
            
            // 查找对应的任务对象
            Task taskToDelete = null;
            for (Task task : taskService.getAllTasks()) {
                if (task.getName().equals(taskName)) {
                    taskToDelete = task;
                    break;
                }
            }
            
            if (taskToDelete != null) {
                // 显示确认对话框
                int confirm = JOptionPane.showConfirmDialog(this, 
                        "确定要删除任务'" + taskName + "'吗？", "确认删除", 
                        JOptionPane.YES_NO_OPTION);
                
                if (confirm == JOptionPane.YES_OPTION) {
                    taskService.deleteTask(taskToDelete.getId());
                    saveData(); // 自动保存数据
                    updateUI(); // 更新界面显示
                }
            } else {
                JOptionPane.showMessageDialog(this, "找不到选中的任务", "错误", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "请先选择要删除的任务");
        }
    }
    
    // 显示添加项目对话框
    private void showAddProjectDialog() {
        ProjectDialog dialog = new ProjectDialog(this, projectService);
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            Project newProject = dialog.getProject();
            projectService.addProject(newProject);
            saveData(); // 自动保存数据
            updateUI(); // 更新界面显示
        }
    }
    
    // 显示编辑项目对话框
    private void showEditProjectDialog() {
        int selectedRow = projectTable.getSelectedRow();
        if (selectedRow >= 0) {
            // 查找对应的项目对象
            Project projectToEdit = projectTableModel.getProjectAt(selectedRow);
            
            if (projectToEdit != null) {
                ProjectDialog dialog = new ProjectDialog(this, projectToEdit, projectService);
                dialog.setVisible(true);
                
                if (dialog.isConfirmed()) {
                    projectService.updateProject(projectToEdit);
                    saveData(); // 自动保存数据
                    updateUI(); // 更新界面显示
                }
            } else {
                JOptionPane.showMessageDialog(this, "找不到选中的项目", "错误", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "请先选择要编辑的项目");
        }
    }
    
    // 删除选中的项目
    private void deleteSelectedProject() {
        int selectedRow = projectTable.getSelectedRow();
        if (selectedRow >= 0) {
            // 查找对应的项目对象
            Project projectToDelete = projectTableModel.getProjectAt(selectedRow);
            
            if (projectToDelete != null) {
                // 检查项目是否关联有任务
                boolean hasTasks = false;
                for (Task task : taskService.getAllTasks()) {
                    if (projectToDelete.getId().equals(task.getProjectId())) {
                        hasTasks = true;
                        break;
                    }
                }
                
                if (hasTasks) {
                    JOptionPane.showMessageDialog(this, "该项目关联有任务，无法删除", "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // 显示确认对话框
                int confirm = JOptionPane.showConfirmDialog(this, 
                        "确定要删除项目'" + projectToDelete.getName() + "'吗？", "确认删除", 
                        JOptionPane.YES_NO_OPTION);
                
                if (confirm == JOptionPane.YES_OPTION) {
                    projectService.deleteProject(projectToDelete.getId());
                    saveData(); // 自动保存数据
                    updateUI(); // 更新界面显示
                }
            } else {
                JOptionPane.showMessageDialog(this, "找不到选中的项目", "错误", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "请先选择要删除的项目");
        }
    }
    
    // 加载数据
    private void loadData() {
        boolean success = dataStorage.loadAllData(taskService, projectService);
        if (success) {
            System.out.println("数据加载成功");
        } else {
            System.out.println("没有找到保存的数据，使用空数据");
        }
    }
    
    // 保存数据
    private void saveData() {
        boolean success = dataStorage.saveAllData(taskService, projectService);
        if (success) {
            JOptionPane.showMessageDialog(this, "数据保存成功");
        } else {
            JOptionPane.showMessageDialog(this, "数据保存失败", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // 执行手动备份
    private void performBackup() {
        boolean success = dataBackupService.performBackup(taskService, projectService);
        if (success) {
            JOptionPane.showMessageDialog(this, "数据备份成功");
        } else {
            JOptionPane.showMessageDialog(this, "数据备份失败", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // 启动提醒检查线程
    private void startReminderChecker() {
        Thread reminderThread = new Thread(() -> {
            while (true) {
                try {
                    // 每分钟检查一次
                    Thread.sleep(60000);
                    reminderService.checkAndTriggerReminders();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }
        });
        reminderThread.setDaemon(true);
        reminderThread.start();
    }
    
    // 主方法已移至Main类
    // 此方法保留用于测试目的
    public static void main(String[] args) {
        Main.main(args);
    }
}