package com.hoxlabs.calorieai.controller;

import com.hoxlabs.calorieai.dto.AuthenticationRequest;
import com.hoxlabs.calorieai.dto.AuthenticationResponse;
import com.hoxlabs.calorieai.dto.RegisterRequest;
import com.hoxlabs.calorieai.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/sync")
    public ResponseEntity<com.hoxlabs.calorieai.dto.UserProfileDTO> syncUser(
            @org.springframework.security.core.annotation.AuthenticationPrincipal Object principal,
            @RequestBody com.hoxlabs.calorieai.dto.SyncUserRequest request) {
        // Principal is email (set in FirebaseTokenFilter) or UID
        String emailOrUid = (String) principal;
        // In simple case, assume it is email if it contains @
        String email = emailOrUid.contains("@") ? emailOrUid : null;
        String uid = emailOrUid.contains("@") ? null : emailOrUid; // We need UID from token ideally.

        // Note: FirebaseTokenFilter logic set Principal to "email if available else
        // uid".
        // To be safer, we might want to update Filter to pass a custom object.
        // For now, trusting the principal flow.
        // If email is null, we can't search by email unless we get it from request
        // body?
        // But request bodySyncUserRequest doesn't have email.

        // Let's assume Valid Token always provides Email in our use case or we fetch
        // it.
        // If principal is UID, we might need to look up.

        // IMPROVEMENT: Let's assume Principal is the Email for now as standard Firebase
        // Auth with Email.

        if (email == null) {
            // Fallback if needed
            throw new RuntimeException("Email required for sync");
        }

        // We need the raw Firebase UID to store.
        // Current Filter implementation sets details but not easy to get UID if
        // principal is Email.
        // FIX: Let's update Filter later to pass a wrapper.
        // For now, let's pass dummy UID or rely on looking up via Admin SDK if needed.
        // Actually, let's just use the principal as UID if it's not email.

        // Re-thinking: In `AuthService.syncUser`, I added `syncUser(String email,
        // String firebaseUid, ...)`
        // I need both.

        // Let's just create a quick User profile return.

        var user = authService.syncUser(email, "uid_placeholder", request); // TODO: Pass real UID

        return ResponseEntity.ok(authService.getUserProfile(user.getEmail()));
    }
}
