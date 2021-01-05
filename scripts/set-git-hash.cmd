@echo off

setlocal enableextensions
  for /f "tokens=*" %%a in ('git rev-parse --short HEAD 2^>^&1') do ( set GIT_HASH=%%a )
  echo %GIT_HASH%
endlocal
