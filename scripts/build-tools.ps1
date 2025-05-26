# Happy_Kid Android Build Tools for Windows
# Optimized build scripts for Windows development environment

param(
    [string]$Action = "help",
    [string]$BuildType = "debug",
    [switch]$Clean,
    [switch]$Test,
    [switch]$Deploy
)

$ProjectRoot = "D:\GitProjects\Happy_Kid"
$GradleWrapper = Join-Path $ProjectRoot "gradlew.bat"

function Write-ColorOutput($ForegroundColor) {
    $fc = $host.UI.RawUI.ForegroundColor
    $host.UI.RawUI.ForegroundColor = $ForegroundColor
    if ($args) {
        Write-Output $args
    }
    $host.UI.RawUI.ForegroundColor = $fc
}

function Test-Prerequisites {
    Write-ColorOutput Yellow "Checking Windows Android Development Prerequisites..."

    # Detect Android SDK location
    $androidSdk = $env:ANDROID_HOME
    if (-not $androidSdk) {
        $androidSdk = "$env:LOCALAPPDATA\Android\Sdk"
        if (-not (Test-Path $androidSdk)) {
            $androidSdk = "$env:USERPROFILE\AppData\Local\Android\Sdk"
        }
    }

    $prerequisites = @{
        "Java" = { java -version 2>&1 | Select-String "version" }
        "Android SDK" = { Test-Path $androidSdk }
        "Gradle" = { Test-Path $GradleWrapper }
        "Git" = { git --version 2>&1 | Select-String "git version" }
        "ADB" = { Test-Path "$androidSdk\platform-tools\adb.exe" }
        "Emulator" = { Test-Path "$androidSdk\emulator\emulator.exe" }
    }

    $allGood = $true
    foreach ($prereq in $prerequisites.GetEnumerator()) {
        try {
            $result = & $prereq.Value
            if ($result) {
                Write-ColorOutput Green "✓ $($prereq.Key) - OK"
            } else {
                Write-ColorOutput Red "✗ $($prereq.Key) - Missing"
                $allGood = $false
            }
        } catch {
            Write-ColorOutput Red "✗ $($prereq.Key) - Error checking"
            $allGood = $false
        }
    }

    # Set environment variables for current session
    if (Test-Path $androidSdk) {
        $env:ANDROID_HOME = $androidSdk
        $env:ANDROID_SDK_ROOT = $androidSdk
        $env:PATH = "$env:PATH;$androidSdk\platform-tools;$androidSdk\emulator;$androidSdk\tools;$androidSdk\tools\bin"
        Write-ColorOutput Green "Updated PATH for current session"
    }

    return $allGood
}

function Invoke-CleanBuild {
    Write-ColorOutput Yellow "Performing clean build..."
    Set-Location $ProjectRoot

    if (Test-Path $GradleWrapper) {
        & $GradleWrapper clean
        if ($LASTEXITCODE -eq 0) {
            Write-ColorOutput Green "Clean completed successfully"
        } else {
            Write-ColorOutput Red "Clean failed with exit code $LASTEXITCODE"
            return $false
        }
    } else {
        Write-ColorOutput Red "Gradle wrapper not found at $GradleWrapper"
        return $false
    }
    return $true
}

function Invoke-Build {
    param([string]$Type = "debug")

    Write-ColorOutput Yellow "Building Happy_Kid app ($Type)..."
    Set-Location $ProjectRoot

    $buildTask = if ($Type -eq "release") { "assembleRelease" } else { "assembleDebug" }

    if (Test-Path $GradleWrapper) {
        & $GradleWrapper $buildTask
        if ($LASTEXITCODE -eq 0) {
            Write-ColorOutput Green "Build completed successfully"

            # Show APK location
            $apkPath = if ($Type -eq "release") {
                Join-Path $ProjectRoot "app\build\outputs\apk\release\app-release.apk"
            } else {
                Join-Path $ProjectRoot "app\build\outputs\apk\debug\app-debug.apk"
            }

            if (Test-Path $apkPath) {
                Write-ColorOutput Green "APK created at: $apkPath"
            }
        } else {
            Write-ColorOutput Red "Build failed with exit code $LASTEXITCODE"
            return $false
        }
    } else {
        Write-ColorOutput Red "Gradle wrapper not found"
        return $false
    }
    return $true
}

function Invoke-Tests {
    Write-ColorOutput Yellow "Running tests..."
    Set-Location $ProjectRoot

    if (Test-Path $GradleWrapper) {
        # Run unit tests
        Write-ColorOutput Cyan "Running unit tests..."
        & $GradleWrapper test

        # Run instrumented tests if emulator is running
        $emulatorRunning = adb devices | Select-String "emulator"
        if ($emulatorRunning) {
            Write-ColorOutput Cyan "Running instrumented tests..."
            & $GradleWrapper connectedAndroidTest
        } else {
            Write-ColorOutput Yellow "No emulator detected. Skipping instrumented tests."
        }

        if ($LASTEXITCODE -eq 0) {
            Write-ColorOutput Green "Tests completed successfully"
        } else {
            Write-ColorOutput Red "Tests failed with exit code $LASTEXITCODE"
            return $false
        }
    }
    return $true
}

