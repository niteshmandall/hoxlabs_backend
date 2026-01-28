# CalorieAI - Intelligent Nutrition Tracker

CalorieAI is a production-grade Spring Boot backend application that leverages Artificial Intelligence to simplify calorie tracking. Users can describe their meals in natural language (e.g., "I had 2 rotis and a cup of dal"), and the system uses **Pollinations.ai** to analyze the text, extract accurate nutritional data (Calories, Protein, Carbs, Fat) using strict Indian-standardized schemas, and log the meal.

## 🚀 Key Features

- **Production-Grade AI Analysis**:
  - Expert "Certified Nutritionist" System Prompts.
  - Strict JSON Schema Validation.
  - Specialized Rule Sets: Indian portion estimations, conservative oil usage.
  - Edge Case Handling: Smart defaults for vague inputs, fast food, and homemade dishes.
- **AI Meal Image Generation**: Automatically generates visual representations of your meals using Pollinations.ai.
- **Meal History**: View complete chat history of past logged meals and AI responses.
- **AI Health Coach**: Conversational mode for advice, tips, and motivation without logging calories. Now context-aware (knows your recent nutrition history).
- **Date-Specific Meal Logging**: Log meals for past dates to keep your history accurate.
- **Enhanced User Profile**: Track comprehensive stats including Age, Gender, Weight, Height, and Macro Goals.
- **Firebase Authentication**: Secure, scalable authentication supporting Google Sign-In and Email/Password.
- **Daily Dashboard**: Aggregates daily nutritional intake against user specific calorie goals.
- **Robust Testing**: Comprehensive test suite covering Unit, Integration, and Edge Case scenarios (>35 tests).
- **Multi-Tenancy & Data Isolation**: Strict user-level data segregation ensures users only access their own meals and metrics.
- **Secure**: Implements Spring Security 6 with stateless Firebase token verification.

## 🛠️ Technology Stack

- **Framework**: Spring Boot 3.4
- **Language**: Java 17+
- **Database**: PostgreSQL (Primary), H2 (Test Isolation)
- **AI Service**: Pollinations.ai (OpenAI compatible API)
- **Authentication**: Firebase Auth (Admin SDK)
- **Security**: Spring Security
- **Build Tool**: Maven

## 🔌 API Reference

### 🔐 Authentication & User Sync

> **Note**: Authentication is handled on the client-side via Firebase. The client obtains a Firebase ID Token and sends it in the `Authorization` header (`Bearer <token>`).

#### 1. Sync User (Login/Register)

Updates the user's profile in the database or creates a new one if they don't exist. Call this immediately after a successful Firebase login on the client.

**Endpoint**: `POST /api/auth/sync`

- **Request Body**:
  ```json
  {
    "name": "Alex",
    "dailyCalorieGoal": 2200,
    "age": 25,
    "gender": "MALE",
    "weight": 70.5,
    "height": 175.0,
    "fitnessGoal": "WEIGHT_LOSS",
    "profilePhotoUrl": "http://..."
  }
  ```
- **Response**: Returns the synced User Profile.

### 👤 User Profile

#### 2. Get User Profile

**Endpoint**: `GET /api/user/profile`

- **Response**:
  ```json
  {
    "id": 123,
    "name": "Alex",
    "email": "user@example.com",
    "age": 25,
    "gender": "MALE",
    "weight": 70.5,
    "height": 175.0,
    "fitnessGoal": "WEIGHT_LOSS",
    "calorieGoal": 2000,
    "proteinGoal": 150,
    "carbsGoal": 200,
    "fatGoal": 65,
    "role": "USER",
    "profilePhotoUrl": "http://..."
  }
  ```

#### 3. Update User Profile

**Endpoint**: `PUT /api/user/profile`

- **Request Body** (All fields optional):
  ```json
  {
    "name": "New Name",
    "dailyCalorieGoal": 2200,
    "proteinGoal": 160,
    "carbsGoal": 210,
    "fatGoal": 70,
    "profilePhotoUrl": "https://example.com/me.jpg"
  }
  ```
- **Response**: Returns the updated User Profile.

#### 4. Upload Profile Image

**Endpoint**: `POST /api/user/profile-image`

