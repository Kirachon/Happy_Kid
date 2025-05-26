# Setup Java for Happy_Kid Android Development

function Write-ColorOutput($ForegroundColor) {
    $fc = $host.UI.RawUI.ForegroundColor
    $host.UI.RawUI.ForegroundColor = $ForegroundColor
    if ($args) {
        Write-Output $args
    }
    $host.UI.RawUI.ForegroundColor = $fc
}

function Find-JavaInstallation {
    Write-ColorOutput Yellow "Searching for Java installations..."
    
    # Common Java installation paths
    $javaPaths = @(
        "$env:JAVA_HOME",
        "$env:ProgramFiles\Java\*\bin\java.exe",
        "$env:ProgramFiles\Eclipse Adoptium\*\bin\java.exe",
        "$env:ProgramFiles\Microsoft\*\bin\java.exe",
        "$env:ProgramFiles\Amazon Corretto\*\bin\java.exe",
        "${env:ProgramFiles(x86)}\Java\*\bin\java.exe",
        "${env:ProgramFiles(x86)}\Eclipse Adoptium\*\bin\java.exe",
        "$env:LOCALAPPDATA\Programs\Eclipse Adoptium\*\bin\java.exe",
        # Android Studio embedded JDK
        "$env:LOCALAPPDATA\JetBrains\Toolbox\apps\AndroidStudio\*\jbr\bin\java.exe",
        "$env:ProgramFiles\Android\Android Studio\jbr\bin\java.exe",
        "${env:ProgramFiles(x86)}\Android\Android Studio\jbr\bin\java.exe"
    )
    
    $foundJavas = @()
    
    foreach ($path in $javaPaths) {
        if ($path) {
            $resolvedPaths = Resolve-Path $path -ErrorAction SilentlyContinue
            foreach ($resolvedPath in $resolvedPaths) {
                if (Test-Path $resolvedPath) {
                    try {
                        $version = & $resolvedPath -version 2>&1 | Select-String "version"
                        if ($version) {
                            $javaHome = Split-Path (Split-Path $resolvedPath -Parent) -Parent
                            $foundJavas += [PSCustomObject]@{
                                Path = $resolvedPath
                                JavaHome = $javaHome
                                Version = $version.ToString().Trim()
                            }
                            Write-ColorOutput Green "Found Java: $resolvedPath"
                            Write-ColorOutput White "  Version: $($version.ToString().Trim())"
                            Write-ColorOutput White "  JAVA_HOME: $javaHome"
                        }
                    } catch {
                        # Skip invalid Java installations
                    }
                }
            }
        }
    }
    
    return $foundJavas
}

function Select-BestJava($javaInstallations) {
    if ($javaInstallations.Count -eq 0) {
        return $null
    }
    
    # Prefer Android Studio embedded JDK
    $androidStudioJdk = $javaInstallations | Where-Object { $_.JavaHome -like "*Android Studio*" -or $_.JavaHome -like "*AndroidStudio*" }
    if ($androidStudioJdk) {
        Write-ColorOutput Green "Selected Android Studio embedded JDK"
        return $androidStudioJdk[0]
    }
    
    # Prefer Java 11 or higher
    $java11Plus = $javaInstallations | Where-Object { 
        $_.Version -match "version `"(1[1-9]|[2-9][0-9])" 
    }
    if ($java11Plus) {
        Write-ColorOutput Green "Selected Java 11+ installation"
        return $java11Plus[0]
    }
    
    # Use the first available Java
    Write-ColorOutput Yellow "Using first available Java installation"
    return $javaInstallations[0]
}

function Set-JavaEnvironment($javaInstallation) {
    if (-not $javaInstallation) {
        Write-ColorOutput Red "No Java installation found!"
        Write-ColorOutput Yellow "Please install Java 11+ or Android Studio"
        return $false
    }
    
    Write-ColorOutput Yellow "Setting up Java environment..."
    
    # Set environment variables for current session
    $env:JAVA_HOME = $javaInstallation.JavaHome
    $env:PATH = "$($javaInstallation.JavaHome)\bin;$env:PATH"
    
    Write-ColorOutput Green "JAVA_HOME set to: $($javaInstallation.JavaHome)"
    
    # Test Java
    try {
        $javaVersion = java -version 2>&1
        Write-ColorOutput Green "✓ Java is now accessible"
        Write-ColorOutput White "  $($javaVersion[0])"
        return $true
    } catch {
        Write-ColorOutput Red "✗ Failed to access Java: $($_.Exception.Message)"
        return $false
    }
}

function Show-JavaInstructions($javaInstallation) {
    Write-ColorOutput Cyan "`n=== Java Setup Complete ==="
    
    if ($javaInstallation) {
        Write-ColorOutput White "Java is configured for this session:"
        Write-ColorOutput Green "JAVA_HOME: $($javaInstallation.JavaHome)"
        Write-ColorOutput White ""
        Write-ColorOutput Yellow "To make this permanent:"
        Write-ColorOutput White "1. Open System Properties > Environment Variables"
        Write-ColorOutput White "2. Add JAVA_HOME = $($javaInstallation.JavaHome)"
        Write-ColorOutput White "3. Add to PATH: $($javaInstallation.JavaHome)\bin"
        Write-ColorOutput White ""
        Write-ColorOutput Green "You can now build the Happy_Kid project:"
        Write-ColorOutput White "  .\scripts\build-tools.ps1 -Action build"
    } else {
        Write-ColorOutput Red "Java setup failed!"
        Write-ColorOutput Yellow "Please install one of the following:"
        Write-ColorOutput White "1. Android Studio (includes embedded JDK)"
        Write-ColorOutput White "2. Eclipse Temurin JDK 11+ from https://adoptium.net/"
        Write-ColorOutput White "3. Microsoft OpenJDK from https://www.microsoft.com/openjdk"
        Write-ColorOutput White "4. Amazon Corretto JDK from https://aws.amazon.com/corretto/"
    }
}

# Main execution
Write-ColorOutput Green "Java Setup for Happy_Kid Android Development"
Write-ColorOutput Green "==========================================="

$javaInstallations = Find-JavaInstallation
$selectedJava = Select-BestJava $javaInstallations
$success = Set-JavaEnvironment $selectedJava

Show-JavaInstructions $selectedJava

if ($success) {
    exit 0
} else {
    exit 1
}
