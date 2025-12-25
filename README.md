# CalorieAI - Intelligent Nutrition Tracker

CalorieAI is a robust Spring Boot backend application that leverages Artificial Intelligence to simplify calorie tracking. Users can describe their meals in natural language (e.g., "I had 2 rotis and a cup of dal"), and the system uses **Pollinations.ai** to analyze the text, extract nutritional data (Calories, Protein, Carbs, Fat), and log the meal.

## 🚀 Key Features

- **AI-Powered Food Analysis**: Uses Pollinations.ai (OpenAI compatible) to parse natural language meal descriptions into structured nutritional data.
- **User Authentication**: Secure JWT-based registration and login system.
- **Daily Dashboard**: Aggregates daily nutritional intake against user specific calorie goals.
- **Robust Testing**: Comprehensive test suite covering Unit, Integration, and Edge Case scenarios (~35 tests).
- **Secure**: Implements Spring Security 6 with stateless JWT authentication.

## 🛠️ Technology Stack

- **Framework**: Spring Boot 3.4
- **Language**: Java 17+
- **Database**: H2 (Dev/Test), PostgreSQL (Production ready)
- **AI Service**: Pollinations.ai (Free Tier)
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

### Dashboard

- `GET /api/dashboard/daily` - Get today's nutrition summary. (Requires Bearer Token)
  - Response: `{"totalCalories": 500, "goal": 2000, "remaining": 1500, ...}`

## 🧪 Testing

The project includes a unified test suite that runs all Unit, Integration, and validation tests.

**Run All Tests:**

```bash
mvn test -Dtest=CalorieAiTestSuite
```

**Test Coverage:**

- **Services**: `AuthService`, `MealService`, `PollinationsNutritionService`
- **Controllers**: `AuthController`, `MealController`, `DashboardController`
- **Repositories**: JPQL Query validation
- **Security**: JWT generation/validation, Filter logic
- **Validation**: DTO constraints (Null/Empty checks)

## 🏃‍♂️ Running Locally

1.  **Clone the repository**.
2.  **Build the project**:
    ```bash
    mvn clean install
    ```
3.  **Run the application**:
    ```bash
    mvn spring-boot:run
    ```
4.  The API will be available at `http://localhost:8080`.

## 📂 Project Structure

```
src/main/java/com/hoxlabs/calorieai
├── config/          # Security & App Config
├── controller/      # REST Endpoints
├── dto/             # Data Transfer Objects
├── entity/          # JPA Entities
├── exception/       # Global Error Handling
├── repository/      # Data Access Interfaces
├── security/        # JWT Filter & Util
└── service/         # Business Logic & AI Integration
```

---

_Built with ❤️ by HoxLabs_
