@ECHO OFF

set MVN=
for /f "delims=" %%i in ('where.exe mvn') do @set MVN="%%i"
call :DequotedEcho %MVN%

IF %MVN% == "" (
    call :DequotedEcho "The `mvn` (Maven) application could not be found. Ensure it is installed and placed in your PATH."
    EXIT /B
) ELSE (
    %MVN% exec:java -Dexec.mainClass="gov.nist.hla.genx.GenxPongFederate" -Dexec.args="%*"
)
goto :eof

:DequotedEcho
    setlocal
    set thestring=%~1
    echo %thestring%
    endlocal
    goto :eof