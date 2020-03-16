@echo off
SETLOCAL EnableDelayedExpansion

cd ..

::-------------------------------------------------------------------
:: run the Default Control Application
::-------------------------------------------------------------------
echo.
echo.-------------------------------------------------------
echo.Starting The Control Application
echo.-------------------------------------------------------
@echo on


java -cp dist/RobotEngine.jar de.kmj.robots.controlApp.DefaultControlApplication res\ControlApp.config
ENDLOCAL

:: uncomment for debugging purposes
PAUSE

