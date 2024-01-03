This is a Netty-based WebSocket communication server that supports cluster mode operation. Running instances can be arbitrarily expanded as needed without any cluster burden, and data distribution is achieved through Redis data sharing (subscription and publishing).

main feature:
1. Simple and easy to understand. On this basis, plug-ins can be expanded to achieve unique functions.
2. Support efficient operation
3. Support cluster operation
4. The mechanism for cluster instances to transmit messages to each other is simple
5. Simple configuration


Operating environment:
Jdk11
Springboot2.2.4




start up:

1. Jar file running mode

$#>java -jar privateimserver-1.0.0-SNAPSHOT.jar 1,<ip>,<port>



2. docker running mode

2.1. Install docker environment

2.2. Create docker-compose.yml


---------------document content------------------
#version: "1.0.0"
services:
   privateimserver:
     container_name: privateimserver
     build:
       context: ./privateimserver/im
       dockerfile: dockerfile
     ports:
       - "0.0.0.0:<port>:<port>"





2.3. Create dockerfile again

---------------document content------------------
FROM centos:7


MAINTAINER privateimserver

VOLUME /privateimserver/im

RUN mkdir -p /privateimserver/im

WORKDIR/privateimserver/im

ADD jdk-8u311-linux-x64.tar.gz /privateimserver/im

#Configure java environment variables

ENV JAVA_HOME /privateimserver/im/jdk1.8.0_311

ENV CLASSPATH $JAVA_HOME/lib/dt.jar:$JAVA_HOME/lib/tools.jar

ENV PATH $PATH:$JAVA_HOME/bin:$CATALINA_HOME/bin

COPY ./privateimserver-1.0.0-SNAPSHOT.jar /privateimserver/im/privateimserver-1.0.0-SNAPSHOT.jar

EXPOSE <port>

ENTRYPOINT ["nohup", "java", "-Dfile.encoding=utf-8","-Xms1024m","-Xms1024m","-jar","/privateimserver/im/privateimserver-1.0.0-SNAPSHOT. jar"] # Defined parameters

CMD ["$@"] # Variable parameters



2.4. docker build -t privateimserver:v1.0.0.


2.5 docker run -itd -p <port>:<port> --name privateimserver_01 <Images name> 1,<ip>,<port>




3. Operation parameter description: 1,<ip>,<port>

1: Test environment, 2: Production environment

<ip>: IP address of the device running this service

<port>: IM server external service port
