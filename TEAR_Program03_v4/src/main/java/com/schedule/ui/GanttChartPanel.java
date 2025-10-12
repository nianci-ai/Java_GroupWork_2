package com.schedule.ui;

import com.schedule.model.Task;
import com.schedule.service.TaskService;
import com.schedule.util.DateUtil;

import javax.swing.*;
import java.awt.*;
import java.util.Date;
import java.util.List;
import java.text.SimpleDateFormat;

/**
 * 甘特图面板 - 用于显示任务的甘特图视图
 */
public class GanttChartPanel extends JPanel {
    // 视图模式枚举
    public enum ViewMode {
        DAILY,   // 日视图
        WEEKLY,  // 周视图
        MONTHLY  // 月视图
    }
    
    private TaskService taskService;
    private ViewMode viewMode = ViewMode.DAILY;
    private Date currentDate;
    
    // 甘特图配置
    private static final int HEADER_HEIGHT = 40;
    private static final int TASK_ROW_HEIGHT = 30;
    private static final int HOUR_WIDTH = 60; // 日视图中每小时的宽度
    private static final int DAY_WIDTH = 80;  // 周/月视图中每天的宽度
    
    // 颜色配置
    private static final Color HEADER_BACKGROUND = Color.LIGHT_GRAY;
    private static final Color GRID_COLOR = Color.GRAY;
    private static final Color TASK_COLORS[] = {
        new Color(79, 129, 189),  // 蓝色 - 会议
        new Color(192, 80, 77),   // 红色 - 截止日期
        new Color(155, 187, 89)   // 绿色 - 日常事务
    };
    private static final Color TEXT_COLOR = Color.BLACK;
    
