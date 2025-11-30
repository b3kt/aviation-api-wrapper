@echo off
REM Script untuk menjalankan semua tests di Windows

echo Running all tests...
call mvn clean test

if %ERRORLEVEL% EQU 0 (
    echo All tests passed!
) else (
    echo Some tests failed. Check the output above.
    exit /b 1
)

