# Happy_Kid Implementation Verification Script
# Windows-optimized verification for Android development

param(
    [switch]$Detailed,
    [switch]$BuildTest,
    [switch]$All
)

$ProjectRoot = "D:\GitProjects\Happy_Kid"
$ErrorCount = 0
$WarningCount = 0

function Write-ColorOutput($ForegroundColor) {
    $fc = $host.UI.RawUI.ForegroundColor
    $host.UI.RawUI.ForegroundColor = $ForegroundColor
    if ($args) {
        Write-Output $args
    }
    $host.UI.RawUI.ForegroundColor = $fc
}

function Test-FileExists($FilePath, $Description) {
    if (Test-Path $FilePath) {
        Write-ColorOutput Green "✓ $Description"
        return $true
    } else {
        Write-ColorOutput Red "✗ $Description - Missing: $FilePath"
        $script:ErrorCount++
        return $false
    }
}

function Test-DirectoryExists($DirPath, $Description) {
    if (Test-Path $DirPath -PathType Container) {
        Write-ColorOutput Green "✓ $Description"
        return $true
    } else {
        Write-ColorOutput Yellow "⚠ $Description - Missing: $DirPath"
        $script:WarningCount++
        return $false
    }
}

function Test-WindowsEnvironment {
    Write-ColorOutput Cyan "=== Windows Development Environment Verification ==="
    
    # Check Windows version
    $windowsVersion = [System.Environment]::OSVersion.Version
    if ($windowsVersion.Major -ge 10) {
        Write-ColorOutput Green "✓ Windows Version: $($windowsVersion.ToString())"
    } else {
        Write-ColorOutput Red "✗ Windows Version: $($windowsVersion.ToString()) - Requires Windows 10+"
        $script:ErrorCount++
    }
    
    # Check PowerShell version
    $psVersion = $PSVersionTable.PSVersion
    if ($psVersion.Major -ge 5) {
        Write-ColorOutput Green "✓ PowerShell Version: $($psVersion.ToString())"
    } else {
        Write-ColorOutput Red "✗ PowerShell Version: $($psVersion.ToString()) - Requires PowerShell 5+"
        $script:ErrorCount++
    }
    
    # Check environment variables
    $androidHome = $env:ANDROID_HOME
    if ($androidHome) {
        Write-ColorOutput Green "✓ ANDROID_HOME: $androidHome"
    } else {
        Write-ColorOutput Yellow "⚠ ANDROID_HOME not set"
        $script:WarningCount++
    }
    
    $javaHome = $env:JAVA_HOME
    if ($javaHome) {
        Write-ColorOutput Green "✓ JAVA_HOME: $javaHome"
    } else {
        Write-ColorOutput Yellow "⚠ JAVA_HOME not set (using embedded JDK)"
        $script:WarningCount++
    }
}

function Test-ProjectStructure {
    Write-ColorOutput Cyan "=== Project Structure Verification ==="
    
    Set-Location $ProjectRoot
    
    # Core project files
    Test-FileExists "build.gradle" "Root build.gradle"
    Test-FileExists "settings.gradle" "Settings.gradle"
    Test-FileExists "gradle.properties" "Gradle properties"
    Test-FileExists "gradlew.bat" "Gradle wrapper (Windows)"
    Test-FileExists "gradle\wrapper\gradle-wrapper.properties" "Gradle wrapper properties"
    
    # App module files
    Test-FileExists "app\build.gradle" "App build.gradle"
    Test-FileExists "app\src\main\AndroidManifest.xml" "Android manifest"
    
    # Source code structure
    Test-DirectoryExists "app\src\main\java\com\happykid\toddlerabc" "Main source directory"
    Test-FileExists "app\src\main\java\com\happykid\toddlerabc\HappyKidApplication.kt" "Application class"
    Test-FileExists "app\src\main\java\com\happykid\toddlerabc\MainActivity.kt" "Main activity"
    
    # UI and navigation
    Test-DirectoryExists "app\src\main\java\com\happykid\toddlerabc\ui" "UI package"
    Test-FileExists "app\src\main\java\com\happykid\toddlerabc\ui\navigation\HappyKidNavigation.kt" "Navigation component"
    Test-FileExists "app\src\main\java\com\happykid\toddlerabc\ui\theme\Theme.kt" "Theme definition"
    Test-FileExists "app\src\main\java\com\happykid\toddlerabc\ui\theme\Color.kt" "Color definitions"
    Test-FileExists "app\src\main\java\com\happykid\toddlerabc\ui\theme\Type.kt" "Typography definitions"
    
    # Utilities
    Test-FileExists "app\src\main\java\com\happykid\toddlerabc\util\WindowsDeviceUtils.kt" "Windows utilities"
    
    # Resources
    Test-FileExists "app\src\main\res\values\strings.xml" "String resources"
    Test-FileExists "app\src\main\res\values\colors.xml" "Color resources"
    Test-FileExists "app\src\main\res\values\themes.xml" "Theme resources"
    
    # Test directories
    Test-DirectoryExists "app\src\test\java\com\happykid" "Unit test directory"
    Test-DirectoryExists "app\src\androidTest\java\com\happykid" "Android test directory"
    
    # Scripts and documentation
    Test-DirectoryExists "scripts" "Scripts directory"
    Test-FileExists "scripts\windows-dev-setup.ps1" "Windows setup script"
    Test-FileExists "scripts\build-tools.ps1" "Build tools script"
    Test-FileExists "scripts\verify-implementation.ps1" "Verification script"
    
    Test-DirectoryExists "docs" "Documentation directory"
    Test-FileExists "docs\windows-architecture-specification.md" "Architecture specification"
    Test-FileExists "docs\implementation-plan.md" "Implementation plan"
    
    # Version control
    Test-FileExists ".gitignore" "Git ignore file"
    Test-FileExists "LICENSE" "License file"
    Test-FileExists "README.md" "README file"
}

