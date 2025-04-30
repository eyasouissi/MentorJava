package tn.esprit.services;


import java.util.List;

public interface
IServices<T>  {
    void ajouter(T t);
    void modifier(T t);
    void supprimer(int id);
    T getOne(T t);
    List<T> getAll();
}
