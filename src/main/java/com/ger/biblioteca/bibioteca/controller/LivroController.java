package com.ger.biblioteca.bibioteca.controller;

import com.ger.biblioteca.bibioteca.model.Livro;
import com.ger.biblioteca.bibioteca.repository.AutorRepository;
import com.ger.biblioteca.bibioteca.repository.CategoriaRepository;
import com.ger.biblioteca.bibioteca.repository.EmprestimoRepository;
import com.ger.biblioteca.bibioteca.repository.LivroRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.transaction.annotation.Transactional;

@Controller
@RequestMapping("/livros")
public class LivroController {

    private final LivroRepository livroRepository;
    private final AutorRepository autorRepository;
    private final CategoriaRepository categoriaRepository;
    private final EmprestimoRepository emprestimoRepository;

    public LivroController(LivroRepository livroRepository,
                           AutorRepository autorRepository,
                           CategoriaRepository categoriaRepository,
                           EmprestimoRepository emprestimoRepository) {
        this.livroRepository = livroRepository;
        this.autorRepository = autorRepository;
        this.categoriaRepository = categoriaRepository;
        this.emprestimoRepository = emprestimoRepository;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("livros", livroRepository.findAll());
        return "livros/lista";
    }

    @GetMapping("/novo")
    public String novoLivro(Model model) {
        model.addAttribute("livro", new Livro());
        model.addAttribute("autores", autorRepository.findAll());
        model.addAttribute("categorias", categoriaRepository.findAll());
        return "livros/form";
    }

    @GetMapping("/{id}")
    public String editarLivro(@PathVariable Long id, Model model) {
        Livro livro = livroRepository.findById(id).orElse(null);
        if (livro == null) {
            return "redirect:/livros";
        }
        model.addAttribute("livro", livro);
        model.addAttribute("autores", autorRepository.findAll());
        model.addAttribute("categorias", categoriaRepository.findAll());
        return "livros/form";
    }

    @PostMapping("/{id}")
    public String atualizarLivro(@PathVariable Long id,
                                 @ModelAttribute Livro livro,
                                 @RequestParam(required = false) Long autorId,
                                 @RequestParam(required = false) Long categoriaId,
                                 RedirectAttributes redirectAttributes) {
        Livro salvo = livroRepository.findById(id).orElse(null);
        if (salvo == null) {
            return "redirect:/livros";
        }

        salvo.setTitulo(livro.getTitulo());
        salvo.setIsbn(livro.getIsbn());
        if (autorId != null) {
            salvo.setAutor(autorRepository.findById(autorId).orElse(salvo.getAutor()));
        }
        if (categoriaId != null) {
            salvo.setCategoria(categoriaRepository.findById(categoriaId).orElse(salvo.getCategoria()));
        }
        livroRepository.save(salvo);

        redirectAttributes.addFlashAttribute("mensagem",
                "Livro \"" + salvo.getTitulo() + "\" atualizado com sucesso!");
        return "redirect:/livros";
    }

    @PostMapping("/{id}/excluir")
    @Transactional
    public String excluirLivro(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Livro livro = livroRepository.findById(id).orElse(null);
        if (livro == null) {
            return "redirect:/livros";
        }

        if (!livro.isDisponivel()
                || emprestimoRepository.existsByLivro_IdAndDevolvidoFalse(livro.getId())) {
            redirectAttributes.addFlashAttribute("erro",
                    "Não é possível excluir o livro \"" + livro.getTitulo() +
                            "\" enquanto estiver com empréstimo em aberto.");
            return "redirect:/livros";
        }

        emprestimoRepository.deleteByLivro_Id(livro.getId());
        livroRepository.delete(livro);

        redirectAttributes.addFlashAttribute("mensagem",
                "Livro \"" + livro.getTitulo() + "\" excluído.");
        return "redirect:/livros";
    }

    @PostMapping
    public String cadastrarLivro(@ModelAttribute Livro livro,
                                 @RequestParam(required = false) Long autorId,
                                 @RequestParam(required = false) Long categoriaId,
                                 RedirectAttributes redirectAttributes) {
        if (autorId == null || categoriaId == null) {
            redirectAttributes.addFlashAttribute("erro",
                    "Para cadastrar um livro, selecione um autor e uma categoria.");
            return "redirect:/livros/novo";
        }

        livro.setAutor(autorRepository.findById(autorId).orElse(null));
        livro.setCategoria(categoriaRepository.findById(categoriaId).orElse(null));
        livroRepository.save(livro);

        redirectAttributes.addFlashAttribute("mensagem",
                "Livro \"" + livro.getTitulo() + "\" cadastrado com sucesso!");
        return "redirect:/livros";
    }
}