- **Content-Type**: `multipart/form-data`
- **Request Part**: `image` (The image file)
- **Response**:
  ```json
  {
    "url": "/uploads/users/123/profile.jpg"
  }
  ```

### 🍛 Meals

> **Note**: All Meal and Dashboard endpoints require the `Authorization` header with a valid Firebase ID Token.

#### 5. Log a Meal (AI Analysis)

**Endpoint**: `POST /api/meals/log`

- **Request Body**:
  ```json
  {
    "text": "I had 2 idlis and sambar",
    "mealType": "BREAKFAST", // Options: BREAKFAST, LUNCH, DINNER, SNACK
    "date": "2023-10-25" // Optional: For historic logging (YYYY-MM-DD)
  }
  ```
- **Response**:
  ```json
  {
    "id": 1,
    "text": "I had 2 idlis and sambar",
    "imageUrl": "https://image.pollinations.ai/prompt/I%20had%202%20idlis%20and%20sambar",
    "timestamp": "2023-10-27T08:30:00",
    "foodItems": [
      {
        "name": "idlis",
        "quantity": "2 pieces",
        "calories": 120,
        "protein": 4.0,
        "carbs": 24.0,
        "fat": 0.5
      },
      ...
    ],
    "totalCalories": 250
  }
  ```

#### 6. Get Meal History

**Endpoint**: `GET /api/meals/history`

- **Query Param (Optional)**: `date=YYYY-MM-DD` (Filter logs by specific date)
- **Response**: A list of meal logs, sorted by newest first.

#### 7. Delete a Meal

**Endpoint**: `DELETE /api/meals/{id}`

- **Path Parameter**: `id` (ID of the meal to delete)
- **Response**: `200 OK` (Empty body)

### 💬 AI Coach

#### 8. Get Health Advice

**Endpoint**: `POST /api/chat/advice`

- **Request Body**:
  ```json
  {
    "message": "How can I get more protein as a vegetarian?"
  }
  ```
- **Response**:
  ```json
  {
    "message": "You can try adding lentils, chickpeas, paneer, or soya chunks to your diet. Greek yogurt is also a great source!",
    "is_logging_action": false
  }
  ```

> **Context Awareness**: The backend automatically injects the user's last 7 days of nutrition summaries and their current goals into the prompt.

### 📊 Dashboard

#### 9. Get Daily Summary

**Endpoint**: `GET /api/dashboard/daily`

- **Query Param (Optional)**: `date=YYYY-MM-DD` (Defaults to today)

- **Response**:
  ```json
  {
    "id": 5,
    "date": "2023-10-27",
    "totalCalories": 1250,
    "totalProtein": 65.5,
    "totalCarbs": 140.0,
    "totalFat": 45.0
  }
  ```

## 🧪 Testing

The project includes a unified test suite that runs all Unit, Integration, and validation tests. Tests are configured to run on an isolated in-memory H2 database.

**Run All Tests:**

```bash
# Ensure you clean and install to recompile changed files
mvn clean test -Dtest=CalorieAiTestSuite
```

## 🏃‍♂️ Running Locally

1.  **Prerequisites**:
    - Java 17+
    - Maven
    - PostgreSQL (Local or Docker) running on port `5432`.
    - database named `calorieTracker`.

2.  **Firebase Configuration**:
    - **Required**: Place your `firebase-service-account.json` file in the project root.
    - Ensure `application.yml` points to it:
      ```yaml
      firebase:
        config:
          path: firebase-service-account.json
      ```

3.  **Build & Run**:
    ```bash
    mvn clean install
    mvn spring-boot:run
    ```
4.  The API will be available at `http://localhost:8080`.

## 📂 Project Structure

```
src/main/java/com/hoxlabs/calorieai
├── config/          # Firebase Config, Security Config
├── controller/      # REST Endpoints (Auth, Meal, Dashboard)
├── dto/             # Data Transfer Objects
├── entity/          # JPA Entities (User, MealLog, FoodItem)
├── exception/       # Global Error Handling
├── repository/      # Data Access Interfaces
├── security/        # FirebaseTokenFilter
├── service/         # Business Logic & AI Integration
└── utils/           # Utility Classes
```

---

_Built with ❤️ by HoxLabs_
