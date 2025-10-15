# 个人日程安排与提醒系统使用指南

本项目是一个个人日程安排与提醒系统，可以帮助用户管理工作任务、设置提醒、管理待办事项等。

## 项目结构
```
TEAR_Program/
├── bin/           # 编译后的class文件
└── src/main/java/com/scheduler/ # 源代码目录
    ├── Main.java                  # 程序入口
    ├── model/                     # 数据模型
    ├── service/                   # 服务层
    ├── storage/                   # 数据存储
    └── ui/                        # 用户界面
```

## 不通过CMD运行程序的方法

除了使用命令行（CMD）运行外，您还可以通过以下方式运行程序：

### 方法一：使用Java IDE（推荐）

#### 使用IntelliJ IDEA运行：
1. 打开IntelliJ IDEA
2. 选择 "File" > "Open..."
3. 浏览并选择项目根目录 `d:\Java program\TEAR_Program`
4. 等待项目导入完成
5. 在左侧项目结构中找到 `src/main/java/com/scheduler/Main.java`
6. 右键点击Main.java文件，选择 "Run 'Main.main()'"

#### 使用Eclipse运行：
1. 打开Eclipse
2. 选择 "File" > "Import..."
3. 选择 "General" > "Existing Projects into Workspace"
4. 点击 "Next"，然后点击 "Browse..." 选择项目根目录
5. 选中项目，点击 "Finish"
6. 在Package Explorer中找到 `com.scheduler` 包下的 `Main.java`
7. 右键点击Main.java文件，选择 "Run As" > "Java Application"

#### 使用NetBeans运行：
1. 打开NetBeans
2. 选择 "File" > "Open Project..."
3. 浏览并选择项目根目录
4. 点击 "Open Project"
5. 在Projects面板中展开项目，找到 `Main.java`
6. 右键点击Main.java文件，选择 "Run File"

### 方法二：使用Java图形界面启动器

您可以创建一个简单的批处理文件（.bat），双击即可运行程序：

1. 在项目根目录创建一个文本文件，命名为 `启动程序.bat`
2. 编辑文件内容，添加以下命令：
   ```
   @echo off
   java -cp bin com.scheduler.Main
   pause
   ```
3. 保存文件，然后双击该批处理文件即可运行程序

## 功能说明

1. **日程录入**：记录任务名称、内容、时间、优先级等
2. **提醒功能**：根据任务时间提前提醒
3. **任务管理**：标记任务状态，支持排序
4. **数据统计**：统计任务完成情况和延迟率
5. **数据备份**：定期自动备份和手动备份功能

## 开发注意事项

- 确保JDK已正确安装并配置环境变量
- 如需修改代码，请使用UTF-8编码以支持中文显示
- 程序默认会在应用程序目录下创建数据文件和备份文件