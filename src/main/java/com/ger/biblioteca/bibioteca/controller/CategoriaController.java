package com.ger.biblioteca.bibioteca.controller;

import com.ger.biblioteca.bibioteca.model.Categoria;
import com.ger.biblioteca.bibioteca.repository.CategoriaRepository;
import com.ger.biblioteca.bibioteca.repository.LivroRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/categorias")
public class CategoriaController {

    private final CategoriaRepository categoriaRepository;
    private final LivroRepository livroRepository;

    public CategoriaController(CategoriaRepository categoriaRepository, LivroRepository livroRepository) {
        this.categoriaRepository = categoriaRepository;
        this.livroRepository = livroRepository;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("categorias", categoriaRepository.findAll());
        return "categorias/lista";
    }

    @GetMapping("/novo")
    public String novaCategoria(Model model) {
        model.addAttribute("categoria", new Categoria());
        return "categorias/form";
    }

    @GetMapping("/{id}")
    public String editarCategoria(@PathVariable Long id, Model model) {
        Categoria categoria = categoriaRepository.findById(id).orElse(null);
        if (categoria == null) {
            return "redirect:/categorias";
        }
        model.addAttribute("categoria", categoria);
        return "categorias/form";
    }

    @PostMapping("/{id}")
    public String atualizarCategoria(@PathVariable Long id,
                                     @ModelAttribute Categoria categoria,
                                     RedirectAttributes redirectAttributes) {
        Categoria salvo = categoriaRepository.findById(id).orElse(null);
        if (salvo == null) {
            return "redirect:/categorias";
        }
        salvo.setNome(categoria.getNome());
        salvo.setDescricao(categoria.getDescricao());
        categoriaRepository.save(salvo);

        redirectAttributes.addFlashAttribute("mensagem",
                "Categoria \"" + salvo.getNome() + "\" atualizada com sucesso!");
        return "redirect:/categorias";
    }

    @PostMapping("/{id}/excluir")
    public String excluirCategoria(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Categoria categoria = categoriaRepository.findById(id).orElse(null);
        if (categoria == null) {
            return "redirect:/categorias";
        }

        if (livroRepository.existsByCategoria_Id(id)) {
            redirectAttributes.addFlashAttribute("erro",
                    "Não é possível excluir a categoria \"" + categoria.getNome() +
                            "\" porque existem livros nela classificados.");
            return "redirect:/categorias";
        }

        categoriaRepository.delete(categoria);
        redirectAttributes.addFlashAttribute("mensagem",
                "Categoria \"" + categoria.getNome() + "\" excluída.");
        return "redirect:/categorias";
    }

    @PostMapping
    public String cadastrarCategoria(@ModelAttribute Categoria categoria, RedirectAttributes redirectAttributes) {
        categoriaRepository.save(categoria);
        redirectAttributes.addFlashAttribute("mensagem",
                "Categoria \"" + categoria.getNome() + "\" cadastrada com sucesso!");
        return "redirect:/categorias";
    }
}
