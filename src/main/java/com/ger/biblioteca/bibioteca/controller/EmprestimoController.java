package com.ger.biblioteca.bibioteca.controller;

import com.ger.biblioteca.bibioteca.model.Emprestimo;
import com.ger.biblioteca.bibioteca.model.Livro;
import com.ger.biblioteca.bibioteca.repository.EmprestimoRepository;
import com.ger.biblioteca.bibioteca.repository.LivroRepository;
import com.ger.biblioteca.bibioteca.repository.UsuarioRepository;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/emprestimos")
public class EmprestimoController {

    private final EmprestimoRepository emprestimoRepository;
    private final LivroRepository livroRepository;
    private final UsuarioRepository usuarioRepository;

    public EmprestimoController(EmprestimoRepository emprestimoRepository,
                                LivroRepository livroRepository,
                                UsuarioRepository usuarioRepository) {
        this.emprestimoRepository = emprestimoRepository;
        this.livroRepository = livroRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("emprestimos", emprestimoRepository.findAll());
        return "emprestimos/lista";
    }

    @GetMapping("/novo")
    public String novoEmprestimo(Model model) {
        model.addAttribute("emprestimo", new Emprestimo());
        model.addAttribute("livros", livroRepository.findAll());
        model.addAttribute("usuarios", usuarioRepository.findAll());
        return "emprestimos/form";
    }

    @PostMapping
    public String cadastrarEmprestimo(@ModelAttribute Emprestimo emprestimo,
                                      @RequestParam(required = false) Long livroId,
                                      @RequestParam(required = false) Long usuarioId,
                                      RedirectAttributes redirectAttributes) {
        if (livroId == null || usuarioId == null) {
            redirectAttributes.addFlashAttribute("erro",
                    "Para registar um empréstimo, selecione um livro e um utilizador.");
            return "redirect:/emprestimos/novo";
        }

        if (emprestimoRepository.existsByLivro_IdAndDevolvidoFalse(livroId)) {
            redirectAttributes.addFlashAttribute("erro",
                    "Este livro já tem um empréstimo em aberto. Devolva-o primeiro.");
            return "redirect:/emprestimos/novo";
        }

        emprestimo.setLivro(livroRepository.findById(livroId).orElse(null));
        emprestimo.setUsuario(usuarioRepository.findById(usuarioId).orElse(null));
        emprestimoRepository.save(emprestimo);

        Livro livro = emprestimo.getLivro();
        if (livro != null) {
            livro.setDisponivel(false);
            livroRepository.save(livro);
        }

        redirectAttributes.addFlashAttribute("mensagem",
                "Empréstimo do livro \"" + (emprestimo.getLivro() != null ? emprestimo.getLivro().getTitulo() : "") + "\" registado!");
        return "redirect:/emprestimos";
    }

    @PostMapping("/{id}/devolver")
    @Transactional
    public String devolver(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Emprestimo emprestimo = emprestimoRepository.findById(id).orElse(null);
        if (emprestimo == null || emprestimo.isDevolvido()) {
            return "redirect:/emprestimos";
        }

        emprestimo.setDevolvido(true);
        emprestimoRepository.save(emprestimo);

        Livro livro = emprestimo.getLivro();
        if (livro != null) {
            livro.setDisponivel(true);
            livroRepository.save(livro);
        }

        redirectAttributes.addFlashAttribute("mensagem",
                "Devolução do livro \"" + (livro != null ? livro.getTitulo() : "") + "\" registada!");
        return "redirect:/emprestimos";
    }
}