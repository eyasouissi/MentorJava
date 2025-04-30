package tn.esprit.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Objects;

@Entity
@Table(name = "groupstudent")
public class GroupStudent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nbr_members", nullable = false)
    private Integer memberCount = 0;

    @Column(columnDefinition = "TEXT")
    @NotBlank(message = "Description cannot be empty!")
    @Size(min = 5, message = "Description must contain at least 5 characters!")
    private String description;

    @Column(name = "nom_group", length = 255, nullable = false)
    @NotBlank(message = "Group name cannot be empty!")
    private String name;

    @ManyToMany
    @JoinTable(
            name = "group_student_members",
            joinColumns = @JoinColumn(name = "group_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> members = new HashSet<>();

    @Column(name = "fichier_pdf", length = 255, nullable = true)
    private String pdfFile;

    @Column(name = "date_creation_group", nullable = false)
    private LocalDate creationDate;

    @Column(length = 255, nullable = true)
    private String image;

    @Column(name = "date_meet", nullable = true)
    @FutureOrPresent(message = "Please, enter a valid date!")
    private LocalDate meetingDate;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Project> projects = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private User createdBy;

    // Constructors
    public GroupStudent() {
        this.creationDate = LocalDate.now();
    }

    public GroupStudent(String name, String description, User createdBy) {
        this();
        this.name = name;
        this.description = description;
        this.createdBy = createdBy;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(Integer memberCount) {
        this.memberCount = memberCount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<User> getMembers() {
        return members;
    }

    public void setMembers(Set<User> members) {
        this.members = members;
        this.memberCount = members.size();
    }

    public String getPdfFile() {
        return pdfFile;
    }

    public void setPdfFile(String pdfFile) {
        this.pdfFile = pdfFile;
    }

    public LocalDate getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDate creationDate) {
        this.creationDate = creationDate;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public LocalDate getMeetingDate() {
        return meetingDate;
    }

    public void setMeetingDate(LocalDate meetingDate) {
        this.meetingDate = meetingDate;
    }

    public List<Project> getProjects() {
        return projects;
    }

    public void setProjects(List<Project> projects) {
        this.projects = projects;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    // Relationship management methods
    public void addMember(User member) {
        if (!members.contains(member)) {
            members.add(member);
            member.getGroups().add(this);
            memberCount = members.size();
        }
    }

    public void removeMember(User member) {
        if (members.remove(member)) {
            member.getGroups().remove(this);
            memberCount = members.size();
        }
    }

    public void addProject(Project project) {
        if (!projects.contains(project)) {
            projects.add(project);
            project.setGroup(this);
        }
    }

    public void removeProject(Project project) {
        if (projects.remove(project)) {
            project.setGroup(null);
        }
    }

    // toString() method
    @Override
    public String toString() {
        return "GroupStudent{" +
                "id=" + id +
                ", memberCount=" + memberCount +
                ", description='" + (description != null ? description.substring(0, Math.min(description.length(), 30)) + "..." : "null") + '\'' +
                ", name='" + name + '\'' +
                ", membersCount=" + members.size() +
                ", pdfFile='" + pdfFile + '\'' +
                ", creationDate=" + creationDate +
                ", image='" + image + '\'' +
                ", meetingDate=" + meetingDate +
                ", projectsCount=" + projects.size() +
                ", createdById=" + (createdBy != null ? createdBy.getId() : null) +
                '}';
    }

    // equals() method
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GroupStudent that = (GroupStudent) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(name, that.name) &&
                Objects.equals(creationDate, that.creationDate);
    }

    // hashCode() method
    @Override
    public int hashCode() {
        return Objects.hash(id, name, creationDate);
    }

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String name;
        private String description;
        private User createdBy;
        private LocalDate meetingDate;
        private String image;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder createdBy(User createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        public Builder meetingDate(LocalDate meetingDate) {
            this.meetingDate = meetingDate;
            return this;
        }

        public Builder image(String image) {
            this.image = image;
            return this;
        }

        public GroupStudent build() {
            GroupStudent group = new GroupStudent(name, description, createdBy);
            group.setMeetingDate(meetingDate);
            group.setImage(image);
            return group;
        }
    }
}