package com.secureteam.auth;

import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class AbacPolicyEngine {

    /**
     * Evaluates access based on Subject, Resource, and Environment attributes.
     * Core logic for SecureTeam Access: Temporary & Context-Aware.
     */
    public boolean evaluate(Map<String, Object> subject, Map<String, Object> resource, Map<String, Object> env) {

        // 1. Temporal Check: Account Expiration (Mandatory for SecureTeam Access)
        Long accessExpiry = (Long) subject.get("access_expiry");
        if (accessExpiry != null && System.currentTimeMillis() > accessExpiry) {
            System.out.println("[DENY] Access Expired for subject: " + subject.get("subject"));
            return false;
        }

        // 2. Context-Aware: Device Fingerprinting
        String expectedFingerprint = (String) subject.get("device_id");
        String currentFingerprint = (String) env.get("device_id");
        if (expectedFingerprint != null && !expectedFingerprint.equals(currentFingerprint)) {
            System.out.println("[DENY] Device Fingerprint Mismatch!");
            return false;
        }

        // 3. Project-Based Access Control
        String requiredProject = (String) resource.get("project_id");
        List<String> authorizedProjects = (List<String>) subject.get("projects");

        if (requiredProject != null && (authorizedProjects == null || !authorizedProjects.contains(requiredProject))) {
            System.out.println("[DENY] Subject not authorized for project: " + requiredProject);
            return false;
        }

        // 4. Time-of-Day Constraints (JIT Logic)
        String department = (String) subject.get("department");
        String resourceType = (String) resource.get("type");

        if ("external_collaborator".equals(department) || "engineering".equals(department)) {
            LocalTime now = LocalTime.now();
            LocalTime start = LocalTime.parse("09:00");
            LocalTime end = LocalTime.parse("18:00");

            if (now.isBefore(start) || now.isAfter(end)) {
                System.out.println("[DENY] Access outside of authorized hours.");
                return false;
            }
        }

        // Default Allow if all security checks pass and it's a known department
        return department != null;
    }
}
