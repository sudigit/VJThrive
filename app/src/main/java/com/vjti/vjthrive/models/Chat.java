package com.vjti.vjthrive.models;

import java.util.List;

public class Chat {
    private String chat_id;
    private String name;
    private List<String> programmes;
    private List<String> depts;
    private List<String> branches;
    private List<String> gradYears;
    private String created_by;
    private List<String> members;
    private boolean isGroup;

    public Chat() {
        // Required for Firestore
    }

    public Chat(String chat_id, String name, List<String> programmes, List<String> depts,
                List<String> branches, List<String> gradYears, String created_by,
                List<String> members, boolean isGroup) {
        this.chat_id = chat_id;
        this.name = name;
        this.programmes = programmes;
        this.depts = depts;
        this.branches = branches;
        this.gradYears = gradYears;
        this.created_by = created_by;
        this.members = members;
        this.isGroup = isGroup;
    }

    public String getChat_id() { return chat_id; }
    public void setChat_id(String chat_id) { this.chat_id = chat_id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<String> getProgrammes() { return programmes; }
    public void setProgrammes(List<String> programmes) { this.programmes = programmes; }

    public List<String> getDepts() { return depts; }
    public void setDepts(List<String> depts) { this.depts = depts; }

    public List<String> getBranches() { return branches; }
    public void setBranches(List<String> branches) { this.branches = branches; }

    public List<String> getGradYears() { return gradYears; }
    public void setGradYears(List<String> gradYears) { this.gradYears = gradYears; }

    public String getCreated_by() { return created_by; }
    public void setCreated_by(String created_by) { this.created_by = created_by; }

    public List<String> getMembers() { return members; }
    public void setMembers(List<String> members) { this.members = members; }

    public boolean isGroup() { return isGroup; }
    public void setGroup(boolean group) { isGroup = group; }
}
