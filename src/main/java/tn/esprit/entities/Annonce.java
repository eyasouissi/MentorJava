package tn.esprit.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "annonce")
public class Annonce {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "image_a", length = 255)
    private String imageA;

    @Column(name = "titre_a", length = 255)
    @NotBlank(message = "Le titre est obligatoire.")
    @Size(min = 3, message = "Le titre doit contenir au moins {min} caractères.")
    private String titreA;

    @Column(name = "description_a", columnDefinition = "TEXT")
    @NotBlank(message = "La description est obligatoire.")
    @Size(min = 10, message = "La description doit contenir au moins {min} caractères.")
    private String descriptionA;

    @Column(name = "date_a")
    private LocalDateTime dateA;

    @ManyToOne
    @JoinColumn(name = "evenement_id", nullable = false)
    private Evenement evenement;

    // Constructors
    public Annonce() {
        this.dateA = LocalDateTime.now();
    }

    public Annonce(String titreA, String descriptionA) {
        this();
        this.titreA = titreA;
        this.descriptionA = descriptionA;
    }

    public Annonce(String imageA, String titreA, String descriptionA, Evenement evenement) {
        this(titreA, descriptionA);
        this.imageA = imageA;
        this.evenement = evenement;
    }

    // toString() method
    @Override
    public String toString() {
        return "Annonce{" +
                "id=" + id +
                ", imageA='" + imageA + '\'' +
                ", titreA='" + titreA + '\'' +
                ", descriptionA='" + (descriptionA != null ?
                descriptionA.substring(0, Math.min(descriptionA.length(), 30)) + "..." : "null") + '\'' +
                ", dateA=" + dateA +
                ", evenementId=" + (evenement != null ? evenement.getId() : null) +
                '}';
    }

    // equals() method
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Annonce annonce = (Annonce) o;
        return Objects.equals(id, annonce.id) &&
                Objects.equals(titreA, annonce.titreA) &&
                Objects.equals(dateA, annonce.dateA);
    }

    // hashCode() method
    @Override
    public int hashCode() {
        return Objects.hash(id, titreA, dateA);
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getImageA() {
        return imageA;
    }

    public void setImageA(String imageA) {
        this.imageA = imageA;
    }

    public String getTitreA() {
        return titreA;
    }

    public void setTitreA(String titreA) {
        this.titreA = titreA;
    }

    public String getDescriptionA() {
        return descriptionA;
    }

    public void setDescriptionA(String descriptionA) {
        this.descriptionA = descriptionA;
    }

    public LocalDateTime getDateA() {
        return dateA;
    }

    public void setDateA(LocalDateTime dateA) {
        this.dateA = dateA;
    }

    public Evenement getEvenement() {
        return evenement;
    }

    public void setEvenement(Evenement evenement) {
        this.evenement = evenement;
    }
}