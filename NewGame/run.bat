@echo off
cd /d "%~dp0"
javac -d . util\*.java entity\*.java particle\*.java enemy\*.java ability\*.java manager\*.java ui\*.java core\*.java
if %ERRORLEVEL% NEQ 0 (
    echo Compilation failed!
    pause
    exit /b %ERRORLEVEL%
)
echo Compilation successful! Starting game...
java -cp . core.Game
pause
