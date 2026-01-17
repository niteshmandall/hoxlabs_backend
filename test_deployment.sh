#!/bin/bash
set -e

# Config
BASE_URL="https://calorie.brainbrief.in"
# BASE_URL="http://localhost:8080" # Debug mode
EMAIL="testuser_$(date +%s)@example.com"
PASSWORD="securePassword123"

echo "============================================="
echo "   Verifying Deployment: $BASE_URL "
echo "============================================="

# 1. Health Check
echo "[1/4] Checking Health..."
HTTP_STATUS=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/actuator/health")
if [ "$HTTP_STATUS" -eq 200 ]; then
    echo "✅ Health Check Passed (200 OK)"
else
    echo "❌ Health Check Failed ($HTTP_STATUS)"
    exit 1
fi

# 2. Register
echo "[2/4] Registering User ($EMAIL)..."
REGISTER_PAYLOAD=$(cat <<EOF
{
  "email": "$EMAIL",
  "password": "$PASSWORD",
  "name": "Test User",
  "age": 30,
  "gender": "MALE",
  "weight": 75.0,
  "height": 180.0,
  "fitnessGoal": "WEIGHT_LOSS",
  "calorieGoal": 2000
}
EOF
)

REGISTER_RES=$(curl -s -X POST "$BASE_URL/api/auth/register" \
  -H "Content-Type: application/json" \
  -d "$REGISTER_PAYLOAD")

# Extract Token (using python for reliability)
TOKEN=$(echo "$REGISTER_RES" | python3 -c "import sys, json; print(json.load(sys.stdin).get('token', ''))")

if [ -n "$TOKEN" ]; then
    echo "✅ Registration Successful. Token received."
else
    echo "❌ Registration Failed. Response:"
    echo "$REGISTER_RES"
    exit 1
fi

# 3. Get Profile
echo "[3/4] Verifying User Profile..."
PROFILE_RES=$(curl -s -X GET "$BASE_URL/api/user/profile" \
  -H "Authorization: Bearer $TOKEN")

RETRIEVED_EMAIL=$(echo "$PROFILE_RES" | python3 -c "import sys, json; print(json.load(sys.stdin).get('email', ''))")

if [ "$RETRIEVED_EMAIL" == "$EMAIL" ]; then
    echo "✅ Profile Verified (Email matches)."
else
    echo "❌ Profile Verification Failed. Got: $RETRIEVED_EMAIL"
    exit 1
fi

# 4. Log a Meal (Test AI/Mock Service)
echo "[4/4] Logging a Meal..."
MEAL_PAYLOAD='{"text": "I had 2 rotis", "mealType": "LUNCH"}'
MEAL_RES=$(curl -s -X POST "$BASE_URL/api/meals/log" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d "$MEAL_PAYLOAD")

# Check for items
ITEMS_COUNT=$(echo "$MEAL_RES" | python3 -c "import sys, json; print(len(json.load(sys.stdin).get('foodItems', [])))")
IS_MOCK=$(echo "$MEAL_RES" | python3 -c "import sys, json; print('mock' in json.load(sys.stdin).get('clarification', '').lower())")

if [ "$ITEMS_COUNT" -gt 0 ]; then
    echo "✅ Meal Logged Successfully. ($ITEMS_COUNT items found)"
    if [ "$IS_MOCK" == "True" ]; then
        echo "⚠️  Note: Service returned MOCK data (Expected since API key is missing)."
    else
        echo "🎉 Real AI Response received!"
    fi
else
    echo "❌ Meal Logging Failed."
    echo "$MEAL_RES"
    exit 1
fi

echo "============================================="
echo "   🎉 ALL SYSTEMS OPERATIONAL 🎉"
echo "============================================="
