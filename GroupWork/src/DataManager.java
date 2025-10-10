// DataManager.java - 数据持久化管理
import java.io.*;
import java.util.List;

public class DataManager {
    private static final String TASKS_FILE = "tasks.dat";
    private static final String PROJECTS_FILE = "projects.dat";

    public static void saveTasks(List<Task> tasks) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(TASKS_FILE))) {
            oos.writeObject(tasks);
            System.out.println("任务数据保存成功");
        } catch (IOException e) {
            System.err.println("保存任务数据失败: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public static List<Task> loadTasks() {
        File file = new File(TASKS_FILE);
        if (!file.exists()) {
            return new java.util.ArrayList<>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(TASKS_FILE))) {
            return (List<Task>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("加载任务数据失败: " + e.getMessage());
            return new java.util.ArrayList<>();
        }
    }

    public static void saveProjects(List<Project> projects) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(PROJECTS_FILE))) {
            oos.writeObject(projects);
            System.out.println("项目数据保存成功");
        } catch (IOException e) {
            System.err.println("保存项目数据失败: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public static List<Project> loadProjects() {
        File file = new File(PROJECTS_FILE);
        if (!file.exists()) {
            return new java.util.ArrayList<>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(PROJECTS_FILE))) {
            return (List<Project>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("加载项目数据失败: " + e.getMessage());
            return new java.util.ArrayList<>();
        }
    }

    public static void exportToTextFile(List<Task> tasks, String filePath) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            writer.println("=== 日程安排导出 ===");
            writer.println("导出时间: " + new java.util.Date());
            writer.println("任务数量: " + tasks.size());
            writer.println();

            for (Task task : tasks) {
                writer.printf("任务: %s%n", task.getName());
                writer.printf("类型: %s%n", task.getType());
                writer.printf("状态: %s%n", task.getStatus());
                writer.printf("开始: %s%n", task.getStartTime());
                writer.printf("结束: %s%n", task.getEndTime());
                writer.printf("优先级: %d%n", task.getPriority());
                writer.printf("项目: %s%n", task.getProject());
                writer.printf("内容: %s%n", task.getContent());
                writer.println("---");
            }
            System.out.println("数据导出成功: " + filePath);
        } catch (IOException e) {
            System.err.println("导出数据失败: " + e.getMessage());
        }
    }
}

