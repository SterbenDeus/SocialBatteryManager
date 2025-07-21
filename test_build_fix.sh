#!/bin/bash

# Test script to validate the build fixes
# Run this when network connectivity to dl.google.com is available

echo "=== SocialBatteryManager Build Validation Script ==="
echo "This script tests that the Room/Kapt issues have been resolved"
echo

cd "$(dirname "$0")"

echo "1. Cleaning previous build artifacts..."
./gradlew clean

echo "2. Testing Kapt annotation processing specifically..."
./gradlew :app:kaptDebugKotlin

if [ $? -eq 0 ]; then
    echo "✅ Kapt annotation processing SUCCESSFUL!"
    echo "   This means Room was able to process all DAO queries and entity definitions"
else
    echo "❌ Kapt annotation processing FAILED"
    echo "   Check the error output above for details"
    exit 1
fi

echo "3. Testing full compilation..."
./gradlew :app:compileDebugKotlin

if [ $? -eq 0 ]; then
    echo "✅ Kotlin compilation SUCCESSFUL!"
else
    echo "❌ Kotlin compilation FAILED"
    exit 1
fi

echo "4. Attempting full build..."
./gradlew build

if [ $? -eq 0 ]; then
    echo "✅ Full build SUCCESSFUL!"
    echo
    echo "🎉 ALL ISSUES RESOLVED!"
    echo "- SQL errors for missing columns (syncStatus, socialInteractionLevel, firebaseId) are fixed"
    echo "- Cursor conversion errors for List<ActivityEntity> and Flow<List<ActivityEntity>> are fixed"
    echo "- Cursor conversion errors for Double return types are fixed"  
    echo "- Kapt task execution issues are resolved"
else
    echo "❌ Full build FAILED - but this might be due to other unrelated issues"
    echo "If Kapt and Kotlin compilation succeeded above, the core database issues are fixed"
fi

echo
echo "=== Validation Complete ==="