# Quick Setup Script for Happy_Kid Android Development on Windows
# This script will help you set up the development environment quickly

param(
    [switch]$CreateAVD,
    [switch]$FixPaths,
    [switch]$All
)

function Write-ColorOutput($ForegroundColor) {
    $fc = $host.UI.RawUI.ForegroundColor
    $host.UI.RawUI.ForegroundColor = $ForegroundColor
    if ($args) {
        Write-Output $args
    }
    $host.UI.RawUI.ForegroundColor = $fc
}

function Find-AndroidSDK {
    Write-ColorOutput Yellow "Locating Android SDK..."
    
    $possiblePaths = @(
        "$env:ANDROID_HOME",
        "$env:LOCALAPPDATA\Android\Sdk",
        "$env:USERPROFILE\AppData\Local\Android\Sdk",
        "C:\Users\$env:USERNAME\AppData\Local\Android\Sdk"
    )
    
    foreach ($path in $possiblePaths) {
        if ($path -and (Test-Path $path)) {
            Write-ColorOutput Green "Found Android SDK at: $path"
            return $path
        }
    }
    
    Write-ColorOutput Red "Android SDK not found. Please install Android Studio first."
    return $null
}

function Set-EnvironmentPaths {
    Write-ColorOutput Yellow "Setting up environment paths..."
    
    $androidSdk = Find-AndroidSDK
    if (-not $androidSdk) {
        return $false
    }
    
    # Set environment variables for current session
    $env:ANDROID_HOME = $androidSdk
    $env:ANDROID_SDK_ROOT = $androidSdk
    
    # Add to PATH for current session
    $pathsToAdd = @(
        "$androidSdk\platform-tools",
        "$androidSdk\emulator",
        "$androidSdk\tools",
        "$androidSdk\tools\bin",
        "$androidSdk\cmdline-tools\latest\bin"
    )
    
    foreach ($pathToAdd in $pathsToAdd) {
        if ((Test-Path $pathToAdd) -and ($env:PATH -notlike "*$pathToAdd*")) {
            $env:PATH = "$env:PATH;$pathToAdd"
            Write-ColorOutput Green "Added to PATH: $pathToAdd"
        }
    }
    
    # Verify ADB is accessible
    try {
        $adbVersion = & "$androidSdk\platform-tools\adb.exe" version
        Write-ColorOutput Green "ADB is accessible: $($adbVersion[0])"
    } catch {
        Write-ColorOutput Red "ADB not accessible"
        return $false
    }
    
    return $true
}

function Create-HappyKidAVD {
    Write-ColorOutput Yellow "Creating Happy_Kid AVD..."
    
    $androidSdk = Find-AndroidSDK
    if (-not $androidSdk) {
        return $false
    }
    
    # Find avdmanager
    $avdManager = "$androidSdk\cmdline-tools\latest\bin\avdmanager.bat"
    if (-not (Test-Path $avdManager)) {
        $avdManager = "$androidSdk\tools\bin\avdmanager.bat"
    }
    
    if (-not (Test-Path $avdManager)) {
        Write-ColorOutput Red "AVD Manager not found. Please install Android SDK Command Line Tools."
        Write-ColorOutput Yellow "In Android Studio: Tools > SDK Manager > SDK Tools > Android SDK Command-line Tools"
        return $false
    }
    
    # Find sdkmanager
    $sdkManager = "$androidSdk\cmdline-tools\latest\bin\sdkmanager.bat"
    if (-not (Test-Path $sdkManager)) {
        $sdkManager = "$androidSdk\tools\bin\sdkmanager.bat"
    }
    
    if (Test-Path $sdkManager) {
        Write-ColorOutput Yellow "Installing required SDK components..."
        try {
            # Accept licenses first
            Write-ColorOutput Yellow "Accepting SDK licenses..."
            echo "y" | & $sdkManager --licenses
            
            # Install required components
            & $sdkManager "platform-tools" "platforms;android-30" "build-tools;30.0.3" "system-images;android-30;google_apis;x86_64" "emulator"
            Write-ColorOutput Green "SDK components installed"
        } catch {
            Write-ColorOutput Red "Error installing SDK components: $($_.Exception.Message)"
        }
    }
    
    # Check if AVD already exists
    try {
        $existingAvds = & $avdManager list avd
        if ($existingAvds -match "HappyKid_Test_API30") {
            Write-ColorOutput Yellow "AVD HappyKid_Test_API30 already exists"
            return $true
        }
    } catch {
        # Continue to create AVD
    }
    
    # Create the AVD
    try {
        Write-ColorOutput Yellow "Creating AVD HappyKid_Test_API30..."
        & $avdManager create avd -n "HappyKid_Test_API30" -k "system-images;android-30;google_apis;x86_64" -d "pixel_4" --force
        Write-ColorOutput Green "AVD created successfully!"
        return $true
    } catch {
        Write-ColorOutput Red "Error creating AVD: $($_.Exception.Message)"
        return $false
    }
}

