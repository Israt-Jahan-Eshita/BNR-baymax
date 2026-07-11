$ErrorActionPreference = "Stop"

if (-not $env:SONAR_TOKEN) {
    Write-Error "SONAR_TOKEN is not set. Cannot run analysis."
    exit 1
}

# Verify Docker is running
docker info > $null 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Error "Docker is not running."
    exit 1
}

# Verify SonarQube is reachable
try {
    $response = Invoke-WebRequest -Uri "http://localhost:9000/api/system/status" -UseBasicParsing -TimeoutSec 2 -ErrorAction Stop
} catch {
    Write-Error "SonarQube is not reachable at http://localhost:9000. Is the container running?"
    exit 1
}

# Read current HEAD SHA
$HEAD_SHA = git rev-parse HEAD

# Verify clean working tree
$status = git status --porcelain
if ($status) {
    Write-Error "Working tree is not clean. Commit or stash changes before analyzing HEAD."
    exit 1
}

Write-Host "Analyzing HEAD: $HEAD_SHA"

# Backend
Set-Location superagent-api
Write-Host "Running backend tests and Sonar analysis..."
./mvnw clean verify sonar:sonar "-Dsonar.projectKey=bnr-baymax-api" "-Dsonar.host.url=http://localhost:9000" "-Dsonar.projectVersion=$HEAD_SHA" "-Dsonar.qualitygate.wait=true"
if ($LASTEXITCODE -ne 0) {
    Write-Error "Backend analysis or Quality Gate failed."
    exit 1
}
Set-Location ..

# Frontend
Set-Location superagent-web
Write-Host "Running frontend build and Sonar analysis..."
npm ci
if ($LASTEXITCODE -ne 0) { Write-Error "npm ci failed"; exit 1 }
npm run build
if ($LASTEXITCODE -ne 0) { Write-Error "npm run build failed"; exit 1 }

npm run sonar -- "-Dsonar.projectKey=bnr-baymax-web" "-Dsonar.host.url=http://localhost:9000" "-Dsonar.projectVersion=$HEAD_SHA" "-Dsonar.qualitygate.wait=true"
if ($LASTEXITCODE -ne 0) {
    Write-Error "Frontend analysis or Quality Gate failed."
    exit 1
}
Set-Location ..

Write-Host "Both Quality Gates passed!"

# Write success marker
$markerDir = ".git/sonar-passed"
if (-not (Test-Path $markerDir)) {
    New-Item -ItemType Directory -Path $markerDir | Out-Null
}
New-Item -ItemType File -Path "$markerDir/$HEAD_SHA" -Force | Out-Null
Write-Host "Marker created at $markerDir/$HEAD_SHA"
