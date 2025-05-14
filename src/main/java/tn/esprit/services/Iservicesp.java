package tn.esprit.services;

import tn.esprit.entities.Offre;

import java.sql.SQLException;
import java.util.List;

public interface Iservicesp <T>{
    void ajouter(T t) throws SQLException;
    void supprimer(T t) throws SQLException ;
    public void modifier(int id, T t) throws SQLException;
    List<T> recuperer() throws SQLException;
}