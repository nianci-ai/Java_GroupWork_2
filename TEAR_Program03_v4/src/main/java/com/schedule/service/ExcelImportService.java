package com.schedule.service;

import com.schedule.model.Project;
import com.schedule.model.Task;
import com.schedule.service.TaskService;
import com.schedule.util.DateUtil;

import java.io.*;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * CSV导入服务类 - 提供从CSV格式文件导入任务数据的功能
 */
public class ExcelImportService {
    private TaskService taskService;

    public ExcelImportService(TaskService taskService) {
        this.taskService = taskService;
    }

    /**
     * 统一导入方法，只支持CSV格式
     */
    public int importTasks(File file) throws Exception {
        String fileName = file.getName().toLowerCase();
        if (fileName.endsWith(".csv")) {
            return importTasksFromCsv(file);
        } else {
            throw new IllegalArgumentException("只支持CSV文件格式，当前文件：" + fileName);
        }
    }
    
    /**
     * 从CSV文件导入任务数据
     * CSV格式：任务名称,开始时间(yyyy-MM-dd HH:mm),结束时间(yyyy-MM-dd HH:mm),优先级,任务类型,项目名称,任务内容
     * 例如：团队会议,2023-09-01 10:00,2023-09-01 11:30,高,会议,项目A,讨论项目进度
     */
    public int importTasksFromCsv(File csvFile) {
        int importedCount = 0;
        String[] encodingOptions = {"UTF-8", "GBK", "GB18030", "ISO-8859-1"};
        boolean fileReadSuccessfully = false;

        // 尝试不同的编码方案
        for (String encoding : encodingOptions) {
            try {
                importedCount = 0;
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(csvFile), encoding))) {
                    String line;
                    // 读取第一行
                    String firstLine = reader.readLine();
                    
                    // 检查文件是否为空
                    if (firstLine == null) {
                        System.err.println("文件为空");
                        continue;
                    }

                    // 检查第一行是否是标题行
                    boolean hasHeader = !isLineData(firstLine);
                    System.out.println("尝试编码: " + encoding + ", 第一行: " + firstLine + ", 是标题行: " + hasHeader);
                    fileReadSuccessfully = true;

                    // 如果不是标题行，处理第一行
                    if (!hasHeader) {
                        if (processLine(firstLine)) {
                            importedCount++;
                        }
                    }

                    // 处理剩余行
                    while ((line = reader.readLine()) != null) {
                        if (line.trim().isEmpty()) continue;
                        if (processLine(line)) {
                            importedCount++;
                        }
                    }
                }
                // 如果读取成功且导入了数据，就使用这个编码
                if (fileReadSuccessfully && importedCount > 0) {
                    System.out.println("成功使用编码: " + encoding + "，导入了 " + importedCount + " 条数据");
                    break;
                }
            } catch (UnsupportedEncodingException e) {
                System.err.println("不支持的编码: " + encoding);
            } catch (IOException e) {
                System.err.println("读取文件失败(" + encoding + "): " + e.getMessage());
            }
        }

        if (!fileReadSuccessfully) {
            System.err.println("所有编码方案都尝试失败，无法读取文件");
        }
        System.out.println("最终导入数据条数: " + importedCount);

        return importedCount;
    }

    /**
     * 判断一行是否是数据行（而不是标题行）
     */
    private boolean isLineData(String line) {
        try {
            String[] parts = splitCsvLine(line);
            // 检查第二和第三个字段是否看起来像日期时间
            if (parts.length >= 3) {
                try {
                    // 尝试解析日期，如果成功，很可能是数据行
                    parseDateTime(parts[1]);
                    parseDateTime(parts[2]);
                    return true;
                } catch (ParseException e) {
                    // 不是有效的日期时间，可能是标题行
                    return false;
                }
            }
        } catch (Exception e) {
            // 发生异常，不是数据行
        }
        return false;
    }

    /**
     * 处理一行CSV数据，返回是否成功导入
     */
    private boolean processLine(String line) {
        try {
            // 解析CSV行
            String[] parts = splitCsvLine(line);
            if (parts.length >= 6) {
                // 清理各个字段的前后空格
                for (int i = 0; i < parts.length; i++) {
                    parts[i] = parts[i].trim();
                }

                // 创建或获取项目
                String projectName = parts.length > 5 ? parts[5] : "无项目";
                Project project = getOrCreateProject(projectName);

                // 创建任务
                Task task = createTaskFromParts(parts, project);
                if (task != null) {
                    // 使用不带提醒的方法添加任务，避免批量导入时创建过多提醒
                    taskService.addTaskWithoutReminder(task);
                    return true;
                }
            } else {
                System.err.println("字段数量不足，跳过行: " + line);
            }
        } catch (Exception e) {
            System.err.println("导入行失败: " + line + ", 错误: " + e.getMessage());
        }
        return false;
    }

    /**
     * 解析CSV行，处理引号内的逗号和转义引号
     */
    private String[] splitCsvLine(String line) {
        List<String> parts = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                // 处理转义引号 ("")
                if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++; // 跳过下一个引号
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                parts.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        // 添加最后一个字段
        parts.add(current.toString().trim());

        return parts.toArray(new String[0]);
    }

    /**
     * 获取或创建项目
     */
    private Project getOrCreateProject(String projectName) {
        if (projectName.isEmpty() || "无项目".equals(projectName)) {
            return null;
        }

        // 检查是否已存在同名项目
        for (Project project : taskService.getAllProjects()) {
            if (projectName.equals(project.getName())) {
                return project;
            }
        }

        // 创建新项目
        Project newProject = new Project("project_" + System.currentTimeMillis(), projectName);
        taskService.addProject(newProject);
        return newProject;
    }

    /**
     * 从CSV部分创建任务
     */
    private Task createTaskFromParts(String[] parts, Project project) {
        try {
            String name = parts[0];
            Date startTime = parseDateTime(parts[1]);
            Date endTime = parseDateTime(parts[2]);
            Task.Priority priority = getPriorityFromString(parts[3]);
            Task.TaskType type = getTaskTypeFromString(parts[4]);

            // 创建任务
            Task task = new Task("task_import_" + System.currentTimeMillis(), name, startTime, endTime, priority, type, project);

            // 设置任务内容（如果有）
            if (parts.length > 6) {
                task.setContent(parts[6]);
            }

            return task;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 解析日期时间字符串，支持多种格式
     */
    private Date parseDateTime(String dateStr) throws ParseException {
        // 尝试多种日期格式
        String[] formats = {
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd HH:mm",
            "yyyy/MM/dd HH:mm:ss",
            "yyyy/MM/dd HH:mm"
        };
        
        for (String format : formats) {
            try {
                return DateUtil.parseDate(dateStr, format);
            } catch (ParseException e) {
                // 尝试下一种格式
                continue;
            }
        }
        
        // 如果所有格式都失败，抛出异常
        throw new ParseException("无法解析日期: " + dateStr, 0);
    }

    /**
     * 从字符串获取优先级
     */
    private Task.Priority getPriorityFromString(String priorityStr) {
        for (Task.Priority priority : Task.Priority.values()) {
            if (priority.toString().equals(priorityStr)) {
                return priority;
            }
        }
        return Task.Priority.MEDIUM; // 默认优先级
    }

    /**
     * 从字符串获取任务类型
     */
    private Task.TaskType getTaskTypeFromString(String typeStr) {
        for (Task.TaskType type : Task.TaskType.values()) {
            if (type.toString().equals(typeStr)) {
                return type;
            }
        }
        return Task.TaskType.DAILY; // 默认任务类型
    }


    
    /**
     * 生成导入模板文件
     */
    public void generateImportTemplate(File templateFile) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(templateFile))) {
            // 写入标题行
            writer.write("任务名称,开始时间(yyyy-MM-dd HH:mm),结束时间(yyyy-MM-dd HH:mm),优先级(低/中/高/紧急),任务类型(会议/截止日期/日常事务),项目名称,任务内容\n");
            // 写入示例数据
            writer.write("示例会议,2023-09-01 10:00,2023-09-01 11:30,高,会议,项目A,讨论项目进度\n");
            writer.write("周报提交,2023-09-08 17:00,2023-09-08 18:00,紧急,截止日期,项目A,提交本周工作进展\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}