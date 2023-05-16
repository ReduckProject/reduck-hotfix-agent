## 介绍
主要实验java-agent的功能，目前实现功能如下
* 通过`premain`方式转换实现动态替换jar包内的类
    * 需要类名和原类名保持一致

## 使用
目标 `jar`启动的时候通过增加`-javaagent:${AgentJarPath}=${hotfixClassPath}`参数
* `${AgentJarPath} `为代理jar路径 
* `${hotfixClassPath}`为替换的类路径