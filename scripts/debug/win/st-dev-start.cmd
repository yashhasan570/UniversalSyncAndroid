@echo off
::
SET STMONITORED=1
start "" "%ProgramFiles%\UniversalSync\universalsync.exe" --no-console --no-browser
::
goto :eof
