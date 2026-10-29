package com.ger.biblioteca.bibioteca.controller;

import com.ger.biblioteca.bibioteca.model.Autor;
import com.ger.biblioteca.bibioteca.repository.AutorRepository;
import com.ger.biblioteca.bibioteca.repository.LivroRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/autores")
public class AutorController {

    private final AutorRepository autorRepository;
    private final LivroRepository livroRepository;

    public AutorController(AutorRepository autorRepository, LivroRepository livroRepository) {
        this.autorRepository = autorRepository;
        this.livroRepository = livroRepository;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("autores", autorRepository.findAll());
        return "autores/lista";
    }

    @GetMapping("/novo")
    public String novoAutor(Model model) {
        model.addAttribute("autor", new Autor());
        return "autores/form";
    }

    @GetMapping("/{id}")
    public String editarAutor(@PathVariable Long id, Model model) {
        Autor autor = autorRepository.findById(id).orElse(null);
        if (autor == null) {
            return "redirect:/autores";
        }
        model.addAttribute("autor", autor);
        return "autores/form";
    }

    @PostMapping("/{id}")
    public String atualizarAutor(@PathVariable Long id,
                                 @ModelAttribute Autor autor,
                                 RedirectAttributes redirectAttributes) {
        Autor salvo = autorRepository.findById(id).orElse(null);
        if (salvo == null) {
            return "redirect:/autores";
        }
        salvo.setNome(autor.getNome());
        salvo.setNacionalidade(autor.getNacionalidade());
        salvo.setObrasPublicadas(autor.getObrasPublicadas());
        autorRepository.save(salvo);

        redirectAttributes.addFlashAttribute("mensagem",
                "Autor \"" + salvo.getNome() + "\" atualizado com sucesso!");
        return "redirect:/autores";
    }

    @PostMapping("/{id}/excluir")
    public String excluirAutor(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Autor autor = autorRepository.findById(id).orElse(null);
        if (autor == null) {
            return "redirect:/autores";
        }

        if (livroRepository.existsByAutor_Id(id)) {
            redirectAttributes.addFlashAttribute("erro",
                    "Não é possível excluir o autor \"" + autor.getNome() +
                            "\" porque existem livros dele no acervo.");
            return "redirect:/autores";
        }

        autorRepository.delete(autor);
        redirectAttributes.addFlashAttribute("mensagem",
                "Autor \"" + autor.getNome() + "\" excluído.");
        return "redirect:/autores";
    }

    @PostMapping
    public String cadastrarAutor(@ModelAttribute Autor autor, RedirectAttributes redirectAttributes) {
        autorRepository.save(autor);
        redirectAttributes.addFlashAttribute("mensagem",
                "Autor \"" + autor.getNome() + "\" cadastrado com sucesso!");
        return "redirect:/autores";
    }
}
