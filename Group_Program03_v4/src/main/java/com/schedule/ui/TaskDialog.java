package com.schedule.ui;

import com.schedule.model.Task;
import com.schedule.model.Project;
import com.schedule.service.TaskService;
import com.schedule.util.DateUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.ParseException;
import java.util.Date;

/**
 * 任务对话框 - 用于添加和编辑任务
 */
public class TaskDialog extends JDialog {
    private TaskService taskService;
    private Task task; // 要编辑的任务，如果是添加则为null
    private boolean confirmed = false;
    
    // UI组件
    private JTextField nameField;
    private JTextArea contentArea;
    private JTextField startDateField;
    private JTextField startTimeField;
    private JTextField endDateField;
    private JTextField endTimeField;
    private JComboBox<Task.Priority> priorityComboBox;
    private JComboBox<Task.TaskStatus> statusComboBox;
    private JComboBox<Task.TaskType> typeComboBox;
    private JComboBox<Project> projectComboBox;
    private JSpinner reminderSpinner;
    
    public TaskDialog(Frame owner, String title, TaskService taskService) {
        super(owner, title, true);
        this.taskService = taskService;
        this.task = null;
        initUI();
    }
    
    public TaskDialog(Frame owner, String title, TaskService taskService, Task task) {
        super(owner, title, true);
        this.taskService = taskService;
        this.task = task;
        initUI();
        loadTaskData();
    }
    
