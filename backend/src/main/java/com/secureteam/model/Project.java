package com.secureteam.model;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "projects")
public class Project implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @Column(name = "project_id", unique = true, nullable = false)
    private String projectId; // e.g., "PRJ-SEC-01"

    public Project() {
    }

    public Project(String name, String projectId) {
        this.name = name;
        this.projectId = projectId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }
}
