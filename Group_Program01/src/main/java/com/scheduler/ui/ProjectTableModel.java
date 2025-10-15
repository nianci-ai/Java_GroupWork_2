package com.scheduler.ui;

import com.scheduler.model.Project;
import com.scheduler.service.ProjectService;

import javax.swing.table.AbstractTableModel;
import java.util.List;

/**
 * 项目表格的数据模型类，负责管理项目表格的数据显示和更新
 */
public class ProjectTableModel extends AbstractTableModel {
    private static final long serialVersionUID = 1L;
    
    private final String[] columnNames = {"项目名称", "描述"};
    private List<Project> projects;
    private final ProjectService projectService;
    
    /**
     * 构造函数
     * @param projectService 项目服务对象，用于获取项目数据
     */
    public ProjectTableModel(ProjectService projectService) {
        this.projectService = projectService;
        this.projects = projectService.getAllProjects();
    }
    
    /**
     * 更新表格数据
     */
    public void updateData() {
        this.projects = projectService.getAllProjects();
        fireTableDataChanged();
    }
    
    /**
     * 根据索引获取项目对象
     * @param rowIndex 行索引
     * @return 项目对象
     */
    public Project getProjectAt(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < projects.size()) {
            return projects.get(rowIndex);
        }
        return null;
    }
    
    @Override
    public int getRowCount() {
        return projects.size();
    }
    
    @Override
    public int getColumnCount() {
        return columnNames.length;
    }
    
    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }
    
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Project project = projects.get(rowIndex);
        
        switch (columnIndex) {
            case 0: // 项目名称
                return project.getName();
            case 1: // 描述
                return project.getDescription();
            default:
                return null;
        }
    }
    
    /**
     * 按项目名称排序
     */
    public void sortByName() {
        projects.sort((p1, p2) -> p1.getName().compareToIgnoreCase(p2.getName()));
        fireTableDataChanged();
    }
}