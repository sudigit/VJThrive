package com.vjti.vjthrive.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Centralized provider for all college structure data:
 * Programmes, Departments (per programme), Branches (per department),
 * Graduation years (dynamic).
 *
 * Update the maps here when the real VJTI structure is finalized.
 */
public class CollegeDataProvider {

    // ── Programmes ──────────────────────────────────────────────────────
    private static final String[] PROGRAMMES = {"Diploma", "B.Tech.", "M.Tech."};

    // ── Programme → Duration (years) ────────────────────────────────────
    private static final Map<String, Integer> PROGRAMME_DURATION = new HashMap<>();

    static {
        PROGRAMME_DURATION.put("Diploma", 3);
        PROGRAMME_DURATION.put("B.Tech.", 4);
        PROGRAMME_DURATION.put("M.Tech.", 2);
    }

    // ── Programme → Departments ─────────────────────────────────────────
    private static final Map<String, List<String>> PROGRAMME_DEPARTMENTS = new HashMap<>();

    static {
        List<String> commonDepartments = Arrays.asList(
                "Computer Engineering",
                "Information Technology",
                "Electrical Engineering",
                "Mechanical Engineering",
                "Civil Engineering",
                "Textile Technology",
                "Production Engineering"
        );

        // All programmes share the same department list (per user's clarification)
        PROGRAMME_DEPARTMENTS.put("Diploma", commonDepartments);
        PROGRAMME_DEPARTMENTS.put("B.Tech.", commonDepartments);
        PROGRAMME_DEPARTMENTS.put("M.Tech.", commonDepartments);
    }

    // ── Department → Branches ───────────────────────────────────────────
    // For now, branch = department name (user will provide real mapping later)
    private static final Map<String, List<String>> DEPARTMENT_BRANCHES = new HashMap<>();

    static {
        DEPARTMENT_BRANCHES.put("Computer Engineering",
                Arrays.asList("Computer Engineering"));
        DEPARTMENT_BRANCHES.put("Information Technology",
                Arrays.asList("Information Technology"));
        DEPARTMENT_BRANCHES.put("Electrical Engineering",
                Arrays.asList("Electrical Engineering"));
        DEPARTMENT_BRANCHES.put("Mechanical Engineering",
                Arrays.asList("Mechanical Engineering"));
        DEPARTMENT_BRANCHES.put("Civil Engineering",
                Arrays.asList("Civil Engineering"));
        DEPARTMENT_BRANCHES.put("Textile Technology",
                Arrays.asList("Textile Technology"));
        DEPARTMENT_BRANCHES.put("Production Engineering",
                Arrays.asList("Production Engineering"));
    }

    // ── Public API ──────────────────────────────────────────────────────

    public static String[] getProgrammes() {
        return PROGRAMMES;
    }

    /**
     * Returns departments available for a given programme.
     */
    public static List<String> getDepartments(String programme) {
        List<String> departments = PROGRAMME_DEPARTMENTS.get(programme);
        return departments != null ? departments : new ArrayList<>();
    }

    /**
     * Returns all unique departments across all programmes.
     * Used for faculty signup (faculty departments = same as student).
     */
    public static List<String> getAllDepartments() {
        // Since all programmes share the same list, just return the first one
        List<String> all = PROGRAMME_DEPARTMENTS.get("B.Tech.");
        return all != null ? all : new ArrayList<>();
    }

    /**
     * Returns branches available for a given department.
     */
    public static List<String> getBranches(String department) {
        List<String> branches = DEPARTMENT_BRANCHES.get(department);
        return branches != null ? branches : new ArrayList<>();
    }

    /**
     * Returns the duration in years for a given programme.
     */
    public static int getDuration(String programme) {
        Integer duration = PROGRAMME_DURATION.get(programme);
        return duration != null ? duration : 4; // default to 4
    }

    /**
     * Returns dynamic graduation year options for a given programme.
     * E.g., for B.Tech. (4 years) in 2026: [2027, 2028, 2029, 2030]
     */
    public static List<String> getGraduationYears(String programme) {
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        int duration = getDuration(programme);
        List<String> years = new ArrayList<>();
        for (int i = 1; i <= duration; i++) {
            years.add(String.valueOf(currentYear + i));
        }
        return years;
    }

}
