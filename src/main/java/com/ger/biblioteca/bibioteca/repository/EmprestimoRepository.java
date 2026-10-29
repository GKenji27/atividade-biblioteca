package com.ger.biblioteca.bibioteca.repository;

import com.ger.biblioteca.bibioteca.model.Emprestimo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmprestimoRepository extends JpaRepository<Emprestimo, Long> {

    List<Emprestimo> findByDevolvidoFalse();

    boolean existsByLivro_IdAndDevolvidoFalse(Long livroId);

    boolean existsByUsuario_Id(Long id);

    boolean existsByUsuario_IdAndDevolvidoFalse(Long id);

    void deleteByLivro_Id(Long livroId);

    void deleteByUsuario_Id(Long id);
}
