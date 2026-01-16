# Install Android Command-Line Tools
# Run this in PowerShell as Administrator

Write-Host "Installing Android SDK..." -ForegroundColor Cyan

# Create SDK directory
$sdkDir = "C:\Android\sdk"
New-Item -ItemType Directory -Force -Path $sdkDir

# Download command-line tools
$toolsUrl = "https://dl.google.com/android/repository/commandlinetools-win-9477386_latest.zip"
$zipFile = "$env:TEMP\cmdline-tools.zip"

Write-Host "Downloading command-line tools..." -ForegroundColor Yellow
Invoke-WebRequest -Uri $toolsUrl -OutFile $zipFile

Write-Host "Extracting..." -ForegroundColor Yellow
Expand-Archive -Path $zipFile -DestinationPath $sdkDir -Force

# Create correct directory structure
New-Item -ItemType Directory -Force -Path "$sdkDir\cmdline-tools\latest"
Move-Item -Path "$sdkDir\cmdline-tools\*" -Destination "$sdkDir\cmdline-tools\latest\" -Force

# Set environment variables
[Environment]::SetEnvironmentVariable("ANDROID_HOME", $sdkDir, "User")
[Environment]::SetEnvironmentVariable("ANDROID_SDK_ROOT", $sdkDir, "User")

$env:ANDROID_HOME = $sdkDir
$env:ANDROID_SDK_ROOT = $sdkDir

# Accept licenses and install platform
& "$sdkDir\cmdline-tools\latest\bin\sdkmanager.bat" --licenses
& "$sdkDir\cmdline-tools\latest\bin\sdkmanager.bat" "platform-tools" "platforms;android-34" "build-tools;34.0.0"

Write-Host "Done! Android SDK installed to: $sdkDir" -ForegroundColor Green
Write-Host "Please restart your terminal for environment variables to take effect." -ForegroundColor Yellow

# Create local.properties
echo "sdk.dir=$sdkDir" | Out-File -FilePath "local.properties"
