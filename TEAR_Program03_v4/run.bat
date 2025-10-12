@echo off

REM 个人日程安排与提醒系统启动脚本

REM 创建bin目录（如果不存在）
if not exist bin mkdir bin

REM 编译源代码（指定UTF-8编码）
javac -encoding UTF-8 -d bin src\main\java\com\schedule\Main.java src\main\java\com\schedule\model\Task.java src\main\java\com\schedule\service\ExcelImportService.java src\main\java\com\schedule\ui\MainFrame.java src\main\java\com\schedule\util\DateUtil.java

REM 检查编译是否成功
if %errorlevel% neq 0 (
    echo Compile failed! Please check your code.
    pause
    exit /b %errorlevel%
)

REM 运行程序
echo Compile success, starting program...
java -cp bin com.schedule.Main

REM 暂停以便查看输出
pause