Private im server  support cluster version 1.0.0<br>

This is a message server based on Netty WebSocket. It also requires Redis as the basic message service, supports large-scale deployment, and supports millions of users.<br><br>

Operating environment:<br>
1. Jdk11<br>
2. Springboot2.2.4<br>
3. Redis<br>

<br>
<br>
<br>

❶→Startup parameter case:<br>
<1,127.0.0.1,9922,0><br>
-1: Describes the operating environment. This version supports test, pre-release, and production lines.<br>
-127.0.0.1: Server IP running the IM service<br>
-9922: Indicates the listening port of the server<br>
-0: Indicates the message forwarding mechanism between clusters, 0/1 0=uses the built-in Socket as the communication mechanism between clusters, 1=uses Redis subscription and publishing as the communication mechanism between clusters<br>
<br>
<br>
<br>

❷→Special Instructions:<br>
1. <-server -Xmx3550m -XX:+HeapDumpOnOutOfMemoryError -XX:+PrintGCDetails -XX:+PrintGCDateStamps>   These parameters must be placed after <1,127.0.0.1,9922,0><br>
2. Please refer to application.yml for Redis configuration. You can specify parameters for the test environment, pre-release environment, and production environment respectively.<br>

❸→Deployment method<br>
1. Jar startup command case<br>
java -jar privateimserver-1.0.0-SNAPSHOT.jar 1,127.0.0.1,9922,0 --server.port=8047 -server -Xmx3550m -XX:+HeapDumpOnOutOfMemoryError -XX:+PrintGCDetails -XX:+PrintGCDateStamps<br>
<br>
java -jar privateimserver-1.0.0-SNAPSHOT.jar 1,127.0.0.1,9955,0 --server.port=8049 -server -Xmx3550m -XX:+HeapDumpOnOutOfMemoryError -XX:+PrintGCDetails -XX:+PrintGCDateStamps<br>
<br>
java -jar privateimserver-1.0.0-SNAPSHOT.jar 1,127.0.0.1,9966,0 --server.port=8048 -server -Xmx3550m -XX:+HeapDumpOnOutOfMemoryError -XX:+PrintGCDetails -XX:+PrintGCDateStamps<br>
<br>

2. Please refer to the docker deployment operation process for how to start docker mode.<br>
<br><br>
