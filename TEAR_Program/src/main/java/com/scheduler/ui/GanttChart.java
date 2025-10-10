package com.scheduler.ui;

import com.scheduler.model.Task;
import com.scheduler.service.TaskService;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * 甘特图类：用于绘制任务的甘特图视图
 */
public class GanttChart extends JPanel {
    private TaskService taskService; // 任务服务
    private List<Task> tasks; // 要显示的任务列表
    private Date startDate; // 甘特图开始日期
    private Date endDate; // 甘特图结束日期
    private int daysVisible; // 可见的天数
    private final int HEADER_HEIGHT = 50; // 头部高度
    private final int ROW_HEIGHT = 40; // 每行高度
    private final int LEFT_MARGIN = 150; // 左侧边距（任务名称区域宽度）
    private final int BOTTOM_MARGIN = 30; // 底部边距（日期标签区域高度）
    private final int CELL_WIDTH_PER_DAY = 80; // 每天单元格宽度
    
    // 构造方法
    public GanttChart(TaskService taskService) {
        this.taskService = taskService;
        this.daysVisible = 7; // 默认显示7天
        this.tasks = new ArrayList<>();
        
        // 设置甘特图日期范围为从今天开始的7天
        Calendar calendar = Calendar.getInstance();
        this.startDate = calendar.getTime();
        calendar.add(Calendar.DAY_OF_MONTH, daysVisible - 1);
        this.endDate = calendar.getTime();
        
        // 设置面板属性
        setPreferredSize(new Dimension(LEFT_MARGIN + daysVisible * CELL_WIDTH_PER_DAY, 600));
        setBackground(Color.WHITE);
    }
    
    // 设置要显示的任务
    public void setTasks(List<Task> tasks) {
        this.tasks = tasks != null ? tasks : new ArrayList<>();
        repaint();
    }
    
    // 设置甘特图显示的天数
    public void setDaysVisible(int days) {
        this.daysVisible = Math.max(1, days);
        
        // 更新日期范围
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        calendar.add(Calendar.DAY_OF_MONTH, daysVisible - 1);
        this.endDate = calendar.getTime();
        
        setPreferredSize(new Dimension(LEFT_MARGIN + daysVisible * CELL_WIDTH_PER_DAY, 600));
        revalidate();
        repaint();
    }
    
    // 设置甘特图的开始日期
    public void setStartDate(Date date) {
        this.startDate = date;
        
        // 更新结束日期
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        calendar.add(Calendar.DAY_OF_MONTH, daysVisible - 1);
        this.endDate = calendar.getTime();
        
        repaint();
    }
    
