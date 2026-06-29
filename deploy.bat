@echo off
chcp 65001 >nul
cd /d "%~dp0"

echo.
echo ╔══════════════════════════════════════════════════╗
echo ║     🚀 高校教资刷题 - 一键部署                  ║
echo ╚══════════════════════════════════════════════════╝
echo.
echo   [1] 全部部署 (后端 + 前端)
echo   [2] 仅部署后端 (SpringBoot)
echo   [3] 仅部署前端 (Vue3)
echo   [4] 查看服务状态
echo   [5] 查看后端日志
echo   [6] 查看前端日志
echo.

set /p choice="请选择 (1-6): "

if "%choice%"=="1" python deploy.py all
if "%choice%"=="2" python deploy.py backend
if "%choice%"=="3" python deploy.py frontend
if "%choice%"=="4" python deploy.py all status
if "%choice%"=="5" python deploy.py backend logs
if "%choice%"=="6" python deploy.py frontend logs

echo.
echo 按任意键退出...
pause >nul
