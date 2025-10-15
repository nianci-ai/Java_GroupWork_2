package com.schedule.ui;

import com.schedule.model.Task;
import com.schedule.model.Project;
import com.schedule.model.Reminder;
import com.schedule.service.TaskService;
import com.schedule.service.StatisticsService;
import com.schedule.service.BackupService;
import com.schedule.service.ExcelImportService;
import com.schedule.util.DateUtil;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.text.SimpleDateFormat;
import java.util.Comparator;

/**
 * 主窗口类 - 应用程序的主界面
 */
public class MainFrame extends JFrame {
    private TaskService taskService;
    private StatisticsService statisticsService;
    private BackupService backupService;
    private ExcelImportService excelImportService;
    
    // UI组件
    private JTabbedPane tabbedPane;
    private JPanel dailyViewPanel;
    private JPanel weeklyViewPanel;
    private JPanel monthlyViewPanel;
    private JPanel taskManagementPanel;
    private JPanel statisticsPanel;
    private JPanel backupPanel;
    
    private JTable taskTable;
    private DefaultTableModel taskTableModel;
    
    // 甘特图组件
    private GanttChartPanel ganttChartPanel;
    
    public MainFrame() {
        // 初始化服务
        taskService = new TaskService();
        statisticsService = new StatisticsService(taskService);
        backupService = new BackupService(taskService);
        excelImportService = new ExcelImportService(taskService);
        
        // 启动自动备份
        backupService.scheduleAutoBackup();
        
        // 初始化UI
        initUI();
        
        // 更新过期任务状态
        taskService.updateOverdueTasksStatus();
        
        // 加载任务数据
        loadAllTasks();
        loadTasksForToday();
        
        // 启动提醒检查线程
        startReminderChecker();
    }
    
    // 添加带TaskService参数的构造函数，用于外部传入服务实例
    public MainFrame(TaskService taskService) {
        // 使用传入的服务实例
        this.taskService = taskService;
        statisticsService = new StatisticsService(taskService);
        backupService = new BackupService(taskService);
        excelImportService = new ExcelImportService(taskService);
        
        // 启动自动备份
        backupService.scheduleAutoBackup();
        
        // 初始化UI
        initUI();
        
        // 加载任务数据
        loadAllTasks();
        loadTasksForToday();
        
        // 启动提醒检查线程
        startReminderChecker();
    }
    
    private void initUI() {
        // 设置窗口属性
        setTitle("个人日程安排与提醒系统");
        setSize(1024, 768);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // 创建选项卡面板
        tabbedPane = new JTabbedPane();
        
        // 创建各个视图面板
        dailyViewPanel = createDailyViewPanel();
        weeklyViewPanel = createWeeklyViewPanel();
        monthlyViewPanel = createMonthlyViewPanel();
        taskManagementPanel = createTaskManagementPanel();
        statisticsPanel = createStatisticsPanel();
        backupPanel = createBackupPanel();
        
        // 添加选项卡
        tabbedPane.addTab("日视图", dailyViewPanel);
        tabbedPane.addTab("周视图", weeklyViewPanel);
        tabbedPane.addTab("月视图", monthlyViewPanel);
        tabbedPane.addTab("任务管理", taskManagementPanel);
        tabbedPane.addTab("数据统计", statisticsPanel);
        tabbedPane.addTab("数据备份", backupPanel);
        
        // 添加到主窗口
        add(tabbedPane);
    }
    
    private JPanel createDailyViewPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // 顶部日期选择器
        JPanel topPanel = new JPanel();
        JLabel dateLabel = new JLabel("选择日期: ");
        JTextField dateField = new JTextField(15);
        dateField.setText(DateUtil.formatDate(DateUtil.getToday(), DateUtil.DATE_ONLY_FORMAT));
        JButton prevButton = new JButton("<");
        JButton nextButton = new JButton(">");
        
        topPanel.add(prevButton);
        topPanel.add(dateLabel);
        topPanel.add(dateField);
        topPanel.add(nextButton);
        
