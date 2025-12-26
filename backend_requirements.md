# Backend API Update Requirements

The Android client now collects additional user profile information during the signup process to provide personalized nutrition insights.

We need to update the backend API to accept and store these new fields.

## 1. Update `POST /api/auth/register`

Modify the registration endpoint to accept the following additional fields in the JSON body.

### Request Body Changes

**Current Fields:**

- `email`: String
- `password`: String
- `calorieGoal`: Integer

**New Fields to Add:**

- `name`: String (e.g., "Alex")
- `age`: Integer (e.g., 25)
- `gender`: String (Enum: "Male", "Female", "Non-binary", "Prefer not to say")
- `weight`: Float (in kg, e.g., 70.5)
- `height`: Float (in cm, e.g., 175.0)
- `fitnessGoal`: String (Enum: "WEIGHT_LOSS", "MUSCLE_GAIN", "OVERALL_FITNESS")

### Example JSON Payload

```json
{
  "email": "user@example.com",
  "password": "securePassword123",
  "calorieGoal": 2200,
  "name": "Alex Smith",
  "age": 25,
  "gender": "Male",
  "weight": 70.5,
  "height": 175.0,
  "fitnessGoal": "WEIGHT_LOSS"
}
```

## 2. Update `User` Model

Ensure the database schema for the User table includes columns for these new fields so they can be retrieved later via the user profile endpoint.

## 3. (Optional) Update `GET /api/user/profile`

If not already present, ensure the user profile response includes these fields so the app can display them after login on other devices.
