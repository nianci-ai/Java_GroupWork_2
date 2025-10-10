/**
 * 日程录入系统 
 */
public class SimpleScheduleSystem 
{
    
    // 存储所有任务的列表
    private static List<Task> taskList = new ArrayList<>();
    
    public static void main(String[] args) 
    {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("=== 简单日程录入系统 ===");
        
        while (true)
        {
            System.out.println("\n请选择操作:");
            System.out.println("1. 添加新任务");
            System.out.println("2. 查看所有任务");
            System.out.println("3. 退出系统");
            System.out.print("请输入选择: ");
            
            String choice = scanner.nextLine();
            
            switch (choice) {
                case "1":
                    addNewTask(scanner);
                    break;
                case "2":
                    showAllTasks();
                    break;
                case "3":
                    System.out.println("谢谢使用！");
                    scanner.close();
                    return;
                default:
                    System.out.println("输入错误，请重新选择");
            }
        }
    }
    
    /**
     * 添加新任务
     */
    private static void addNewTask(Scanner scanner) 
    {
        System.out.println("\n=== 添加新任务 ===");
        
        try {
            // 输入任务名称
            System.out.print("任务名称: ");
            String name = scanner.nextLine();
            
            // 输入任务内容
            System.out.print("任务内容: ");
            String content = scanner.nextLine();
            
            // 输入开始时间
            System.out.print("开始时间(格式: 2024-01-15 09:00): ");
            String startStr = scanner.nextLine();
            Date startTime = parseTime(startStr);
            
            // 输入结束时间
            System.out.print("结束时间(格式: 2024-01-15 10:00): ");
            String endStr = scanner.nextLine();
            Date endTime = parseTime(endStr);
            
            // 选择优先级
            System.out.print("优先级(1-低 2-中 3-高): ");
            int priorityNum = Integer.parseInt(scanner.nextLine());
            String priority = getPriority(priorityNum);
            
            // 选择类型
            System.out.print("类型(1-会议 2-截止日期 3-日常): ");
            int typeNum = Integer.parseInt(scanner.nextLine());
            String type = getType(typeNum);
            
            // 输入项目名
            System.out.print("项目名称: ");
            String project = scanner.nextLine();
            
            // 创建任务并添加到列表
            Task newTask = new Task(name, content, startTime, endTime, priority, type, project);
            
            // 检查时间冲突
            if (checkTimeConflict(newTask)) 
            {
                System.out.println("时间冲突，添加失败！");
            } else {
                taskList.add(newTask);
                System.out.println("任务添加成功！");
            }
            
        } catch (Exception e) 
        {
            System.out.println("输入错误: " + e.getMessage());
        }
    }
    
    /**
     * 显示所有任务
     */
    private static void showAllTasks()
    {
        System.out.println("\n=== 所有任务 ===");
        
        if (taskList.isEmpty()) 
        {
            System.out.println("还没有任务哦~");
            return;
        }
        
        // 按开始时间排序
        taskList.sort((t1, t2) -> t1.startTime.compareTo(t2.startTime));
        
        SimpleDateFormat sdf = new SimpleDateFormat("MM月dd日 HH:mm");
        
        for (int i = 0; i < taskList.size(); i++) 
        {
            Task task = taskList.get(i);
            System.out.println((i + 1) + ". " + task.name);
            System.out.println("   内容: " + task.content);
            System.out.println("   时间: " + sdf.format(task.startTime) + " - " + sdf.format(task.endTime));
            System.out.println("   优先级: " + task.priority + " | 类型: " + task.type + " | 项目: " + task.project);
            System.out.println("   ---");
        }
        
        System.out.println("总共 " + taskList.size() + " 个任务");
    }
    
    /**
     * 解析时间字符串
     */
    private static Date parseTime(String timeStr) throws Exception 
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return sdf.parse(timeStr);
    }
    
    /**
     * 获取优先级文字
     */
    private static String getPriority(int num) 
    {
        switch (num) 
        {
            case 1: return "低";
            case 2: return "中";
            case 3: return "高";
            default: return "中";
        }
    }
    
    /**
     * 获取类型文字
     */
    private static String getType(int num) 
    {
        switch (num) 
        {
            case 1: return "会议";
            case 2: return "截止日期";
            case 3: return "日常";
            default: return "日常";
        }
    }
    
    /**
     * 检查时间冲突
     */
    private static boolean checkTimeConflict(Task newTask)
    {
        for (Task task : taskList)
        {
            // 如果新任务的开始时间在某个任务的开始和结束时间之间，就有冲突
            if (newTask.startTime.before(task.endTime) && task.startTime.before(newTask.endTime)) {
                System.out.println("与现有任务冲突: " + task.name);
                return true;
            }
        }
        return false;
    }
}

/**
 * 任务类 - 简单的数据容器
 */
class Task 
{
    String name;        // 任务名称
    String content;     // 任务内容
    Date startTime;     // 开始时间
    Date endTime;       // 结束时间
    String priority;    // 优先级
    String type;        // 任务类型
    String project;     // 所属项目
    
    public Task(String name, String content, Date startTime, Date endTime, 
                String priority, String type, String project)
    {
        this.name = name;
        this.content = content;
        this.startTime = startTime;
        this.endTime = endTime;
        this.priority = priority;
        this.type = type;
        this.project = project;
    }
}