    private void initUI() {
        setSize(500, 400);
        setLocationRelativeTo(getOwner());
        
        // 创建面板
        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // 添加组件
        panel.add(new JLabel("任务名称：*"));
        nameField = new JTextField();
        panel.add(nameField);
        
        panel.add(new JLabel("任务内容："));
        contentArea = new JTextArea(3, 20);
        JScrollPane contentScrollPane = new JScrollPane(contentArea);
        panel.add(contentScrollPane);
        
        panel.add(new JLabel("开始日期：*"));
        startDateField = new JTextField(DateUtil.formatDate(new Date(), DateUtil.DATE_ONLY_FORMAT));
        panel.add(startDateField);
        
        panel.add(new JLabel("开始时间：*"));
        startTimeField = new JTextField("09:00");
        panel.add(startTimeField);
        
        panel.add(new JLabel("结束日期：*"));
        endDateField = new JTextField(DateUtil.formatDate(new Date(), DateUtil.DATE_ONLY_FORMAT));
        panel.add(endDateField);
        
        panel.add(new JLabel("结束时间：*"));
        endTimeField = new JTextField("10:00");
        panel.add(endTimeField);
        
        panel.add(new JLabel("优先级：*"));
        priorityComboBox = new JComboBox<>(Task.Priority.values());
        priorityComboBox.setSelectedItem(Task.Priority.MEDIUM);
        panel.add(priorityComboBox);
        
        panel.add(new JLabel("状态："));
        statusComboBox = new JComboBox<>(Task.TaskStatus.values());
        statusComboBox.setSelectedItem(Task.TaskStatus.NOT_STARTED);
        panel.add(statusComboBox);
        
        panel.add(new JLabel("任务类型：*"));
        typeComboBox = new JComboBox<>(Task.TaskType.values());
        typeComboBox.setSelectedItem(Task.TaskType.DAILY);
        panel.add(typeComboBox);
        
        panel.add(new JLabel("所属项目："));
        projectComboBox = new JComboBox<>();
        loadProjects();
        panel.add(projectComboBox);
        
        panel.add(new JLabel("提前提醒(分钟)："));
        reminderSpinner = new JSpinner(new SpinnerNumberModel(30, 0, 1440, 5));
        panel.add(reminderSpinner);
        
        // 添加按钮面板
        JPanel buttonPanel = new JPanel();
        JButton okButton = new JButton("确定");
        JButton cancelButton = new JButton("取消");
        
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        
        // 组装对话框
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(panel, BorderLayout.CENTER);
        contentPane.add(buttonPanel, BorderLayout.SOUTH);
        
        // 添加事件监听器
        okButton.addActionListener(e -> onOK());
        cancelButton.addActionListener(e -> onCancel());
        
        // 添加窗口监听器
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });
        
        // 添加键盘监听器
        getRootPane().setDefaultButton(okButton);
    }
    
    // 加载项目列表
    private void loadProjects() {
        projectComboBox.addItem(null); // 添加空选项
        for (Project project : taskService.getAllProjects()) {
            projectComboBox.addItem(project);
        }
    }
    
    // 加载任务数据（编辑模式）
    private void loadTaskData() {
        if (task == null) {
            return;
        }
        
        nameField.setText(task.getName());
        contentArea.setText(task.getContent());
        startDateField.setText(DateUtil.formatDate(task.getStartTime(), DateUtil.DATE_ONLY_FORMAT));
        startTimeField.setText(DateUtil.formatDate(task.getStartTime(), DateUtil.TIME_ONLY_FORMAT));
        endDateField.setText(DateUtil.formatDate(task.getEndTime(), DateUtil.DATE_ONLY_FORMAT));
        endTimeField.setText(DateUtil.formatDate(task.getEndTime(), DateUtil.TIME_ONLY_FORMAT));
        priorityComboBox.setSelectedItem(task.getPriority());
        statusComboBox.setSelectedItem(task.getStatus());
        typeComboBox.setSelectedItem(task.getType());
        
        // 选择项目
        Project project = task.getProject();
        if (project != null) {
            for (int i = 0; i < projectComboBox.getItemCount(); i++) {
                Project item = projectComboBox.getItemAt(i);
                if (item != null && item.getId().equals(project.getId())) {
                    projectComboBox.setSelectedIndex(i);
                    break;
                }
            }
        }
        
        reminderSpinner.setValue(task.getReminderMinutes());
    }
    
    // 验证输入
    private boolean validateInput() {
        // 验证必填项
        if (nameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入任务名称", "错误", JOptionPane.ERROR_MESSAGE);
            nameField.requestFocus();
            return false;
        }
        
        // 验证日期和时间格式
        try {
            // 解析开始时间
            String startDateTime = startDateField.getText().trim() + " " + startTimeField.getText().trim();
            Date startTime = DateUtil.parseDate(startDateTime, "yyyy-MM-dd HH:mm");
            
            // 解析结束时间
            String endDateTime = endDateField.getText().trim() + " " + endTimeField.getText().trim();
            Date endTime = DateUtil.parseDate(endDateTime, "yyyy-MM-dd HH:mm");
            
            // 验证时间逻辑
            if (endTime.before(startTime)) {
                JOptionPane.showMessageDialog(this, "结束时间不能早于开始时间", "错误", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            
        } catch (ParseException e) {
            JOptionPane.showMessageDialog(this, "日期或时间格式错误，请使用YYYY-MM-DD和HH:mm格式", "错误", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        return true;
    }
    
    // 确定按钮事件处理
    private void onOK() {
        if (!validateInput()) {
            return;
        }
        
        try {
            // 解析日期和时间
            String startDateTime = startDateField.getText().trim() + " " + startTimeField.getText().trim();
            Date startTime = DateUtil.parseDate(startDateTime, "yyyy-MM-dd HH:mm");
            
            String endDateTime = endDateField.getText().trim() + " " + endTimeField.getText().trim();
            Date endTime = DateUtil.parseDate(endDateTime, "yyyy-MM-dd HH:mm");
            
            // 获取其他参数
            String name = nameField.getText().trim();
            String content = contentArea.getText().trim();
            Task.Priority priority = (Task.Priority) priorityComboBox.getSelectedItem();
            Task.TaskStatus status = (Task.TaskStatus) statusComboBox.getSelectedItem();
            Task.TaskType type = (Task.TaskType) typeComboBox.getSelectedItem();
            Project project = (Project) projectComboBox.getSelectedItem();
            int reminderMinutes = (Integer) reminderSpinner.getValue();
            
            if (task == null) {
                // 添加任务
                task = new Task(null, name, startTime, endTime, priority, type, project);
                task.setContent(content);
                task.setReminderMinutes(reminderMinutes);
                taskService.addTask(task);
            } else {
                // 编辑任务
                task.setName(name);
                task.setContent(content);
                task.setStartTime(startTime);
                task.setEndTime(endTime);
                task.setPriority(priority);
                task.setStatus(status);
                task.setType(type);
                task.setProject(project);
                task.setReminderMinutes(reminderMinutes);
                taskService.updateTask(task);
            }
            
            confirmed = true;
            dispose();
            
        } catch (ParseException e) {
            JOptionPane.showMessageDialog(this, "日期或时间格式错误", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // 取消按钮事件处理
    private void onCancel() {
        confirmed = false;
        dispose();
    }
    
    // 获取用户是否确认
    public boolean isConfirmed() {
        return confirmed;
    }
}