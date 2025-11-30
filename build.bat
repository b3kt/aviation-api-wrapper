@echo off
REM Script untuk build aplikasi di Windows

echo Building Aviation API...
call mvn clean package

if %ERRORLEVEL% EQU 0 (
    echo Build successful!
    echo JAR location: target\aviation-api-0.0.1-SNAPSHOT.jar
    echo.
    echo To run the application:
    echo   java -jar target\aviation-api-0.0.1-SNAPSHOT.jar
    echo   OR
    echo   mvn spring-boot:run
) else (
    echo Build failed. Check the output above.
    exit /b 1
)

