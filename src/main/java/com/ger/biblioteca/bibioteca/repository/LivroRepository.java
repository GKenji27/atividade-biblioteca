package com.ger.biblioteca.bibioteca.repository;

import com.ger.biblioteca.bibioteca.model.Livro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LivroRepository extends JpaRepository<Livro, Long> {

    Optional<Livro> findByIsbn(String isbn);

    boolean existsByAutor_Id(Long id);

    boolean existsByCategoria_Id(Long id);
}
