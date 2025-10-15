package com.scheduler.ui;

import com.scheduler.model.Project;
import com.scheduler.service.ProjectService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * 项目编辑对话框，用于添加和编辑项目信息
 */
public class ProjectDialog extends JDialog {
    private static final long serialVersionUID = 1L;
    
    private JTextField nameField;
    private JTextArea descriptionArea;
    private JButton confirmButton;
    private JButton cancelButton;
    
    private boolean confirmed = false;
    private Project project;
    private final ProjectService projectService;
    
    /**
     * 构造函数 - 用于添加新项目
     * @param parent 父窗口
     * @param projectService 项目服务对象
     */
    public ProjectDialog(Frame parent, ProjectService projectService) {
        super(parent, "添加项目", true);
        this.projectService = projectService;
        this.project = new Project();
        initComponents();
    }
    
    /**
     * 构造函数 - 用于编辑现有项目
     * @param parent 父窗口
     * @param project 要编辑的项目对象
     * @param projectService 项目服务对象
     */
    public ProjectDialog(Frame parent, Project project, ProjectService projectService) {
        super(parent, "编辑项目", true);
        this.projectService = projectService;
        this.project = project;
        initComponents();
        loadProjectData();
    }
    
    /**
     * 初始化对话框组件
     */
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setSize(400, 300);
        setLocationRelativeTo(getParent());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        // 创建面板
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // 项目名称
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        contentPanel.add(new JLabel("项目名称："), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        nameField = new JTextField(20);
        contentPanel.add(nameField, gbc);
        
        // 项目描述
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.NORTHEAST;
        contentPanel.add(new JLabel("项目描述："), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        descriptionArea = new JTextArea(5, 20);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(descriptionArea);
        contentPanel.add(scrollPane, gbc);
        
        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        confirmButton = new JButton("确定");
        cancelButton = new JButton("取消");
        
        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);
        
        // 添加面板到对话框
        add(contentPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        
        // 添加事件监听器
        confirmButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (validateInput()) {
                    saveProjectData();
                    confirmed = true;
                    dispose();
                }
            }
        });
        
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        
        // 添加窗口监听器，处理窗口关闭事件
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });
        
        // 添加ESC键关闭对话框
        getRootPane().registerKeyboardAction(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }
    
    /**
     * 加载项目数据到表单
     */
    private void loadProjectData() {
        nameField.setText(project.getName());
        descriptionArea.setText(project.getDescription());
    }
    
    /**
     * 保存表单数据到项目对象
     */
    private void saveProjectData() {
        project.setName(nameField.getText().trim());
        project.setDescription(descriptionArea.getText().trim());
    }
    
    /**
     * 验证用户输入
     * @return 输入是否有效
     */
    private boolean validateInput() {
        String name = nameField.getText().trim();
        
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "项目名称不能为空", "输入错误", JOptionPane.ERROR_MESSAGE);
            nameField.requestFocus();
            return false;
        }
        
        // 检查项目名称是否已存在（编辑时排除当前项目）
        for (Project p : projectService.getAllProjects()) {
            if (p.getName().equals(name) && !p.getId().equals(project.getId())) {
                JOptionPane.showMessageDialog(this, "项目名称已存在", "输入错误", JOptionPane.ERROR_MESSAGE);
                nameField.requestFocus();
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * 检查用户是否点击了确定按钮
     * @return 是否确认
     */
    public boolean isConfirmed() {
        return confirmed;
    }
    
    /**
     * 获取项目对象
     * @return 项目对象
     */
    public Project getProject() {
        return project;
    }
}