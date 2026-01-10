# Backend API Update Requirements

The Android client now collects additional user profile information during the signup process to provide personalized nutrition insights.

We need to update the backend API to accept and store these new fields.

## 0. Update Authentication Response (CRITICAL)

The `POST /api/auth/register` and `POST /api/auth/login` endpoints currently only return a `token`.

**Requirement:**
Please include the user's unique `id` (and optionally the full profile) in the response validation.

**Current Response:**

```json
{ "token": "..." }
```

**Required Response:**

```json
{
  "token": "...",
  "user": {
    "id": 123,
    "email": "user@example.com",
    "name": "Alex",
    ...
  }
}
```

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

## 3. Update `Meals` Endpoints

We need to support deleting meals to allow users to "Undo" or "Edit" their logs.

**Requirement:**
Implement `DELETE /api/meals/{id}`.

- **Path Parameter**: `id` (The ID of the meal to delete).
- **Response**: `200 OK` (Empty body or success message).
- **Behavior**: Permanently remove the meal log and its associated food items from the database.

## 4. (Optional) Update `GET /api/user/profile`

If not already present, ensure the user profile response includes these fields so the app can display them after login on other devices.

## 5. Update User Profile (NEW)

The app now allows users to edit their profile, including setting custom macro goals and a profile photo.

**Requirement:**
Implement `PUT /api/user/profile` (or `PATCH`).

**New Fields to Support:**

- `profilePhotoUrl`: String (URL to image, or handle file upload separately)
- `dailyCalorieGoal`: Integer (Update existing field)
- `proteinGoal`: Integer (Target grams of protein)
- `carbsGoal`: Integer (Target grams of carbs)
- `fatGoal`: Integer (Target grams of fat)

**Example JSON Payload:**

```json
{
  "dailyCalorieGoal": 2300,
  "proteinGoal": 150,
  "carbsGoal": 250,
  "fatGoal": 80,
  "profilePhotoUrl": "https://example.com/photo.jpg"
}
```

**Notes:**

- If `proteinGoal` et al. are not set, the backend should ideally calculate default values based on the `dailyCalorieGoal` or return 0/null to let the client decide.
- The `AuthRepository.updateUser` client-side function expects to call this endpoint.

## 6. Fix Refresh Token Transaction (CRITICAL)

The refresh token endpoints are throwing `InvalidDataAccessApiUsageException: No EntityManager with actual transaction available` during token rotation/deletion.

**Requirement:**
Ensure that any service method that deletes or removes the `RefreshToken` entity (e.g. `refreshTokenService.verifyExpiration` or `deleteByUserId`) is annotated with `@Transactional`.

**Log Reference:**
`org.springframework.dao.InvalidDataAccessApiUsageException: No EntityManager with actual transaction available for current thread - cannot reliably process 'remove' call`

## 7. Fix Expired Token Response Code (IMPORTANT)

The backend currently returns `403 Forbidden` when an access token is expired or invalid. Ideally, it should return `401 Unauthorized`.

**Problem:**
OkHttp's `Authenticator` (used for auto-refreshing tokens) is ONLY triggered on `401` responses. It ignores `403`. This causes the app to fail silently or crash when the token expires instead of refreshing it.

**Workaround Applied (Client-Side):**
The Android client has been patched to intercept `403` responses and rewrite the code to `401` to force the refresh logic to trigger.

**Requirement:**
Update the Spring Security configuration handling JWT validation to return `HttpStatus.UNAUTHORIZED (401)` when the token is expired or signature check fails, rather than `HttpStatus.FORBIDDEN (403)`.

## 8. Implement Profile Image Upload

The client expects a dedicated endpoint to upload profile pictures.

**Endpoint:** `POST /api/user/profile-image`
**Content-Type:** `multipart/form-data`
**Parameter:** `image` (File)

**Response:**

```json
{
  "url": "localstorage/users/123/profile.jpg"
}
```

**Behavior:**

1. Accept the file upload.
2. Search storage (local/S3).
3. Update the `User` entity's `profilePhotoUrl` with the new URL.
4. Return the URL in a JSON object.

### Technical Implementation Guide (Spring Boot)

To resolve the `404 Not Found` error, add the following logic to your backend:

**1. Controller (`UserController.java`):**

```java
@PostMapping(value = "/profile-image", consumes = "multipart/form-data")
public ResponseEntity<Map<String, String>> uploadProfileImage(
        @RequestParam("image") MultipartFile image,
        Principal principal
) {
    // 1. Save file to disk/S3
    String imageUrl = userService.uploadProfileImage(principal.getName(), image);

    // 2. Return URL
    return ResponseEntity.ok(Collections.singletonMap("url", imageUrl));
}
```

**2. Service (`UserService.java`):**

```java
public String uploadProfileImage(String email, MultipartFile file) {
    // A. Create uploads directory if missing
    // B. Save file with unique name (e.g. UUID + extension)
    // C. Update User entity: user.setProfilePhotoUrl(fileUrl)
    // D. Return the public URL
}
```

**3. Static Resource Configuration:**
If saving locally, ensure `WebMvcConfigurer` maps `/uploads/**` to your storage directory so the Android app can download the image.

## 9. Fix Login "Duplicate Key" Error (CRITICAL)

**Issue:**
Login fails with `500 Internal Server Error` (or `400 Bad Request`) containing:
`duplicate key value violates unique constraint ... Key (user_id)=(...) already exists.`

**Cause:**
The `RefreshToken` table allows only **one** token per user (Unique Constraint). The backend attempts to creates a _new_ token row on every login without deleting the old one.

**Fix:**
In your `RefreshTokenService.createRefreshToken(userId)` method:

1.  Add `@Transactional` annotation.
2.  **Delete** the existing token for the user before creating a new one.

```java
@Transactional
public RefreshToken createRefreshToken(Long userId) {
    // 1. Delete old token (Fixes Duplicate Key Error)
    refreshTokenRepository.deleteByUserId(userId);

    // 2. Create new token
    RefreshToken refreshToken = new RefreshToken();
    refreshToken.setUser(userRepository.findById(userId).get());
    refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
    refreshToken.setToken(UUID.randomUUID().toString());

    return refreshTokenRepository.save(refreshToken);
}
```

## 10. Support Date-Specific Meal Logging

The app now facilitates logging meals for past dates via a date selector.

**Requirement:**
Update the meal logging endpoints (both natural language and structured) to accept an optional `date` parameter.

**Endpoint:** `POST /api/meals/log` (or equivalent AI parsing endpoint)

**Request Body Update:**
Add `date` field (ISO 8601 Date String: `YYYY-MM-DD`).

```json
{
  "text": "I had 2 eggs and toast",
  "date": "2025-10-25" // Optional. Defaults to NOW if parsed.
}
```

**Behavior:**

- If `date` is provided, the logged meal and its nutritional data must be associated with that specific calendar date in the database.
- This ensures daily summaries for past dates are accurate.

## 11. Implement AI Coach / Advice Mode

The app introduced a specific "Coach Mode" separate from "Meal Logging".

**Requirement:**
Implement a dedicated endpoint (or a mode flag) for conversational AI that does _not_ aggressively try to log food, but instead provides health advice.

**Endpoint:** `POST /api/chat/advice`

**Request Body:**

```json
{
  "message": "How can I get more protein without eating meat?"
}
```

**Response:**

```json
{
  "message": "You can try lentils, chickpeas, quinoa, or tofu...",
  "is_logging_action": false
}
```

**Context Awareness (Optional but Recommended):**

- The backend should ideally inject the user's recent nutrition stats (e.g., "You've been low on protein this week...") into the LLM context for personalized advice.
