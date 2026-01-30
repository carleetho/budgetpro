#!/bin/bash
# AXIOM Script Integration Tests
# Verifies the end-to-end workflow of AXIOM bash scripts.

set -e

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m'

# Setup
TEST_DIR=$(mktemp -d)
ORIGINAL_DIR=$(pwd)
FAILURES=0

echo -e "${GREEN}🧪 AXIOM Integration Tests${NC}"
echo "Running in: $TEST_DIR"

cleanup() {
    echo "Cleaning up..."
    rm -rf "$TEST_DIR"
}
trap cleanup EXIT

setup_repo() {
    cd "$TEST_DIR"
    git init --quiet
    git config user.email "test@example.com"
    git config user.name "Test User"
    
    # Copy AXIOM tools
    mkdir -p tools/axiom
    # Copy from original location to temp location
    # We need to copy recursively to ensure all python files and scripts are there
    cp -r "$ORIGINAL_DIR/tools" .
    
    # Copy config if it exists
    if [ -f "$ORIGINAL_DIR/axiom.config.yaml" ]; then
        cp "$ORIGINAL_DIR/axiom.config.yaml" .
    fi
    
    # Copy quick execution script
    if [ -f "$ORIGINAL_DIR/axiom.sh" ]; then
        cp "$ORIGINAL_DIR/axiom.sh" .
    fi
}

assert_exit_code() {
    local code=$1
    local expected=$2
    local msg=$3
    
    # 0 = Success
    # 1 = Violations Found (Blocking)
    # 2 = System Error
    
    if [ "$code" -eq "$expected" ]; then
        echo -e "  ✅ PASS: $msg"
    else
        echo -e "  ❌ FAIL: $msg (Got $code, Expected $expected)"
        FAILURES=$((FAILURES + 1))
    fi
}

# --- Test 1: Installation ---
echo -e "\nRunning Test 1: Fresh Installation..."
setup_repo

# Run install.sh
chmod +x tools/axiom/install.sh
./tools/axiom/install.sh > /dev/null
assert_exit_code $? 0 "Installation script should succeed"

# Verify directories
if [ -d ".budgetpro" ] && [ -d "tools/axiom/validators" ] && [ -f ".git/hooks/pre-commit" ]; then
    echo -e "  ✅ PASS: Directory structure created"
else
    echo -e "  ❌ FAIL: Directory structure missing"
    FAILURES=$((FAILURES + 1))
    ls -R
fi

# --- Test 2: Idempotency ---
echo -e "\nRunning Test 2: Idempotent Installation..."
./tools/axiom/install.sh > /dev/null
assert_exit_code $? 0 "Re-running install should succeed"

# --- Test 3: Pre-commit Hook (Dry Run / Clean) ---
echo -e "\nRunning Test 3: Pre-commit Hook (Clean)..."
# We simulate a commit by running the hook directly or via git checking
# Note: axiom_sentinel might fail if python path is wrong or dependencies missing inside temp env.
# But we copied tools/ so it should work if we have libs installed on system or venv.
# We are relying on the user's python env.

# Create a harmless file
echo "print('hello')" > clean_file.py
git add clean_file.py

# Run hook manually to see output
.git/hooks/pre-commit --dry-run > /dev/null
assert_exit_code $? 0 "Clean dry-run should pass"

# --- Test 4: Quick Execution Script ---
echo -e "\nRunning Test 4: CLI Execution..."
if [ -f "axiom.sh" ]; then
    ./axiom.sh --dry-run > /dev/null
    assert_exit_code $? 0 "axiom.sh --dry-run should pass"
else
    echo -e "  ❌ FAIL: axiom.sh not found after installation"
    FAILURES=$((FAILURES + 1))
fi

# --- Summary ---
echo -e "\n======================="
if [ $FAILURES -eq 0 ]; then
    echo -e "${GREEN}ALL TESTS PASSED ($TEST_DIR)${NC}"
    exit 0
else
    echo -e "${RED}$FAILURES TESTS FAILED${NC}"
    exit 1
fi
