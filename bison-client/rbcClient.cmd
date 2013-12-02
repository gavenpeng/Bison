@echo off
setlocal

SET CLASSPATH=E:/maven_reposity/org/slf4j/slf4j-api/1.7.5/slf4j-api-1.7.5.jar
SET CLASSPATH=E:/maven_reposity/org/jdom/jdom/2.0.2/jdom-2.0.2.jar;%CLASSPATH%
SET CLASSPATH=E:\cmg-projects\rbc-server\target\rbc-client-1.0.jar;%CLASSPATH%

set RBCSCLIENTMAIN=com.mina.rbc.Main
echo on
java -Dconf.dir=E:\cmg-projects\rbc-client\conf -cp "%CLASSPATH%" %RBCSCLIENTMAIN%

endlocal

