# 基础镜像
FROM 172.22.3.4/library/openjdk:8-jre

# VOLUME命令用于让你的容器访问宿主机上的目录。
VOLUME /tmp

# 更改时区
ENV TZ=Asia/Shanghai
RUN ln -sf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# ADD命令有两个参数，源和目标。它的基本作用是从源系统的文件系统上复制文件到目标容器的文件系统。如果源是一个URL，那该URL的内容将被下载并复制到容器中。
ADD dawn-community-manage-backend-1.0.0-SNAPSHOT.jar dawn-community-manage.jar

# 注释配置文件，使用ConfigMap进行配置管理
# ADD application.yml /application.yml

# RUN命令是Dockerfile执行命令的核心部分。它接受命令作为参数并用于创建镜像。
RUN sh -c 'touch /dawn-community-manage.jar'

# ENV命令用于设置环境变量。这些变量以”key=value”的形式存在，并可以在容器内被脚本或者程序调用。
ENV JAVA_OPTS=""

# ENTRYPOINT 帮助你配置一个容器使之可执行化
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /dawn-community-manage.jar" ]