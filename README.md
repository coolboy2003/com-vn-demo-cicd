# Slack CAB Bot

Bot to generate CAB UAT automatically.

## Clone Repository Without CI/CD Files

If you want to clone this repository without the CI/CD configuration files, you can use sparse checkout. Follow these steps:

### Method 1: Using the provided scripts

1. Clone the repository normally:
   ```
   git clone <repository-url>
   cd <repository-directory>
   ```

2. Run the appropriate script for your operating system:
   - For Linux/Mac:
     ```
     chmod +x setup-sparse-checkout.sh
     ./setup-sparse-checkout.sh
     ```
   - For Windows:
     ```
     setup-sparse-checkout.bat
     ```

### Method 2: Manual setup

1. Clone the repository normally:
   ```
   git clone <repository-url>
   cd <repository-directory>
   ```

2. Enable sparse checkout:
   ```
   git config core.sparseCheckout true
   ```

3. Create sparse checkout file:
   ```
   mkdir -p .git/info
   touch .git/info/sparse-checkout
   ```

4. Specify which files to include/exclude:
   ```
   echo "/*" > .git/info/sparse-checkout
   echo "!.github/workflows/ci-cd.yml" >> .git/info/sparse-checkout
   echo "!.github/workflows/qodana_code_quality.yml" >> .git/info/sparse-checkout
   ```

5. Update the working directory:
   ```
   git read-tree -mu HEAD
   ```

After following these steps, the CI/CD configuration files will be excluded from your working directory.

## Development

[Add development instructions here]

## Testing

[Add testing instructions here]

## Deployment

[Add deployment instructions here]
