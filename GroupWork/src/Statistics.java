// Statistics.java - 统计类
public class Statistics {
    private int totalTasks;
    private int completedTasks;
    private int overdueTasks;
    private double completionRate;
    private double overdueRate;

    public Statistics(int totalTasks, int completedTasks, int overdueTasks,
                      double completionRate, double overdueRate) {
        this.totalTasks = totalTasks;
        this.completedTasks = completedTasks;
        this.overdueTasks = overdueTasks;
        this.completionRate = completionRate;
        this.overdueRate = overdueRate;
    }

    // Getters
    public int getTotalTasks() { return totalTasks; }
    public int getCompletedTasks() { return completedTasks; }
    public int getOverdueTasks() { return overdueTasks; }
    public double getCompletionRate() { return completionRate; }
    public double getOverdueRate() { return overdueRate; }

    @Override
    public String toString() {
        return String.format("统计信息: 总任务=%d, 已完成=%d, 逾期=%d, 完成率=%.1f%%, 逾期率=%.1f%%",
                totalTasks, completedTasks, overdueTasks, completionRate, overdueRate);
    }
}