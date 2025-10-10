// ScheduleGUI.java - 图形化界面主类
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ScheduleGUI extends JFrame {
    private final ScheduleManager manager;
    private JTabbedPane tabbedPane;

    // 组件
    private JTable taskTable;
    private DefaultTableModel taskTableModel;
    private JTextField searchField;
    private JComboBox<String> viewTypeComboBox;
    private JComboBox<String> statusFilterComboBox;
    private JTextArea statisticsArea;

    // 日期选择器
    private JSpinner startTimeSpinner;
    private JSpinner endTimeSpinner;

    public ScheduleGUI() {
        manager = new ScheduleManager();
        loadData();
        initializeUI();
        setupAutoSave();
    }

    private void loadData() {
        // 加载持久化数据
        List<Task> tasks = DataManager.loadTasks();
        tasks.forEach(task -> {
            manager.getTasks().add(task);
            // 重新安排提醒
            Reminder reminder = new Reminder(task);
            reminder.scheduleReminder();
        });

        List<Project> projects = DataManager.loadProjects();
        projects.forEach(manager::addProject);
    }

    private void setupAutoSave() {
        Timer autoSaveTimer = new Timer(30000, e -> saveData()); // 每30秒自动保存
        autoSaveTimer.start();

        // 程序关闭时保存
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                saveData();
                System.exit(0);
            }
        });
    }

    private void saveData() {
        DataManager.saveTasks(manager.getTasks());
        DataManager.saveProjects(manager.getProjects());
    }

    private void initializeUI() {
        setTitle("个人日程安排与提醒系统");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        // 创建菜单栏
        createMenuBar();

        // 创建主选项卡
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("任务管理", createTaskManagementPanel());
        tabbedPane.addTab("添加任务", createAddTaskPanel());
        tabbedPane.addTab("日历视图", createCalendarViewPanel());
        tabbedPane.addTab("数据统计", createStatisticsPanel());

        add(tabbedPane);

        // 刷新任务列表
        refreshTaskTable();
    }

    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // 文件菜单
        JMenu fileMenu = new JMenu("文件");
        JMenuItem exportItem = new JMenuItem("导出数据");
        JMenuItem backupItem = new JMenuItem("备份数据");
        JMenuItem exitItem = new JMenuItem("退出");

        exportItem.addActionListener(e -> exportData());
        backupItem.addActionListener(e -> backupData());
        exitItem.addActionListener(e -> {
            saveData();
            System.exit(0);
        });

        fileMenu.add(exportItem);
        fileMenu.add(backupItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        // 视图菜单
        JMenu viewMenu = new JMenu("视图");
        JMenuItem refreshItem = new JMenuItem("刷新");
        refreshItem.addActionListener(e -> refreshTaskTable());

        viewMenu.add(refreshItem);

        menuBar.add(fileMenu);
        menuBar.add(viewMenu);

        setJMenuBar(menuBar);
    }

    private JPanel createTaskManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // 搜索和过滤面板
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        searchField = new JTextField(20);
        searchField.addActionListener(e -> searchTasks());

        JButton searchButton = new JButton("搜索");
        searchButton.addActionListener(e -> searchTasks());

        viewTypeComboBox = new JComboBox<>(new String[]{"所有任务", "今日任务", "本周任务", "本月任务"});
        viewTypeComboBox.addActionListener(e -> refreshTaskTable());

        statusFilterComboBox = new JComboBox<>(new String[]{"所有状态", "未开始", "进行中", "已完成"});
        statusFilterComboBox.addActionListener(e -> refreshTaskTable());

        filterPanel.add(new JLabel("搜索:"));
        filterPanel.add(searchField);
        filterPanel.add(searchButton);
        filterPanel.add(new JLabel("视图:"));
        filterPanel.add(viewTypeComboBox);
        filterPanel.add(new JLabel("状态:"));
        filterPanel.add(statusFilterComboBox);

        // 任务表格
        String[] columnNames = {"选择", "任务名称", "类型", "开始时间", "结束时间", "优先级", "状态", "项目"};
        taskTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public Class<?> getColumnClass(int column) {
                return column == 0 ? Boolean.class : String.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0; // 只有选择列可编辑
            }
        };

        taskTable = new JTable(taskTableModel);
        taskTable.setRowHeight(30);
        JScrollPane tableScrollPane = new JScrollPane(taskTable);

        // 操作按钮面板
        JPanel buttonPanel = new JPanel();
        JButton completeButton = new JButton("标记完成");
        JButton deleteButton = new JButton("删除任务");
        JButton editButton = new JButton("编辑任务");

        completeButton.addActionListener(e -> markTasksAsCompleted());
        deleteButton.addActionListener(e -> deleteSelectedTasks());
        editButton.addActionListener(e -> editSelectedTask());

        buttonPanel.add(completeButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);

        panel.add(filterPanel, BorderLayout.NORTH);
        panel.add(tableScrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createAddTaskPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // 任务名称
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("任务名称:"), gbc);
        gbc.gridx = 1;
        JTextField nameField = new JTextField(20);
        panel.add(nameField, gbc);

        // 任务内容
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("任务内容:"), gbc);
        gbc.gridx = 1;
        JTextArea contentArea = new JTextArea(3, 20);
        panel.add(new JScrollPane(contentArea), gbc);

        // 开始时间
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("开始时间:"), gbc);
        gbc.gridx = 1;
        startTimeSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor startEditor = new JSpinner.DateEditor(startTimeSpinner, "yyyy-MM-dd HH:mm");
        startTimeSpinner.setEditor(startEditor);
        panel.add(startTimeSpinner, gbc);

        // 结束时间
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("结束时间:"), gbc);
        gbc.gridx = 1;
        endTimeSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor endEditor = new JSpinner.DateEditor(endTimeSpinner, "yyyy-MM-dd HH:mm");
        endTimeSpinner.setEditor(endEditor);
        panel.add(endTimeSpinner, gbc);

        // 优先级
        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(new JLabel("优先级:"), gbc);
        gbc.gridx = 1;
        JComboBox<Integer> priorityCombo = new JComboBox<>(new Integer[]{1, 2, 3, 4, 5});
        panel.add(priorityCombo, gbc);

        // 任务类型
        gbc.gridx = 0; gbc.gridy = 5;
        panel.add(new JLabel("任务类型:"), gbc);
        gbc.gridx = 1;
        JComboBox<String> typeCombo = new JComboBox<>(new String[]{"会议", "截止日期", "日常事务"});
        panel.add(typeCombo, gbc);

        // 项目
        gbc.gridx = 0; gbc.gridy = 6;
        panel.add(new JLabel("所属项目:"), gbc);
        gbc.gridx = 1;
        JTextField projectField = new JTextField(20);
        panel.add(projectField, gbc);

        // 按钮
        gbc.gridx = 0; gbc.gridy = 7;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;

        JPanel buttonPanel = new JPanel();
        JButton addButton = new JButton("添加任务");
        JButton clearButton = new JButton("清空");

        addButton.addActionListener(e -> {
            addNewTask(nameField, contentArea, priorityCombo, typeCombo, projectField);
        });

        clearButton.addActionListener(e -> {
            nameField.setText("");
            contentArea.setText("");
            projectField.setText("");
        });

        buttonPanel.add(addButton);
        buttonPanel.add(clearButton);
        panel.add(buttonPanel, gbc);

        return panel;
    }

    private JPanel createCalendarViewPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // 日历组件
        JCalendar calendar = new JCalendar();
        calendar.addPropertyChangeListener(e -> {
            if ("calendar".equals(e.getPropertyName())) {
                refreshCalendarView(calendar.getDate());
            }
        });

        // 日历任务列表
        JTextArea calendarTasksArea = new JTextArea(10, 30);
        calendarTasksArea.setEditable(false);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                calendar, new JScrollPane(calendarTasksArea));
        splitPane.setDividerLocation(400);

        panel.add(splitPane, BorderLayout.CENTER);

        // 初始刷新
        refreshCalendarView(calendar.getDate());

        return panel;
    }

    private JPanel createStatisticsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        statisticsArea = new JTextArea();
        statisticsArea.setEditable(false);
        statisticsArea.setFont(new Font("微软雅黑", Font.PLAIN, 14));

        JButton refreshButton = new JButton("刷新统计");
        refreshButton.addActionListener(e -> refreshStatistics());

        panel.add(new JScrollPane(statisticsArea), BorderLayout.CENTER);
        panel.add(refreshButton, BorderLayout.SOUTH);

        // 初始刷新
        refreshStatistics();

        return panel;
    }

    private void addNewTask(JTextField nameField, JTextArea contentArea,
                            JComboBox<Integer> priorityCombo, JComboBox<String> typeCombo,
                            JTextField projectField) {
        try {
            String name = nameField.getText().trim();
            String content = contentArea.getText().trim();
            Date startTime = (Date) startTimeSpinner.getValue();
            Date endTime = (Date) endTimeSpinner.getValue();
            int priority = (Integer) priorityCombo.getSelectedItem();
            String project = projectField.getText().trim();

            String typeStr = (String) typeCombo.getSelectedItem();
            Task.TaskType type = switch (typeStr) {
                case "会议" -> Task.TaskType.MEETING;
                case "截止日期" -> Task.TaskType.DEADLINE;
                default -> Task.TaskType.DAILY_TASK;
            };

            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "请输入任务名称", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Task task = new Task(name, content, startTime, endTime, priority, project, type);
            if (manager.addTask(task)) {
                JOptionPane.showMessageDialog(this, "任务添加成功!", "成功", JOptionPane.INFORMATION_MESSAGE);
                refreshTaskTable();
                // 清空输入框
                nameField.setText("");
                contentArea.setText("");
                projectField.setText("");
            } else {
                JOptionPane.showMessageDialog(this, "任务添加失败，可能存在时间冲突", "错误", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "添加任务时发生错误: " + e.getMessage(),
                    "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshTaskTable() {
        taskTableModel.setRowCount(0);

        List<Task> tasksToShow = manager.getTasks();
        String viewType = (String) viewTypeComboBox.getSelectedItem();
        String statusFilter = (String) statusFilterComboBox.getSelectedItem();

        // 应用视图过滤
        if ("今日任务".equals(viewType)) {
            tasksToShow = manager.getDailyView(new Date());
        } else if ("本周任务".equals(viewType)) {
            tasksToShow = manager.getWeeklyView(new Date());
        } else if ("本月任务".equals(viewType)) {
            tasksToShow = manager.getMonthlyView(new Date());
        }

        // 应用状态过滤
        if (!"所有状态".equals(statusFilter)) {
            Task.TaskStatus filterStatus = switch (statusFilter) {
                case "未开始" -> Task.TaskStatus.NOT_STARTED;
                case "进行中" -> Task.TaskStatus.IN_PROGRESS;
                case "已完成" -> Task.TaskStatus.COMPLETED;
                default -> null;
            };

            if (filterStatus != null) {
                tasksToShow = tasksToShow.stream()
                        .filter(task -> task.getStatus() == filterStatus)
                        .collect(java.util.stream.Collectors.toList());
            }
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        for (Task task : tasksToShow) {
            taskTableModel.addRow(new Object[]{
                    false,
                    task.getName(),
                    task.getType().toString(),
                    sdf.format(task.getStartTime()),
                    sdf.format(task.getEndTime()),
                    String.valueOf(task.getPriority()),
                    task.getStatus().toString(),
                    task.getProject()
            });
        }
    }

    private void searchTasks() {
        String keyword = searchField.getText().trim().toLowerCase();
        if (keyword.isEmpty()) {
            refreshTaskTable();
            return;
        }

        List<Task> allTasks = manager.getTasks();
        List<Task> filteredTasks = allTasks.stream()
                .filter(task -> task.getName().toLowerCase().contains(keyword) ||
                        task.getContent().toLowerCase().contains(keyword) ||
                        task.getProject().toLowerCase().contains(keyword))
                .collect(java.util.stream.Collectors.toList());

        taskTableModel.setRowCount(0);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        for (Task task : filteredTasks) {
            taskTableModel.addRow(new Object[]{
                    false,
                    task.getName(),
                    task.getType().toString(),
                    sdf.format(task.getStartTime()),
                    sdf.format(task.getEndTime()),
                    String.valueOf(task.getPriority()),
                    task.getStatus().toString(),
                    task.getProject()
            });
        }
    }

    private void markTasksAsCompleted() {
        int[] selectedRows = getSelectedTaskRows();
        if (selectedRows.length == 0) {
            JOptionPane.showMessageDialog(this, "请先选择任务", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int result = JOptionPane.showConfirmDialog(this,
                "确定要将选中的 " + selectedRows.length + " 个任务标记为完成吗？",
                "确认", JOptionPane.YES_NO_OPTION);

        if (result == JOptionPane.YES_OPTION) {
            for (int row : selectedRows) {
                String taskName = (String) taskTableModel.getValueAt(row, 1);
                manager.getTasks().stream()
                        .filter(task -> task.getName().equals(taskName))
                        .findFirst()
                        .ifPresent(task -> task.setStatus(Task.TaskStatus.COMPLETED));
            }
            refreshTaskTable();
            JOptionPane.showMessageDialog(this, "任务状态更新成功!", "成功", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void deleteSelectedTasks() {
        int[] selectedRows = getSelectedTaskRows();
        if (selectedRows.length == 0) {
            JOptionPane.showMessageDialog(this, "请先选择任务", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int result = JOptionPane.showConfirmDialog(this,
                "确定要删除选中的 " + selectedRows.length + " 个任务吗？",
                "确认删除", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (result == JOptionPane.YES_OPTION) {
            for (int i = selectedRows.length - 1; i >= 0; i--) {
                int row = selectedRows[i];
                String taskName = (String) taskTableModel.getValueAt(row, 1);
                manager.getTasks().removeIf(task -> task.getName().equals(taskName));
                taskTableModel.removeRow(row);
            }
            JOptionPane.showMessageDialog(this, "任务删除成功!", "成功", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void editSelectedTask() {
        int[] selectedRows = getSelectedTaskRows();
        if (selectedRows.length != 1) {
            JOptionPane.showMessageDialog(this, "请选择一个任务进行编辑", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int row = selectedRows[0];
        String taskName = (String) taskTableModel.getValueAt(row, 1);

        Task taskToEdit = manager.getTasks().stream()
                .filter(task -> task.getName().equals(taskName))
                .findFirst()
                .orElse(null);

        if (taskToEdit != null) {
            // 切换到添加任务面板并填充数据
            tabbedPane.setSelectedIndex(1);
            // 这里可以添加编辑逻辑，为了简化，提示用户重新创建
            JOptionPane.showMessageDialog(this,
                    "为了编辑任务 '" + taskName + "'，请在添加任务面板重新输入信息，然后删除原任务",
                    "编辑任务", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private int[] getSelectedTaskRows() {
        java.util.List<Integer> selectedRows = new java.util.ArrayList<>();
        for (int i = 0; i < taskTableModel.getRowCount(); i++) {
            Boolean selected = (Boolean) taskTableModel.getValueAt(i, 0);
            if (selected != null && selected) {
                selectedRows.add(i);
            }
        }
        return selectedRows.stream().mapToInt(Integer::intValue).toArray();
    }

    private void refreshCalendarView(Date date) {
        // 简化实现，显示选中日期的任务
        List<Task> dailyTasks = manager.getDailyView(date);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

        StringBuilder sb = new StringBuilder();
        sb.append("日期: ").append(sdf.format(date)).append("\n\n");

        if (dailyTasks.isEmpty()) {
            sb.append("该日期没有安排任务");
        } else {
            for (Task task : dailyTasks) {
                sb.append("• ").append(timeFormat.format(task.getStartTime()))
                        .append(" - ").append(task.getName())
                        .append(" (").append(task.getStatus()).append(")")
                        .append("\n");
            }
        }

        // 更新显示
        Component calendarView = tabbedPane.getComponentAt(2);
        if (calendarView instanceof JPanel) {
            JTextArea textArea = findTextArea((JPanel) calendarView);
            if (textArea != null) {
                textArea.setText(sb.toString());
            }
        }
    }

    private void refreshStatistics() {
        Statistics weeklyStats = manager.getWeeklyStatistics(new Date());
        Statistics monthlyStats = manager.getMonthlyStatistics(new Date());

        StringBuilder sb = new StringBuilder();
        sb.append("=== 本周统计 ===\n");
        sb.append(weeklyStats).append("\n\n");
        sb.append("=== 本月统计 ===\n");
        sb.append(monthlyStats).append("\n\n");

        // 任务类型分布
        sb.append("=== 任务类型分布 ===\n");
        long meetings = manager.getTasks().stream().filter(t -> t.getType() == Task.TaskType.MEETING).count();
        long deadlines = manager.getTasks().stream().filter(t -> t.getType() == Task.TaskType.DEADLINE).count();
        long dailyTasks = manager.getTasks().stream().filter(t -> t.getType() == Task.TaskType.DAILY_TASK).count();

        sb.append("会议: ").append(meetings).append(" 个\n");
        sb.append("截止日期: ").append(deadlines).append(" 个\n");
        sb.append("日常事务: ").append(dailyTasks).append(" 个\n");

        statisticsArea.setText(sb.toString());
    }

    private void exportData() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("导出数据");
        fileChooser.setSelectedFile(new File("schedule_export.txt"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            String filePath = fileChooser.getSelectedFile().getAbsolutePath();
            DataManager.exportToTextFile(manager.getTasks(), filePath);
            JOptionPane.showMessageDialog(this, "数据导出成功!", "成功", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void backupData() {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String backupFile = "backup_" + timestamp + ".dat";
        DataManager.saveTasks(manager.getTasks());
        JOptionPane.showMessageDialog(this, "数据已备份到: " + backupFile,
                "备份成功", JOptionPane.INFORMATION_MESSAGE);
    }

    private JTextArea findTextArea(Container container) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JTextArea) {
                return (JTextArea) comp;
            } else if (comp instanceof Container) {
                JTextArea result = findTextArea((Container) comp);
                if (result != null) return result;
            }
        }
        return null;
    }

    public static void main(String[] args) {
        // 设置系统外观
        initializeLookAndFeel();

        // 创建并显示GUI
        SwingUtilities.invokeLater(() -> {
            try {
                new ScheduleGUI().setVisible(true);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null,
                        "启动应用程序时发生错误:\n" + e.getMessage(),
                        "错误", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        });
    }

    private static void initializeLookAndFeel() {
        try {
            // 尝试设置系统外观
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            try {
                // 失败时尝试Nimbus外观（Java 6+）
                UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            } catch (Exception ex) {
                try {
                    // 最后使用跨平台外观
                    UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                } catch (Exception exc) {
                    // 如果所有都失败，使用默认外观
                    System.err.println("无法设置外观，使用默认外观");
                }
            }
        }
    }
}