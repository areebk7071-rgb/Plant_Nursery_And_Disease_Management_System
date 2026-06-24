@echo off
setlocal
title Botanical Treatment Advisor Launcher

echo ===================================================
echo   Botanical Treatment Advisor Launcher
echo ===================================================
echo.

set JAR_FILE=target\plant-disease-management-1.0.0-shaded.jar

if not exist "%JAR_FILE%" (
    echo [INFO] Shaded executable JAR not found. Building project...
    echo.
    call mvn clean package -DskipTests
) else (
    echo [INFO] Shaded executable JAR found.
)

echo.
if exist "%JAR_FILE%" (
    echo [INFO] Starting application...
    echo.
    java -jar "%JAR_FILE%"
) else (
    echo [ERROR] Failed to build or locate the shaded executable JAR.
    echo.
    pause
)

endlocal
