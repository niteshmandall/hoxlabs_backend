#!/bin/bash

# Configuration
# Configuration
BASE_URL="${1:-http://localhost:8080}"
# Remove trailing slash if present
BASE_URL=${BASE_URL%/}
EMAIL="perf_$(date +%s)@test.com"
PASSWORD="password123"

echo "============================================"
echo "Performance Benchmark: $BASE_URL"
echo "============================================"

# 1. Health Check
echo "Checking Health..."
curl -s -o /dev/null -w "Health Check: %{time_total}s\n" "$BASE_URL/actuator/health"

# 2. Register
echo "Registering User ($EMAIL)..."
REGISTER_PAYLOAD=$(cat <<EOF
{
  "email": "$EMAIL",
  "password": "$PASSWORD",
  "name": "Perf User",
  "age": 30,
  "gender": "MALE",
  "weight": 75.0,
  "height": 180.0,
  "fitnessGoal": "OVERALL_FITNESS",
  "calorieGoal": 2500,
  "proteinGoal": 150,
  "carbsGoal": 250,
  "fatGoal": 80
}
EOF
)

REGISTER_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/register" \
  -H "Content-Type: application/json" \
  -d "$REGISTER_PAYLOAD")

# Extract Token (assuming response contains token at root or inside an object)
# AuthService.java returns AuthenticationResponse { token, refreshToken, user }
TOKEN=$(echo "$REGISTER_RESPONSE" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

if [ -z "$TOKEN" ]; then
  echo "Registration failed or token not found."
  echo "Response: $REGISTER_RESPONSE"
  # Try login if user already exists (unlikely with timestamp email but good fallback)
  LOGIN_PAYLOAD="{\"email\":\"$EMAIL\",\"password\":\"$PASSWORD\"}"
  LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/authenticate" \
    -H "Content-Type: application/json" \
    -d "$LOGIN_PAYLOAD")
  TOKEN=$(echo "$LOGIN_RESPONSE" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
fi

if [ -z "$TOKEN" ]; then
  echo "Failed to obtain token. Aborting."
  exit 1
fi

echo "Token obtained."

# 3. Measure Profile Latency
echo "Measuring Profile Endpoint Latency (5 iterations)..."
TOTAL_TIME=0
for i in {1..5}; do
  TIME=$(curl -s -o /dev/null -w "%{time_total}" -H "Authorization: Bearer $TOKEN" "$BASE_URL/api/user/profile")
  echo "Run $i: ${TIME}s"
  TOTAL_TIME=$(echo "$TOTAL_TIME + $TIME" | bc)
done

AVG_TIME=$(echo "scale=3; $TOTAL_TIME / 5" | bc)
echo "--------------------------------------------"
echo "Average Profile Latency: ${AVG_TIME}s"
echo "--------------------------------------------"
