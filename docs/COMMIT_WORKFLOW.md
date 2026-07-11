# BNR Baymax Commit Workflow

This repository enforces strict static code analysis and commit message metadata for every push.

## Mandatory Workflow

1. **MAKE CHANGE**
2. **VERIFY** (run your local tests/build)
3. **COMMIT WITH `Prompt:` TRAILER**
   - Your commit message MUST contain a 1-2 sentence summary starting exactly with `Prompt: ` (case-insensitive search).
   - Example:
     ```
     feat(deploy): prepare Render and Supabase configuration

     Prompt: Prepare BNR Baymax for Supabase and Render deployment, add Docker-based SonarQube quality gates.
     ```
4. **SONAR ANALYZES EXACT COMMIT**
   - The `post-commit` Git hook runs automatically after you commit.
   - It executes `sonar-analyze-head.ps1` which analyzes your exact new commit against your local Docker SonarQube container.
5. **BOTH QUALITY GATES PASS**
   - The script waits for both Backend and Frontend Quality Gates.
   - If successful, it writes a marker in `.git/sonar-passed/<SHA>`.
6. **PUSH**
   - The `pre-push` hook verifies every outgoing commit has the `Prompt:` metadata and a successful Sonar marker. If they do, the push succeeds!
