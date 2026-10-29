package com.ger.biblioteca.bibioteca.repository;

import com.ger.biblioteca.bibioteca.model.Autor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AutorRepository extends JpaRepository<Autor, Long> {

    java.util.Optional<Autor> findByNome(String nome);
}
