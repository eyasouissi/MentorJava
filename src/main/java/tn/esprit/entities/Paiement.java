package tn.esprit.entities;

import java.time.LocalDateTime;

public class Paiement {
    private Long id;
    private User user;
    private Offre offre;
    private LocalDateTime paymentDate;

    public Paiement() {
        this.paymentDate = LocalDateTime.now();
    }

    public Paiement(User user, Offre offre) {
        this();
        this.user = user;
        this.offre = offre;
    }

    public Paiement(User user, Offre offre, LocalDateTime paymentDate) {
        this.user = user;
        this.offre = offre;
        this.paymentDate = paymentDate;
    }

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

    @Override
    public String toString() {
        return "Paiement{" +
                "id=" + id +
                ", user=" + user +
                ", offre=" + offre +
                ", paymentDate=" + paymentDate +
                '}';
    }
}