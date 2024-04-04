Private im server  support cluster version 1.0.0

This is a message server based on Netty WebSocket. It also requires Redis as the basic message service, supports large-scale deployment, and supports millions of users.

❶→Startup parameter case:
<1,127.0.0.1,9922,0>
-1: Describes the operating environment. This version supports test, pre-release, and production lines.
-127.0.0.1: Server IP running the IM service
-9922: Indicates the listening port of the server
-0: Indicates the message forwarding mechanism between clusters, 0/1 0=uses the built-in Socket as the communication mechanism between clusters, 1=uses Redis subscription and publishing as the communication mechanism between clusters



❷→Special Instructions:
1. <-server -Xmx3550m -XX:+HeapDumpOnOutOfMemoryError -XX:+PrintGCDetails -XX:+PrintGCDateStamps>   These parameters must be placed after <1,127.0.0.1,9922,0>
2. Please refer to application.yml for Redis configuration. You can specify parameters for the test environment, pre-release environment, and production environment respectively.

❸→Deployment method
1. Jar startup command case
java -jar privateimserver-1.0.0-SNAPSHOT.jar 1,127.0.0.1,9922,0 --server.port=8047 -server -Xmx3550m -XX:+HeapDumpOnOutOfMemoryError -XX:+PrintGCDetails -XX:+PrintGCDateStamps

java -jar privateimserver-1.0.0-SNAPSHOT.jar 1,127.0.0.1,9955,0 --server.port=8049 -server -Xmx3550m -XX:+HeapDumpOnOutOfMemoryError -XX:+PrintGCDetails -XX:+PrintGCDateStamps

java -jar privateimserver-1.0.0-SNAPSHOT.jar 1,127.0.0.1,9966,0 --server.port=8048 -server -Xmx3550m -XX:+HeapDumpOnOutOfMemoryError -XX:+PrintGCDetails -XX:+PrintGCDateStamps


2. Please refer to the docker deployment operation process for how to start docker mode.