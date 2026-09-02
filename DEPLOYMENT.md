# IntelliERP - GitHub Deployment Guide

## 🚀 Deployment Overview

Your IntelliERP application is now configured for automated deployment on GitHub with:
- **GitHub Actions CI/CD** for build automation, testing, and security scanning
- **GitHub Pages** for frontend static asset hosting
- **Artifact storage** for build outputs

---

## 📋 What's Enabled

### 1. **CI/CD Pipeline** (`.github/workflows/ci-cd.yml`)
Automatically triggered on every `push` and `pull_request`:

✅ **Build Job**
- Compile Java 21 code with Maven
- Package JAR artifacts
- Store build artifacts for 30 days

✅ **Test Coverage Job**
- Run all automated tests
- Generate code coverage reports
- Upload to Codecov

✅ **Code Quality Job**
- Compile source code
- Run checkstyle validation

✅ **Security Scanning Job**
- Trivy vulnerability scanning
- GitHub Security integration

### 2. **GitHub Pages Deployment** (`.github/workflows/pages-deploy.yml`)
Automatically deploys frontend on pushes to `main`:
- Uploads static frontend files (HTML, CSS, JS)
- Available at: `https://Sky-ydv2008.github.io/bharat/`

---

## 🔄 How It Works

### On Every Push to `main`:
1. GitHub Actions automatically triggers the **CI/CD Pipeline**
2. Code is compiled and tested
3. If tests pass, artifacts are stored
4. Frontend is deployed to GitHub Pages (if static files changed)

### On Pull Requests:
1. CI/CD pipeline runs to validate changes
2. Tests must pass before merging
3. Results are posted as PR comments

---

## 📊 View Your Deployments

| Resource | Link |
|----------|------|
| **Actions Workflows** | https://github.com/Sky-ydv2008/bharat/actions |
| **GitHub Pages** | https://Sky-ydv2008.github.io/bharat/ |
| **Build Artifacts** | https://github.com/Sky-ydv2008/bharat/actions (under each workflow run) |
| **Security Scan Results** | https://github.com/Sky-ydv2008/bharat/security/code-scanning |

---

## 🛠️ Configuration Details

### Java & Maven Settings
- **Java Version**: 21 (Temurin)
- **Maven Cache**: Enabled for faster builds
- **Test Execution**: All tests run automatically

### Artifacts & Retention
- **JAR Files**: Stored for 30 days
- **Location**: Download from Actions → Workflow Run → Artifacts

### Security Scanning
- **Trivy Scanner**: Scans for vulnerabilities
- **Results**: Available in GitHub Security tab

---

## ⚙️ Customization

### To Modify the Pipeline:
Edit `.github/workflows/ci-cd.yml`:
```yaml
- name: Run Tests
  run: mvn test -Dspring.profiles.active=test
```

### To Change Pages Deployment:
Edit `.github/workflows/pages-deploy.yml`:
- Trigger conditions
- Asset paths
- Deployment settings

### To Add More Workflows:
Create new `.yml` files in `.github/workflows/`

---

## 🔐 Security Best Practices

✅ **Current Setup**:
- GitHub Actions use minimal permissions
- Workflow files are version controlled
- Secrets not stored in code
- Automated vulnerability scanning

### To Add Secrets (e.g., API keys):
1. Go to: **Settings → Secrets and variables → Actions**
2. Click **New repository secret**
3. Use in workflow: `${{ secrets.YOUR_SECRET_NAME }}`

---

## 📈 Monitoring & Debugging

### Check Workflow Status:
1. Go to **Actions** tab
2. Click on workflow run
3. View logs for each job
4. Check failed steps for error messages

### Re-run Failed Jobs:
- Click **Re-run failed jobs** on workflow run page
- Or re-run all jobs

### View Artifacts:
1. Open workflow run
2. Scroll to **Artifacts** section
3. Download JAR files for local testing

---

## 🚀 Next Steps

1. **Enable Branch Protection**: 
   - Settings → Branches → Add Rule
   - Require CI to pass before merge

2. **Set Up Notifications**:
   - Settings → Notifications
   - Enable workflow notifications

3. **Add More Environments**:
   - Create deployment workflow for staging/production

4. **Integrate with External Services**:
   - Render, Heroku, AWS for runtime deployment

---

## 📞 Troubleshooting

| Issue | Solution |
|-------|----------|
| Tests failing | Check recent changes in Actions logs |
| Pages not updating | Verify `Settings → Pages` has correct source |
| No artifacts | Check retention days (30 by default) |
| Build slow | Maven cache should improve speed automatically |

---

## 📚 Learn More

- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [GitHub Pages Guide](https://docs.github.com/en/pages)
- [Maven CI/CD Best Practices](https://maven.apache.org/guides/)

---

**Your IntelliERP application is now fully set up for GitHub deployment! 🎉**
