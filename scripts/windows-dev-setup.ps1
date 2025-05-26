# Windows Android Development Environment Setup Script
# Happy_Kid Project - Windows Optimization

param(
    [switch]$InstallTools,
    [switch]$ConfigureEnvironment,
    [switch]$SetupEmulator,
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

function Test-Administrator {
    $currentUser = [Security.Principal.WindowsIdentity]::GetCurrent()
    $principal = New-Object Security.Principal.WindowsPrincipal($currentUser)
    return $principal.IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
}

function Install-DevelopmentTools {
    Write-ColorOutput Yellow "Installing Windows Android Development Tools..."

    # Check if running as administrator
    if (-not (Test-Administrator)) {
        Write-ColorOutput Red "Please run this script as Administrator for tool installation."
        return
    }

    # Install Chocolatey if not present
    if (-not (Get-Command choco -ErrorAction SilentlyContinue)) {
        Write-ColorOutput Green "Installing Chocolatey..."
        Set-ExecutionPolicy Bypass -Scope Process -Force
        [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072
        Invoke-Expression ((New-Object System.Net.WebClient).DownloadString('https://chocolatey.org/install.ps1'))
    }

    # Install essential tools
    $tools = @(
        "git",
        "androidstudio",
        "openjdk11",
        "gradle",
        "nodejs",
        "vscode",
        "powershell-core"
    )

    foreach ($tool in $tools) {
        Write-ColorOutput Green "Installing $tool..."
        choco install $tool -y
    }
}

function Set-WindowsEnvironment {
    Write-ColorOutput Yellow "Configuring Windows Environment for Android Development..."

    # Detect Android SDK location
    $androidHome = "$env:LOCALAPPDATA\Android\Sdk"
    if (-not (Test-Path $androidHome)) {
        $androidHome = "$env:USERPROFILE\AppData\Local\Android\Sdk"
    }
    if (-not (Test-Path $androidHome)) {
        Write-ColorOutput Red "Android SDK not found. Please install Android Studio first."
        return $false
    }

    $gradleHome = "$env:USERPROFILE\.gradle"

    Write-ColorOutput Green "Found Android SDK at: $androidHome"

    [Environment]::SetEnvironmentVariable("ANDROID_HOME", $androidHome, "User")
    [Environment]::SetEnvironmentVariable("ANDROID_SDK_ROOT", $androidHome, "User")
    [Environment]::SetEnvironmentVariable("GRADLE_HOME", $gradleHome, "User")

    # Add to PATH
    $currentPath = [Environment]::GetEnvironmentVariable("PATH", "User")
    $pathsToAdd = @(
        "$androidHome\platform-tools",
        "$androidHome\emulator",
        "$androidHome\tools",
        "$androidHome\tools\bin",
        "$gradleHome\bin"
    )

    foreach ($pathToAdd in $pathsToAdd) {
        if ($currentPath -notlike "*$pathToAdd*") {
            $currentPath += ";$pathToAdd"
        }
    }

    [Environment]::SetEnvironmentVariable("PATH", $currentPath, "User")

    # Update current session PATH
    $env:ANDROID_HOME = $androidHome
    $env:ANDROID_SDK_ROOT = $androidHome
    $env:PATH = "$env:PATH;$androidHome\platform-tools;$androidHome\emulator;$androidHome\tools;$androidHome\tools\bin"

    # Configure Windows Defender exclusions
    Write-ColorOutput Green "Configuring Windows Defender exclusions..."
    try {
        Add-MpPreference -ExclusionPath "D:\GitProjects" -ErrorAction SilentlyContinue
        Add-MpPreference -ExclusionPath "$env:USERPROFILE\.android" -ErrorAction SilentlyContinue
        Add-MpPreference -ExclusionPath "$env:USERPROFILE\.gradle" -ErrorAction SilentlyContinue
        Add-MpPreference -ExclusionPath "$env:LOCALAPPDATA\Android" -ErrorAction SilentlyContinue
        Add-MpPreference -ExclusionProcess "java.exe" -ErrorAction SilentlyContinue
        Add-MpPreference -ExclusionProcess "gradle.exe" -ErrorAction SilentlyContinue
    } catch {
        Write-ColorOutput Red "Could not configure Windows Defender exclusions. Please run as Administrator."
    }
}

function Initialize-AndroidEmulator {
    Write-ColorOutput Yellow "Setting up Android Emulator for Windows..."

    $androidHome = $env:ANDROID_HOME
    if (-not $androidHome) {
        $androidHome = "$env:LOCALAPPDATA\Android\Sdk"
    }

    $sdkManager = "$androidHome\cmdline-tools\latest\bin\sdkmanager.bat"
    $avdManager = "$androidHome\cmdline-tools\latest\bin\avdmanager.bat"

    # Try alternative locations for SDK tools
    if (-not (Test-Path $sdkManager)) {
        $sdkManager = "$androidHome\tools\bin\sdkmanager.bat"
        $avdManager = "$androidHome\tools\bin\avdmanager.bat"
    }

    if (Test-Path $sdkManager) {
        Write-ColorOutput Green "Found SDK Manager at: $sdkManager"

        # Install required SDK components
        Write-ColorOutput Green "Installing Android SDK components..."
        try {
            & $sdkManager --licenses
            & $sdkManager "platform-tools" "platforms;android-30" "build-tools;30.0.3" "system-images;android-30;google_apis;x86_64" "emulator"

            Write-ColorOutput Green "SDK components installed successfully"
        } catch {
            Write-ColorOutput Red "Error installing SDK components: $($_.Exception.Message)"
        }

        # Create AVD for testing
        Write-ColorOutput Green "Creating Android Virtual Device..."
        try {
            # Check if AVD already exists
            $existingAvds = & $avdManager list avd | Select-String "HappyKid_Test_API30"
            if ($existingAvds) {
                Write-ColorOutput Yellow "AVD HappyKid_Test_API30 already exists"
            } else {
                & $avdManager create avd -n "HappyKid_Test_API30" -k "system-images;android-30;google_apis;x86_64" -d "pixel_4" --force
                Write-ColorOutput Green "AVD created successfully"
            }
        } catch {
            Write-ColorOutput Red "Error creating AVD: $($_.Exception.Message)"
        }
    } else {
        Write-ColorOutput Red "Android SDK Manager not found at expected locations."
        Write-ColorOutput Yellow "Please ensure Android Studio is properly installed with SDK tools."
        Write-ColorOutput Yellow "Expected locations:"
        Write-ColorOutput Yellow "  $androidHome\cmdline-tools\latest\bin\sdkmanager.bat"
        Write-ColorOutput Yellow "  $androidHome\tools\bin\sdkmanager.bat"
    }
}

function Initialize-ProjectStructure {
    Write-ColorOutput Yellow "Initializing Happy_Kid project structure..."

    $projectRoot = "D:\GitProjects\Happy_Kid"
    $directories = @(
        "app\src\main\java\com\happykid",
        "app\src\main\res\layout",
        "app\src\main\res\values",
        "app\src\main\res\drawable",
        "app\src\main\res\raw",
        "app\src\test\java\com\happykid",
        "app\src\androidTest\java\com\happykid",
        "scripts",
        "docs",
        "assets\audio",
        "assets\images",
        "assets\animations"
    )

    foreach ($dir in $directories) {
        $fullPath = Join-Path $projectRoot $dir
        if (-not (Test-Path $fullPath)) {
            New-Item -ItemType Directory -Path $fullPath -Force
            Write-ColorOutput Green "Created directory: $dir"
        }
    }
}

# Main execution logic
if ($All -or $InstallTools) {
    Install-DevelopmentTools
}

if ($All -or $ConfigureEnvironment) {
    Set-WindowsEnvironment
}

if ($All -or $SetupEmulator) {
    Initialize-AndroidEmulator
}

if ($All) {
    Initialize-ProjectStructure
}

Write-ColorOutput Green "Windows Android Development Environment Setup Complete!"
Write-ColorOutput Yellow "Please restart your terminal to apply environment variable changes."
