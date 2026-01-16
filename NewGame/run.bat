@echo off
cd /d "%~dp0"
javac -d . com\polygonwars\util\*.java com\polygonwars\entity\*.java com\polygonwars\particle\*.java com\polygonwars\enemy\*.java com\polygonwars\ability\*.java com\polygonwars\manager\*.java com\polygonwars\ui\*.java com\polygonwars\core\*.java
if %ERRORLEVEL% NEQ 0 (
    echo Compilation failed!
    pause
    exit /b %ERRORLEVEL%
)
echo Compilation successful! Starting game...
java -cp . com.polygonwars.core.Game
pause
