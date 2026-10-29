package com.ger.biblioteca.bibioteca.repository;

import com.ger.biblioteca.bibioteca.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
}
