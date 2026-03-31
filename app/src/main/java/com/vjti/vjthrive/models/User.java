package com.vjti.vjthrive.models;

public class User {
    private String name;
    private String email;
    private String rollNo;
    private String programme; // B.Tech, M.Tech, Diploma
    private String department; // CE, IT, ME, etc.
    private String branch;
    private int graduationYear;
    private String role; // student, faculty, club_secretary

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String name, String email, String rollNo, String programme, String department, String branch, int graduationYear, String role) {
        this.name = name;
        this.email = email;
        this.rollNo = rollNo;
        this.programme = programme;
        this.department = department;
        this.branch = branch;
        this.graduationYear = graduationYear;
        this.role = role;
    }

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
    
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
