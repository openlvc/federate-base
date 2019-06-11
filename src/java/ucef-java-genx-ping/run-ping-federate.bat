@ECHO OFF

set JAVA_MAIN_CLASS="gov.nist.hla.genx.GenxPingFederate"

set MVN=
for /f "delims=" %%i in ('where.exe mvn') do @set MVN="%%i"
IF %MVN% == "" (
    call :DequotedEcho "The `mvn` (Maven) application could not be found. Ensure it is installed and placed in your PATH."
    EXIT /B
) ELSE (
    call :WaitForFederationManager
    %MVN% exec:java -Dexec.mainClass="%JAVA_MAIN_CLASS%" -Dexec.args="%*"
)
goto :eof

:WaitForFederationManager
    setlocal enabledelayedexpansion
    REM 'ping' the HTTP server on the federation manager  
    REM to determine when it's a good time to start  
    set CURL_RESPONSE=""
    for /f "delims=" %%i in ('curl -s http://localhost:8080/query/is-waiting-for-federates') do @set CURL_RESPONSE="%%i"
    if %CURL_RESPONSE% == "true" (
        REM ready to go
        EXIT /B
    )
    call :DequotedEcho "The Federation Manager does not seem to be ready yet..."
    TIMEOUT /t 5 /NOBREAK
    if ERRORLEVEL 1 (
        REM if the user presses CTRL+C we want to exit completely
        EXIT /B
        goto :eof
    )
    REM keep looping
    goto :WaitForFederationManager

:DequotedEcho
    setlocal
    set thestring=%~1
    echo %thestring%
    endlocal
    goto :eof