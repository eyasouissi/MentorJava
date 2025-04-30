package tn.esprit.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "paiement")
public class Paiement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_paiement")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user", referencedColumnName = "id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_offre", referencedColumnName = "id_offre", nullable = false)
    private Offre offre;

    @Column(name = "date_paiement", nullable = false)
    private LocalDateTime paymentDate;

    // Constructors
    public Paiement() {
        this.paymentDate = LocalDateTime.now();
    }

    public Paiement(User user, Offre offre) {
        this();
        this.user = user;
        this.offre = offre;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Offre getOffre() {
        return offre;
    }

    public void setOffre(Offre offre) {
        this.offre = offre;
    }

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDateTime paymentDate) {
        this.paymentDate = paymentDate;
    }

    // toString() method
    @Override
    public String toString() {
        return "Paiement{" +
                "id=" + id +
                ", userId=" + (user != null ? user.getId() : null) +
                ", offreId=" + (offre != null ? offre.getId() : null) +
                ", paymentDate=" + paymentDate +
                '}';
    }

    // equals() method
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Paiement paiement = (Paiement) o;
        return Objects.equals(id, paiement.id) &&
                Objects.equals(paymentDate, paiement.paymentDate);
    }

    // hashCode() method
    @Override
    public int hashCode() {
        return Objects.hash(id, paymentDate);
    }

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private User user;
        private Offre offre;
        private LocalDateTime paymentDate;

        public Builder user(User user) {
            this.user = user;
            return this;
        }

        public Builder offre(Offre offre) {
            this.offre = offre;
            return this;
        }

        public Builder paymentDate(LocalDateTime paymentDate) {
            this.paymentDate = paymentDate;
            return this;
        }

        public Paiement build() {
            Paiement paiement = new Paiement(user, offre);
            if (paymentDate != null) {
                paiement.setPaymentDate(paymentDate);
            }
            return paiement;
        }
    }
}
