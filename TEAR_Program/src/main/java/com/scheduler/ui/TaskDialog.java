package com.scheduler.ui;

import com.scheduler.model.Task;
import com.scheduler.model.Project;
import com.scheduler.service.ProjectService;
import com.scheduler.service.ReminderService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 任务对话框类：用于添加和编辑任务
 */
public class TaskDialog extends JDialog {
    private Task task; // 要编辑的任务，为null时表示添加新任务
    private ProjectService projectService;
    private ReminderService reminderService;
    private boolean confirmed = false; // 标记用户是否确认了操作
    
    // UI组件
    private JTextField nameField;
    private JTextArea contentArea;
    private JTextField startTimeField;
    private JTextField endTimeField;
    private JComboBox<Task.Priority> priorityComboBox;
    private JComboBox<Task.TaskStatus> statusComboBox;
    private JComboBox<String> projectComboBox;
    private JComboBox<Task.TaskType> typeComboBox;
    private JSpinner reminderSpinner;
    
    private SimpleDateFormat dateFormat; // 日期格式化对象
    
    // 构造方法：用于添加新任务
    public TaskDialog(Frame owner, ProjectService projectService, ReminderService reminderService) {
        super(owner, "添加任务", true);
        this.task = new Task();
        this.projectService = projectService;
        this.reminderService = reminderService;
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        
        initUI();
        pack();
        setLocationRelativeTo(owner);
    }
    
    // 构造方法：用于编辑现有任务
    public TaskDialog(Frame owner, Task task, ProjectService projectService, ReminderService reminderService) {
        super(owner, "编辑任务", true);
        this.task = task;
        this.projectService = projectService;
        this.reminderService = reminderService;
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        
        initUI();
        loadTaskData();
        pack();
        setLocationRelativeTo(owner);
    }
    
    // 初始化UI
    private void initUI() {
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // 创建表单面板
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // 任务名称
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("任务名称："), gbc);
        
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        nameField = new JTextField(30);
        formPanel.add(nameField, gbc);
        
        // 任务内容
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("任务内容："), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 3;
        gbc.gridheight = 3;
        gbc.fill = GridBagConstraints.BOTH;
        contentArea = new JTextArea(5, 30);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        JScrollPane contentScrollPane = new JScrollPane(contentArea);
        formPanel.add(contentScrollPane, gbc);
        
        // 开始时间
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("开始时间："), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        startTimeField = new JTextField(15);
        startTimeField.setToolTipText("格式：yyyy-MM-dd HH:mm");
        formPanel.add(startTimeField, gbc);
        
