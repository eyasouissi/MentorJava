package tn.esprit.entities;

import javafx.beans.property.*;
import java.time.LocalDateTime;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
public class Annonce {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty titreA = new SimpleStringProperty();
    private final StringProperty descriptionA = new SimpleStringProperty();
    private final ObjectProperty<LocalDateTime> dateA = new SimpleObjectProperty<>();
    private final StringProperty imageUrl = new SimpleStringProperty();
    private final IntegerProperty evenementId = new SimpleIntegerProperty();

    // Constructeurs
    public Annonce() {
    }

    public Annonce(String titreA, String descriptionA, LocalDateTime dateA,
                   String imageUrl, int evenementId) {
        setTitreA(titreA);
        setDescriptionA(descriptionA);
        setDateA(dateA);
        setImageUrl(imageUrl);
        setEvenementId(evenementId);
    }

    // Getters & Setters standards
    public int getId() {
        return id.get();
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public String getTitreA() {
        return titreA.get();
    }

    public void setTitreA(String titreA) {
        this.titreA.set(titreA);
    }

    public String getDescriptionA() {
        return descriptionA.get();
    }

    public void setDescriptionA(String descriptionA) {
        this.descriptionA.set(descriptionA);
    }

    public LocalDateTime getDateA() {
        return dateA.get();
    }

    public void setDateA(LocalDateTime dateA) {
        this.dateA.set(dateA);
    }

    public String getImageUrl() {
        return imageUrl.get();
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl.set(imageUrl);
    }

    public int getEvenementId() {
        return evenementId.get();
    }

    public void setEvenementId(int evenementId) {
        this.evenementId.set(evenementId);
    }

    // Propriétés JavaFX (pour la liaison avec TableView)
    public IntegerProperty idProperty() {
        return id;
    }

    public StringProperty titreAProperty() {
        return titreA;
    }

    public StringProperty descriptionAProperty() {
        return descriptionA;
    }

    public ObjectProperty<LocalDateTime> dateAProperty() {
        return dateA;
    }

    public StringProperty imageUrlProperty() {
        return imageUrl;
    }

    public IntegerProperty evenementIdProperty() {
        return evenementId;
    }

    @Override
    public String toString() {
        return getTitreA() + " - " + getDateA().toString();
    }
}