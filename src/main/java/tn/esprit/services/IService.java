package tn.esprit.services;

import java.util.List;

public interface IService<T> {
    // Ajouter un objet de type T
    void ajouter(T t);

    // Modifier un objet de type T
    void modifier(T t);

    // Supprimer un objet par son identifiant (ici Long)
    void supprimer(Long id);

    // Récupérer un objet par son identifiant
    T getOne(Long id);

    // Récupérer tous les objets de type T
    List<T> getAll();
}
