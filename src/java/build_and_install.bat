@ECHO OFF

set MVN=
for /f "delims=" %%i in ('where.exe mvn') do @set MVN="%%i"
call :DequotedEcho %MVN%

IF %MVN% == "" (
    call :DequotedEcho "The `mvn` (Maven) application could not be found. Ensure it is installed and placed in your PATH."
    EXIT /B
) ELSE (
    call :DequotedEcho "Located `mvn` (Maven) - building and installing..."
    mvn clean verify install -U
)
goto :eof

:DequotedEcho
    setlocal
    rem The tilde in the next line is the really important bit.
    set thestring=%~1
    echo %thestring%
    endlocal
    goto :eof