    public GanttChartPanel(TaskService taskService) {
        this.taskService = taskService;
        this.currentDate = new Date();
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(800, 400));
    }
    
    // 设置视图模式
    public void setViewMode(ViewMode viewMode) {
        this.viewMode = viewMode;
        repaint();
    }
    
    // 更新甘特图数据
    public void updateGanttChart(Date date) {
        this.currentDate = date;
        repaint();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        // 设置抗锯齿
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // 根据视图模式绘制甘特图
        switch (viewMode) {
            case DAILY:
                drawDailyGanttChart(g2d);
                break;
            case WEEKLY:
                drawWeeklyGanttChart(g2d);
                break;
            case MONTHLY:
                drawMonthlyGanttChart(g2d);
                break;
        }
    }
    
    // 绘制日视图甘特图
    private void drawDailyGanttChart(Graphics2D g2d) {
        // 获取当天的任务
        List<Task> tasks = taskService.getTasksByDate(currentDate);
        
        // 计算面板宽度
        int chartWidth = 24 * HOUR_WIDTH; // 24小时
        int panelWidth = Math.max(getWidth(), chartWidth + 150); // 加上任务名称列的宽度
        setPreferredSize(new Dimension(panelWidth, HEADER_HEIGHT + tasks.size() * TASK_ROW_HEIGHT + 20));
        
        // 绘制时间轴头部
        drawDailyHeader(g2d, panelWidth);
        
        // 绘制任务行
        drawTaskRows(g2d, tasks, panelWidth);
    }
    
    // 绘制周视图甘特图
    private void drawWeeklyGanttChart(Graphics2D g2d) {
        // 获取本周的任务
        List<Task> tasks = taskService.getTasksByWeek(currentDate);
        
        // 计算面板宽度
        int chartWidth = 7 * DAY_WIDTH; // 7天
        int panelWidth = Math.max(getWidth(), chartWidth + 150); // 加上任务名称列的宽度
        setPreferredSize(new Dimension(panelWidth, HEADER_HEIGHT + tasks.size() * TASK_ROW_HEIGHT + 20));
        
        // 绘制日期轴头部
        drawWeeklyHeader(g2d, panelWidth);
        
        // 绘制任务行
        drawTaskRowsWeekly(g2d, tasks, panelWidth);
    }
    
    // 绘制月视图甘特图
    private void drawMonthlyGanttChart(Graphics2D g2d) {
        // 获取本月的任务
        List<Task> tasks = taskService.getTasksByMonth(currentDate);
        
        // 计算本月有多少天
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTime(currentDate);
        int daysInMonth = cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH);
        
        // 计算面板宽度
        int chartWidth = daysInMonth * DAY_WIDTH;
        int panelWidth = Math.max(getWidth(), chartWidth + 150); // 加上任务名称列的宽度
        setPreferredSize(new Dimension(panelWidth, HEADER_HEIGHT + tasks.size() * TASK_ROW_HEIGHT + 20));
        
        // 绘制日期轴头部
        drawMonthlyHeader(g2d, panelWidth, daysInMonth);
        
        // 绘制任务行
        drawTaskRowsMonthly(g2d, tasks, panelWidth, daysInMonth);
    }
    
    // 绘制日视图头部
    private void drawDailyHeader(Graphics2D g2d, int panelWidth) {
        // 绘制背景
        g2d.setColor(HEADER_BACKGROUND);
        g2d.fillRect(150, 0, panelWidth - 150, HEADER_HEIGHT);
        
        // 绘制边框
        g2d.setColor(GRID_COLOR);
        g2d.drawRect(150, 0, panelWidth - 150, HEADER_HEIGHT);
        
        // 绘制时间标签
        SimpleDateFormat hourFormat = new SimpleDateFormat("HH:mm");
        g2d.setColor(TEXT_COLOR);
        g2d.setFont(new Font("宋体", Font.PLAIN, 12));
        
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTime(currentDate);
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        
        for (int i = 0; i < 24; i++) {
            int x = 150 + i * HOUR_WIDTH;
            
            // 绘制垂直线
            g2d.drawLine(x, 0, x, HEADER_HEIGHT);
            
            // 绘制时间标签
            String timeLabel = hourFormat.format(cal.getTime());
            FontMetrics metrics = g2d.getFontMetrics();
            int labelWidth = metrics.stringWidth(timeLabel);
            g2d.drawString(timeLabel, x + (HOUR_WIDTH - labelWidth) / 2, HEADER_HEIGHT / 2 + metrics.getAscent() / 2);
            
            // 增加一小时
            cal.add(java.util.Calendar.HOUR_OF_DAY, 1);
        }
    }
    
    // 绘制周视图头部
    private void drawWeeklyHeader(Graphics2D g2d, int panelWidth) {
        // 绘制背景
        g2d.setColor(HEADER_BACKGROUND);
        g2d.fillRect(150, 0, panelWidth - 150, HEADER_HEIGHT);
        
        // 绘制边框
        g2d.setColor(GRID_COLOR);
        g2d.drawRect(150, 0, panelWidth - 150, HEADER_HEIGHT);
        
        // 绘制日期标签
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd");
        SimpleDateFormat dayFormat = new SimpleDateFormat("E");
        g2d.setColor(TEXT_COLOR);
        g2d.setFont(new Font("宋体", Font.PLAIN, 12));
        
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTime(currentDate);
        cal.set(java.util.Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        
        for (int i = 0; i < 7; i++) {
            int x = 150 + i * DAY_WIDTH;
            
            // 绘制垂直线
            g2d.drawLine(x, 0, x, HEADER_HEIGHT);
            
            // 绘制日期标签
            String dateLabel = dateFormat.format(cal.getTime());
            String dayLabel = dayFormat.format(cal.getTime());
            FontMetrics metrics = g2d.getFontMetrics();
            int dateLabelWidth = metrics.stringWidth(dateLabel);
            int dayLabelWidth = metrics.stringWidth(dayLabel);
            
            g2d.drawString(dateLabel, x + (DAY_WIDTH - dateLabelWidth) / 2, HEADER_HEIGHT / 2 - 5);
            g2d.drawString(dayLabel, x + (DAY_WIDTH - dayLabelWidth) / 2, HEADER_HEIGHT / 2 + 15);
            
            // 增加一天
            cal.add(java.util.Calendar.DAY_OF_MONTH, 1);
        }
    }
    
    // 绘制月视图头部
    private void drawMonthlyHeader(Graphics2D g2d, int panelWidth, int daysInMonth) {
        // 绘制背景
        g2d.setColor(HEADER_BACKGROUND);
        g2d.fillRect(150, 0, panelWidth - 150, HEADER_HEIGHT);
        
        // 绘制边框
        g2d.setColor(GRID_COLOR);
        g2d.drawRect(150, 0, panelWidth - 150, HEADER_HEIGHT);
        
        // 绘制日期标签
        g2d.setColor(TEXT_COLOR);
        g2d.setFont(new Font("宋体", Font.PLAIN, 12));
        
        for (int i = 0; i < daysInMonth; i++) {
            int x = 150 + i * DAY_WIDTH;
            
            // 绘制垂直线
            g2d.drawLine(x, 0, x, HEADER_HEIGHT);
            
            // 绘制日期标签
            String dayLabel = String.valueOf(i + 1);
            FontMetrics metrics = g2d.getFontMetrics();
            int labelWidth = metrics.stringWidth(dayLabel);
            g2d.drawString(dayLabel, x + (DAY_WIDTH - labelWidth) / 2, HEADER_HEIGHT / 2 + metrics.getAscent() / 2);
        }
    }
    
    // 绘制任务行（日视图）
    private void drawTaskRows(Graphics2D g2d, List<Task> tasks, int panelWidth) {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        
        // 设置当前日期的开始时间
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTime(currentDate);
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        Date dayStart = cal.getTime();
        
        for (int i = 0; i < tasks.size(); i++) {
            Task task = tasks.get(i);
            int rowTop = HEADER_HEIGHT + i * TASK_ROW_HEIGHT;
            
            // 绘制行背景
            if (i % 2 == 0) {
                g2d.setColor(new Color(245, 245, 245));
            } else {
                g2d.setColor(Color.WHITE);
            }
            g2d.fillRect(0, rowTop, panelWidth, TASK_ROW_HEIGHT);
            
            // 绘制行边框
            g2d.setColor(GRID_COLOR);
            g2d.drawLine(0, rowTop + TASK_ROW_HEIGHT, panelWidth, rowTop + TASK_ROW_HEIGHT);
            g2d.drawLine(150, rowTop, 150, rowTop + TASK_ROW_HEIGHT);
            
            // 绘制任务名称
            g2d.setColor(TEXT_COLOR);
            g2d.setFont(new Font("宋体", Font.PLAIN, 12));
            FontMetrics metrics = g2d.getFontMetrics();
            String taskName = task.getName();
            // 限制任务名称长度，防止显示不全
            if (metrics.stringWidth(taskName) > 130) {
                while (metrics.stringWidth(taskName + "...") > 130) {
                    taskName = taskName.substring(0, taskName.length() - 1);
                }
                taskName += "...";
            }
            g2d.drawString(taskName, 10, rowTop + TASK_ROW_HEIGHT / 2 + metrics.getAscent() / 2);
            
            // 绘制任务条
            long taskStartMillis = task.getStartTime().getTime() - dayStart.getTime();
            long taskEndMillis = task.getEndTime().getTime() - dayStart.getTime();
            
            // 转换为像素位置
            int taskStartX = 150 + (int) (taskStartMillis / (3600 * 1000.0) * HOUR_WIDTH);
            int taskEndX = 150 + (int) (taskEndMillis / (3600 * 1000.0) * HOUR_WIDTH);
            int taskHeight = TASK_ROW_HEIGHT - 6;
            int taskY = rowTop + 3;
            
            // 确保任务条在面板范围内
            taskStartX = Math.max(150, taskStartX);
            taskEndX = Math.min(panelWidth, taskEndX);
            
            // 选择任务颜色
            Color taskColor = getTaskColor(task);
            g2d.setColor(taskColor);
            g2d.fillRoundRect(taskStartX, taskY, taskEndX - taskStartX, taskHeight, 5, 5);
            
            // 绘制任务条边框
            g2d.setColor(taskColor.darker());
            g2d.drawRoundRect(taskStartX, taskY, taskEndX - taskStartX, taskHeight, 5, 5);
            
            // 绘制任务时间标签
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("宋体", Font.BOLD, 10));
            String timeRange = timeFormat.format(task.getStartTime()) + "-" + timeFormat.format(task.getEndTime());
            if (taskEndX - taskStartX > metrics.stringWidth(timeRange) + 10) {
                g2d.drawString(timeRange, taskStartX + 5, taskY + taskHeight / 2 + 4);
            }
        }
    }
    
    // 绘制任务行（周视图）
    private void drawTaskRowsWeekly(Graphics2D g2d, List<Task> tasks, int panelWidth) {
        // 设置本周的开始时间
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTime(currentDate);
        cal.set(java.util.Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        Date weekStart = cal.getTime();
        
        for (int i = 0; i < tasks.size(); i++) {
            Task task = tasks.get(i);
            int rowTop = HEADER_HEIGHT + i * TASK_ROW_HEIGHT;
            
            // 绘制行背景和边框（与日视图相同）
            if (i % 2 == 0) {
                g2d.setColor(new Color(245, 245, 245));
            } else {
                g2d.setColor(Color.WHITE);
            }
            g2d.fillRect(0, rowTop, panelWidth, TASK_ROW_HEIGHT);
            g2d.setColor(GRID_COLOR);
            g2d.drawLine(0, rowTop + TASK_ROW_HEIGHT, panelWidth, rowTop + TASK_ROW_HEIGHT);
            g2d.drawLine(150, rowTop, 150, rowTop + TASK_ROW_HEIGHT);
            
            // 绘制任务名称（与日视图相同）
            g2d.setColor(TEXT_COLOR);
            g2d.setFont(new Font("宋体", Font.PLAIN, 12));
            FontMetrics metrics = g2d.getFontMetrics();
            String taskName = task.getName();
            if (metrics.stringWidth(taskName) > 130) {
                while (metrics.stringWidth(taskName + "...") > 130) {
                    taskName = taskName.substring(0, taskName.length() - 1);
                }
                taskName += "...";
            }
            g2d.drawString(taskName, 10, rowTop + TASK_ROW_HEIGHT / 2 + metrics.getAscent() / 2);
            
            // 绘制任务条
            long taskStartMillis = task.getStartTime().getTime() - weekStart.getTime();
            long taskEndMillis = task.getEndTime().getTime() - weekStart.getTime();
            
            // 转换为像素位置（按天计算）
            int taskStartX = 150 + (int) (taskStartMillis / (24 * 3600 * 1000.0) * DAY_WIDTH);
            int taskEndX = 150 + (int) (taskEndMillis / (24 * 3600 * 1000.0) * DAY_WIDTH);
            int taskHeight = TASK_ROW_HEIGHT - 6;
            int taskY = rowTop + 3;
            
            // 确保任务条在面板范围内
            taskStartX = Math.max(150, taskStartX);
            taskEndX = Math.min(panelWidth, taskEndX);
            
            // 选择任务颜色
            Color taskColor = getTaskColor(task);
            g2d.setColor(taskColor);
            g2d.fillRoundRect(taskStartX, taskY, taskEndX - taskStartX, taskHeight, 5, 5);
            
            // 绘制任务条边框
            g2d.setColor(taskColor.darker());
            g2d.drawRoundRect(taskStartX, taskY, taskEndX - taskStartX, taskHeight, 5, 5);
        }
    }
    
    // 绘制任务行（月视图）
    private void drawTaskRowsMonthly(Graphics2D g2d, List<Task> tasks, int panelWidth, int daysInMonth) {
        // 设置本月的开始时间
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTime(currentDate);
        cal.set(java.util.Calendar.DAY_OF_MONTH, 1);
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        Date monthStart = cal.getTime();
        
        for (int i = 0; i < tasks.size(); i++) {
            Task task = tasks.get(i);
            int rowTop = HEADER_HEIGHT + i * TASK_ROW_HEIGHT;
            
            // 绘制行背景和边框（与日视图相同）
            if (i % 2 == 0) {
                g2d.setColor(new Color(245, 245, 245));
            } else {
                g2d.setColor(Color.WHITE);
            }
            g2d.fillRect(0, rowTop, panelWidth, TASK_ROW_HEIGHT);
            g2d.setColor(GRID_COLOR);
            g2d.drawLine(0, rowTop + TASK_ROW_HEIGHT, panelWidth, rowTop + TASK_ROW_HEIGHT);
            g2d.drawLine(150, rowTop, 150, rowTop + TASK_ROW_HEIGHT);
            
            // 绘制任务名称（与日视图相同）
            g2d.setColor(TEXT_COLOR);
            g2d.setFont(new Font("宋体", Font.PLAIN, 12));
            FontMetrics metrics = g2d.getFontMetrics();
            String taskName = task.getName();
            if (metrics.stringWidth(taskName) > 130) {
                while (metrics.stringWidth(taskName + "...") > 130) {
                    taskName = taskName.substring(0, taskName.length() - 1);
                }
                taskName += "...";
            }
            g2d.drawString(taskName, 10, rowTop + TASK_ROW_HEIGHT / 2 + metrics.getAscent() / 2);
            
            // 绘制任务条
            long taskStartMillis = task.getStartTime().getTime() - monthStart.getTime();
            long taskEndMillis = task.getEndTime().getTime() - monthStart.getTime();
            
            // 转换为像素位置（按天计算）
            int taskStartX = 150 + (int) (taskStartMillis / (24 * 3600 * 1000.0) * DAY_WIDTH);
            int taskEndX = 150 + (int) (taskEndMillis / (24 * 3600 * 1000.0) * DAY_WIDTH);
            int taskHeight = TASK_ROW_HEIGHT - 6;
            int taskY = rowTop + 3;
            
            // 确保任务条在面板范围内
            taskStartX = Math.max(150, taskStartX);
            taskEndX = Math.min(panelWidth, taskEndX);
            
            // 选择任务颜色
            Color taskColor = getTaskColor(task);
            g2d.setColor(taskColor);
            g2d.fillRoundRect(taskStartX, taskY, taskEndX - taskStartX, taskHeight, 5, 5);
            
            // 绘制任务条边框
            g2d.setColor(taskColor.darker());
            g2d.drawRoundRect(taskStartX, taskY, taskEndX - taskStartX, taskHeight, 5, 5);
        }
    }
    
    // 根据任务类型获取颜色
    private Color getTaskColor(Task task) {
        switch (task.getType()) {
            case MEETING:
                return TASK_COLORS[0];
            case DEADLINE:
                return TASK_COLORS[1];
            case DAILY:
                return TASK_COLORS[2];
            default:
                return Color.GRAY;
        }
    }
}