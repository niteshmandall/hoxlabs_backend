# CalorieAI - Intelligent Nutrition Tracker

CalorieAI is a production-grade Spring Boot backend application that leverages Artificial Intelligence to simplify calorie tracking. Users can describe their meals in natural language (e.g., "I had 2 rotis and a cup of dal"), and the system uses **Pollinations.ai** to analyze the text, extract accurate nutritional data (Calories, Protein, Carbs, Fat) using strict Indian-standardized schemas, and log the meal.

## 🚀 Key Features

- **Production-Grade AI Analysis**:
  - Expert "Certified Nutritionist" System Prompts.
  - Strict JSON Schema Validation.
  - Specialized Rule Sets: Indian portion estimations, conservative oil usage.
  - Edge Case Handling: Smart defaults for vague inputs, fast food, and homemade dishes.
- **Meal History**: View complete chat history of past logged meals and AI responses.
- **User Authentication**: Secure JWT-based registration and login system.
- **Daily Dashboard**: Aggregates daily nutritional intake against user specific calorie goals.
- **Robust Testing**: Comprehensive test suite covering Unit, Integration, and Edge Case scenarios (>35 tests).
- **Secure**: Implements Spring Security 6 with stateless JWT authentication.

## 🛠️ Technology Stack

- **Framework**: Spring Boot 3.4
- **Language**: Java 17+
- **Database**: PostgreSQL (Primary), H2 (Test Isolation)
- **AI Service**: Pollinations.ai (OpenAI compatible API)
- **Security**: Spring Security, JJWT
- **Build Tool**: Maven

## 🔌 API Documentation

### Authentication

- `POST /api/auth/register` - Register a new user.
  - Body: `{"email": "...", "password": "...", "calorieGoal": 2000}`
- `POST /api/auth/login` - Authenticate and receive JWT.
  - Body: `{"email": "...", "password": "..."}`

### Meals

- `POST /api/meals/log` - Log a meal using natural language. (Requires Bearer Token)
  - Body: `{"text": "I ate a banana and 2 eggs", "mealType": "BREAKFAST"}`
  - Response: Detailed breakdown of food items and total macros.
- `GET /api/meals/history` - **NEW**: Get full history of logged meals to display in chat.

### Dashboard

- `GET /api/dashboard/daily` - Get today's nutrition summary. (Requires Bearer Token)
  - Response: `{"totalCalories": 500, "goal": 2000, "remaining": 1500, ...}`

## 🧪 Testing

The project includes a unified test suite that runs all Unit, Integration, and validation tests. Tests are configured to run on an isolated in-memory H2 database.

**Run All Tests:**

```bash
mvn test -Dtest=CalorieAiTestSuite
```

## 🏃‍♂️ Running Locally

1.  **Prerequisites**:

    - Java 17+
    - Maven
    - PostgreSQL (Local or Docker) running on port `5432`.
    - Create a database named `calorieTracker`.

2.  **Configuration**:

    - The application is pre-configured to connect to `jdbc:postgresql://localhost:5432/calorieTracker` with user `postgres` and password `root`.
    - Modify `src/main/resources/application.yml` if your credentials differ.

3.  **Build & Run**:
    ```bash
    mvn clean install
    mvn spring-boot:run
    ```
4.  The API will be available at `http://localhost:8080`.

## 📂 Project Structure

```
src/main/java/com/hoxlabs/calorieai
├── config/          # Security & App Config
├── controller/      # REST Endpoints (Auth, Meal, Dashboard)
├── dto/             # Data Transfer Objects
├── entity/          # JPA Entities (User, MealLog, FoodItem)
├── exception/       # Global Error Handling
├── repository/      # Data Access Interfaces
├── security/        # JWT Filter & Util
└── service/         # Business Logic & AI Integration
```

---

_Built with ❤️ by HoxLabs_
