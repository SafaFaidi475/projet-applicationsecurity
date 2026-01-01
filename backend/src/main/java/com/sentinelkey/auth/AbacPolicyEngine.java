package com.sentinelkey.auth;

import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class AbacPolicyEngine {

    /**
     * Evaluates access based on Subject, Resource, and Environment attributes.
     */
    public boolean evaluate(Map<String, Object> subject, Map<String, Object> resource, Map<String, Object> env) {
        // Example Policy: Engineering department can check "audit_logs" only between
        // 09:00 and 18:00

        String department = (String) subject.get("department");
        String resourceType = (String) resource.get("type");

        if ("engineering".equals(department) && "audit_logs".equals(resourceType)) {
            // Check Time Constraint
            LocalTime now = LocalTime.now();
            LocalTime start = LocalTime.parse("09:00");
            LocalTime end = LocalTime.parse("18:00");

            if (now.isBefore(start) || now.isAfter(end)) {
                return false; // Access Denied (Time of Day)
            }

            // In a real implementation: Check Location, Clearance Level, etc.
            return true;
        }

        // Default Deny (Zero Trust)
        return false;
    }
}
