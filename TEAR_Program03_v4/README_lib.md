# Apache POI 库说明

要支持Excel文件(.xlsx)导入功能，需要下载以下Apache POI库的JAR文件并放入lib目录中：

## 必需的JAR文件：
1. **poi-x.y.z.jar** - 核心POI库，提供基本功能
2. **poi-ooxml-x.y.z.jar** - Office Open XML格式支持
3. **poi-ooxml-lite-x.y.z.jar** - 轻量级OOXML支持
4. **commons-collections4-x.y.jar** - Apache Commons Collections库
5. **xmlbeans-x.y.z.jar** - XML处理库

## 下载方式：
1. 访问Apache POI官方网站：https://poi.apache.org/download.html
2. 下载最新的二进制发行版（Binary Distribution）
3. 解压后，将上述JAR文件复制到项目的lib目录中

## 注意事项：
- 确保所有JAR文件的版本一致
- 如果缺少任何依赖，程序可能无法正常运行
- 完成后可以通过run.bat重新启动程序