        // 中间甘特图
        ganttChartPanel = new GanttChartPanel(taskService);
        JScrollPane ganttScrollPane = new JScrollPane(ganttChartPanel);
        
        // 底部任务列表
        String[] columnNames = {"任务名称", "开始时间", "结束时间", "优先级", "状态", "项目"};
        taskTableModel = new DefaultTableModel(columnNames, 0);
        taskTable = new JTable(taskTableModel);
        JScrollPane tableScrollPane = new JScrollPane(taskTable);
        tableScrollPane.setPreferredSize(new Dimension(800, 200));
        
        // 添加按钮面板
        JPanel buttonPanel = new JPanel();
        JButton addButton = new JButton("添加任务");
        JButton editButton = new JButton("编辑任务");
        JButton deleteButton = new JButton("删除任务");
        
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        
        // 组装面板
        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(ganttScrollPane, BorderLayout.CENTER);
        panel.add(tableScrollPane, BorderLayout.SOUTH);
        panel.add(buttonPanel, BorderLayout.EAST);
        
        // 加载今日任务
        loadTasksForToday();
        
        // 添加事件监听器
        addButton.addActionListener(e -> showAddTaskDialog());
        editButton.addActionListener(e -> showEditTaskDialog());
        deleteButton.addActionListener(e -> deleteSelectedTask());
        prevButton.addActionListener(e -> navigateToPreviousDay(dateField));
        nextButton.addActionListener(e -> navigateToNextDay(dateField));
        dateField.addActionListener(e -> loadTasksForDate(dateField.getText()));
        
