# Happy_Kid Implementation Testing Script
# Windows-optimized testing for Android development

param(
    [switch]$Quick,
    [switch]$Full,
    [switch]$AlphabetOnly,
    [switch]$All
)

$ProjectRoot = "D:\GitProjects\Happy_Kid"
$TestResults = @()

function Write-ColorOutput($ForegroundColor) {
    $fc = $host.UI.RawUI.ForegroundColor
    $host.UI.RawUI.ForegroundColor = $ForegroundColor
    if ($args) {
        Write-Output $args
    }
    $host.UI.RawUI.ForegroundColor = $fc
}

function Test-ProjectStructure {
    Write-ColorOutput Cyan "=== Testing Project Structure ==="
    
    $requiredFiles = @(
        "app\src\main\java\com\happykid\toddlerabc\HappyKidApplication.kt",
        "app\src\main\java\com\happykid\toddlerabc\MainActivity.kt",
        "app\src\main\java\com\happykid\toddlerabc\util\WindowsDeviceUtils.kt",
        "app\src\main\java\com\happykid\toddlerabc\ui\navigation\HappyKidNavigation.kt",
        "app\src\main\java\com\happykid\toddlerabc\ui\theme\Theme.kt",
        "app\src\main\java\com\happykid\toddlerabc\ui\theme\Color.kt",
        "app\src\main\java\com\happykid\toddlerabc\ui\theme\Type.kt"
    )
    
    $passed = 0
    $total = $requiredFiles.Count
    
    foreach ($file in $requiredFiles) {
        $fullPath = Join-Path $ProjectRoot $file
        if (Test-Path $fullPath) {
            Write-ColorOutput Green "✓ $file"
            $passed++
        } else {
            Write-ColorOutput Red "✗ $file - Missing"
        }
    }
    
    $script:TestResults += [PSCustomObject]@{
        Category = "Project Structure"
        Passed = $passed
        Total = $total
        Status = if ($passed -eq $total) { "PASS" } else { "FAIL" }
    }
}

function Test-DatabaseImplementation {
    Write-ColorOutput Cyan "=== Testing Database Implementation ==="
    
    $databaseFiles = @(
        "app\src\main\java\com\happykid\toddlerabc\data\local\HappyKidDatabase.kt",
        "app\src\main\java\com\happykid\toddlerabc\data\local\entity\User.kt",
        "app\src\main\java\com\happykid\toddlerabc\data\local\entity\Progress.kt",
        "app\src\main\java\com\happykid\toddlerabc\data\local\entity\Lesson.kt",
        "app\src\main\java\com\happykid\toddlerabc\data\local\dao\UserDao.kt",
        "app\src\main\java\com\happykid\toddlerabc\data\local\dao\ProgressDao.kt",
        "app\src\main\java\com\happykid\toddlerabc\data\local\dao\LessonDao.kt"
    )
    
    $passed = 0
    $total = $databaseFiles.Count
    
    foreach ($file in $databaseFiles) {
        $fullPath = Join-Path $ProjectRoot $file
        if (Test-Path $fullPath) {
            Write-ColorOutput Green "✓ $file"
            $passed++
        } else {
            Write-ColorOutput Red "✗ $file - Missing"
        }
    }
    
    $script:TestResults += [PSCustomObject]@{
        Category = "Database Implementation"
        Passed = $passed
        Total = $total
        Status = if ($passed -eq $total) { "PASS" } else { "FAIL" }
    }
}

function Test-DependencyInjection {
    Write-ColorOutput Cyan "=== Testing Dependency Injection ==="
    
    $diFiles = @(
        "app\src\main\java\com\happykid\toddlerabc\di\DatabaseModule.kt",
        "app\src\main\java\com\happykid\toddlerabc\di\RepositoryModule.kt",
        "app\src\main\java\com\happykid\toddlerabc\di\AudioModule.kt"
    )
    
    $passed = 0
    $total = $diFiles.Count
    
    foreach ($file in $diFiles) {
        $fullPath = Join-Path $ProjectRoot $file
        if (Test-Path $fullPath) {
            Write-ColorOutput Green "✓ $file"
            $passed++
        } else {
            Write-ColorOutput Red "✗ $file - Missing"
        }
    }
    
    $script:TestResults += [PSCustomObject]@{
        Category = "Dependency Injection"
        Passed = $passed
        Total = $total
        Status = if ($passed -eq $total) { "PASS" } else { "FAIL" }
    }
}

