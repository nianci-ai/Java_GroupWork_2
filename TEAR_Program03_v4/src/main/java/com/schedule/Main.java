package com.schedule;

import com.schedule.ui.MainFrame;
import com.schedule.service.TaskService;

import javax.swing.*;

/**
 * 个人日程安排与提醒系统 - 主入口类
 * 负责初始化系统并启动主界面
 */
public class Main {
    /**
     * 程序主入口
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        // 在事件调度线程中启动GUI，确保线程安全
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // 初始化任务服务
                TaskService taskService = new TaskService();
                
                // 创建并显示主窗口
                MainFrame mainFrame = new MainFrame(taskService);
                mainFrame.setVisible(true);
            }
        });
    }
}