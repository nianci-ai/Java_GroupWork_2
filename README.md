\*\*

&nbsp;\* 日程录入系统 

&nbsp;\*/

public class SimpleScheduleSystem {

&nbsp;   

&nbsp;   // 存储所有任务的列表

&nbsp;   private static List<Task> taskList = new ArrayList<>();

&nbsp;   

&nbsp;   public static void main(String\[] args) {

&nbsp;       Scanner scanner = new Scanner(System.in);

&nbsp;       

&nbsp;       System.out.println("=== 简单日程录入系统 ===");

&nbsp;       

&nbsp;       while (true) {

&nbsp;           System.out.println("\\n请选择操作:");

&nbsp;           System.out.println("1. 添加新任务");

&nbsp;           System.out.println("2. 查看所有任务");

&nbsp;           System.out.println("3. 退出系统");

&nbsp;           System.out.print("请输入选择: ");

&nbsp;           

&nbsp;           String choice = scanner.nextLine();

&nbsp;           

&nbsp;           switch (choice) {

&nbsp;               case "1":

&nbsp;                   addNewTask(scanner);

&nbsp;                   break;

&nbsp;               case "2":

&nbsp;                   showAllTasks();

&nbsp;                   break;

&nbsp;               case "3":

&nbsp;                   System.out.println("谢谢使用！");

&nbsp;                   scanner.close();

&nbsp;                   return;

&nbsp;               default:

&nbsp;                   System.out.println("输入错误，请重新选择");

&nbsp;           }

&nbsp;       }

&nbsp;   }

&nbsp;   

&nbsp;   /\*\*

&nbsp;    \* 添加新任务

&nbsp;    \*/

&nbsp;   private static void addNewTask(Scanner scanner) {

&nbsp;       System.out.println("\\n=== 添加新任务 ===");

&nbsp;       

&nbsp;       try {

&nbsp;           // 输入任务名称

&nbsp;           System.out.print("任务名称: ");

&nbsp;           String name = scanner.nextLine();

&nbsp;           

&nbsp;           // 输入任务内容

&nbsp;           System.out.print("任务内容: ");

&nbsp;           String content = scanner.nextLine();

&nbsp;           

&nbsp;           // 输入开始时间

&nbsp;           System.out.print("开始时间(格式: 2024-01-15 09:00): ");

&nbsp;           String startStr = scanner.nextLine();

&nbsp;           Date startTime = parseTime(startStr);

&nbsp;           

&nbsp;           // 输入结束时间

&nbsp;           System.out.print("结束时间(格式: 2024-01-15 10:00): ");

&nbsp;           String endStr = scanner.nextLine();

&nbsp;           Date endTime = parseTime(endStr);

&nbsp;           

&nbsp;           // 选择优先级

&nbsp;           System.out.print("优先级(1-低 2-中 3-高): ");

&nbsp;           int priorityNum = Integer.parseInt(scanner.nextLine());

&nbsp;           String priority = getPriority(priorityNum);

&nbsp;           

&nbsp;           // 选择类型

&nbsp;           System.out.print("类型(1-会议 2-截止日期 3-日常): ");

&nbsp;           int typeNum = Integer.parseInt(scanner.nextLine());

&nbsp;           String type = getType(typeNum);

&nbsp;           

&nbsp;           // 输入项目名

&nbsp;           System.out.print("项目名称: ");

&nbsp;           String project = scanner.nextLine();

&nbsp;           

&nbsp;           // 创建任务并添加到列表

&nbsp;           Task newTask = new Task(name, content, startTime, endTime, priority, type, project);

&nbsp;           

&nbsp;           // 检查时间冲突

&nbsp;           if (checkTimeConflict(newTask)) {

&nbsp;               System.out.println("时间冲突，添加失败！");

&nbsp;           } else {

&nbsp;               taskList.add(newTask);

&nbsp;               System.out.println("任务添加成功！");

&nbsp;           }

&nbsp;           

&nbsp;       } catch (Exception e) {

&nbsp;           System.out.println("输入错误: " + e.getMessage());

&nbsp;       }

&nbsp;   }

&nbsp;   

&nbsp;   /\*\*

&nbsp;    \* 显示所有任务

&nbsp;    \*/

&nbsp;   private static void showAllTasks() {

&nbsp;       System.out.println("\\n=== 所有任务 ===");

&nbsp;       

&nbsp;       if (taskList.isEmpty()) {

&nbsp;           System.out.println("还没有任务哦~");

&nbsp;           return;

&nbsp;       }

&nbsp;       

&nbsp;       // 按开始时间排序

&nbsp;       taskList.sort((t1, t2) -> t1.startTime.compareTo(t2.startTime));

&nbsp;       

&nbsp;       SimpleDateFormat sdf = new SimpleDateFormat("MM月dd日 HH:mm");

&nbsp;       

&nbsp;       for (int i = 0; i < taskList.size(); i++) {

&nbsp;           Task task = taskList.get(i);

&nbsp;           System.out.println((i + 1) + ". " + task.name);

&nbsp;           System.out.println("   内容: " + task.content);

&nbsp;           System.out.println("   时间: " + sdf.format(task.startTime) + " - " + sdf.format(task.endTime));

&nbsp;           System.out.println("   优先级: " + task.priority + " | 类型: " + task.type + " | 项目: " + task.project);

&nbsp;           System.out.println("   ---");

&nbsp;       }

&nbsp;       

&nbsp;       System.out.println("总共 " + taskList.size() + " 个任务");

&nbsp;   }

&nbsp;   

&nbsp;   /\*\*

&nbsp;    \* 解析时间字符串

&nbsp;    \*/

&nbsp;   private static Date parseTime(String timeStr) throws Exception {

&nbsp;       SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

&nbsp;       return sdf.parse(timeStr);

&nbsp;   }

&nbsp;   

&nbsp;   /\*\*

&nbsp;    \* 获取优先级文字

&nbsp;    \*/

&nbsp;   private static String getPriority(int num) {

&nbsp;       switch (num) {

&nbsp;           case 1: return "低";

&nbsp;           case 2: return "中";

&nbsp;           case 3: return "高";

&nbsp;           default: return "中";

&nbsp;       }

&nbsp;   }

&nbsp;   

&nbsp;   /\*\*

&nbsp;    \* 获取类型文字

&nbsp;    \*/

&nbsp;   private static String getType(int num) {

&nbsp;       switch (num) {

&nbsp;           case 1: return "会议";

&nbsp;           case 2: return "截止日期";

&nbsp;           case 3: return "日常";

&nbsp;           default: return "日常";

&nbsp;       }

&nbsp;   }

&nbsp;   

&nbsp;   /\*\*

&nbsp;    \* 检查时间冲突

&nbsp;    \*/

&nbsp;   private static boolean checkTimeConflict(Task newTask) {

&nbsp;       for (Task task : taskList) {

&nbsp;           // 如果新任务的开始时间在某个任务的开始和结束时间之间，就有冲突

&nbsp;           if (newTask.startTime.before(task.endTime) \&\& task.startTime.before(newTask.endTime)) {

&nbsp;               System.out.println(" 与现有任务冲突: " + task.name);

&nbsp;               return true;

&nbsp;           }

&nbsp;       }

&nbsp;       return false;

&nbsp;   }

}



/\*\*

&nbsp;\* 任务类 - 简单的数据容器

&nbsp;\*/

class Task {

&nbsp;   String name;        // 任务名称

&nbsp;   String content;     // 任务内容

&nbsp;   Date startTime;     // 开始时间

&nbsp;   Date endTime;       // 结束时间

&nbsp;   String priority;    // 优先级

&nbsp;   String type;        // 任务类型

&nbsp;   String project;     // 所属项目

&nbsp;   

&nbsp;   public Task(String name, String content, Date startTime, Date endTime, 

&nbsp;               String priority, String type, String project) {

&nbsp;       this.name = name;

&nbsp;       this.content = content;

&nbsp;       this.startTime = startTime;

&nbsp;       this.endTime = endTime;

&nbsp;       this.priority = priority;

&nbsp;       this.type = type;

&nbsp;       this.project = project;

&nbsp;   }

}



