package com.ger.biblioteca.bibioteca.repository;

import com.ger.biblioteca.bibioteca.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
}
