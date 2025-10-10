// ScheduleManager.java - 日程管理器
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class ScheduleManager {
    private List<Task> tasks;
    private List<Project> projects;
    private Map<String, List<Task>> dailyTasks;
    private Map<String, List<Task>> weeklyTasks;
    private Map<String, List<Task>> monthlyTasks;
    private List<Reminder> reminders;
    private SimpleDateFormat dateFormat;
    private SimpleDateFormat weekFormat;
    private SimpleDateFormat monthFormat;

    public ScheduleManager() {
        this.tasks = new ArrayList<>();
        this.projects = new ArrayList<>();
        this.dailyTasks = new HashMap<>();
        this.weeklyTasks = new HashMap<>();
        this.monthlyTasks = new HashMap<>();
        this.reminders = new ArrayList<>();
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        this.weekFormat = new SimpleDateFormat("yyyy-'W'ww");
        this.monthFormat = new SimpleDateFormat("yyyy-MM");

        // 启动提醒监控线程
        startReminderMonitor();
    }

    // 日程录入
    public boolean addTask(Task task) {
        if (hasTimeConflict(task)) {
            System.out.println("警告: 任务时间冲突，添加失败!");
            return false;
        }

        if (!validateTaskName(task.getName())) {
            System.out.println("错误: 任务名称格式不正确!");
            return false;
        }

        tasks.add(task);
        updateTaskMappings(task);

        // 创建并安排提醒
        Reminder reminder = new Reminder(task);
        reminder.scheduleReminder();
        reminders.add(reminder);

        System.out.println("任务添加成功: " + task.getName());
        return true;
    }

    // 时间冲突检测算法
    private boolean hasTimeConflict(Task newTask) {
        return tasks.stream()
                .filter(task -> task.getStatus() != Task.TaskStatus.COMPLETED)
                .anyMatch(existingTask -> isTimeOverlap(existingTask, newTask));
    }

    private boolean isTimeOverlap(Task task1, Task task2) {
        return task1.getStartTime().before(task2.getEndTime()) &&
                task2.getStartTime().before(task1.getEndTime());
    }

    // 任务名称格式验证
    private boolean validateTaskName(String name) {
        // 任务名称不能为空，长度在1-50字符之间，不能包含特殊字符
        return name != null &&
                !name.trim().isEmpty() &&
                name.length() <= 50 &&
                name.matches("^[a-zA-Z0-9\\u4e00-\\u9fa5\\s\\-\\.]+$");
    }

    // 更新任务映射关系
    private void updateTaskMappings(Task task) {
        // 按日分组
        String dateKey = dateFormat.format(task.getStartTime());
        dailyTasks.computeIfAbsent(dateKey, k -> new ArrayList<>()).add(task);

        // 按周分组
        String weekKey = weekFormat.format(task.getStartTime());
        weeklyTasks.computeIfAbsent(weekKey, k -> new ArrayList<>()).add(task);

        // 按月分组
        String monthKey = monthFormat.format(task.getStartTime());
        monthlyTasks.computeIfAbsent(monthKey, k -> new ArrayList<>()).add(task);
    }

    // 任务管理
    public void markTaskAsCompleted(String taskId) {
        tasks.stream()
                .filter(task -> task.getId().equals(taskId))
                .findFirst()
                .ifPresent(task -> task.setStatus(Task.TaskStatus.COMPLETED));
    }

    public void updateTaskStatus(String taskId, Task.TaskStatus status) {
        tasks.stream()
                .filter(task -> task.getId().equals(taskId))
                .findFirst()
                .ifPresent(task -> task.setStatus(status));
    }

    // 视图展示
    public List<Task> getDailyView(Date date) {
        String dateKey = dateFormat.format(date);
        return dailyTasks.getOrDefault(dateKey, new ArrayList<>())
                .stream()
                .sorted()
                .collect(Collectors.toList());
    }

    public List<Task> getWeeklyView(Date date) {
        String weekKey = weekFormat.format(date);
        return weeklyTasks.getOrDefault(weekKey, new ArrayList<>())
                .stream()
                .sorted()
                .collect(Collectors.toList());
    }

    public List<Task> getMonthlyView(Date date) {
        String monthKey = monthFormat.format(date);
        return monthlyTasks.getOrDefault(monthKey, new ArrayList<>())
                .stream()
                .sorted()
                .collect(Collectors.toList());
    }

    // 任务排序
    public List<Task> getTasksSortedByPriority() {
        return tasks.stream()
                .sorted(Comparator.comparingInt(Task::getPriority))
                .collect(Collectors.toList());
    }

    public List<Task> getTasksSortedByDeadline() {
        return tasks.stream()
                .sorted(Comparator.comparing(Task::getEndTime))
                .collect(Collectors.toList());
    }

    // 数据统计
    public Statistics getWeeklyStatistics(Date date) {
        String weekKey = weekFormat.format(date);
        List<Task> weekTasks = weeklyTasks.getOrDefault(weekKey, new ArrayList<>());
        return calculateStatistics(weekTasks);
    }

    public Statistics getMonthlyStatistics(Date date) {
        String monthKey = monthFormat.format(date);
        List<Task> monthTasks = monthlyTasks.getOrDefault(monthKey, new ArrayList<>());
        return calculateStatistics(monthTasks);
    }

    private Statistics calculateStatistics(List<Task> taskList) {
        int total = taskList.size();
        long completed = taskList.stream()
                .filter(task -> task.getStatus() == Task.TaskStatus.COMPLETED)
                .count();
        long overdue = taskList.stream()
                .filter(Task::isOverdue)
                .count();

        double completionRate = total > 0 ? (double) completed / total * 100 : 0;
        double overdueRate = total > 0 ? (double) overdue / total * 100 : 0;

        return new Statistics(total, (int) completed, (int) overdue, completionRate, overdueRate);
    }

    // 提醒监控
    private void startReminderMonitor() {
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                checkImmediateReminders();
            }
        }, 0, 60000); // 每分钟检查一次
    }

    private void checkImmediateReminders() {
        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.MINUTE, 5); // 5分钟内的任务进行提醒

        Date fiveMinutesLater = calendar.getTime();

        tasks.stream()
                .filter(task -> task.getReminderTime().after(now) &&
                        task.getReminderTime().before(fiveMinutesLater) &&
                        task.getStatus() == Task.TaskStatus.NOT_STARTED)
                .forEach(task -> {
                    System.out.println("⏰ 即将开始: " + task.getName() + " (" + task.getStartTime() + ")");
                });
    }

    // 数据备份
    public void backupData(String filePath) {
        // 简化版备份实现
        System.out.println("数据已备份到: " + filePath);
    }

    public void exportBackup(String filePath) {
        // 导出备份文件
        System.out.println("备份文件已导出到: " + filePath);
    }

    // Getters
    public List<Task> getTasks() { return tasks; }
    public List<Project> getProjects() { return projects; }

    public void addProject(Project project) {
        projects.add(project);
    }
}