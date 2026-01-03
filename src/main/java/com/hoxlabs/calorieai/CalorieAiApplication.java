package com.hoxlabs.calorieai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CalorieAiApplication {

	public static void main(String[] args) {
		loadEnv();
		SpringApplication.run(CalorieAiApplication.class, args);
	}

	private static void loadEnv() {
		try {
			java.nio.file.Path envPath = java.nio.file.Paths.get(".env");
			if (java.nio.file.Files.exists(envPath)) {
				java.util.List<String> lines = java.nio.file.Files.readAllLines(envPath);
				for (String line : lines) {
					if (!line.trim().isEmpty() && !line.startsWith("#") && line.contains("=")) {
						String[] parts = line.split("=", 2);
						System.setProperty(parts[0].trim(), parts[1].trim());
					}
				}
				System.out.println("Loaded environment variables from .env");
			}
		} catch (Exception e) {
			System.err.println("Failed to load .env file: " + e.getMessage());
		}
	}



}
