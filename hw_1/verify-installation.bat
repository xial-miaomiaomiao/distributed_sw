@echo off
chcp 65001
echo =========================================
echo 验证安装脚本
 echo =========================================

REM 检查 Java 版本
echo 1. 检查 Java 版本
echo ----------------------------------------
java -version
echo.

REM 检查 Maven 版本
echo 2. 检查 Maven 版本
echo ----------------------------------------
mvn -version
echo.

REM 检查 MySQL 版本
echo 3. 检查 MySQL 版本
echo ----------------------------------------
mysql --version 2>nul || echo MySQL 未安装或未添加到环境变量
echo.

REM 检查 Redis 版本
echo 4. 检查 Redis 版本
echo ----------------------------------------
redis-server --version 2>nul || echo Redis 未安装或未添加到环境变量
echo.

REM 检查 Nginx 版本
echo 5. 检查 Nginx 版本
echo ----------------------------------------
nginx -v 2>nul || echo Nginx 未安装或未添加到环境变量
echo.

REM 检查环境变量
echo 6. 检查环境变量
echo ----------------------------------------
echo JAVA_HOME: %JAVA_HOME%
echo MAVEN_HOME: %MAVEN_HOME%
echo PATH 中是否包含 Java: %PATH:JAVA_HOME=%
echo PATH 中是否包含 Maven: %PATH:MAVEN_HOME=%
echo.

REM 检查项目结构
echo 7. 检查项目结构
echo ----------------------------------------
dir /b "D:\AAAAAA\distributed_sw\distributed_sw\hw_1\backend\src"
echo.

echo =========================================
echo 验证完成
 echo =========================================
echo 按任意键退出...
pause >nul
