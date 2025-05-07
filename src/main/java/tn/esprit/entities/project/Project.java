package tn.esprit.entities.project;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import tn.esprit.entities.group.GroupStudent;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "project")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "titre", length = 255, nullable = false)
    @NotBlank(message = "Project titre cannot be empty!")
    private String titre;

    @Column(name = "description_project", columnDefinition = "TEXT", nullable = false)
    @NotBlank(message = "Description cannot be empty!")
    @Size(min = 5, message = "Description must contain at least 5 characters!")
    private String descriptionProject;

    @Column(name = "difficulte", nullable = false)
    private Integer difficulte;

    @Column(name = "creation_date", nullable = false)
    private LocalDate creationDate;

    @Column(name = "deadline")
    private LocalDate deadline;

    @ManyToOne
    @JoinColumn(name = "group_id", nullable = false)
    private GroupStudent group;

    @Column(name = "pdf_file", length = 255)
    private String pdfFile;

    // Ajout d'un champ pour l'image
    @Column(name = "image", length = 255)
    private String image;

    // Constructeurs
    public Project() {
        this.creationDate = LocalDate.now();
    }

    public Project(String titre, String descriptionProject, Integer difficulte) {
        this();
        this.titre = titre;
        this.descriptionProject = descriptionProject;
        this.difficulte = difficulte;
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDescriptionProject() {
        return descriptionProject;
    }

    public void setDescriptionProject(String descriptionProject) {
        this.descriptionProject = descriptionProject;
    }

    public Integer getDifficulte() {
        return difficulte;
    }

    public void setDifficulte(Integer difficulte) {
        this.difficulte = difficulte;
    }

    public LocalDate getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDate creationDate) {
        this.creationDate = creationDate;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public GroupStudent getGroup() {
        return group;
    }

    public void setGroup(GroupStudent group) {
        this.group = group;
    }

    public String getPdfFile() {
        return pdfFile;
    }

    public void setPdfFile(String pdfFile) {
        this.pdfFile = pdfFile;
    }

    // Getters et Setters pour l'image
    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }


    
    // toString, equals, hashCode
    @Override
    public String toString() {
        return "Project{" +
                "id=" + id +
                ", titre='" + titre + '\'' +
                ", descriptionProject='" + (descriptionProject != null ? descriptionProject.substring(0, Math.min(descriptionProject.length(), 30)) + "..." : "null") + '\'' +
                ", difficulte=" + difficulte +
                ", creationDate=" + creationDate +
                ", deadline=" + deadline +
                ", pdfFile='" + pdfFile + '\'' +
                ", image='" + image + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Project)) return false;
        Project project = (Project) o;
        return Objects.equals(id, project.id) &&
                Objects.equals(titre, project.titre) &&
                Objects.equals(creationDate, project.creationDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, titre, creationDate);
    }


}
