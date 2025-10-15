package com.scheduler.ui;

import com.scheduler.model.Task;
import com.scheduler.service.TaskService;

import javax.swing.table.AbstractTableModel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 任务表格的数据模型类，负责管理任务表格的数据显示和更新
 */
public class TaskTableModel extends AbstractTableModel {
    private static final long serialVersionUID = 1L;
    
    private final String[] columnNames = {"任务名称", "内容", "开始时间", "截止时间", "优先级", "状态", "所属项目"};
    private List<Task> tasks;
    private final TaskService taskService;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    
    /**
     * 构造函数
     * @param taskService 任务服务对象，用于获取任务数据
     */
    public TaskTableModel(TaskService taskService) {
        this.taskService = taskService;
        this.tasks = taskService.getAllTasks();
    }
    
    /**
     * 更新表格数据
     */
    public void updateData() {
        this.tasks = taskService.getAllTasks();
        fireTableDataChanged();
    }
    
    /**
     * 根据索引获取任务对象
     * @param rowIndex 行索引
     * @return 任务对象
     */
    public Task getTaskAt(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < tasks.size()) {
            return tasks.get(rowIndex);
        }
        return null;
    }
    
    @Override
    public int getRowCount() {
        return tasks.size();
    }
    
    @Override
    public int getColumnCount() {
        return columnNames.length;
    }
    
    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }
    
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Task task = tasks.get(rowIndex);
        
        switch (columnIndex) {
            case 0: // 任务名称
                return task.getName();
            case 1: // 内容
                return task.getContent();
            case 2: // 开始时间
                return formatDate(task.getStartTime());
            case 3: // 截止时间
                return formatDate(task.getEndTime());
            case 4: // 优先级
                return getPriorityText(task.getPriority());
            case 5: // 状态
                return getStatusText(task.getStatus());
            case 6: // 所属项目
                return task.getProjectId() != null ? "有项目" : "无";
            default:
                return null;
        }
    }
    
    /**
     * 格式化日期显示
     * @param date 日期对象
     * @return 格式化后的日期字符串
     */
    private String formatDate(Date date) {
        if (date == null) {
            return "";
        }
        return dateFormat.format(date);
    }
    
    /**
     * 获取优先级的文本表示
     * @param priority 优先级枚举
     * @return 优先级文本
     */
    private String getPriorityText(Task.Priority priority) {
        switch (priority) {
            case HIGH:
                return "高";
            case MEDIUM:
                return "中";
            case LOW:
                return "低";
            case URGENT:
                return "紧急";
            default:
                return "未知";
        }
    }
    
    /**
     * 获取状态的文本表示
     * @param status 状态枚举
     * @return 状态文本
     */
    private String getStatusText(Task.TaskStatus status) {
        switch (status) {
            case NOT_STARTED:
                return "未开始";
            case IN_PROGRESS:
                return "进行中";
            case COMPLETED:
                return "已完成";
            default:
                return "未知";
        }
    }
    
    /**
     * 按截止时间排序任务
     */
    public void sortByDeadline() {
        tasks.sort((t1, t2) -> t1.getEndTime().compareTo(t2.getEndTime()));
        fireTableDataChanged();
    }
    
    /**
     * 按优先级排序任务
     */
    public void sortByPriority() {
        tasks.sort((t1, t2) -> t2.getPriority().compareTo(t1.getPriority()));
        fireTableDataChanged();
    }
}