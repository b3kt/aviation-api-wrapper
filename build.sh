#!/bin/bash
# Script untuk build aplikasi

echo "🔨 Building Aviation API..."
mvn clean package

if [ $? -eq 0 ]; then
    echo "✅ Build successful!"
    echo "📦 JAR location: target/aviation-api-0.0.1-SNAPSHOT.jar"
    echo ""
    echo "To run the application:"
    echo "  java -jar target/aviation-api-0.0.1-SNAPSHOT.jar"
    echo "  OR"
    echo "  mvn spring-boot:run"
else
    echo "❌ Build failed. Check the output above."
    exit 1
fi