    // 移动到下一页（向后移动daysVisible天）
    public void nextPage() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        calendar.add(Calendar.DAY_OF_MONTH, daysVisible);
        setStartDate(calendar.getTime());
    }
    
    // 移动到上一页（向前移动daysVisible天）
    public void previousPage() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        calendar.add(Calendar.DAY_OF_MONTH, -daysVisible);
        setStartDate(calendar.getTime());
    }
    
    // 移动到今天
    public void goToToday() {
        Calendar calendar = Calendar.getInstance();
        setStartDate(calendar.getTime());
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // 绘制背景
        drawBackground(g2);
        
        // 绘制日期头部
        drawDateHeader(g2);
        
        // 绘制任务行
        drawTaskRows(g2);
    }
    
    // 绘制背景网格
    private void drawBackground(Graphics2D g2) {
        g2.setColor(new Color(240, 240, 240));
        
        // 绘制垂直线（日期分隔）
        for (int i = 0; i <= daysVisible; i++) {
            int x = LEFT_MARGIN + i * CELL_WIDTH_PER_DAY;
            g2.drawLine(x, HEADER_HEIGHT, x, getHeight() - BOTTOM_MARGIN);
        }
        
        // 绘制水平线（任务行分隔）
        for (int i = 0; i <= tasks.size(); i++) {
            int y = HEADER_HEIGHT + i * ROW_HEIGHT;
            g2.drawLine(LEFT_MARGIN, y, LEFT_MARGIN + daysVisible * CELL_WIDTH_PER_DAY, y);
        }
    }
    
    // 绘制日期头部
    private void drawDateHeader(Graphics2D g2) {
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("宋体", Font.BOLD, 12));
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd");
        SimpleDateFormat dayFormat = new SimpleDateFormat("E");
        
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        
        // 绘制日期标签
        for (int i = 0; i < daysVisible; i++) {
            int x = LEFT_MARGIN + i * CELL_WIDTH_PER_DAY + CELL_WIDTH_PER_DAY / 2;
            int dateY = HEADER_HEIGHT / 3;
            int dayY = HEADER_HEIGHT * 2 / 3;
            
            // 绘制日期
            String dateStr = dateFormat.format(calendar.getTime());
            FontMetrics metrics = g2.getFontMetrics();
            int dateX = x - metrics.stringWidth(dateStr) / 2;
            g2.drawString(dateStr, dateX, dateY);
            
            // 绘制星期
            String dayStr = dayFormat.format(calendar.getTime());
            int dayX = x - metrics.stringWidth(dayStr) / 2;
            g2.drawString(dayStr, dayX, dayY);
            
            // 移动到下一天
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
    }
    
    // 绘制任务行
    private void drawTaskRows(Graphics2D g2) {
        g2.setFont(new Font("宋体", Font.PLAIN, 12));
        
        for (int i = 0; i < tasks.size(); i++) {
            Task task = tasks.get(i);
            int rowY = HEADER_HEIGHT + i * ROW_HEIGHT;
            
            // 绘制任务名称
            drawTaskName(g2, task, rowY);
            
            // 绘制任务条形
            drawTaskBar(g2, task, rowY);
        }
    }
    
    // 绘制任务名称
    private void drawTaskName(Graphics2D g2, Task task, int rowY) {
        g2.setColor(Color.BLACK);
        String taskName = task.getName();
        FontMetrics metrics = g2.getFontMetrics();
        
        // 如果任务名称太长，截断显示
        if (metrics.stringWidth(taskName) > LEFT_MARGIN - 20) {
            while (metrics.stringWidth(taskName + "...") > LEFT_MARGIN - 20 && taskName.length() > 0) {
                taskName = taskName.substring(0, taskName.length() - 1);
            }
            taskName += "...";
        }
        
        int textY = rowY + ROW_HEIGHT / 2 + metrics.getAscent() / 2 - metrics.getDescent();
        g2.drawString(taskName, 10, textY);
    }
    
    // 绘制任务条形
    private void drawTaskBar(Graphics2D g2, Task task, int rowY) {
        if (task.getStartTime() == null || task.getEndTime() == null) {
            return;
        }
        
        // 计算任务条形的起始和结束位置
        double totalDays = (endDate.getTime() - startDate.getTime()) / (1000.0 * 60 * 60 * 24);
        double startDay = Math.max(0, (task.getStartTime().getTime() - startDate.getTime()) / (1000.0 * 60 * 60 * 24));
        double endDay = Math.min(totalDays, (task.getEndTime().getTime() - startDate.getTime()) / (1000.0 * 60 * 60 * 24));
        
        // 如果任务不在显示范围内，不绘制
        if (startDay >= totalDays || endDay <= 0) {
            return;
        }
        
        // 计算任务条形的位置和宽度
        int barX = LEFT_MARGIN + (int) (startDay * CELL_WIDTH_PER_DAY);
        int barWidth = Math.max(10, (int) ((endDay - startDay) * CELL_WIDTH_PER_DAY));
        int barY = rowY + 5;
        int barHeight = ROW_HEIGHT - 10;
        
        // 根据任务状态设置不同的颜色
        Color barColor = getTaskColor(task);
        g2.setColor(barColor);
        g2.fillRoundRect(barX, barY, barWidth, barHeight, 5, 5);
        
        // 绘制边框
        g2.setColor(Color.BLACK);
        g2.drawRoundRect(barX, barY, barWidth, barHeight, 5, 5);
        
        // 绘制优先级标记
        drawPriorityMarker(g2, task, barX, barY, barHeight);
    }
    
    // 根据任务状态获取颜色
    private Color getTaskColor(Task task) {
        switch (task.getStatus()) {
            case COMPLETED:
                return new Color(100, 200, 100); // 绿色表示已完成
            case IN_PROGRESS:
                return new Color(100, 100, 200); // 蓝色表示进行中
            case NOT_STARTED:
                if (task.isOverdue()) {
                    return new Color(200, 100, 100); // 红色表示未开始且已过期
                } else {
                    return new Color(200, 200, 100); // 黄色表示未开始
                }
            default:
                return Color.GRAY;
        }
    }
    
    // 绘制优先级标记
    private void drawPriorityMarker(Graphics2D g2, Task task, int barX, int barY, int barHeight) {
        switch (task.getPriority()) {
            case URGENT:
                g2.setColor(Color.RED);
                g2.fillRect(barX - 5, barY, 5, barHeight);
                break;
            case HIGH:
                g2.setColor(Color.ORANGE);
                g2.fillRect(barX - 5, barY, 5, barHeight);
                break;
            case MEDIUM:
                g2.setColor(Color.YELLOW);
                g2.fillRect(barX - 5, barY, 5, barHeight);
                break;
            case LOW:
                g2.setColor(Color.GREEN);
                g2.fillRect(barX - 5, barY, 5, barHeight);
                break;
        }
    }
}