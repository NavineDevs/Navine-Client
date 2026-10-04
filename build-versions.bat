@echo off
setlocal EnableDelayedExpansion

set OUT=C:\Users\hitbo\Downloads\output
if not exist "%OUT%" mkdir "%OUT%"

set TARGETS=1.21.11 26 26.1 26.1.2 26.2 26.3
set BRANDS=navine navuryx
set FAILED=

echo Building Navine/Navuryx Client targets into %OUT%
echo.

for %%B in (%BRANDS%) do (
  for %%T in (%TARGETS%) do (
    echo ========================================
    echo  %%B %%T
    echo ========================================
    call gradlew.bat clean build "-Ptarget=%%T" "-Pbrand=%%B" --no-daemon
    if errorlevel 1 (
      echo FAILED: %%B %%T
      set FAILED=!FAILED! %%B-%%T
    ) else (
      echo OK: %%B %%T
    )
    echo.
  )
)

if defined FAILED (
  echo Failures:!FAILED!
  exit /b 1
)

echo Built jars in %OUT%
dir /b "%OUT%\Navine-Client-*.jar" 2>nul
dir /b "%OUT%\Navuryx-Client-*.jar" 2>nul
exit /b 0
