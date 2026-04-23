🖥️ Smart Monitoring Platform（ai智能电脑监控平台）

一个能“看懂”你电脑状态的智能监控平台 —— 实时采集硬件数据，AI辅助诊断系统异常。

一个基于Spring Boot的轻量级电脑硬件监控与诊断平台，支持实时监控CPU、内存、磁盘、网速等系统指标，并提供智能内存问题诊断与根因分析功能。

✨ 核心功能
```
实时系统监控：采集CPU使用率、内存占用、磁盘空间、网络上下行速度等关键指标
智能内存诊断：自动检测高内存占用、潜在内存泄漏风险，并给出优化建议
磁盘空间分析：可视化磁盘使用分布，识别大文件与冗余数据
AI根因分析：自动检测异常，调用通义千问深度分析
智能对话： 自然语言查询系统状态，AI助手实时响应
RESTful API：标准化接口，便于对接前端Dashboard或第三方系统
历史回溯： 监控数据持久化，趋势一目了然
```
 
🛠️ 技术栈
```
后端框架：Spring Boot 3.4、Spring MVC
数据持久化：MyBatis-Plus、MySQL
缓存：Redis会话存储、对话历史
AI集成：通义千问API
系统采集：OSHI（系统硬件信息采集）
项目构建：Maven
前端：Thymeleaf + ECharts
```

数据流：硬件(OSHI) → 采集(Service) → 存储(MySQL) → 分析(AI) → 前端展示
 
🚀 快速开始
1. 环境准备
JDK 17+
MySQL 8.0+
Maven 3.8+
 
2. 克隆与配置
```
 git clone https://github.com/yg33568/monitoring-platform.git
 cd monitoring-platform
修改src/main/resources/application.yml中的数据库连接信息：
 spring:
   datasource:
     url: jdbc:mysql://localhost:3306/monitor_db?useSSL=false&serverTimezone=Asia/Shanghai
     username: your_username
     password: your_password
 ```

3. 配置AI API Key
```
spring.ai.dashscope.api-key=你的通义千问API Key
免费申请：阿里云百炼平台
```

4. 启动项目
```
 mvn clean install
 mvn spring-boot:run
```
访问http://localhost:8080即可使用平台。

📁 项目结构  
```
monitoring-platform/
├── src/
│   ├── main/
│   │   ├── java/com/monitor/monitoring_platform/
│   │   │   ├── config/          
│   │   │   │   ├── AiConfig.java
│   │   │   │   └── RedisConfig.java
│   │   │   ├── controller/          # API 接口层
│   │   │   │   ├── AiChatController.java
│   │   │   │   ├── DashboardController.java
│   │   │   │   ├── MetricController.java
│   │   │   │   └── RealSystemMonitorController.java
│   │   │   ├── entity/              # 数据实体类
│   │   │   │   ├── Diagnosis.java
│   │   │   │   ├── DiskInfo.java
│   │   │   │   ├── DiskSpaceAnalysis.java
│   │   │   │   ├── SmartAnalysisResult.java
│   │   │   │   ├── SpaceCategory.java
│   │   │   │   ├── SpaceItem.java
│   │   │   │   └── SystemMetrics.java
│   │   │   ├── mapper/              # 数据访问层
│   │   │   │   └── SystemMetricsMapper.java
│   │   │   ├── service/             # 业务逻辑层
│   │   │   │   ├── AiChatService.java
│   │   │   │   ├── AiSmart.java
│   │   │   │   ├── DashboardService.java
│   │   │   │   ├── DataGeneratorService.java
│   │   │   │   ├── DiskSpaceAnalyzerService.java
│   │   │   │   ├── MetricService.java
│   │   │   │   ├── RealSystemDataService.java
│   │   │   │   ├── RealSystemMonitorService.java
│   │   │   │   ├── RootCauseAnalysisService.java
│   │   │   │   ├── SmartAlertService.java
│   │   │   │   ├── SmartRootCauseService.java
│   │   │   │   └── SystemMetricsService.java
│   │   │   └── MonitoringPlatformApplication.java  # 启动类
│   │   └── resources/               # 配置文件
│   └── test/                        # 单元测试
└── pom.xml                          # Maven 依赖配置
```
 
📊 核心API示例
```
       接口路径                    方法             功能 
 /api/monitor/real-time            GET        获取实时系统指标 
 /api/monitor/diagnose/memory      GET        执行内存问题诊断 
 /api/disk/analysis                GET       获取磁盘空间分析结果 
 /api/rootcause/analyze            POST     提交异常数据并获取根因分析 
 /api/system/real-metrics          GET        获取实时监控数据
 /api/ai/chat                      POST          AI 对话
 ```

📷 界面预览

智能运维监控大屏界面

<img width="1105" height="620" alt="image" src="https://github.com/user-attachments/assets/039a32b2-4ba3-4c05-bcc0-f14d45637902" />

组件监控详情界面

<img width="1104" height="561" alt="image" src="https://github.com/user-attachments/assets/e1cb58cb-762a-4fa0-9523-ae79bec18e0f" />

磁盘空间全景分析

<img width="955" height="812" alt="image" src="https://github.com/user-attachments/assets/63c9b8bf-f197-437d-8562-697fea0557a6" />

AI智能根因分析界面

<img width="2505" height="1049" alt="image" src="https://github.com/user-attachments/assets/47ae8597-0c23-46ed-9bb5-9939d4fc6d5c" />
<img width="2500" height="1294" alt="image" src="https://github.com/user-attachments/assets/13962ef0-5dd3-4161-a91d-c7ac9e83dcf8" />


历史数据查询界面

<img width="1107" height="584" alt="image" src="https://github.com/user-attachments/assets/3a958c2a-2d29-44f5-9826-e9e05a9324b9" />
 
📄 许可证
本项目采用MIT License，可自由使用与修改，详见LICENSE文件。
 
📧 作者
GitHub：https://github.com/yg33568
项目地址：https://github.com/yg33568/monitoring-platform
