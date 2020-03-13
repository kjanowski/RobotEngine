SETLOCAL EnableDelayedExpansion

::-----------------------------------------------
:: change this line to point to your
:: doxygen installation folder
::-----------------------------------------------
SET DOXYGEN_HOME=C:\Program Files\doxygen


cd ..\src
"%DOXYGEN_HOME%\doxygen.exe" ..\doxygen\doxygen.cfg

PAUSE
ENDLOCAL