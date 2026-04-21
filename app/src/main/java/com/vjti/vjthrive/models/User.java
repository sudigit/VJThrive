package com.vjti.vjthrive.models;

import java.util.List;

public class User {
    private String uid;
    private String name;
    private String email;
    private String rollNo;
    private String programme;   // Diploma, B.Tech., M.Tech.
    private String department;  // Computer Engineering, IT, etc.
    private String branch;
    private String mdmSubject;
    private int graduationYear;
    private String role;        // student, faculty, admin
    private boolean isSecretary;

    // Default constructor required for Firestore deserialization
    public User() {}

    // Full constructor (student)
    public User(String name, String email, String rollNo, String programme,
                String department, String branch, int graduationYear,
                String mdmSubject, String role, boolean isSecretary) {
        this.name = name;
        this.email = email;
        this.rollNo = rollNo;
        this.programme = programme;
        this.department = department;
        this.branch = branch;
        this.graduationYear = graduationYear;
        this.mdmSubject = mdmSubject;
        this.role = role;
        this.isSecretary = isSecretary;
    }

    // Faculty constructor (name, email, department, role)
    public User(String name, String email, String department, String role, boolean isSecretary) {
        this.name = name;
        this.email = email;
        this.department = department;
        this.role = role;
        this.isSecretary = isSecretary;
    }

    // Admin constructor (name, email, role)
    public User(String name, String email, String role, boolean isSecretary) {
        this.name = name;
        this.email = email;
        this.role = role;
        this.isSecretary = isSecretary;
    }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRollNo() { return rollNo; }
    public void setRollNo(String rollNo) { this.rollNo = rollNo; }

    public String getProgramme() { return programme; }
    public void setProgramme(String programme) { this.programme = programme; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }

    public int getGraduationYear() { return graduationYear; }
    public void setGraduationYear(int graduationYear) { this.graduationYear = graduationYear; }

    public String getMdmSubject() { return mdmSubject; }
    public void setMdmSubject(String mdmSubject) { this.mdmSubject = mdmSubject; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isSecretary() { return isSecretary; }
    public void setSecretary(boolean secretary) { isSecretary = secretary; }
}
