# Android Development Environment Diagnostic Script

function Write-ColorOutput($ForegroundColor) {
    $fc = $host.UI.RawUI.ForegroundColor
    $host.UI.RawUI.ForegroundColor = $ForegroundColor
    if ($args) {
        Write-Output $args
    }
    $host.UI.RawUI.ForegroundColor = $fc
}

Write-ColorOutput Cyan "=== Android Development Environment Diagnostic ==="

# Check environment variables
Write-ColorOutput Yellow "Environment Variables:"
Write-ColorOutput White "ANDROID_HOME: $env:ANDROID_HOME"
Write-ColorOutput White "ANDROID_SDK_ROOT: $env:ANDROID_SDK_ROOT"
Write-ColorOutput White "JAVA_HOME: $env:JAVA_HOME"

# Check common Android SDK locations
Write-ColorOutput Yellow "`nChecking common Android SDK locations:"
$possiblePaths = @(
    "$env:LOCALAPPDATA\Android\Sdk",
    "$env:USERPROFILE\AppData\Local\Android\Sdk",
    "C:\Users\$env:USERNAME\AppData\Local\Android\Sdk",
    "C:\Android\Sdk"
)

foreach ($path in $possiblePaths) {
    if (Test-Path $path) {
        Write-ColorOutput Green "✓ Found: $path"
        
        # Check subdirectories
        $subdirs = @("platform-tools", "emulator", "tools", "cmdline-tools")
        foreach ($subdir in $subdirs) {
            $fullPath = Join-Path $path $subdir
            if (Test-Path $fullPath) {
                Write-ColorOutput Green "  ✓ $subdir"
            } else {
                Write-ColorOutput Red "  ✗ $subdir"
            }
        }
        
        # Check for ADB
        $adbPath = Join-Path $path "platform-tools\adb.exe"
        if (Test-Path $adbPath) {
            Write-ColorOutput Green "  ✓ ADB found at: $adbPath"
            try {
                $adbVersion = & $adbPath version
                Write-ColorOutput Green "    Version: $($adbVersion[0])"
            } catch {
                Write-ColorOutput Red "    Error running ADB: $($_.Exception.Message)"
            }
        } else {
            Write-ColorOutput Red "  ✗ ADB not found"
        }
        
        # Check for emulator
        $emulatorPath = Join-Path $path "emulator\emulator.exe"
        if (Test-Path $emulatorPath) {
            Write-ColorOutput Green "  ✓ Emulator found at: $emulatorPath"
            try {
                $avdList = & $emulatorPath -list-avds
                Write-ColorOutput Green "    Available AVDs:"
                if ($avdList) {
                    $avdList | ForEach-Object { Write-ColorOutput White "      $_" }
                } else {
                    Write-ColorOutput Yellow "      No AVDs found"
                }
            } catch {
                Write-ColorOutput Red "    Error listing AVDs: $($_.Exception.Message)"
            }
        } else {
            Write-ColorOutput Red "  ✗ Emulator not found"
        }
        
    } else {
        Write-ColorOutput Red "✗ Not found: $path"
    }
}

# Check PATH
Write-ColorOutput Yellow "`nChecking PATH for Android tools:"
$pathEntries = $env:PATH -split ";"
$androidPaths = $pathEntries | Where-Object { $_ -like "*Android*" -or $_ -like "*android*" }
if ($androidPaths) {
    Write-ColorOutput Green "Android-related PATH entries:"
    $androidPaths | ForEach-Object { Write-ColorOutput White "  $_" }
} else {
    Write-ColorOutput Red "No Android-related PATH entries found"
}

# Check if ADB is accessible from PATH
Write-ColorOutput Yellow "`nTesting ADB accessibility:"
try {
    $adbVersion = adb version
    Write-ColorOutput Green "✓ ADB accessible from PATH"
    Write-ColorOutput White "  $($adbVersion[0])"
} catch {
    Write-ColorOutput Red "✗ ADB not accessible from PATH"
    Write-ColorOutput Red "  Error: $($_.Exception.Message)"
}

# Check Java
Write-ColorOutput Yellow "`nChecking Java:"
try {
    $javaVersion = java -version 2>&1
    Write-ColorOutput Green "✓ Java found"
    Write-ColorOutput White "  $($javaVersion[0])"
} catch {
    Write-ColorOutput Red "✗ Java not found"
}

# Check Android Studio installation
Write-ColorOutput Yellow "`nChecking Android Studio:"
$studioPaths = @(
    "$env:LOCALAPPDATA\JetBrains\Toolbox\apps\AndroidStudio",
    "$env:ProgramFiles\Android\Android Studio",
    "C:\Program Files\Android\Android Studio"
)

foreach ($path in $studioPaths) {
    if (Test-Path $path) {
        Write-ColorOutput Green "✓ Android Studio found at: $path"
    }
}

Write-ColorOutput Cyan "`n=== Diagnostic Complete ==="
Write-ColorOutput Yellow "If you see issues above, please:"
Write-ColorOutput White "1. Install Android Studio if not found"
Write-ColorOutput White "2. Open Android Studio and install SDK components"
Write-ColorOutput White "3. Add Android SDK paths to your system PATH"
Write-ColorOutput White "4. Restart PowerShell after making changes"
