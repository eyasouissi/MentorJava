package tn.esprit.entities;

import javafx.beans.property.*;
import java.time.LocalDateTime;

public class Evenement {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final IntegerProperty userId = new SimpleIntegerProperty();
    private final StringProperty titreE = new SimpleStringProperty();
    private final StringProperty descriptionE = new SimpleStringProperty();
    private final ObjectProperty<LocalDateTime> dateDebut = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDateTime> dateFin = new SimpleObjectProperty<>();
    private final StringProperty imageE = new SimpleStringProperty(); // Changé de imageUrl à imageE
    private final StringProperty lieu = new SimpleStringProperty();

    // Constructeurs
    public Evenement() {
    }

    public Evenement(String titreE, String descriptionE, LocalDateTime dateDebut,
                     LocalDateTime dateFin, String imageE, String lieu, int userId) {
        setTitreE(titreE);
        setDescriptionE(descriptionE);
        setDateDebut(dateDebut);
        setDateFin(dateFin);
        setImageE(imageE); // Changé de setImageUrl à setImageE
        setLieu(lieu);
        setUserId(userId);
    }

    // Getters & Setters
    public int getId() {
        return id.get();
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public int getUserId() {
        return userId.get();
    }

    public void setUserId(int userId) {
        this.userId.set(userId);
    }

    public String getTitreE() {
        return titreE.get();
    }

    public void setTitreE(String titreE) {
        this.titreE.set(titreE);
    }

    public String getDescriptionE() {
        return descriptionE.get();
    }

    public void setDescriptionE(String descriptionE) {
        this.descriptionE.set(descriptionE);
    }

    public LocalDateTime getDateDebut() {
        return dateDebut.get();
    }

    public void setDateDebut(LocalDateTime dateDebut) {
        this.dateDebut.set(dateDebut);
    }

    public LocalDateTime getDateFin() {
        return dateFin.get();
    }

    public void setDateFin(LocalDateTime dateFin) {
        this.dateFin.set(dateFin);
    }

    public String getImageE() { // Changé de getImageUrl à getImageE
        return imageE.get();
    }

    public void setImageE(String imageE) { // Changé de setImageUrl à setImageE
        this.imageE.set(imageE);
    }

    public String getLieu() {
        return lieu.get();
    }

    public void setLieu(String lieu) {
        this.lieu.set(lieu);
    }

    // Propriétés JavaFX
    public IntegerProperty idProperty() {
        return id;
    }

    public IntegerProperty userIdProperty() {
        return userId;
    }

    public StringProperty titreEProperty() {
        return titreE;
    }

    public StringProperty descriptionEProperty() {
        return descriptionE;
    }

    public ObjectProperty<LocalDateTime> dateDebutProperty() {
        return dateDebut;
    }

    public ObjectProperty<LocalDateTime> dateFinProperty() {
        return dateFin;
    }

    public StringProperty imageEProperty() { // Changé de imageUrlProperty à imageEProperty
        return imageE;
    }

    public StringProperty lieuProperty() {
        return lieu;
    }

    @Override
    public String toString() {
        return getTitreE() + " (" + getLieu() + ")";
    }
}