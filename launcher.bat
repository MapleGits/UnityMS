@echo off
color f
start /b world.bat
ping localhost -n 1 >nul
start /b login.bat
ping localhost -n 1 >nul
start /b channel.bat