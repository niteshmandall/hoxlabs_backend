package com.hoxlabs.calorieai.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class ImageService {

    @Value("${file.upload-dir}")
    private String uploadDirStr;

    private Path rootLocation;

    @jakarta.annotation.PostConstruct
    public void init() {
        try {
            this.rootLocation = Paths.get(uploadDirStr);
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory", e);
        }
    }

    public String saveProfileImage(MultipartFile file, Long userId) throws IOException {
        // Create user-specific directory: uploads/users/{id}
        Path userDir = rootLocation.resolve("users").resolve(String.valueOf(userId));
        if (!Files.exists(userDir)) {
            Files.createDirectories(userDir);
        }

        // Use a fixed filename "profile.jpg" (or preserve extension if preferred, but fixed is easier for overwrite)
        // For better compatibility, let's try to preserve extension or default to jpg
        String originalFilename = file.getOriginalFilename();
        String extension = "jpg";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
        }
        
        String filename = "profile." + extension;
        Path destinationFile = userDir.resolve(filename);

        // Copy file, overwriting existing
        Files.copy(file.getInputStream(), destinationFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

        // Return relative URL
        return "/uploads/users/" + userId + "/" + filename;
    }
}
