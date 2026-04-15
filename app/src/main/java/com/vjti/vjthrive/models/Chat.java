package com.vjti.vjthrive.models;

import java.util.List;

public class Chat {
    private String chat_id;
    private String name;
    private String programme;
    private String dept;
    private String branch;
    private String gradYear;
    private String created_by;
    private List<String> members;
    private boolean isGroup;

    public Chat() {
        // Required for Firestore
    }

    public Chat(String chat_id, String name, String programme, String dept, String branch, String gradYear, String created_by, List<String> members, boolean isGroup) {
        this.chat_id = chat_id;
        this.name = name;
        this.programme = programme;
        this.dept = dept;
        this.branch = branch;
        this.gradYear = gradYear;
        this.created_by = created_by;
        this.members = members;
        this.isGroup = isGroup;
    }

    public String getChat_id() { return chat_id; }
    public void setChat_id(String chat_id) { this.chat_id = chat_id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getProgramme() { return programme; }
    public void setProgramme(String programme) { this.programme = programme; }

    public String getDept() { return dept; }
    public void setDept(String dept) { this.dept = dept; }

    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }

    public String getGradYear() { return gradYear; }
    public void setGradYear(String gradYear) { this.gradYear = gradYear; }

    public String getCreated_by() { return created_by; }
    public void setCreated_by(String created_by) { this.created_by = created_by; }

    public List<String> getMembers() { return members; }
    public void setMembers(List<String> members) { this.members = members; }

    public boolean isGroup() { return isGroup; }
    public void setGroup(boolean group) { isGroup = group; }
}
