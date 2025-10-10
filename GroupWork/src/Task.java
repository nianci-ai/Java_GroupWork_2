// Task.java - 任务实体类
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.io.Serializable;

//public class Task implements Comparable<Task>, Serializable {
   // private static final long serialVersionUID = 1L;

public class Task implements Comparable<Task> {
    private static final long serialVersionUID = 1L;
    private String id;
    private String name;
    private String content;
    private Date startTime;
    private Date endTime;
    private int priority; // 1-5，1为最高优先级
    private String project;
    private TaskStatus status;
    private TaskType type;
    private Date reminderTime;

    public enum TaskStatus {
        NOT_STARTED, IN_PROGRESS, COMPLETED
    }

    public enum TaskType {
        MEETING, DEADLINE, DAILY_TASK
    }

    public Task(String name, String content, Date startTime, Date endTime,
                int priority, String project, TaskType type) {
        this.id = generateId();
        this.name = name;
        this.content = content;
        this.startTime = startTime;
        this.endTime = endTime;
        this.priority = priority;
        this.project = project;
        this.type = type;
        this.status = TaskStatus.NOT_STARTED;
        calculateReminderTime();
    }

    private String generateId() {
        return "TASK_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
    }

    private void calculateReminderTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startTime);
        calendar.add(Calendar.MINUTE, -30); // 提前30分钟提醒
        this.reminderTime = calendar.getTime();
    }

    // Getters and Setters
    public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Date getStartTime() { return startTime; }
    public void setStartTime(Date startTime) {
        this.startTime = startTime;
        calculateReminderTime();
    }
    public Date getEndTime() { return endTime; }
    public void setEndTime(Date endTime) { this.endTime = endTime; }
    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }
    public String getProject() { return project; }
    public void setProject(String project) { this.project = project; }
    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }
    public TaskType getType() { return type; }
    public void setType(TaskType type) { this.type = type; }
    public Date getReminderTime() { return reminderTime; }

    public boolean isOverdue() {
        return new Date().after(endTime) && status != TaskStatus.COMPLETED;
    }

    public long getRemainingMinutes() {
        long diff = endTime.getTime() - new Date().getTime();
        return diff / (60 * 1000);
    }

    @Override
    public int compareTo(Task other) {
        // 按优先级和截止时间排序
        if (this.priority != other.priority) {
            return Integer.compare(this.priority, other.priority);
        }
        return this.endTime.compareTo(other.endTime);
    }

    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return String.format("任务: %s | 类型: %s | 开始: %s | 结束: %s | 优先级: %d | 状态: %s",
                name, type, sdf.format(startTime), sdf.format(endTime), priority, status);
    }
}
