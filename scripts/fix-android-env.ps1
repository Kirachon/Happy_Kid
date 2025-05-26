# Fix Android Development Environment for Happy_Kid

function Write-ColorOutput($ForegroundColor) {
    $fc = $host.UI.RawUI.ForegroundColor
    $host.UI.RawUI.ForegroundColor = $ForegroundColor
    if ($args) {
        Write-Output $args
    }
    $host.UI.RawUI.ForegroundColor = $fc
}

Write-ColorOutput Green "Fixing Android Development Environment for Happy_Kid"
Write-ColorOutput Green "=================================================="

# Set the correct Android SDK path
$androidSdk = "C:\Users\preda\AppData\Local\Android\Sdk"
Write-ColorOutput Yellow "Using Android SDK: $androidSdk"

# Set environment variables for current session
$env:ANDROID_HOME = $androidSdk
$env:ANDROID_SDK_ROOT = $androidSdk

# Add Android tools to PATH for current session
$pathsToAdd = @(
    "$androidSdk\platform-tools",
    "$androidSdk\emulator",
    "$androidSdk\tools",
    "$androidSdk\tools\bin"
)

foreach ($pathToAdd in $pathsToAdd) {
    if (Test-Path $pathToAdd) {
        $env:PATH = "$pathToAdd;$env:PATH"
        Write-ColorOutput Green "Added to PATH: $pathToAdd"
    } else {
        Write-ColorOutput Yellow "Path not found (skipping): $pathToAdd"
    }
}

# Test ADB
Write-ColorOutput Yellow "`nTesting ADB..."
try {
    $adbPath = "$androidSdk\platform-tools\adb.exe"
    $adbVersion = & $adbPath version
    Write-ColorOutput Green "✓ ADB working: $($adbVersion[0])"
} catch {
    Write-ColorOutput Red "✗ ADB test failed: $($_.Exception.Message)"
    exit 1
}

# Test Emulator and list AVDs
Write-ColorOutput Yellow "`nTesting Emulator..."
try {
    $emulatorPath = "$androidSdk\emulator\emulator.exe"
    $avdList = & $emulatorPath -list-avds
    Write-ColorOutput Green "✓ Emulator working"
    Write-ColorOutput Yellow "Available AVDs:"
    $avdList | ForEach-Object { Write-ColorOutput White "  $_" }
    
    # Check if we can use the existing AVD
    if ($avdList -contains "Medium_Phone_API_36.0") {
        Write-ColorOutput Green "✓ Found existing AVD: Medium_Phone_API_36.0"
        Write-ColorOutput Yellow "We'll use this AVD instead of creating a new one"
    }
} catch {
    Write-ColorOutput Red "✗ Emulator test failed: $($_.Exception.Message)"
    exit 1
}

Write-ColorOutput Green "`n✓ Environment fixed for current session!"
Write-ColorOutput Yellow "`nTo make these changes permanent:"
Write-ColorOutput White "1. Open System Properties > Environment Variables"
Write-ColorOutput White "2. Add ANDROID_HOME = $androidSdk"
Write-ColorOutput White "3. Add to PATH: $androidSdk\platform-tools;$androidSdk\emulator"

Write-ColorOutput Cyan "`n=== Ready to test Happy_Kid! ==="
Write-ColorOutput White "You can now run:"
Write-ColorOutput Green "1. Start emulator with existing AVD:"
Write-ColorOutput White "   & '$androidSdk\emulator\emulator.exe' -avd Medium_Phone_API_36.0"
Write-ColorOutput Green "2. Build the project:"
Write-ColorOutput White "   .\gradlew.bat assembleDebug"
Write-ColorOutput Green "3. Install on emulator:"
Write-ColorOutput White "   & '$androidSdk\platform-tools\adb.exe' install app\build\outputs\apk\debug\app-debug.apk"