function Test-AlphabetModule {
    Write-ColorOutput Cyan "=== Testing Alphabet Learning Module ==="
    
    $alphabetFiles = @(
        "app\src\main\java\com\happykid\toddlerabc\feature\alphabet\presentation\AlphabetScreen.kt",
        "app\src\main\java\com\happykid\toddlerabc\feature\alphabet\presentation\AlphabetViewModel.kt",
        "app\src\main\java\com\happykid\toddlerabc\domain\model\Letter.kt",
        "app\src\main\java\com\happykid\toddlerabc\domain\repository\AlphabetRepository.kt",
        "app\src\main\java\com\happykid\toddlerabc\data\repository\AlphabetRepositoryImpl.kt"
    )
    
    $passed = 0
    $total = $alphabetFiles.Count
    
    foreach ($file in $alphabetFiles) {
        $fullPath = Join-Path $ProjectRoot $file
        if (Test-Path $fullPath) {
            Write-ColorOutput Green "✓ $file"
            $passed++
        } else {
            Write-ColorOutput Red "✗ $file - Missing"
        }
    }
    
    $script:TestResults += [PSCustomObject]@{
        Category = "Alphabet Module"
        Passed = $passed
        Total = $total
        Status = if ($passed -eq $total) { "PASS" } else { "FAIL" }
    }
}

function Test-AudioSystem {
    Write-ColorOutput Cyan "=== Testing Audio System ==="
    
    $audioFiles = @(
        "app\src\main\java\com\happykid\toddlerabc\domain\repository\AudioRepository.kt",
        "app\src\main\java\com\happykid\toddlerabc\data\repository\AudioRepositoryImpl.kt",
        "app\src\main\java\com\happykid\toddlerabc\data\audio\AudioManager.kt",
        "app\src\main\java\com\happykid\toddlerabc\data\audio\WindowsAudioOptimizer.kt",
        "app\src\main\java\com\happykid\toddlerabc\data\audio\SpeechRecognitionManager.kt",
        "app\src\main\java\com\happykid\toddlerabc\data\audio\TextToSpeechManager.kt"
    )
    
    $passed = 0
    $total = $audioFiles.Count
    
    foreach ($file in $audioFiles) {
        $fullPath = Join-Path $ProjectRoot $file
        if (Test-Path $fullPath) {
            Write-ColorOutput Green "✓ $file"
            $passed++
        } else {
            Write-ColorOutput Red "✗ $file - Missing"
        }
    }
    
    $script:TestResults += [PSCustomObject]@{
        Category = "Audio System"
        Passed = $passed
        Total = $total
        Status = if ($passed -eq $total) { "PASS" } else { "FAIL" }
    }
}

function Test-RepositoryImplementations {
    Write-ColorOutput Cyan "=== Testing Repository Implementations ==="
    
    $repoFiles = @(
        "app\src\main\java\com\happykid\toddlerabc\data\repository\UserRepositoryImpl.kt",
        "app\src\main\java\com\happykid\toddlerabc\data\repository\ProgressRepositoryImpl.kt",
        "app\src\main\java\com\happykid\toddlerabc\data\repository\AlphabetRepositoryImpl.kt",
        "app\src\main\java\com\happykid\toddlerabc\data\repository\AudioRepositoryImpl.kt"
    )
    
    $passed = 0
    $total = $repoFiles.Count
    
    foreach ($file in $repoFiles) {
        $fullPath = Join-Path $ProjectRoot $file
        if (Test-Path $fullPath) {
            Write-ColorOutput Green "✓ $file"
            $passed++
        } else {
            Write-ColorOutput Red "✗ $file - Missing"
        }
    }
    
    $script:TestResults += [PSCustomObject]@{
        Category = "Repository Implementations"
        Passed = $passed
        Total = $total
        Status = if ($passed -eq $total) { "PASS" } else { "FAIL" }
    }
}