function Test-Setup {
    Write-ColorOutput Yellow "Testing setup..."
    
    $androidSdk = Find-AndroidSDK
    if (-not $androidSdk) {
        return $false
    }
    
    # Test ADB
    try {
        $adbPath = "$androidSdk\platform-tools\adb.exe"
        $adbVersion = & $adbPath version
        Write-ColorOutput Green "✓ ADB working: $($adbVersion[0])"
    } catch {
        Write-ColorOutput Red "✗ ADB not working"
        return $false
    }
    
    # Test Emulator
    try {
        $emulatorPath = "$androidSdk\emulator\emulator.exe"
        if (Test-Path $emulatorPath) {
            $avdList = & $emulatorPath -list-avds
            Write-ColorOutput Green "✓ Emulator found"
            Write-ColorOutput Yellow "Available AVDs:"
            $avdList | ForEach-Object { Write-ColorOutput White "  $_" }
            
            if ($avdList -contains "HappyKid_Test_API30") {
                Write-ColorOutput Green "✓ HappyKid_Test_API30 AVD ready"
            } else {
                Write-ColorOutput Yellow "⚠ HappyKid_Test_API30 AVD not found"
            }
        } else {
            Write-ColorOutput Red "✗ Emulator not found"
        }
    } catch {
        Write-ColorOutput Red "✗ Error testing emulator"
    }
    
    return $true
}

function Show-Instructions {
    Write-ColorOutput Cyan "=== Happy_Kid Development Setup Complete ==="
    Write-ColorOutput White ""
    Write-ColorOutput White "Next steps:"
    Write-ColorOutput Green "1. Start the emulator:"
    Write-ColorOutput White "   .\scripts\build-tools.ps1 -Action emulator"
    Write-ColorOutput White ""
    Write-ColorOutput Green "2. Build the project:"
    Write-ColorOutput White "   .\scripts\build-tools.ps1 -Action build"
    Write-ColorOutput White ""
    Write-ColorOutput Green "3. Deploy to emulator:"
    Write-ColorOutput White "   .\scripts\build-tools.ps1 -Action deploy"
    Write-ColorOutput White ""
    Write-ColorOutput Yellow "If you encounter issues:"
    Write-ColorOutput White "- Restart PowerShell to refresh environment variables"
    Write-ColorOutput White "- Run this script again with -All parameter"
    Write-ColorOutput White "- Check Android Studio SDK Manager for missing components"
}

# Main execution
Write-ColorOutput Green "Happy_Kid Quick Setup for Windows"
Write-ColorOutput Green "=================================="

if ($FixPaths -or $All) {
    if (-not (Set-EnvironmentPaths)) {
        Write-ColorOutput Red "Failed to set up environment paths"
        exit 1
    }
}

if ($CreateAVD -or $All) {
    if (-not (Create-HappyKidAVD)) {
        Write-ColorOutput Red "Failed to create AVD"
        exit 1
    }
}

if (-not ($FixPaths -or $CreateAVD -or $All)) {
    # Default: do everything
    if (-not (Set-EnvironmentPaths)) {
        Write-ColorOutput Red "Failed to set up environment paths"
        exit 1
    }
    
    if (-not (Create-HappyKidAVD)) {
        Write-ColorOutput Red "Failed to create AVD"
        exit 1
    }
}

Test-Setup
Show-Instructions

Write-ColorOutput Green "Setup completed successfully!"