        return panel;
    }
    
    private JPanel createWeeklyViewPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        // 简化实现，与日视图类似
        JLabel label = new JLabel("周视图 - 显示本周所有任务", SwingConstants.CENTER);
        label.setFont(new Font("宋体", Font.PLAIN, 24));
        panel.add(label, BorderLayout.CENTER);
        
        // 添加一个简单的甘特图预览
        GanttChartPanel weeklyGanttPanel = new GanttChartPanel(taskService);
        weeklyGanttPanel.setViewMode(GanttChartPanel.ViewMode.WEEKLY);
        panel.add(weeklyGanttPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createMonthlyViewPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        // 简化实现，与日视图类似
        JLabel label = new JLabel("月视图 - 显示本月所有任务", SwingConstants.CENTER);
        label.setFont(new Font("宋体", Font.PLAIN, 24));
        panel.add(label, BorderLayout.CENTER);
        
        // 添加一个简单的甘特图预览
        GanttChartPanel monthlyGanttPanel = new GanttChartPanel(taskService);
        monthlyGanttPanel.setViewMode(GanttChartPanel.ViewMode.MONTHLY);
        panel.add(monthlyGanttPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createTaskManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // 任务表格
        String[] columnNames = {"任务名称", "开始时间", "结束时间", "优先级", "状态", "类型", "项目"};
        taskTableModel = new DefaultTableModel(columnNames, 0);
        taskTable = new JTable(taskTableModel);
        JScrollPane tableScrollPane = new JScrollPane(taskTable);
        
        // 过滤和排序选项
        JPanel filterPanel = new JPanel();
        JLabel statusLabel = new JLabel("状态: ");
        JComboBox<Task.TaskStatus> statusComboBox = new JComboBox<>(Task.TaskStatus.values());
        statusComboBox.setSelectedIndex(-1); // 不选择任何状态
        
        JLabel priorityLabel = new JLabel("优先级: ");
        JComboBox<Task.Priority> priorityComboBox = new JComboBox<>(Task.Priority.values());
        priorityComboBox.setSelectedIndex(-1); // 不选择任何优先级
        
        JLabel sortLabel = new JLabel("排序: ");
        String[] sortOptions = {"默认", "按截止时间", "按优先级"};
        JComboBox<String> sortComboBox = new JComboBox<>(sortOptions);
        
        filterPanel.add(statusLabel);
        filterPanel.add(statusComboBox);
        filterPanel.add(priorityLabel);
        filterPanel.add(priorityComboBox);
        filterPanel.add(sortLabel);
        filterPanel.add(sortComboBox);
        
        // 按钮面板
        JPanel buttonPanel = new JPanel();
        JButton addButton = new JButton("添加任务");
        JButton editButton = new JButton("编辑任务");
        JButton deleteButton = new JButton("删除任务");
        JButton refreshButton = new JButton("刷新");
        JButton importButton = new JButton("导入Excel数据");
        
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(importButton);
        
        // 组装面板
        panel.add(filterPanel, BorderLayout.NORTH);
        panel.add(tableScrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        // 加载所有任务
        loadAllTasks();
        
        // 添加事件监听器
        addButton.addActionListener(e -> showAddTaskDialog());
        editButton.addActionListener(e -> showEditTaskDialog());
        deleteButton.addActionListener(e -> deleteSelectedTask());
        refreshButton.addActionListener(e -> loadAllTasks());
        importButton.addActionListener(e -> importExcelData());
        
        statusComboBox.addActionListener(e -> filterTasks(statusComboBox, priorityComboBox, sortComboBox));
        priorityComboBox.addActionListener(e -> filterTasks(statusComboBox, priorityComboBox, sortComboBox));
        sortComboBox.addActionListener(e -> filterTasks(statusComboBox, priorityComboBox, sortComboBox));
        
        return panel;
    }
    
    private JPanel createStatisticsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // 顶部月份选择器
        JPanel topPanel = new JPanel();
        JLabel monthLabel = new JLabel("选择月份: ");
        JComboBox<String> monthComboBox = new JComboBox<>(getMonthOptions());
        JButton viewStatisticsButton = new JButton("查看统计");
        
        topPanel.add(monthLabel);
        topPanel.add(monthComboBox);
        topPanel.add(viewStatisticsButton);
        
        // 中间统计卡片 - 改为4x2网格以容纳额外的九月份延迟率卡片
        JPanel statsCardsPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        
        // 统计卡片
        JPanel completedThisWeekPanel = createStatCard("本周完成任务", statisticsService.getCompletedTasksThisWeek() + " 个");
        JPanel completedThisMonthPanel = createStatCard("本月完成任务", statisticsService.getCompletedTasksThisMonth() + " 个");
        JPanel delayRatePanel = createStatCard("任务延迟率", String.format("%.2f%%", statisticsService.getTaskDelayRate()));
        JPanel completionRatePanel = createStatCard("本周完成率", String.format("%.2f%%", statisticsService.getTaskCompletionRateThisWeek()));
        JPanel inProgressPanel = createStatCard("进行中任务", statisticsService.getInProgressTasksCount() + " 个");
        JPanel upcomingPanel = createStatCard("即将开始任务", statisticsService.getUpcomingTasksCount() + " 个");
        
        // 添加九月份延迟率统计卡片
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        JPanel septemberDelayRatePanel = createStatCard(
            currentYear + "年9月任务延迟率", 
            String.format("%.2f%%", taskService.calculateSeptemberDelayRate())
        );
        JPanel emptyPanel = new JPanel(); // 用于占位
        
        // 添加到面板
        statsCardsPanel.add(completedThisWeekPanel);
        statsCardsPanel.add(completedThisMonthPanel);
        statsCardsPanel.add(delayRatePanel);
        statsCardsPanel.add(completionRatePanel);
        statsCardsPanel.add(inProgressPanel);
        statsCardsPanel.add(upcomingPanel);
        statsCardsPanel.add(septemberDelayRatePanel);
        statsCardsPanel.add(emptyPanel);
        
        // 组装面板
        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(statsCardsPanel, BorderLayout.CENTER);
        
        // 添加事件监听器
        viewStatisticsButton.addActionListener(e -> {
            String selectedMonth = (String) monthComboBox.getSelectedItem();
            updateStatisticsForMonth(selectedMonth, statsCardsPanel);
        });
        
        return panel;
    }
    
    private String[] getMonthOptions() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
        
        String[] months = new String[12]; // 显示最近12个月
        for (int i = 0; i < 12; i++) {
            cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) - i);
            months[i] = sdf.format(cal.getTime());
            cal.add(Calendar.MONTH, i); // 重置回当前月份
        }
        
        // 默认选择当前月份
        int currentMonthIndex = 0;
        for (int i = 0; i < months.length; i++) {
            if (months[i].equals(sdf.format(Calendar.getInstance().getTime()))) {
                currentMonthIndex = i;
                break;
            }
        }
        
        // 排序，让当前月份在第一位
        String[] sortedMonths = new String[12];
        sortedMonths[0] = months[currentMonthIndex];
        int index = 1;
        for (int i = 0; i < months.length; i++) {
            if (i != currentMonthIndex) {
                sortedMonths[index++] = months[i];
            }
        }
        
        return sortedMonths;
    }
    
    private void updateStatisticsForMonth(String monthStr, JPanel statsCardsPanel) {
        try {
            // 解析月份字符串
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
            Date monthDate = sdf.parse(monthStr);
            
            // 获取该月的所有任务
            List<Task> tasksInMonth = taskService.getTasksByMonth(monthDate);
            
            // 计算统计数据
            int completedTasks = 0;
            int delayedTasks = 0;
            int totalTasks = tasksInMonth.size();
            int inProgressTasks = 0;
            int upcomingTasks = 0;
            
            Date today = DateUtil.getToday();
            
            for (Task task : tasksInMonth) {
                if (task.getStatus() == Task.TaskStatus.COMPLETED) {
                    completedTasks++;
                    // 检查是否延迟完成（如果任务的完成时间晚于结束时间）
                    // 注意：当前Task类中没有记录完成时间，这里简化判断为任务是否在结束时间之后仍未完成
                    // 由于我们无法知道实际完成时间，这里只是一个示例逻辑
                } else if (task.getStatus() == Task.TaskStatus.IN_PROGRESS) {
                    inProgressTasks++;
                }
                
                // 检查是否即将开始（未来3天内）
                if (task.getStartTime().after(today) && task.getStartTime().before(DateUtil.addDays(today, 3))) {
                    upcomingTasks++;
                }
                
                // 检查是否已经延迟（结束时间已过且任务未完成）
                if (task.getEndTime().before(today) && task.getStatus() != Task.TaskStatus.COMPLETED) {
                    delayedTasks++;
                }
            }
            
            // 计算完成率和延迟率
            double completionRate = totalTasks > 0 ? (double) completedTasks / totalTasks * 100 : 0;
            double delayRate = totalTasks > 0 ? (double) delayedTasks / totalTasks * 100 : 0;
            
            // 更新统计卡片
            statsCardsPanel.removeAll();
            
            JPanel completedTasksPanel = createStatCard(monthStr + " 完成任务", completedTasks + " 个");
            JPanel totalTasksPanel = createStatCard(monthStr + " 总任务", totalTasks + " 个");
            JPanel delayRatePanel = createStatCard(monthStr + " 任务延迟率", String.format("%.2f%%", delayRate));
            JPanel completionRatePanel = createStatCard(monthStr + " 任务完成率", String.format("%.2f%%", completionRate));
            JPanel inProgressPanel = createStatCard(monthStr + " 进行中任务", inProgressTasks + " 个");
            JPanel upcomingPanel = createStatCard(monthStr + " 即将开始任务", upcomingTasks + " 个");
            
            statsCardsPanel.add(completedTasksPanel);
            statsCardsPanel.add(totalTasksPanel);
            statsCardsPanel.add(delayRatePanel);
            statsCardsPanel.add(completionRatePanel);
            statsCardsPanel.add(inProgressPanel);
            statsCardsPanel.add(upcomingPanel);
            
            // 刷新面板
            statsCardsPanel.revalidate();
            statsCardsPanel.repaint();
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "解析月份失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void importExcelData() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("选择CSV文件");
        
        // 设置文件过滤器
        FileNameExtensionFilter filter = new FileNameExtensionFilter("CSV文件", "csv");
        fileChooser.setFileFilter(filter);
        
        int returnValue = fileChooser.showOpenDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                // 调用Excel导入服务导入数据
                int importedCount = excelImportService.importTasks(selectedFile);
                
                JOptionPane.showMessageDialog(this, 
                        "导入成功！共导入 " + importedCount + " 条任务数据。", 
                        "成功", JOptionPane.INFORMATION_MESSAGE);
                
                // 刷新任务列表
                loadAllTasks();
                loadTasksForToday();
                
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, 
                        "导入失败：" + e.getMessage(), 
                        "错误", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }
    
    private JPanel createStatCard(String title, String value) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(title));
        panel.setBackground(Color.LIGHT_GRAY);
        
        JLabel valueLabel = new JLabel(value, SwingConstants.CENTER);
        valueLabel.setFont(new Font("宋体", Font.BOLD, 24));
        
        panel.add(valueLabel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createBackupPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // 备份列表
        DefaultListModel<String> backupListModel = new DefaultListModel<>();
        JList<String> backupList = new JList<>(backupListModel);
        JScrollPane listScrollPane = new JScrollPane(backupList);
        
        // 按钮面板
        JPanel buttonPanel = new JPanel();
        JButton backupNowButton = new JButton("立即备份");
        JButton restoreButton = new JButton("恢复备份");
        JButton deleteBackupButton = new JButton("删除备份");
        JButton refreshButton = new JButton("刷新列表");
        
        buttonPanel.add(backupNowButton);
        buttonPanel.add(restoreButton);
        buttonPanel.add(deleteBackupButton);
        buttonPanel.add(refreshButton);
        
        // 组装面板
        panel.add(listScrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        // 加载备份列表
        loadBackupList(backupListModel);
        
        // 添加事件监听器
        backupNowButton.addActionListener(e -> performBackup());
        restoreButton.addActionListener(e -> restoreFromSelectedBackup(backupList));
        deleteBackupButton.addActionListener(e -> deleteSelectedBackup(backupList, backupListModel));
        refreshButton.addActionListener(e -> loadBackupList(backupListModel));
        
        return panel;
    }
    
    // 加载今日任务
    private void loadTasksForToday() {
        Date today = DateUtil.getToday();
        List<Task> tasks = taskService.getTasksByDate(today);
        updateTaskTable(tasks);
        ganttChartPanel.updateGanttChart(today);
    }
    
    // 加载指定日期的任务
    private void loadTasksForDate(String dateStr) {
        try {
            Date date = DateUtil.parseDate(dateStr, DateUtil.DATE_ONLY_FORMAT);
            List<Task> tasks = taskService.getTasksByDate(date);
            updateTaskTable(tasks);
            ganttChartPanel.updateGanttChart(date);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "日期格式错误，请使用YYYY-MM-DD格式", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // 加载所有任务
    private void loadAllTasks() {
        List<Task> tasks = taskService.getAllTasks();
        updateTaskTable(tasks);
    }
    
    // 更新任务表格
    private void updateTaskTable(List<Task> tasks) {
        taskTableModel.setRowCount(0); // 清空表格
        
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        
        for (Task task : tasks) {
            Object[] rowData = {
                task.getName(),
                timeFormat.format(task.getStartTime()),
                timeFormat.format(task.getEndTime()),
                task.getPriority(),
                task.getStatus(),
                task.getType(),
                task.getProject() != null ? task.getProject().getName() : "无"
            };
            taskTableModel.addRow(rowData);
        }
    }
    
    // 显示添加任务对话框
    private void showAddTaskDialog() {
        TaskDialog dialog = new TaskDialog(this, "添加任务", taskService);
        dialog.setVisible(true);
        if (dialog.isConfirmed()) {
            // 刷新任务列表
            if (tabbedPane.getSelectedIndex() == 0) { // 日视图
                loadTasksForToday();
            } else if (tabbedPane.getSelectedIndex() == 3) { // 任务管理
                loadAllTasks();
            }
        }
    }
    
    // 显示编辑任务对话框
    private void showEditTaskDialog() {
        int selectedRow = taskTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请先选择一个任务", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        String taskName = (String) taskTableModel.getValueAt(selectedRow, 0);
        // 查找对应的任务
        List<Task> tasks = taskService.getAllTasks();
        Task selectedTask = null;
        for (Task task : tasks) {
            if (task.getName().equals(taskName)) {
                selectedTask = task;
                break;
            }
        }
        
        if (selectedTask != null) {
            TaskDialog dialog = new TaskDialog(this, "编辑任务", taskService, selectedTask);
            dialog.setVisible(true);
            if (dialog.isConfirmed()) {
                // 刷新任务列表
                if (tabbedPane.getSelectedIndex() == 0) { // 日视图
                    loadTasksForToday();
                } else if (tabbedPane.getSelectedIndex() == 3) { // 任务管理
                    loadAllTasks();
                }
            }
        }
    }
    
    // 删除选中的任务
    private void deleteSelectedTask() {
        int selectedRow = taskTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请先选择一个任务", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        String taskName = (String) taskTableModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "确定要删除任务'" + taskName + "'吗？", "确认删除", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            // 查找对应的任务
            List<Task> tasks = taskService.getAllTasks();
            for (Task task : tasks) {
                if (task.getName().equals(taskName)) {
                    taskService.deleteTask(task.getId());
                    break;
                }
            }
            
            // 刷新任务列表
            if (tabbedPane.getSelectedIndex() == 0) { // 日视图
                loadTasksForToday();
            } else if (tabbedPane.getSelectedIndex() == 3) { // 任务管理
                loadAllTasks();
            }
        }
    }
    
    // 导航到前一天
    private void navigateToPreviousDay(JTextField dateField) {
        try {
            Date date = DateUtil.parseDate(dateField.getText(), DateUtil.DATE_ONLY_FORMAT);
            Date previousDay = DateUtil.addDays(date, -1);
            dateField.setText(DateUtil.formatDate(previousDay, DateUtil.DATE_ONLY_FORMAT));
            loadTasksForDate(dateField.getText());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "日期格式错误", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // 导航到后一天
    private void navigateToNextDay(JTextField dateField) {
        try {
            Date date = DateUtil.parseDate(dateField.getText(), DateUtil.DATE_ONLY_FORMAT);
            Date nextDay = DateUtil.addDays(date, 1);
            dateField.setText(DateUtil.formatDate(nextDay, DateUtil.DATE_ONLY_FORMAT));
            loadTasksForDate(dateField.getText());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "日期格式错误", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // 过滤任务
    private void filterTasks(JComboBox<Task.TaskStatus> statusComboBox, 
                           JComboBox<Task.Priority> priorityComboBox, 
                           JComboBox<String> sortComboBox) {
        List<Task> tasks = taskService.getAllTasks();
        
        // 按状态过滤
        Task.TaskStatus selectedStatus = (Task.TaskStatus) statusComboBox.getSelectedItem();
        if (selectedStatus != null) {
            tasks = tasks.stream()
                    .filter(task -> task.getStatus() == selectedStatus)
                    .collect(java.util.stream.Collectors.toList());
        }
        
        // 按优先级过滤
        Task.Priority selectedPriority = (Task.Priority) priorityComboBox.getSelectedItem();
        if (selectedPriority != null) {
            tasks = tasks.stream()
                    .filter(task -> task.getPriority() == selectedPriority)
                    .collect(java.util.stream.Collectors.toList());
        }
        
        // 排序
        String sortOption = (String) sortComboBox.getSelectedItem();
        if ("按截止时间".equals(sortOption)) {
            tasks.sort(Comparator.comparing(Task::getEndTime));
        } else if ("按优先级".equals(sortOption)) {
            tasks.sort(Comparator.comparing(Task::getPriority).reversed());
        }
        
        // 更新表格
        updateTaskTable(tasks);
    }
    
    // 加载备份列表
    private void loadBackupList(DefaultListModel<String> model) {
        model.clear();
        List<String> backupFiles = backupService.listBackupFiles();
        for (String file : backupFiles) {
            File backupFile = new File(file);
            model.addElement(backupFile.getName() + " (" + new Date(backupFile.lastModified()) + ")");
        }
    }
    
    // 执行备份
    private void performBackup() {
        String backupPath = backupService.backupData();
        if (backupPath != null) {
            JOptionPane.showMessageDialog(this, "备份成功！备份文件保存在：" + backupPath, "成功", JOptionPane.INFORMATION_MESSAGE);
            // 刷新备份列表
            if (tabbedPane.getSelectedIndex() == 5) { // 备份面板
                JList<String> backupList = (JList<String>) ((JScrollPane) backupPanel.getComponent(0)).getViewport().getView();
                DefaultListModel<String> model = (DefaultListModel<String>) backupList.getModel();
                loadBackupList(model);
            }
        } else {
            JOptionPane.showMessageDialog(this, "备份失败！", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // 从选中的备份恢复
    private void restoreFromSelectedBackup(JList<String> backupList) {
        int selectedIndex = backupList.getSelectedIndex();
        if (selectedIndex == -1) {
            JOptionPane.showMessageDialog(this, "请先选择一个备份文件", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, "确定要恢复此备份吗？当前数据将会被覆盖。", "确认恢复", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            List<String> backupFiles = backupService.listBackupFiles();
            String selectedBackup = backupFiles.get(selectedIndex);
            boolean success = backupService.restoreFromBackup(selectedBackup);
            
            if (success) {
                JOptionPane.showMessageDialog(this, "恢复成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                // 刷新所有任务列表
                loadAllTasks();
                loadTasksForToday();
            } else {
                JOptionPane.showMessageDialog(this, "恢复失败！", "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    // 删除选中的备份
    private void deleteSelectedBackup(JList<String> backupList, DefaultListModel<String> model) {
        int selectedIndex = backupList.getSelectedIndex();
        if (selectedIndex == -1) {
            JOptionPane.showMessageDialog(this, "请先选择一个备份文件", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, "确定要删除此备份文件吗？", "确认删除", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            List<String> backupFiles = backupService.listBackupFiles();
            String selectedBackup = backupFiles.get(selectedIndex);
            File file = new File(selectedBackup);
            if (file.delete()) {
                model.remove(selectedIndex);
                JOptionPane.showMessageDialog(this, "删除成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "删除失败！", "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    // 启动提醒检查线程
    private void startReminderChecker() {
        Thread reminderThread = new Thread(() -> {
            while (true) {
                try {
                    // 检查是否有需要提醒的任务
                    List<Reminder> reminders = taskService.getActiveReminders();
                    for (Reminder reminder : reminders) {
                        // 在EDT线程中显示提醒对话框
                        SwingUtilities.invokeLater(() -> {
                            Task task = reminder.getTask();
                            JOptionPane.showMessageDialog(this,
                                    "任务提醒：" + task.getName() + "\n开始时间：" + DateUtil.formatDate(task.getStartTime()) + "\n优先级：" + task.getPriority(),
                                    "任务提醒",
                                    JOptionPane.INFORMATION_MESSAGE);
                            taskService.markReminderAsNotified(reminder.getId());
                        });
                    }
                    
                    // 每分钟检查一次
                    Thread.sleep(60 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }
        });
        reminderThread.setDaemon(true);
        reminderThread.start();
    }
    
    public static void main(String[] args) {
        // 在EDT线程中启动应用程序
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}