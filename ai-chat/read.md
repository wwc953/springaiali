# 动态管理prompt
## 新增pom
        <dependency>
            <groupId>com.alibaba.cloud.ai</groupId>
            <artifactId>spring-ai-alibaba-starter-nacos-prompt</artifactId>
        </dependency>
## application.properties新增配置
    #nacos prompt tmpl 监听功能
    spring.nacos.config.enabled=true
    spring.nacos.username=nacos
    spring.nacos.password=nacos
    #开启 nacos 的 prompt tmpl 监听功能
    spring.ai.nacos.prompt.template.enabled=true

## nacos 新建配置项
    
    dataId = "spring.ai.alibaba.configurable.prompt" 
    group = "DEFAULT_GROUP" 
    内容是json数组：

 ``` json
    [
        {
            "name":"模板名称",
            "template":"预置的Prompt模板内容",
            "model":{
                "key":"value"
            }
        },
        {
            "name":"mac-template",
            "template":"你是一个MacOS专家，请基于以下上下文回答：\n\n---------------------\n{question_answer_context}\n---------------------\n\n请结合给定上下文和提供的历史信息，用中文 Markdown 格式回答，若答案不在上下文中请明确告知。",
            "model":{
                "key":"value"
            }
        }
    ]
```
        