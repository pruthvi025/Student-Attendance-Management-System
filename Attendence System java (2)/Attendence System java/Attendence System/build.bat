@echo off
echo Building and running Attendance Management System...

if exist "target" (
    echo Cleaning previous build...
    rmdir /s /q target
)

echo Compiling project with Maven...
call mvn clean package

if %ERRORLEVEL% neq 0 (
    echo Build failed! Check the error messages above.
    exit /b 1
)

echo Build successful. Starting application...
java -jar target/attendance-management-system-1.0-SNAPSHOT-jar-with-dependencies.jar

echo Application exited.
pause 