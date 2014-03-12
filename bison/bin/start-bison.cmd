@echo off
setlocal

SET CLASSPATH=E:/maven_reposity/org/slf4j/slf4j-api/1.7.5/slf4j-api-1.7.5.jar
SET CLASSPATH=E:/maven_reposity/org/jdom/jdom/2.0.2/jdom-2.0.2.jar;%CLASSPATH%
SET CLASSPATH=E:/maven_reposity/mysql/mysql-connector-java/5.1.17/mysql-connector-java-5.1.17.jar;%CLASSPATH%
SET CLASSPATH=E:\maven_reposity\com\github\stephenc\high-scale-lib\high-scale-lib\1.1.2\high-scale-lib-1.1.2.jar;%CLASSPATH%
SET CLASSPATH=E:/cmg-projects/mina/target/mina-0.0.1-SNAPSHOT.jar;%CLASSPATH%
SET CLASSPATH=E:\cmg-projects\bison-stub\target\bison-stub-0.0.1-SNAPSHOT.jar;%CLASSPATH%
SET CLASSPATH=E:\cmg-projects\bison\target\bison-server-0.0.1-SNAPSHOT.jar;%CLASSPATH%

set RBCSERVERMAIN=com.chamago.bison.server.BisonServer
echo on
java -Dconf.dir=E:\cmg-projects\bison\conf -cp "%CLASSPATH%" %RBCSERVERMAIN%

endlocal

