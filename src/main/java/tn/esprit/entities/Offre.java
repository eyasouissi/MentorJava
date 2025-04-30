package tn.esprit.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "offre")
public class Offre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_offre")
    private Long id;

    @Column(name = "nom_offre", length = 255, nullable = false)
    @NotBlank(message = "The name cannot be empty.")
    @Size(min = 3, max = 50, message = "The name must be between {min} and {max} characters.")
    private String name = "";

    @Column(name = "image_offre", length = 255, nullable = true)
    private String imagePath;

    @Transient
    @NotNull(message = "Please upload an image.")
    private MultipartFile imageFile;

    @Column(nullable = false)
    @NotNull(message = "The price cannot be empty.")
    @DecimalMin(value = "0.01", message = "The price must be greater than 0.")
    private Double price;

    @Column(name = "date_debut", nullable = false)
    @NotNull(message = "Please provide a start date.")
    @FutureOrPresent(message = "The start date must be today or in the future.")
    private LocalDateTime startDate;

    @Column(name = "date_fin", nullable = true)
    @NotNull(message = "Please provide an end date.")
    @Future(message = "The end date must be in the future.")
    private LocalDate endDate;

    @Column(length = 255, nullable = false)
    @NotBlank(message = "Description cannot be empty.")
    @Size(min = 5, max = 100, message = "Description must be between {min} and {max} characters.")
    private String description;

    @OneToMany(mappedBy = "offre", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Paiement> paiements = new ArrayList<>();

    // Constructors
    public Offre() {
        this.startDate = LocalDateTime.now();
    }

    public Offre(String name, Double price, String description) {
        this();
        this.name = name;
        this.price = price;
        this.description = description;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public MultipartFile getImageFile() {
        return imageFile;
    }

    public void setImageFile(MultipartFile imageFile) {
        this.imageFile = imageFile;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Paiement> getPaiements() {
        return paiements;
    }

    public void setPaiements(List<Paiement> paiements) {
        this.paiements = paiements;
    }

    // Business methods
    public void addPaiement(Paiement paiement) {
        if (!paiements.contains(paiement)) {
            paiements.add(paiement);
            paiement.setOffre(this);
        }
    }

    public void removePaiement(Paiement paiement) {
        if (paiements.remove(paiement)) {
            paiement.setOffre(null);
        }
    }

    // toString() method
    @Override
    public String toString() {
        return "Offre{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", imagePath='" + imagePath + '\'' +
                ", price=" + price +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", description='" + description + '\'' +
                ", paiementsCount=" + paiements.size() +
                '}';
    }

    // equals() method
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Offre offre = (Offre) o;
        return Objects.equals(id, offre.id) &&
                Objects.equals(name, offre.name) &&
                Objects.equals(startDate, offre.startDate);
    }

    // hashCode() method
    @Override
    public int hashCode() {
        return Objects.hash(id, name, startDate);
    }

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String name;
        private Double price;
        private String description;
        private LocalDateTime startDate;
        private LocalDate endDate;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder price(Double price) {
            this.price = price;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder startDate(LocalDateTime startDate) {
            this.startDate = startDate;
            return this;
        }

        public Builder endDate(LocalDate endDate) {
            this.endDate = endDate;
            return this;
        }

        public Offre build() {
            Offre offre = new Offre(name, price, description);
            offre.setStartDate(startDate != null ? startDate : LocalDateTime.now());
            offre.setEndDate(endDate);
            return offre;
        }
    }
}