function Test-WindowsOptimizations {
    Write-ColorOutput Cyan "=== Testing Windows Optimizations ==="
    
    $windowsFiles = @(
        "scripts\windows-dev-setup.ps1",
        "scripts\build-tools.ps1",
        "scripts\verify-implementation.ps1",
        "docs\windows-architecture-specification.md",
        "docs\implementation-plan.md"
    )
    
    $passed = 0
    $total = $windowsFiles.Count
    
    foreach ($file in $windowsFiles) {
        $fullPath = Join-Path $ProjectRoot $file
        if (Test-Path $fullPath) {
            Write-ColorOutput Green "✓ $file"
            $passed++
        } else {
            Write-ColorOutput Red "✗ $file - Missing"
        }
    }
    
    $script:TestResults += [PSCustomObject]@{
        Category = "Windows Optimizations"
        Passed = $passed
        Total = $total
        Status = if ($passed -eq $total) { "PASS" } else { "FAIL" }
    }
}

function Show-TestSummary {
    Write-ColorOutput Cyan "=== Test Summary ==="
    
    $totalPassed = ($TestResults | Measure-Object -Property Passed -Sum).Sum
    $totalTests = ($TestResults | Measure-Object -Property Total -Sum).Sum
    $passedCategories = ($TestResults | Where-Object { $_.Status -eq "PASS" }).Count
    $totalCategories = $TestResults.Count
    
    Write-ColorOutput White "Test Results by Category:"
    foreach ($result in $TestResults) {
        $color = if ($result.Status -eq "PASS") { "Green" } else { "Red" }
        Write-ColorOutput $color "$($result.Category): $($result.Passed)/$($result.Total) - $($result.Status)"
    }
    
    Write-ColorOutput White ""
    Write-ColorOutput White "Overall Results:"
    Write-ColorOutput White "Categories: $passedCategories/$totalCategories passed"
    Write-ColorOutput White "Individual Tests: $totalPassed/$totalTests passed"
    
    $overallStatus = if ($passedCategories -eq $totalCategories) { "PASS" } else { "FAIL" }
    $statusColor = if ($overallStatus -eq "PASS") { "Green" } else { "Red" }
    
    Write-ColorOutput $statusColor "Overall Status: $overallStatus"
    
    if ($overallStatus -eq "PASS") {
        Write-ColorOutput Green "🎉 All tests passed! Happy_Kid implementation is ready for development."
    } else {
        Write-ColorOutput Red "❌ Some tests failed. Please review the missing components."
    }
    
    Write-ColorOutput White ""
    Write-ColorOutput White "Next Steps:"
    Write-ColorOutput White "1. Run Windows emulator: .\scripts\build-tools.ps1 -Action emulator"
    Write-ColorOutput White "2. Build project: .\scripts\build-tools.ps1 -Action build"
    Write-ColorOutput White "3. Deploy to emulator: .\scripts\build-tools.ps1 -Action deploy"
    Write-ColorOutput White "4. Test Alphabet module functionality"
}

# Main execution
Write-ColorOutput Green "Happy_Kid Implementation Testing"
Write-ColorOutput Green "Windows Android Development Environment"
Write-ColorOutput Green "====================================="

Set-Location $ProjectRoot

if ($Quick -or $All) {
    Test-ProjectStructure
    Test-AlphabetModule
}

if ($AlphabetOnly) {
    Test-AlphabetModule
    Test-AudioSystem
}

if ($Full -or $All) {
    Test-ProjectStructure
    Test-DatabaseImplementation
    Test-DependencyInjection
    Test-AlphabetModule
    Test-AudioSystem
    Test-RepositoryImplementations
    Test-WindowsOptimizations
}

if (-not ($Quick -or $Full -or $AlphabetOnly -or $All)) {
    # Default: run core tests
    Test-ProjectStructure
    Test-AlphabetModule
    Test-AudioSystem
}

Show-TestSummary

# Return appropriate exit code
$overallPassed = ($TestResults | Where-Object { $_.Status -eq "PASS" }).Count
$totalCategories = $TestResults.Count

if ($overallPassed -eq $totalCategories) {
    exit 0
} else {
    exit 1
}
