package com.scheduler;

import com.scheduler.ui.MainFrame;

import javax.swing.*;

/**
 * 程序主入口类
 */
public class Main {
    /**
     * 主方法：程序的入口点
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        // 在事件调度线程中启动UI，确保线程安全
        SwingUtilities.invokeLater(() -> {
            try {
                // 设置系统外观，使界面更符合操作系统风格
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                // 如果设置系统外观失败，使用默认外观
                e.printStackTrace();
            }
            
            // 创建并显示主窗口
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}