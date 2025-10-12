# 🔧 配置文件设置指南

本项目的配置文件包含敏感的API密钥信息，需要进行特殊处理以确保安全。

## 🛡️ 安全配置原则

1. **永远不要提交真实的配置文件到Git仓库**
2. **使用模板文件作为配置示例**
3. **在本地创建真实的配置文件**
4. **确保.gitignore正确配置**

## 📋 配置步骤

### 1️⃣ 创建本地配置文件

**复制模板文件：**
```bash
# 复制模板文件作为起点
cp src/main/resources/application-template.yml src/main/resources/application.yml
```

**或者创建新的配置文件：**
```bash
# 直接创建新的配置文件
touch src/main/resources/application.yml
```

### 2️⃣ 编辑配置文件

在 `src/main/resources/application.yml` 中添加你的真实配置：

```yaml
# AI配置
ai:
  kimi:
    api-key: "your-actual-kimi-api-key"  # 从这里获取：https://platform.moonshot.cn/
    base-url: "https://api.moonshot.cn/v1"
    model: "moonshot-v1-8k"
    max-tokens: 2000
    temperature: 0.3

# Spring配置
spring:
  application:
    name: ai-log-analyzer

# 日志配置
logging:
  level:
    com.lllkkk.ai.agent: DEBUG
    org.springframework.web: INFO
```

### 3️⃣ 验证配置

**检查.gitignore配置：**
确保 `.gitignore` 文件包含以下内容：
```
### Sensitive Configuration Files ###
src/main/resources/application.yml
src/main/resources/application-*.yml
src/main/resources/application-*.properties

# Allow template and example files
!src/main/resources/application-template.yml
!src/main/resources/application-example.yml
```

**测试配置是否生效：**
```bash
# 运行应用前检查配置
./mvnw spring-boot:run
```

## 🔑 获取Kimi API密钥

1. **注册账号**：访问 [Moonshot AI平台](https://platform.moonshot.cn/)
2. **创建API密钥**：在控制台中创建新的API密钥
3. **选择合适的模型**：
   - `moonshot-v1-8k`：8K上下文，适合大多数场景
   - `moonshot-v1-32k`：32K上下文，适合长文本分析
   - `moonshot-v1-128k`：128K上下文，适合超长日志分析

## 🚨 安全提醒

### ⚠️ 绝对不要做的事情：
- ❌ 不要将真实的API密钥提交到Git仓库
- ❌ 不要将配置文件分享到公开代码仓库
- ❌ 不要将API密钥硬编码在源代码中
- ❌ 不要将API密钥暴露在日志输出中

### ✅ 推荐的安全实践：
- ✅ 使用环境变量存储敏感配置
- ✅ 定期轮换API密钥
- ✅ 使用不同的密钥用于不同环境（开发/测试/生产）
- ✅ 监控API使用情况，设置告警
- ✅ 在CI/CD流程中安全地注入配置

## 🔧 高级配置选项

### 环境变量配置（推荐）

你可以使用环境变量来配置敏感信息：

```bash
# 设置环境变量
export KIMI_API_KEY="your-actual-api-key"
export SPRING_PROFILES_ACTIVE="dev"
```

然后在 `application.yml` 中引用：
```yaml
ai:
  kimi:
    api-key: ${KIMI_API_KEY:default-key}  # 使用环境变量，提供默认值
```

### 多环境配置

创建不同环境的配置文件：
- `application-dev.yml` - 开发环境
- `application-test.yml` - 测试环境
- `application-prod.yml` - 生产环境

**激活特定配置：**
```bash
# 运行时使用特定配置
./mvnw spring-boot:run -Dspring.profiles.active=dev
```

## 🐛 常见问题

**Q: 应用启动时报配置缺失错误？**
A: 确保创建了 `application.yml` 文件，并且包含了所有必需的配置项。

**Q: 如何验证我的API密钥是否有效？**
A: 启动应用后查看日志，如果AI调用失败会显示相关错误信息。

**Q: 不小心提交了配置文件怎么办？**
A:
1. 立即在平台上重置API密钥
2. 使用 `git filter-branch` 或 `BFG Repo-Cleaner` 从历史记录中删除
3. 更新 .gitignore 确保不再提交

**Q: 团队协作时如何共享配置？**
A: 使用安全的配置共享方式：
- 公司内部配置中心
- 加密的环境变量
- 安全的密钥管理服务

## 📞 需要帮助？

如果你遇到配置相关的问题：
1. 检查配置文件的语法是否正确
2. 确认API密钥是否有效
3. 查看应用启动日志中的错误信息
4. 确保网络可以访问AI服务
5. 联系团队成员或查阅相关文档

---

**记住：配置安全是团队每个成员的责任！ 🛡️**