function Start-Emulator {
    Write-ColorOutput Yellow "Starting Android Emulator..."

    # Detect Android SDK location
    $androidSdk = $env:ANDROID_HOME
    if (-not $androidSdk) {
        $androidSdk = "$env:LOCALAPPDATA\Android\Sdk"
        if (-not (Test-Path $androidSdk)) {
            $androidSdk = "$env:USERPROFILE\AppData\Local\Android\Sdk"
        }
    }

    $emulatorPath = "$androidSdk\emulator\emulator.exe"
    $adbPath = "$androidSdk\platform-tools\adb.exe"
    # Use existing AVD or fallback to HappyKid_Test_API30
    $preferredAvd = "Medium_Phone_API_36.0"
    $fallbackAvd = "HappyKid_Test_API30"

    # Check if AVD exists
    if (Test-Path $emulatorPath) {
        Write-ColorOutput Green "Found emulator at: $emulatorPath"

        # List available AVDs
        $avdList = & $emulatorPath -list-avds
        Write-ColorOutput Yellow "Available AVDs:"
        $avdList | ForEach-Object { Write-ColorOutput White "  $_" }

        # Choose which AVD to use
        $avdToUse = $null
        if ($avdList -contains $preferredAvd) {
            $avdToUse = $preferredAvd
            Write-ColorOutput Green "Using preferred AVD: $preferredAvd"
        } elseif ($avdList -contains $fallbackAvd) {
            $avdToUse = $fallbackAvd
            Write-ColorOutput Green "Using fallback AVD: $fallbackAvd"
        } elseif ($avdList.Count -gt 0) {
            $avdToUse = $avdList[0]
            Write-ColorOutput Yellow "Using first available AVD: $avdToUse"
        }

        if ($avdToUse) {
            Write-ColorOutput Green "Starting AVD: $avdToUse"
            Start-Process -FilePath $emulatorPath -ArgumentList "-avd", $avdToUse, "-no-snapshot-load", "-gpu", "host" -NoNewWindow
            Write-ColorOutput Green "Emulator starting..."

            # Wait for emulator to be ready
            if (Test-Path $adbPath) {
                Write-ColorOutput Yellow "Waiting for emulator to be ready..."
                $timeout = 120 # 2 minutes timeout
                $elapsed = 0
                do {
                    Start-Sleep -Seconds 5
                    $elapsed += 5
                    try {
                        $devices = & $adbPath devices | Select-String "emulator.*device"
                        if ($devices) {
                            Write-ColorOutput Green "Emulator is ready!"
                            return
                        }
                    } catch {
                        # Continue waiting
                    }

                    if ($elapsed -ge $timeout) {
                        Write-ColorOutput Red "Timeout waiting for emulator to start"
                        return
                    }

                    Write-ColorOutput Yellow "Still waiting... ($elapsed seconds)"
                } while (-not $devices)
            } else {
                Write-ColorOutput Yellow "ADB not found. Emulator started but cannot verify status."
            }
        } else {
            Write-ColorOutput Red "No suitable AVD found. Available AVDs:"
            $avdList | ForEach-Object { Write-ColorOutput Red "  $_" }
            Write-ColorOutput Yellow "Please create an AVD in Android Studio or run:"
            Write-ColorOutput Yellow "  .\scripts\windows-dev-setup.ps1 -SetupEmulator"
        }
    } else {
        Write-ColorOutput Red "Emulator not found at $emulatorPath"
        Write-ColorOutput Yellow "Please ensure Android Studio is properly installed."
    }
}

function Deploy-ToDevice {
    Write-ColorOutput Yellow "Deploying to connected device/emulator..."

    $devices = adb devices | Select-String "device$"
    if (-not $devices) {
        Write-ColorOutput Red "No devices connected. Please connect a device or start an emulator."
        return $false
    }

    $apkPath = Join-Path $ProjectRoot "app\build\outputs\apk\debug\app-debug.apk"
    if (Test-Path $apkPath) {
        adb install -r $apkPath
        if ($LASTEXITCODE -eq 0) {
            Write-ColorOutput Green "App deployed successfully"

            # Launch the app
            adb shell am start -n "com.happykid/.MainActivity"
            Write-ColorOutput Green "App launched"
        } else {
            Write-ColorOutput Red "Deployment failed"
            return $false
        }
    } else {
        Write-ColorOutput Red "APK not found. Please build the app first."
        return $false
    }
    return $true
}

function Show-Help {
    Write-ColorOutput Cyan @"
Happy_Kid Android Build Tools for Windows

Usage: .\build-tools.ps1 -Action <action> [options]

Actions:
  check       - Check development prerequisites
  clean       - Clean build artifacts
  build       - Build the application (default: debug)
  test        - Run unit and instrumented tests
  emulator    - Start Android emulator
  deploy      - Deploy app to connected device/emulator
  full        - Clean, build, test, and deploy

Options:
  -BuildType  - Specify build type: debug (default) or release
  -Clean      - Perform clean before build
  -Test       - Run tests after build
  -Deploy     - Deploy after successful build

Examples:
  .\build-tools.ps1 -Action check
  .\build-tools.ps1 -Action build -BuildType release
  .\build-tools.ps1 -Action full -BuildType debug
"@
}

# Main execution logic
switch ($Action.ToLower()) {
    "check" {
        Test-Prerequisites
    }
    "clean" {
        Invoke-CleanBuild
    }
    "build" {
        if ($Clean) { Invoke-CleanBuild }
        Invoke-Build -Type $BuildType
        if ($Test) { Invoke-Tests }
        if ($Deploy) { Deploy-ToDevice }
    }
    "test" {
        Invoke-Tests
    }
    "emulator" {
        Start-Emulator
    }
    "deploy" {
        Deploy-ToDevice
    }
    "full" {
        if (Test-Prerequisites) {
            Invoke-CleanBuild
            if (Invoke-Build -Type $BuildType) {
                if (Invoke-Tests) {
                    Deploy-ToDevice
                }
            }
        }
    }
    default {
        Show-Help
    }
}
