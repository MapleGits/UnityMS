@echo off
@title Login Server
@color f
set CLASSPATH=.;dist\unityms.jar;dist\mina-core.jar;dist\slf4j-api.jar;dist\slf4j-jdk14.jar;dist\mysql-connector-java-bin.jar
java -Xmx100m -Drecvops=recvops.properties -Dsendops=sendops.properties -Dwzpath=wz\ -Dlogin.config=login.properties -Djavax.net.ssl.keyStore=filename.keystore -Djavax.net.ssl.keyStorePassword=passwd -Djavax.net.ssl.trustStore=filename.keystore -Djavax.net.ssl.trustStorePassword=passwd net.login.LoginServer
pause