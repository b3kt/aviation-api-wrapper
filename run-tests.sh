#!/bin/bash
# Script untuk menjalankan semua tests

echo "🧪 Running all tests..."
mvn clean test

if [ $? -eq 0 ]; then
    echo "✅ All tests passed!"
else
    echo "❌ Some tests failed. Check the output above."
    exit 1
fi

