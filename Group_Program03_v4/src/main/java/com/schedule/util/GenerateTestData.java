package com.schedule.util;

import com.schedule.model.Task;
import com.schedule.model.Project;
import com.schedule.model.Task.Priority;
import com.schedule.model.Task.TaskStatus;
import com.schedule.model.Task.TaskType;
import com.schedule.service.TaskService;

import java.util.Calendar;
import java.util.Date;
import java.util.Random;

/**
 * 测试数据生成类 - 用于生成随机测试数据
 */
public class GenerateTestData {
    private static final String[] TASK_NAMES = {
            "团队会议", "客户访谈", "周报提交", "需求分析", "代码评审",
            "系统测试", "文档编写", "培训学习", "项目规划", "风险评估",
            "预算审核", "进度跟踪", "资源分配", "问题解决", "质量检查",
            "用户反馈", "功能演示", "系统部署", "数据备份", "安全审计"
    };
    
    private static final String[] PROJECT_NAMES = {
            "项目A", "项目B", "项目C", "项目D", "项目E"
    };
    
    private static final String[] PROJECT_DESCRIPTIONS = {
            "企业管理系统开发", "客户关系管理平台", "数据分析系统", "电商平台重构", "移动应用开发"
    };
    
    private static final Random RANDOM = new Random();
    
    /**
     * 生成100条9月份的随机任务数据
     * @param taskService 任务服务实例
     */
    public static void generateSeptemberTasks(TaskService taskService) {
        // 创建项目
        Project[] projects = new Project[PROJECT_NAMES.length];
        for (int i = 0; i < PROJECT_NAMES.length; i++) {
            Project project = new Project("proj_" + i, PROJECT_NAMES[i], PROJECT_DESCRIPTIONS[i]);
            taskService.addProject(project);
            projects[i] = project;
        }
        
        // 获取当前年份
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        
        // 生成100条9月份的随机任务
        for (int i = 0; i < 100; i++) {
            // 随机生成9月份的日期
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.YEAR, currentYear);
            cal.set(Calendar.MONTH, Calendar.SEPTEMBER);
            cal.set(Calendar.DAY_OF_MONTH, 1 + RANDOM.nextInt(30)); // 9月有30天
            cal.set(Calendar.HOUR_OF_DAY, 9 + RANDOM.nextInt(9)); // 9:00-17:00
            cal.set(Calendar.MINUTE, RANDOM.nextBoolean() ? 0 : 30); // 整点或半点
            Date startTime = cal.getTime();
            
            // 随机生成结束时间（1-3小时后）
            cal.add(Calendar.HOUR_OF_DAY, 1 + RANDOM.nextInt(3));
            Date endTime = cal.getTime();
            
            // 随机选择任务名称、优先级、类型和项目
            String taskName = TASK_NAMES[RANDOM.nextInt(TASK_NAMES.length)];
            Priority priority = Priority.values()[RANDOM.nextInt(Priority.values().length)];
            TaskType type = TaskType.values()[RANDOM.nextInt(TaskType.values().length)];
            Project project = projects[RANDOM.nextInt(projects.length)];
            
            // 随机设置任务状态（30%已完成，5%已延迟，其余未开始）
            TaskStatus status;
            int statusRand = RANDOM.nextInt(100);
            if (statusRand < 30) {
                status = TaskStatus.COMPLETED;
            } else if (statusRand < 35) {
                status = TaskStatus.DELAYED;
            } else {
                status = TaskStatus.NOT_STARTED;
            }
            
            // 创建任务
            Task task = new Task("task_" + i, taskName, startTime, endTime, priority, type, project);
            task.setStatus(status);
            task.setContent("这是" + taskName + "的详细内容，包含任务目标、步骤和注意事项。");
            
            // 添加任务（不添加提醒）
            taskService.addTaskWithoutReminder(task);
        }
        
        System.out.println("已生成100条9月份的随机任务数据");
    }
}