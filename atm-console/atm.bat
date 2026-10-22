@echo off
rem Ejecuta la consola del ATM: atm <comando> [argumentos]
rem Requiere haber generado el JAR con "mvn clean package".
java -jar "%~dp0target\atm-console.jar" %*
exit /b %ERRORLEVEL%
