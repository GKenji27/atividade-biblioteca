package com.ger.biblioteca.bibioteca.controller;

import com.ger.biblioteca.bibioteca.model.Usuario;
import com.ger.biblioteca.bibioteca.repository.EmprestimoRepository;
import com.ger.biblioteca.bibioteca.repository.UsuarioRepository;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;
    private final EmprestimoRepository emprestimoRepository;

    public UsuarioController(UsuarioRepository usuarioRepository, EmprestimoRepository emprestimoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.emprestimoRepository = emprestimoRepository;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("usuarios", usuarioRepository.findAll());
        return "usuarios/lista";
    }

    @GetMapping("/novo")
    public String novoUsuario(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "usuarios/form";
    }

    @GetMapping("/{id}")
    public String editarUsuario(@PathVariable Long id, Model model) {
        Usuario usuario = usuarioRepository.findById(id).orElse(null);
        if (usuario == null) {
            return "redirect:/usuarios";
        }
        model.addAttribute("usuario", usuario);
        return "usuarios/form";
    }

    @PostMapping("/{id}")
    public String atualizarUsuario(@PathVariable Long id,
                                   @ModelAttribute Usuario usuario,
                                   RedirectAttributes redirectAttributes) {
        Usuario salvo = usuarioRepository.findById(id).orElse(null);
        if (salvo == null) {
            return "redirect:/usuarios";
        }
        salvo.setNome(usuario.getNome());
        salvo.setEmail(usuario.getEmail());
        salvo.setMatricula(usuario.getMatricula());
        usuarioRepository.save(salvo);

        redirectAttributes.addFlashAttribute("mensagem",
                "Utilizador \"" + salvo.getNome() + "\" atualizado com sucesso!");
        return "redirect:/usuarios";
    }

    @PostMapping("/{id}/excluir")
    @Transactional
    public String excluirUsuario(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Usuario usuario = usuarioRepository.findById(id).orElse(null);
        if (usuario == null) {
            return "redirect:/usuarios";
        }

        if (emprestimoRepository.existsByUsuario_IdAndDevolvidoFalse(id)) {
            redirectAttributes.addFlashAttribute("erro",
                    "Não é possível excluir o utilizador \"" + usuario.getNome() +
                            "\" porque tem empréstimos em aberto. Devolva primeiro.");
            return "redirect:/usuarios";
        }

        emprestimoRepository.deleteByUsuario_Id(id);
        usuarioRepository.delete(usuario);

        redirectAttributes.addFlashAttribute("mensagem",
                "Utilizador \"" + usuario.getNome() + "\" excluído.");
        return "redirect:/usuarios";
    }

    @PostMapping
    public String cadastrarUsuario(@ModelAttribute Usuario usuario, RedirectAttributes redirectAttributes) {
        usuarioRepository.save(usuario);
        redirectAttributes.addFlashAttribute("mensagem",
                "Utilizador \"" + usuario.getNome() + "\" cadastrado com sucesso!");
        return "redirect:/usuarios";
    }
}