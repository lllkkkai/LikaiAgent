#!/bin/bash

# AI日志分析项目 - 初始化设置脚本
# 用于安全地创建和配置应用程序

echo "🚀 AI日志分析项目 - 初始化设置"
echo "=================================="
echo

# 检查Java环境
if command -v java &> /dev/null; then
    echo "✅ Java环境检测: $(java -version 2>&1 | head -n1)"
else
    echo "❌ 未检测到Java环境，请先安装Java 21+"
    exit 1
fi

# 检查Maven环境
if command -v mvn &> /dev/null; then
    echo "✅ Maven环境检测: $(mvn -version | head -n1)"
else
    echo "❌ 未检测到Maven环境，请先安装Maven 3.6+"
    exit 1
fi

echo
echo "📋 配置文件设置"
echo "----------------"

# 检查配置文件是否存在
CONFIG_FILE="src/main/resources/application.yml"
TEMPLATE_FILE="src/main/resources/application-template.yml"

if [ -f "$CONFIG_FILE" ]; then
    echo "⚠️  配置文件 $CONFIG_FILE 已存在"
    read -p "是否覆盖现有配置文件？(y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo "🛑 操作已取消"
        exit 0
    fi
fi

# 复制模板文件
if [ -f "$TEMPLATE_FILE" ]; then
    cp "$TEMPLATE_FILE" "$CONFIG_FILE"
    echo "✅ 已从模板创建配置文件: $CONFIG_FILE"
else
    echo "❌ 模板文件 $TEMPLATE_FILE 不存在，创建基础配置文件"
    cat > "$CONFIG_FILE" << 'EOF'
# AI配置
ai:
  kimi:
    api-key: "your-kimi-api-key-here"  # 请替换为你的Kimi API密钥
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
EOF
fi

echo
echo "🔑 重要提醒"
echo "-----------"
echo "1. 请编辑 $CONFIG_FILE 文件，填入你的Kimi API密钥"
echo "2. 可以从 https://platform.moonshot.cn/ 获取API密钥"
echo "3. 根据需要调整其他配置项（如模型、温度参数等）"
echo "4. 确保不要提交真实的配置文件到Git仓库"
echo
echo "🛡️  安全配置检查"
echo "----------------"

# 验证gitignore配置
if grep -q "application.yml" .gitignore; then
    echo "✅ .gitignore 已配置忽略 application.yml"
else
    echo "⚠️  .gitignore 未配置忽略 application.yml，请手动添加"
fi

# 验证敏感信息
if grep -q "sk-PcYFH3YUMdf9g8IJ5IUcVEBxI2BP3viBTGgzsgXzyOnZoIKU" "$CONFIG_FILE" 2>/dev/null; then
    echo "❌ 警告：配置文件中包含示例API密钥，请立即替换为真实密钥"
fi

echo
echo "🎯 下一步操作"
echo "-----------"
echo "1. 编辑配置文件: vim $CONFIG_FILE"
echo "2. 运行项目: ./mvnw spring-boot:run"
echo "3. 查看文档: cat CONFIG_SETUP.md"
echo
echo "🎉 设置完成！项目已准备好运行。"
echo "   记住：保护好你的API密钥，不要提交到代码仓库！"

# 可选：直接打开编辑器
if command -v code &> /dev/null; then
    read -p "是否使用VS Code打开配置文件？(y/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        code "$CONFIG_FILE"
    fi
elif command -v vim &> /dev/null; then
    read -p "是否使用Vim编辑配置文件？(y/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        vim "$CONFIG_FILE"
    fi
fi

echo "✨ 祝使用愉快！"