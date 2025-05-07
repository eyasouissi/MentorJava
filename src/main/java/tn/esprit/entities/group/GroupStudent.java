package tn.esprit.entities.group;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import tn.esprit.entities.project.Project;
import jakarta.persistence.*;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "groupstudent")
public class GroupStudent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nbr_members", nullable = true)
    private Integer memberCount = 0;

    @Column(name = "description", columnDefinition = "TEXT", nullable = false)
    @NotBlank(message = "Description cannot be empty!")
    @Size(min = 5, message = "Description must contain at least 5 characters!")
    private String description;

    @Column(name = "nom_group", length = 255, nullable = false)
    @NotBlank(message = "Group name cannot be empty!")
    private String name;

    @Column(name = "fichier_pdf", length = 255)
    private String pdfFile;

    @Column(name = "date_creation_group", nullable = false)
    private LocalDate creationDate;

    @Column(name = "image", length = 255)
    private String image;

    @Column(name = "date_meet")
    @FutureOrPresent(message = "Meeting date must be today or in the future!")
    private LocalDate meetingDate;

    // ========================== Constructors ==========================

    public GroupStudent() {
        this.creationDate = LocalDate.now();
    }

    public GroupStudent(String name, String description) {
        this();
        this.name = name;
        this.description = description;
    }

    // ========================== Getters and Setters ==========================

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


    @ElementCollection
    @CollectionTable(name = "group_project_names", joinColumns = @JoinColumn(name = "group_id"))
    @Column(name = "project_name")
    private List<String> projectNames = new ArrayList<>();
public void setProjectNames(List<String> projectNames) {
    this.projectNames = projectNames;
}
public List<String> getProjectNames() {
    return projectNames;
}


    // ========================== toString, equals, hashCode ==========================

    @Override
    public String toString() {
        return "GroupStudent{" +
                "id=" + id +
                ", memberCount=" + memberCount +
                ", description='" + (description != null ? description.substring(0, Math.min(description.length(), 30)) + "..." : "null") + '\'' +
                ", name='" + name + '\'' +
                ", pdfFile='" + pdfFile + '\'' +
                ", creationDate=" + creationDate +
                ", image='" + image + '\'' +
                ", meetingDate=" + meetingDate +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GroupStudent)) return false;
        GroupStudent that = (GroupStudent) o;
        return Objects.equals(id, that.id) &&
               Objects.equals(name, that.name) &&
               Objects.equals(creationDate, that.creationDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, creationDate);
    }

    // ========================== Builder Pattern (Optionnel) ==========================

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String name;
        private String description;
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

        public Builder meetingDate(LocalDate meetingDate) {
            this.meetingDate = meetingDate;
            return this;
        }

        public Builder image(String image) {
            this.image = image;
            return this;
        }

        public GroupStudent build() {
            GroupStudent group = new GroupStudent(name, description);
            group.setMeetingDate(meetingDate);
            group.setImage(image);
            return group;
        }
    }

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Project> projects = new ArrayList<>();

 public List<Project> getProjects() {
        return projects;
    }

    public void setProjects(List<Project> projects) {
        this.projects = projects;
    }

    public void addProject(Project project) {
        projects.add(project);
        project.setGroup(this); // Bi-directionnel
    }

    public void removeProject(Project project) {
        projects.remove(project);
        project.setGroup(null);
    }

}