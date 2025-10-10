// Project.java - 项目实体类
import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;

public class Project implements Serializable {
    private static final long serialVersionUID = 1L;    private String name;
    private String description;
    private List<Task> tasks;

    public Project(String name, String description) {
        this.name = name;
        this.description = description;
        this.tasks = new ArrayList<>();
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public List<Task> getTasks() { return tasks; }

    public void addTask(Task task) {
        tasks.add(task);
    }

    public void removeTask(Task task) {
        tasks.remove(task);
    }

    public int getCompletedTaskCount() {
        return (int) tasks.stream()
                .filter(task -> task.getStatus() == Task.TaskStatus.COMPLETED)
                .count();
    }

    public double getCompletionRate() {
        if (tasks.isEmpty()) return 0.0;
        return (double) getCompletedTaskCount() / tasks.size() * 100;
    }
}