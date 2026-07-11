# SonarQube Local Setup

The hackathon requires every commit to be analyzed locally by SonarQube before it can be pushed to the repository.

## Step 1: Start SonarQube
1. Ensure Docker Desktop is running on Windows.
2. Open PowerShell and run:
   ```powershell
   docker compose -f infra/sonarqube/docker-compose.yml up -d
   ```
3. Wait a few moments for SonarQube to start up.

## Step 2: Initial Setup
1. Open [http://localhost:9000](http://localhost:9000) in your browser.
2. Log in with the default credentials:
   - **Login**: `admin`
   - **Password**: `admin`
3. You will be prompted to change the password immediately.

## Step 3: Generate the Token
1. Go to **Administration -> Security -> Users**.
2. Click on the token icon next to the Administrator user to generate a new token.
3. Name it something like `local-dev` and generate it.
4. **IMPORTANT:** Store this token in your current PowerShell environment. NEVER commit it to the repository.
   ```powershell
   $env:SONAR_TOKEN="<YOUR_GENERATED_TOKEN>"
   ```

## Step 4: Create Projects
For the analysis to run, you need to create two manual projects in SonarQube (Project -> Create Project -> Manually):
1. **Backend Project Key**: `bnr-baymax-api`
2. **Frontend Project Key**: `bnr-baymax-web`

Once these projects exist and the token is set in your environment, your `git commit` hooks will successfully analyze your code!
