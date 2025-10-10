// JCalendar.java - 简单的日历组件
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class JCalendar extends JPanel {
    private Calendar calendar;
    private JLabel monthLabel;
    private JPanel daysPanel;

    public JCalendar() {
        calendar = Calendar.getInstance();
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());

        // 月份导航
        JPanel navigationPanel = new JPanel(new BorderLayout());
        monthLabel = new JLabel("", JLabel.CENTER);
        monthLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));

        JButton prevButton = new JButton("←");
        JButton nextButton = new JButton("→");

        prevButton.addActionListener(e -> {
            calendar.add(Calendar.MONTH, -1);
            updateCalendar();
        });

        nextButton.addActionListener(e -> {
            calendar.add(Calendar.MONTH, 1);
            updateCalendar();
        });

        navigationPanel.add(prevButton, BorderLayout.WEST);
        navigationPanel.add(monthLabel, BorderLayout.CENTER);
        navigationPanel.add(nextButton, BorderLayout.EAST);

        // 星期标题
        JPanel weekdaysPanel = new JPanel(new GridLayout(1, 7));
        String[] weekdays = {"日", "一", "二", "三", "四", "五", "六"};
        for (String day : weekdays) {
            JLabel label = new JLabel(day, JLabel.CENTER);
            label.setFont(new Font("微软雅黑", Font.BOLD, 14));
            weekdaysPanel.add(label);
        }

        // 日期面板
        daysPanel = new JPanel(new GridLayout(6, 7));

        add(navigationPanel, BorderLayout.NORTH);
        add(weekdaysPanel, BorderLayout.CENTER);
        add(daysPanel, BorderLayout.SOUTH);

        updateCalendar();
    }

    private void updateCalendar() {
        daysPanel.removeAll();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月");
        monthLabel.setText(sdf.format(calendar.getTime()));

        Calendar temp = (Calendar) calendar.clone();
        temp.set(Calendar.DAY_OF_MONTH, 1);

        int firstDayOfWeek = temp.get(Calendar.DAY_OF_WEEK);
        int daysInMonth = temp.getActualMaximum(Calendar.DAY_OF_MONTH);

        // 填充前面的空白
        for (int i = 1; i < firstDayOfWeek; i++) {
            daysPanel.add(new JLabel(""));
        }

        // 添加日期按钮
        for (int day = 1; day <= daysInMonth; day++) {
            JButton dayButton = new JButton(String.valueOf(day));
            dayButton.setMargin(new Insets(2, 2, 2, 2));

            final int currentDay = day;
            dayButton.addActionListener(e -> {
                temp.set(Calendar.DAY_OF_MONTH, currentDay);
                firePropertyChange("calendar", null, temp.getTime());
            });

            daysPanel.add(dayButton);
        }

        daysPanel.revalidate();
        daysPanel.repaint();
    }

    public Date getDate() {
        return calendar.getTime();
    }

    public void setDate(Date date) {
        calendar.setTime(date);
        updateCalendar();
    }
}
