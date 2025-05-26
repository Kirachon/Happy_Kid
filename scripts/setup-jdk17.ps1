# Happy_Kid Android Project - JDK 17 Environment Setup
# This script configures the development environment to use JDK 17
# for compatibility with Android Gradle Plugin 8.1.4

Write-Host "=== Happy_Kid JDK 17 Environment Setup ===" -ForegroundColor Green

# Check if JDK 17 is installed
$jdk17Path = "C:\Program Files\Java\jdk-17"
if (-not (Test-Path $jdk17Path)) {
    Write-Host "ERROR: JDK 17 not found at $jdk17Path" -ForegroundColor Red
    Write-Host "Please install JDK 17 before running this script." -ForegroundColor Yellow
    exit 1
}

# Verify JDK 17 installation
$javaVersion = & "$jdk17Path\bin\java.exe" -version 2>&1
Write-Host "Found JDK 17: $($javaVersion[0])" -ForegroundColor Green

# Set environment variables for current session
$env:JAVA_HOME = $jdk17Path
$env:PATH = "$jdk17Path\bin;" + ($env:PATH -replace "C:\\Program Files\\Android\\Android Studio\\jbr\\bin;", "")

Write-Host "Environment configured:" -ForegroundColor Cyan
Write-Host "  JAVA_HOME: $env:JAVA_HOME" -ForegroundColor White
Write-Host "  Java Version: $(& java -version 2>&1 | Select-Object -First 1)" -ForegroundColor White

# Verify Gradle configuration
Write-Host "`nVerifying Gradle configuration..." -ForegroundColor Cyan
if (Test-Path "gradle.properties") {
    $gradleProps = Get-Content "gradle.properties" | Where-Object { $_ -match "org.gradle.jvmargs" }
    if ($gradleProps -match "MaxPermSize") {
        Write-Host "WARNING: gradle.properties still contains deprecated MaxPermSize option" -ForegroundColor Yellow
    } else {
        Write-Host "✓ gradle.properties JVM args are JDK 17 compatible" -ForegroundColor Green
    }
} else {
    Write-Host "WARNING: gradle.properties not found in current directory" -ForegroundColor Yellow
}

# Verify gradlew.bat configuration
if (Test-Path "gradlew.bat") {
    $gradlewContent = Get-Content "gradlew.bat" | Where-Object { $_ -match "DEFAULT_JVM_OPTS" }
    if ($gradlewContent -match "MaxPermSize") {
        Write-Host "WARNING: gradlew.bat still contains deprecated MaxPermSize option" -ForegroundColor Yellow
    } else {
        Write-Host "✓ gradlew.bat JVM args are JDK 17 compatible" -ForegroundColor Green
    }
} else {
    Write-Host "WARNING: gradlew.bat not found in current directory" -ForegroundColor Yellow
}

Write-Host "`n=== Environment Setup Complete ===" -ForegroundColor Green
Write-Host "You can now run: .\gradlew.bat assembleDebug" -ForegroundColor Cyan
Write-Host "Note: This configuration is for the current PowerShell session only." -ForegroundColor Yellow
Write-Host "For permanent configuration, set JAVA_HOME in Windows Environment Variables." -ForegroundColor Yellow