        // 截止时间
        gbc.gridx = 2;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("截止时间："), gbc);
        
        gbc.gridx = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        endTimeField = new JTextField(15);
        endTimeField.setToolTipText("格式：yyyy-MM-dd HH:mm");
        formPanel.add(endTimeField, gbc);
        
        // 优先级
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("优先级："), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        priorityComboBox = new JComboBox<>(Task.Priority.values());
        formPanel.add(priorityComboBox, gbc);
        
        // 状态
        gbc.gridx = 2;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("状态："), gbc);
        
        gbc.gridx = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        statusComboBox = new JComboBox<>(Task.TaskStatus.values());
        formPanel.add(statusComboBox, gbc);
        
        // 所属项目
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("所属项目："), gbc);
        
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        projectComboBox = new JComboBox<>();
        loadProjects();
        formPanel.add(projectComboBox, gbc);
        
        // 任务类型
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("任务类型："), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        typeComboBox = new JComboBox<>(Task.TaskType.values());
        formPanel.add(typeComboBox, gbc);
        
        // 提前提醒时间
        gbc.gridx = 2;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("提前提醒(分钟)："), gbc);
        
        gbc.gridx = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        SpinnerModel reminderModel = new SpinnerNumberModel(30, 0, 1440, 5); // 默认30分钟，范围0-1440分钟(24小时)，步长5分钟
        reminderSpinner = new JSpinner(reminderModel);
        formPanel.add(reminderSpinner, gbc);
        
        // 添加表单面板到内容面板
        contentPanel.add(formPanel, BorderLayout.CENTER);
        
        // 创建按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        
        JButton okButton = new JButton("确定");
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (validateForm()) {
                    saveTaskData();
                    confirmed = true;
                    dispose();
                }
            }
        });
        
        JButton cancelButton = new JButton("取消");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        
        // 添加按钮面板到内容面板
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // 设置内容面板
        setContentPane(contentPanel);
    }
    
    // 加载项目数据到下拉框
    private void loadProjects() {
        projectComboBox.addItem("无项目"); // 添加"无项目"选项
        
        List<Project> projects = projectService.getAllProjects();
        for (Project project : projects) {
            projectComboBox.addItem(project.getId() + ": " + project.getName());
        }
    }
    
    // 加载任务数据到表单
    private void loadTaskData() {
        if (task == null) {
            return;
        }
        
        nameField.setText(task.getName());
        contentArea.setText(task.getContent());
        
        if (task.getStartTime() != null) {
            startTimeField.setText(dateFormat.format(task.getStartTime()));
        }
        
        if (task.getEndTime() != null) {
            endTimeField.setText(dateFormat.format(task.getEndTime()));
        }
        
        priorityComboBox.setSelectedItem(task.getPriority());
        statusComboBox.setSelectedItem(task.getStatus());
        
        // 选择所属项目
        if (task.getProjectId() != null) {
            Project project = projectService.getProjectById(task.getProjectId());
            if (project != null) {
                String projectItem = project.getId() + ": " + project.getName();
                projectComboBox.setSelectedItem(projectItem);
            }
        } else {
            projectComboBox.setSelectedIndex(0); // 选择"无项目"
        }
        
        typeComboBox.setSelectedItem(task.getType());
        reminderSpinner.setValue(task.getReminderMinutes());
    }
    
    // 验证表单数据
    private boolean validateForm() {
        // 验证任务名称
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入任务名称", "错误", JOptionPane.ERROR_MESSAGE);
            nameField.requestFocus();
            return false;
        }
        
        // 验证时间格式
        Date startTime = null;
        Date endTime = null;
        
        if (!startTimeField.getText().trim().isEmpty()) {
            try {
                startTime = dateFormat.parse(startTimeField.getText().trim());
            } catch (ParseException e) {
                JOptionPane.showMessageDialog(this, "开始时间格式错误，请使用yyyy-MM-dd HH:mm格式", "错误", JOptionPane.ERROR_MESSAGE);
                startTimeField.requestFocus();
                return false;
            }
        }
        
        if (!endTimeField.getText().trim().isEmpty()) {
            try {
                endTime = dateFormat.parse(endTimeField.getText().trim());
            } catch (ParseException e) {
                JOptionPane.showMessageDialog(this, "截止时间格式错误，请使用yyyy-MM-dd HH:mm格式", "错误", JOptionPane.ERROR_MESSAGE);
                endTimeField.requestFocus();
                return false;
            }
        }
        
        // 验证开始时间和截止时间的关系
        if (startTime != null && endTime != null && startTime.after(endTime)) {
            JOptionPane.showMessageDialog(this, "开始时间不能晚于截止时间", "错误", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        return true;
    }
    
    // 保存表单数据到任务对象
    private void saveTaskData() {
        task.setName(nameField.getText().trim());
        task.setContent(contentArea.getText().trim());
        
        // 设置时间
        try {
            if (!startTimeField.getText().trim().isEmpty()) {
                task.setStartTime(dateFormat.parse(startTimeField.getText().trim()));
            } else {
                task.setStartTime(null);
            }
            
            if (!endTimeField.getText().trim().isEmpty()) {
                task.setEndTime(dateFormat.parse(endTimeField.getText().trim()));
            } else {
                task.setEndTime(null);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        
        // 设置其他属性
        task.setPriority((Task.Priority) priorityComboBox.getSelectedItem());
        task.setStatus((Task.TaskStatus) statusComboBox.getSelectedItem());
        task.setType((Task.TaskType) typeComboBox.getSelectedItem());
        task.setReminderMinutes((Integer) reminderSpinner.getValue());
        
        // 设置项目
        String selectedProject = (String) projectComboBox.getSelectedItem();
        if (selectedProject != null && !selectedProject.equals("无项目")) {
            // 提取项目ID（格式：ID: 名称）
            String projectId = selectedProject.split(":")[0].trim();
            task.setProjectId(projectId);
        } else {
            task.setProjectId(null);
        }
        
        // 如果设置了开始时间和提醒时间，创建提醒
        if (task.getStartTime() != null && task.getReminderMinutes() > 0) {
            reminderService.setReminder(task.getId(), task.getReminderMinutes());
        }
    }
    
    // 获取任务对象
    public Task getTask() {
        return task;
    }
    
    // 检查用户是否确认了操作
    public boolean isConfirmed() {
        return confirmed;
    }
    
    // 工具方法：显示今天的日期时间作为默认值
    public void setDefaultToToday() {
        Calendar calendar = Calendar.getInstance();
        startTimeField.setText(dateFormat.format(calendar.getTime()));
        
        // 默认结束时间为今天的18:00
        calendar.set(Calendar.HOUR_OF_DAY, 18);
        calendar.set(Calendar.MINUTE, 0);
        endTimeField.setText(dateFormat.format(calendar.getTime()));
    }
}