@echo off
REM Script to set up sparse checkout to exclude CI/CD files
REM Run this after cloning the repository

REM Enable sparse checkout
git config core.sparseCheckout true

REM Create sparse checkout file if it doesn't exist
mkdir .git\info 2>nul
type nul > .git\info\sparse-checkout

REM Add all files except CI/CD files
echo /* > .git\info\sparse-checkout
echo !.github/workflows/ci-cd.yml >> .git\info\sparse-checkout
echo !.github/workflows/qodana_code_quality.yml >> .git\info\sparse-checkout

REM Update the working directory
git read-tree -mu HEAD

echo Sparse checkout configured. CI/CD files will be excluded from your working directory.
