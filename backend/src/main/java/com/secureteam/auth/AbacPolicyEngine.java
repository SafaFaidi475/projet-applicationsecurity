package com.secureteam.auth;

import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@ApplicationScoped
public class AbacPolicyEngine {

    private static final Logger LOGGER = Logger.getLogger(AbacPolicyEngine.class.getName());

    /**
     * Evaluates access based on Subject, Resource, and Environment attributes.
     * Core logic for SecureTeam Access: Temporary & Context-Aware.
     */
    public boolean evaluate(Map<String, Object> subject, Map<String, Object> resource, Map<String, Object> env) {

        // 1. Temporal Check: Account Expiration (Mandatory for SecureTeam Access)
        Long accessExpiry = (Long) subject.get("access_expiry");
        if (accessExpiry != null && System.currentTimeMillis() > accessExpiry) {
            LOGGER.warning("[DENY] Access Expired for subject: " + subject.get("subject"));
            return false;
        }

        // 2. Context-Aware: Device Fingerprinting
        String expectedFingerprint = (String) subject.get("device_id");
        String currentFingerprint = (String) env.get("device_id");
        if (expectedFingerprint != null && !expectedFingerprint.equals(currentFingerprint)) {
            LOGGER.warning("[DENY] Device Fingerprint Mismatch!");
            return false;
        }

        // 3. Project-Based Access Control
        String requiredProject = (String) resource.get("project_id");
        Object authorizedProjectsObj = subject.get("projects");
        List<String> authorizedProjects = new ArrayList<>();
        if (authorizedProjectsObj instanceof List) {
            for (Object obj : (List<?>) authorizedProjectsObj) {
                if (obj instanceof String) {
                    authorizedProjects.add((String) obj);
                }
            }
        }

        if (requiredProject != null
                && (authorizedProjects.isEmpty() || !authorizedProjects.contains(requiredProject))) {
            LOGGER.warning("[DENY] Subject not authorized for project: " + requiredProject);
            return false;
        }

        // 4. Time-of-Day Constraints (JIT Logic)
        String department = (String) subject.get("department");

        if ("external_collaborator".equals(department) || "engineering".equals(department)) {
            LocalTime now = LocalTime.now();
            LocalTime start = LocalTime.parse("09:00");
            LocalTime end = LocalTime.parse("18:00");

            if (now.isBefore(start) || now.isAfter(end)) {
                LOGGER.warning("[DENY] Access outside of authorized hours.");
                return false;
            }
        }

        // Default Allow if all security checks pass and it's a known department
        return department != null;
    }
}
