package tn.esprit.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "evenement")
public class Evenement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "titre_e", length = 255)
    @NotBlank(message = "Le titre est obligatoire.")
    @Size(min = 3, message = "Le titre doit contenir au moins 3 caractères.")
    private String titreE;

    @Column(name = "description_e", columnDefinition = "TEXT")
    @NotBlank(message = "La description est obligatoire.")
    @Size(min = 10, message = "La description doit contenir au moins 10 caractères.")
    private String descriptionE;

    @Column(name = "date_debut")
    @NotNull(message = "La date de début est obligatoire.")
    private LocalDateTime dateDebut;

    @Column(name = "date_fin")
    @NotNull(message = "La date de fin est obligatoire.")
    @Future(message = "La date de fin doit être dans le futur.")
    private LocalDateTime dateFin;

    @Column(name = "image_e", length = 255)
    private String imageE;

    @ManyToOne
    @JoinColumn(name = "annonce_id", nullable = false)
    private Annonce annonce;

    // Constructors
    public Evenement() {
        // Default constructor
    }

    public Evenement(String titreE, String descriptionE, LocalDateTime dateDebut, LocalDateTime dateFin) {
        this.titreE = titreE;
        this.descriptionE = descriptionE;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
    }

    public Evenement(String titreE, String descriptionE, LocalDateTime dateDebut,
                     LocalDateTime dateFin, String imageE, Annonce annonce) {
        this(titreE, descriptionE, dateDebut, dateFin);
        this.imageE = imageE;
        this.annonce = annonce;
    }

    // toString() method
    @Override
    public String toString() {
        return "Evenement{" +
                "id=" + id +
                ", titreE='" + titreE + '\'' +
                ", descriptionE='" + (descriptionE != null ?
                descriptionE.substring(0, Math.min(descriptionE.length(), 30)) + "..." : "null") + '\'' +
                ", dateDebut=" + dateDebut +
                ", dateFin=" + dateFin +
                ", imageE='" + imageE + '\'' +
                ", annonceId=" + (annonce != null ? annonce.getId() : null) +
                '}';
    }

    // equals() method
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Evenement evenement = (Evenement) o;
        return Objects.equals(id, evenement.id) &&
                Objects.equals(titreE, evenement.titreE) &&
                Objects.equals(dateDebut, evenement.dateDebut);
    }

    // hashCode() method
    @Override
    public int hashCode() {
        return Objects.hash(id, titreE, dateDebut);
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitreE() {
        return titreE;
    }

    public void setTitreE(String titreE) {
        this.titreE = titreE;
    }

    public String getDescriptionE() {
        return descriptionE;
    }

    public void setDescriptionE(String descriptionE) {
        this.descriptionE = descriptionE;
    }

    public LocalDateTime getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDateTime dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDateTime getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDateTime dateFin) {
        this.dateFin = dateFin;
    }

    public String getImageE() {
        return imageE;
    }

    public void setImageE(String imageE) {
        this.imageE = imageE;
    }

    public Annonce getAnnonce() {
        return annonce;
    }

    public void setAnnonce(Annonce annonce) {
        this.annonce = annonce;
    }
}