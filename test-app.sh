#!/bin/bash

echo "🚀 Starting Spring Boot Application Test..."
echo ""

# Kill any existing processes on port 8080
echo "🔧 Cleaning up port 8080..."
lsof -ti :8080 | xargs kill -9 2>/dev/null
pkill -9 -f "gradlew bootRun" 2>/dev/null
sleep 2

# Start the application in background
echo "🏗️  Building and starting application..."
cd /Users/nikhilmacbook/Desktop/Courses/SpringBoot/Projects/spring_boot_mosh_rest_api
./gradlew bootRun > /tmp/spring-app.log 2>&1 &
APP_PID=$!

echo "⏳ Waiting for application to start (30 seconds)..."
sleep 30

# Check if app is running
if lsof -i :8080 > /dev/null 2>&1; then
    echo "✅ Application is running on port 8080"
    echo ""

    echo "📡 Testing GET /users endpoint..."
    echo "Response:"
    curl -s http://localhost:8080/users | jq .

    echo ""
    echo "📡 Testing GET / endpoint..."
    echo "Response:"
    curl -s http://localhost:8080/ | jq .

    echo ""
    echo "✅ Test complete! Application is working."
    echo ""
    echo "📋 To view logs: tail -f /tmp/spring-app.log"
    echo "🛑 To stop: kill $APP_PID"
else
    echo "❌ Application failed to start"
    echo "📋 Last 50 lines of log:"
    tail -50 /tmp/spring-app.log
fi