function Test-GradleConfiguration {
    Write-ColorOutput Cyan "=== Gradle Configuration Verification ==="
    
    Set-Location $ProjectRoot
    
    if (Test-Path "gradlew.bat") {
        try {
            Write-ColorOutput Yellow "Testing Gradle wrapper..."
            $gradleOutput = & .\gradlew.bat tasks --quiet 2>&1
            if ($LASTEXITCODE -eq 0) {
                Write-ColorOutput Green "✓ Gradle wrapper functional"
            } else {
                Write-ColorOutput Red "✗ Gradle wrapper failed: $gradleOutput"
                $script:ErrorCount++
            }
        } catch {
            Write-ColorOutput Red "✗ Gradle wrapper error: $($_.Exception.Message)"
            $script:ErrorCount++
        }
    }
}

function Test-AndroidSDK {
    Write-ColorOutput Cyan "=== Android SDK Verification ==="
    
    $androidHome = $env:ANDROID_HOME
    if ($androidHome) {
        # Check SDK components
        $platformTools = Join-Path $androidHome "platform-tools"
        $buildTools = Join-Path $androidHome "build-tools"
        $platforms = Join-Path $androidHome "platforms"
        
        Test-DirectoryExists $platformTools "Platform tools"
        Test-DirectoryExists $buildTools "Build tools"
        Test-DirectoryExists $platforms "Android platforms"
        
        # Check ADB
        $adbPath = Join-Path $platformTools "adb.exe"
        if (Test-Path $adbPath) {
            try {
                $adbVersion = & $adbPath version 2>&1
                Write-ColorOutput Green "✓ ADB available: $($adbVersion[0])"
            } catch {
                Write-ColorOutput Yellow "⚠ ADB not functional"
                $script:WarningCount++
            }
        }
    }
}

function Test-BuildProcess {
    Write-ColorOutput Cyan "=== Build Process Test ==="
    
    Set-Location $ProjectRoot
    
    if (Test-Path "gradlew.bat") {
        Write-ColorOutput Yellow "Testing clean build..."
        try {
            $buildOutput = & .\gradlew.bat clean assembleDebug --quiet 2>&1
            if ($LASTEXITCODE -eq 0) {
                Write-ColorOutput Green "✓ Clean debug build successful"
                
                # Check if APK was generated
                $apkPath = "app\build\outputs\apk\debug\app-debug.apk"
                if (Test-Path $apkPath) {
                    $apkSize = (Get-Item $apkPath).Length / 1MB
                    Write-ColorOutput Green "✓ APK generated: $([math]::Round($apkSize, 2)) MB"
                } else {
                    Write-ColorOutput Yellow "⚠ APK not found at expected location"
                    $script:WarningCount++
                }
            } else {
                Write-ColorOutput Red "✗ Build failed: $buildOutput"
                $script:ErrorCount++
            }
        } catch {
            Write-ColorOutput Red "✗ Build error: $($_.Exception.Message)"
            $script:ErrorCount++
        }
    }
}

function Show-Summary {
    Write-ColorOutput Cyan "=== Verification Summary ==="
    
    if ($ErrorCount -eq 0 -and $WarningCount -eq 0) {
        Write-ColorOutput Green "🎉 All checks passed! Happy_Kid project is ready for development."
    } elseif ($ErrorCount -eq 0) {
        Write-ColorOutput Yellow "⚠ $WarningCount warning(s) found. Project is functional but some optimizations recommended."
    } else {
        Write-ColorOutput Red "❌ $ErrorCount error(s) and $WarningCount warning(s) found. Please address errors before proceeding."
    }
    
    Write-ColorOutput White "Next steps:"
    Write-ColorOutput White "1. Run: .\scripts\windows-dev-setup.ps1 -All (if not done)"
    Write-ColorOutput White "2. Open project in Android Studio"
    Write-ColorOutput White "3. Start Windows emulator: .\scripts\build-tools.ps1 -Action emulator"
    Write-ColorOutput White "4. Build and deploy: .\scripts\build-tools.ps1 -Action full"
    
    if ($Detailed) {
        Write-ColorOutput Cyan "=== Detailed Project Information ==="
        Write-ColorOutput White "Project: Happy_Kid Toddler ABC Learning App"
        Write-ColorOutput White "Platform: Android (API 21+)"
        Write-ColorOutput White "Development: Windows-optimized"
        Write-ColorOutput White "Architecture: Clean Architecture with MVVM"
        Write-ColorOutput White "UI Framework: Jetpack Compose"
        Write-ColorOutput White "Language: Kotlin"
        Write-ColorOutput White "Build System: Gradle"
        Write-ColorOutput White "Target Audience: Toddlers aged 1-4"
    }
}

# Main execution
Write-ColorOutput Green "Happy_Kid Implementation Verification"
Write-ColorOutput Green "Windows Android Development Environment"
Write-ColorOutput Green "======================================="

Test-WindowsEnvironment
Test-ProjectStructure

if ($All -or $BuildTest) {
    Test-GradleConfiguration
    Test-AndroidSDK
    Test-BuildProcess
}

Show-Summary

# Return appropriate exit code
if ($ErrorCount -gt 0) {
    exit 1
} else {
    exit 0
}
