#!/bin/bash

# Script to set up sparse checkout to exclude CI/CD files
# Run this after cloning the repository

# Enable sparse checkout
git config core.sparseCheckout true

# Create sparse checkout file if it doesn't exist
mkdir -p .git/info
touch .git/info/sparse-checkout

# Add all files except CI/CD files
cat > .git/info/sparse-checkout << EOF
/*
!.github/workflows/ci-cd.yml
!.github/workflows/qodana_code_quality.yml
EOF

# Update the working directory
git read-tree -mu HEAD

echo "Sparse checkout configured. CI/CD files will be excluded from your working directory."
