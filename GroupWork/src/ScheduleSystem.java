// ScheduleSystem.java - 主系统类
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class ScheduleSystem {
    private ScheduleManager manager;
    private Scanner scanner;
    private SimpleDateFormat dateTimeFormat;

    public ScheduleSystem() {
        this.manager = new ScheduleManager();
        this.scanner = new Scanner(System.in);
        this.dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    }

    public void start() {
        System.out.println("=== 个人日程安排与提醒系统 ===");

        while (true) {
            showMenu();
            int choice = getIntInput("请选择操作: ");

            switch (choice) {
                case 1:
                    addTask();
                    break;
                case 2:
                    viewTasks();
                    break;
                case 3:
                    updateTaskStatus();
                    break;
                case 4:
                    viewStatistics();
                    break;
                case 5:
                    backupData();
                    break;
                case 6:
                    addProject();
                    break;
                case 0:
                    System.out.println("感谢使用日程管理系统，再见!");
                    return;
                default:
                    System.out.println("无效选择，请重新输入!");
            }
        }
    }

    private void showMenu() {
        System.out.println("\n===== 主菜单 =====");
        System.out.println("1. 添加任务");
        System.out.println("2. 查看任务");
        System.out.println("3. 更新任务状态");
        System.out.println("4. 查看统计");
        System.out.println("5. 数据备份");
        System.out.println("6. 添加项目");
        System.out.println("0. 退出系统");
    }

    private void addTask() {
        try {
            System.out.println("\n--- 添加新任务 ---");
            System.out.print("任务名称: ");
            String name = scanner.nextLine();

            System.out.print("任务内容: ");
            String content = scanner.nextLine();

            System.out.print("开始时间 (yyyy-MM-dd HH:mm): ");
            Date startTime = dateTimeFormat.parse(scanner.nextLine());

            System.out.print("结束时间 (yyyy-MM-dd HH:mm): ");
            Date endTime = dateTimeFormat.parse(scanner.nextLine());

            System.out.print("优先级 (1-5, 1为最高): ");
            int priority = Integer.parseInt(scanner.nextLine());

            System.out.print("所属项目: ");
            String project = scanner.nextLine();

            System.out.println("任务类型: 1.会议 2.截止日期 3.日常事务");
            int typeChoice = Integer.parseInt(scanner.nextLine());
            Task.TaskType type = switch (typeChoice) {
                case 1 -> Task.TaskType.MEETING;
                case 2 -> Task.TaskType.DEADLINE;
                case 3 -> Task.TaskType.DAILY_TASK;
                default -> Task.TaskType.DAILY_TASK;
            };

            Task task = new Task(name, content, startTime, endTime, priority, project, type);
            if (manager.addTask(task)) {
                System.out.println("✅ 任务添加成功!");
            }

        } catch (ParseException e) {
            System.out.println("日期格式错误，请使用 yyyy-MM-dd HH:mm 格式");
        } catch (Exception e) {
            System.out.println("输入错误: " + e.getMessage());
        }
    }

    private void viewTasks() {
        System.out.println("\n--- 查看任务 ---");
        System.out.println("1. 日视图");
        System.out.println("2. 周视图");
        System.out.println("3. 月视图");
        System.out.println("4. 按优先级排序");
        System.out.println("5. 按截止时间排序");

        int choice = getIntInput("请选择视图: ");
        try {
            switch (choice) {
                case 1:
                    System.out.print("查看日期 (yyyy-MM-dd): ");
                    Date date = new SimpleDateFormat("yyyy-MM-dd").parse(scanner.nextLine());
                    displayTasks(manager.getDailyView(date), "日视图");
                    break;
                case 2:
                    System.out.print("查看周 (yyyy-MM-dd): ");
                    Date weekDate = new SimpleDateFormat("yyyy-MM-dd").parse(scanner.nextLine());
                    displayTasks(manager.getWeeklyView(weekDate), "周视图");
                    break;
                case 3:
                    System.out.print("查看月 (yyyy-MM): ");
                    Date monthDate = new SimpleDateFormat("yyyy-MM").parse(scanner.nextLine());
                    displayTasks(manager.getMonthlyView(monthDate), "月视图");
                    break;
                case 4:
                    displayTasks(manager.getTasksSortedByPriority(), "按优先级排序");
                    break;
                case 5:
                    displayTasks(manager.getTasksSortedByDeadline(), "按截止时间排序");
                    break;
                default:
                    System.out.println("无效选择!");
            }
        } catch (ParseException e) {
            System.out.println("日期格式错误!");
        }
    }

    private void displayTasks(java.util.List<Task> tasks, String title) {
        System.out.println("\n--- " + title + " ---");
        if (tasks.isEmpty()) {
            System.out.println("暂无任务");
        } else {
            tasks.forEach(task -> {
                String statusIcon = switch (task.getStatus()) {
                    case COMPLETED -> "✅";
                    case IN_PROGRESS -> "🔄";
                    case NOT_STARTED -> "⏳";
                };
                System.out.println(statusIcon + " " + task);
                if (task.isOverdue()) {
                    System.out.println("   ⚠️ 已逾期!");
                }
            });
        }
    }

    private void updateTaskStatus() {
        System.out.println("\n--- 更新任务状态 ---");
        displayTasks(manager.getTasks(), "所有任务");

        System.out.print("输入任务ID: ");
        String taskId = scanner.nextLine();

        System.out.println("选择状态: 1.未开始 2.进行中 3.已完成");
        int statusChoice = getIntInput("请选择: ");

        Task.TaskStatus status = switch (statusChoice) {
            case 1 -> Task.TaskStatus.NOT_STARTED;
            case 2 -> Task.TaskStatus.IN_PROGRESS;
            case 3 -> Task.TaskStatus.COMPLETED;
            default -> null;
        };

        if (status != null) {
            manager.updateTaskStatus(taskId, status);
            System.out.println("✅ 任务状态更新成功!");
        } else {
            System.out.println("无效状态选择!");
        }
    }

    private void viewStatistics() {
        System.out.println("\n--- 数据统计 ---");
        System.out.println("1. 周统计");
        System.out.println("2. 月统计");

        int choice = getIntInput("请选择: ");
        try {
            switch (choice) {
                case 1:
                    System.out.print("查看周 (yyyy-MM-dd): ");
                    Date weekDate = new SimpleDateFormat("yyyy-MM-dd").parse(scanner.nextLine());
                    Statistics weekStats = manager.getWeeklyStatistics(weekDate);
                    System.out.println(weekStats);
                    break;
                case 2:
                    System.out.print("查看月 (yyyy-MM): ");
                    Date monthDate = new SimpleDateFormat("yyyy-MM").parse(scanner.nextLine());
                    Statistics monthStats = manager.getMonthlyStatistics(monthDate);
                    System.out.println(monthStats);
                    break;
                default:
                    System.out.println("无效选择!");
            }
        } catch (ParseException e) {
            System.out.println("日期格式错误!");
        }
    }

    private void backupData() {
        System.out.println("\n--- 数据备份 ---");
        System.out.print("输入备份文件路径: ");
        String path = scanner.nextLine();
        manager.backupData(path);
        System.out.println("✅ 备份完成!");
    }

    private void addProject() {
        System.out.println("\n--- 添加项目 ---");
        System.out.print("项目名称: ");
        String name = scanner.nextLine();
        System.out.print("项目描述: ");
        String description = scanner.nextLine();

        Project project = new Project(name, description);
        manager.addProject(project);
        System.out.println("✅ 项目添加成功!");
    }

    private int getIntInput(String prompt) {
        System.out.print(prompt);
        try {
            return Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public static void main(String[] args) {
        ScheduleSystem system = new ScheduleSystem();
        system.start();
    }
}