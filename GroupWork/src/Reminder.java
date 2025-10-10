// Reminder.java - 提醒类
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class Reminder {
    private Task task;
    private boolean triggered;

    public Reminder(Task task) {
        this.task = task;
        this.triggered = false;
    }

    public void scheduleReminder() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (!triggered && task.getStatus() != Task.TaskStatus.COMPLETED) {
                    triggerReminder();
                    triggered = true;
                }
            }
        }, task.getReminderTime());
    }

    private void triggerReminder() {
        System.out.println("🔔 提醒: 任务 '" + task.getName() + "' 即将开始!");
        System.out.println("   内容: " + task.getContent());
        System.out.println("   开始时间: " + task.getStartTime());
        // 在实际应用中，这里可以添加弹窗、声音提醒等
    }

    public boolean isTriggered() {
        return triggered;
    }
}