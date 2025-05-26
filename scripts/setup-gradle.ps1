# Setup Gradle for Happy_Kid Android Development

function Write-ColorOutput($ForegroundColor) {
    $fc = $host.UI.RawUI.ForegroundColor
    $host.UI.RawUI.ForegroundColor = $ForegroundColor
    if ($args) {
        Write-Output $args
    }
    $host.UI.RawUI.ForegroundColor = $fc
}

function Download-GradleWrapper {
    Write-ColorOutput Yellow "Setting up Gradle wrapper..."
    
    $gradleWrapperUrl = "https://services.gradle.org/distributions/gradle-8.4-bin.zip"
    $gradleWrapperJarUrl = "https://github.com/gradle/gradle/raw/v8.4.0/gradle/wrapper/gradle-wrapper.jar"
    
    # Create gradle wrapper directory
    $wrapperDir = "gradle\wrapper"
    if (-not (Test-Path $wrapperDir)) {
        New-Item -ItemType Directory -Path $wrapperDir -Force | Out-Null
    }
    
    # Download gradle-wrapper.jar
    $jarPath = "$wrapperDir\gradle-wrapper.jar"
    try {
        Write-ColorOutput Yellow "Downloading Gradle wrapper JAR..."
        Invoke-WebRequest -Uri $gradleWrapperJarUrl -OutFile $jarPath
        Write-ColorOutput Green "✓ Downloaded gradle-wrapper.jar"
    } catch {
        Write-ColorOutput Red "Failed to download gradle-wrapper.jar: $($_.Exception.Message)"
        return $false
    }
    
    # Create gradle-wrapper.properties
    $propertiesPath = "$wrapperDir\gradle-wrapper.properties"
    $propertiesContent = @"
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-8.4-bin.zip
networkTimeout=10000
validateDistributionUrl=true
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
"@
    
    Set-Content -Path $propertiesPath -Value $propertiesContent
    Write-ColorOutput Green "✓ Created gradle-wrapper.properties"
    
    # Create gradlew.bat
    $gradlewBatContent = @'
@rem
@rem Copyright 2015 the original author or authors.
@rem
@rem Licensed under the Apache License, Version 2.0 (the "License");
@rem you may not use this file except in compliance with the License.
@rem You may obtain a copy of the License at
@rem
@rem      https://www.apache.org/licenses/LICENSE-2.0
@rem
@rem Unless required by applicable law or agreed to in writing, software
@rem distributed under the License is distributed on an "AS IS" BASIS,
@rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@rem See the License for the specific language governing permissions and
@rem limitations under the License.
@rem

@if "%DEBUG%"=="" @echo off
@rem ##########################################################################
@rem
@rem  Gradle startup script for Windows
@rem
@rem ##########################################################################

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~dp0
if "%DIRNAME%"=="" set DIRNAME=.
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%

@rem Resolve any "." and ".." in APP_HOME to make it shorter.
for %%i in ("%APP_HOME%") do set APP_HOME=%%~fi

@rem Add default JVM options here. You can also use JAVA_OPTS and GRADLE_OPTS to pass JVM options to this script.
set DEFAULT_JVM_OPTS="-Xmx4096m" "-Xms1024m" "-XX:MaxPermSize=512m" "-XX:+HeapDumpOnOutOfMemoryError" "-Dfile.encoding=UTF-8"

@rem Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if %ERRORLEVEL% equ 0 goto execute

echo.
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

if exist "%JAVA_EXE%" goto execute

echo.
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:execute
@rem Setup the command line

set CLASSPATH=%APP_HOME%\gradle\wrapper\gradle-wrapper.jar


@rem Execute Gradle
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %GRADLE_OPTS% "-Dorg.gradle.appname=%APP_BASE_NAME%" -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*

:end
@rem End local scope for the variables with windows NT shell
if %ERRORLEVEL% equ 0 goto mainEnd

:fail
rem Set variable GRADLE_EXIT_CONSOLE if you need the _script_ return code instead of
rem the _cmd_ return code when the batch file is called from a command line.
if not "" == "%GRADLE_EXIT_CONSOLE%" exit 1
exit /b 1

:mainEnd
if "%OS%"=="Windows_NT" endlocal

:omega
'@
    
    Set-Content -Path "gradlew.bat" -Value $gradlewBatContent
    Write-ColorOutput Green "✓ Created gradlew.bat"
    
    return $true
}

function Test-GradleSetup {
    Write-ColorOutput Yellow "Testing Gradle setup..."
    
    if (-not (Test-Path "gradlew.bat")) {
        Write-ColorOutput Red "✗ gradlew.bat not found"
        return $false
    }
    
    if (-not (Test-Path "gradle\wrapper\gradle-wrapper.jar")) {
        Write-ColorOutput Red "✗ gradle-wrapper.jar not found"
        return $false
    }
    
    if (-not (Test-Path "gradle\wrapper\gradle-wrapper.properties")) {
        Write-ColorOutput Red "✗ gradle-wrapper.properties not found"
        return $false
    }
    
    Write-ColorOutput Green "✓ All Gradle wrapper files present"
    
    # Test Gradle execution
    try {
        Write-ColorOutput Yellow "Testing Gradle execution..."
        $gradleOutput = & .\gradlew.bat --version
        if ($LASTEXITCODE -eq 0) {
            Write-ColorOutput Green "✓ Gradle wrapper working"
            Write-ColorOutput White "  $($gradleOutput[0])"
            return $true
        } else {
            Write-ColorOutput Red "✗ Gradle wrapper failed"
            return $false
        }
    } catch {
        Write-ColorOutput Red "✗ Error testing Gradle: $($_.Exception.Message)"
        return $false
    }
}

# Main execution
Write-ColorOutput Green "Gradle Setup for Happy_Kid Android Development"
Write-ColorOutput Green "============================================="

$success = Download-GradleWrapper

if ($success) {
    $testSuccess = Test-GradleSetup
    if ($testSuccess) {
        Write-ColorOutput Cyan "`n=== Gradle Setup Complete ==="
        Write-ColorOutput Green "You can now build the project:"
        Write-ColorOutput White "  .\gradlew.bat assembleDebug"
        Write-ColorOutput White "  .\scripts\build-tools.ps1 -Action build"
        exit 0
    } else {
        Write-ColorOutput Red "Gradle setup test failed"
        exit 1
    }
} else {
    Write-ColorOutput Red "Gradle setup failed"
    exit 1
}
