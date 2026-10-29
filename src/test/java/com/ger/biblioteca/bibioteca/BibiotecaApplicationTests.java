package com.ger.biblioteca.bibioteca;

import com.ger.biblioteca.bibioteca.model.Autor;
import com.ger.biblioteca.bibioteca.model.Categoria;
import com.ger.biblioteca.bibioteca.model.Livro;
import com.ger.biblioteca.bibioteca.model.Usuario;
import com.ger.biblioteca.bibioteca.repository.AutorRepository;
import com.ger.biblioteca.bibioteca.repository.CategoriaRepository;
import com.ger.biblioteca.bibioteca.repository.EmprestimoRepository;
import com.ger.biblioteca.bibioteca.repository.LivroRepository;
import com.ger.biblioteca.bibioteca.repository.UsuarioRepository;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
class BibiotecaApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private AutorRepository autorRepository;

	@Autowired
	private CategoriaRepository categoriaRepository;

	@Autowired
	private LivroRepository livroRepository;

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private EmprestimoRepository emprestimoRepository;

	@Test
	void contextLoads() {
	}

	@Test
	void paginaInicialEstaDisponivel() throws Exception {
		mockMvc.perform(get("/"))
				.andExpect(status().isOk())
				.andExpect(view().name("inicio"));
	}

	@Test
	void cadaModuloMostraSuaLista() throws Exception {
		Map<String, String> modulos = Map.of(
				"/livros", "livros/lista",
				"/autores", "autores/lista",
				"/categorias", "categorias/lista",
				"/usuarios", "usuarios/lista",
				"/emprestimos", "emprestimos/lista");

		for (Map.Entry<String, String> modulo : modulos.entrySet()) {
			mockMvc.perform(get(modulo.getKey()))
					.andExpect(status().isOk())
					.andExpect(view().name(modulo.getValue()));
		}
	}

	@Test
	void formularioDeLivroEstaDisponivel() throws Exception {
		mockMvc.perform(get("/livros/novo"))
				.andExpect(status().isOk())
				.andExpect(view().name("livros/form"))
				.andExpect(model().attributeExists("livro", "autores", "categorias"));
	}

	@Test
	void livroCadastradoFicaSalvoNoBancoEApareceNaLista() throws Exception {
		Autor autor = autorRepository.save(new Autor("Machado de Assis", "Brasil", 9));
		Categoria categoria = categoriaRepository.save(new Categoria("Romance", "Narrativa romantica"));

		mockMvc.perform(post("/livros")
						.param("titulo", "Dom Casmurro")
						.param("autorId", autor.getId().toString())
						.param("categoriaId", categoria.getId().toString())
						.param("isbn", "9788535902778"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/livros"));

		Livro livroSalvo = livroRepository.findByIsbn("9788535902778").orElse(null);

		assertThat(livroSalvo).isNotNull();
		assertThat(livroSalvo.getTitulo()).isEqualTo("Dom Casmurro");
		assertThat(livroSalvo.getAutor().getNome()).isEqualTo("Machado de Assis");
		assertThat(livroSalvo.getCategoria().getNome()).isEqualTo("Romance");
		assertThat(livroSalvo.isDisponivel()).isTrue();

		mockMvc.perform(get("/livros"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("Dom Casmurro")))
				.andExpect(content().string(Matchers.containsString("Machado de Assis")));
	}

	@Test
	void cadastrarAutorRedirecionaParaALista() throws Exception {
		mockMvc.perform(post("/autores")
						.param("nome", "Eca de Queiroz")
						.param("nacionalidade", "Portugal")
						.param("obrasPublicadas", "4"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/autores"));

		assertThat(autorRepository.findByNome("Eca de Queiroz")).isPresent();
	}

	@Test
	void emprestimoRegistadoIndisponibilizaOLivroEApareceNaLista() throws Exception {
		Autor autor = autorRepository.save(new Autor("Jose Simoes Alves", "Portugal", 3));
		Categoria categoria = categoriaRepository.save(new Categoria("Ficcao", "Historias ficticias"));
		Usuario usuario = usuarioRepository.save(new Usuario("Ana Silva", "ana@email.com", "M2026-001"));
		Livro livro = livroRepository.save(new Livro("A Cidade e as Serras", autor, "9789722012345", categoria, true));

		mockMvc.perform(post("/emprestimos")
						.param("livroId", livro.getId().toString())
						.param("usuarioId", usuario.getId().toString()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/emprestimos"));

		Livro livroAposEmprestimo = livroRepository.findById(livro.getId()).orElse(null);
		assertThat(livroAposEmprestimo).isNotNull();
		assertThat(livroAposEmprestimo.isDisponivel()).isFalse();

		mockMvc.perform(get("/emprestimos"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("A Cidade e as Serras")))
				.andExpect(content().string(Matchers.containsString("Em aberto")));
	}

	@Test
	void editarLivroAtualizaNoBanco() throws Exception {
		Autor autor = autorRepository.save(new Autor("Autor Editavel", "Brasil", 1));
		Categoria categoria = categoriaRepository.save(new Categoria("Categoria Editavel", "desc"));
		Livro livro = livroRepository.save(new Livro("Titulo Original", autor, "999999", categoria, true));

		mockMvc.perform(get("/livros/" + livro.getId()))
				.andExpect(status().isOk())
				.andExpect(view().name("livros/form"))
				.andExpect(model().attribute("livro", Matchers.hasProperty("titulo", Matchers.is("Titulo Original"))))
				.andExpect(model().attribute("autores", Matchers.notNullValue()))
				.andExpect(model().attribute("categorias", Matchers.notNullValue()));

		Autor novoAutor = autorRepository.save(new Autor("Novo Autor", "Portugal", 2));
		mockMvc.perform(post("/livros/" + livro.getId())
						.param("titulo", "Titulo Editado")
						.param("autorId", novoAutor.getId().toString())
						.param("categoriaId", categoria.getId().toString())
						.param("isbn", "999999"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/livros"));

		Livro atualizado = livroRepository.findById(livro.getId()).orElse(null);
		assertThat(atualizado).isNotNull();
		assertThat(atualizado.getTitulo()).isEqualTo("Titulo Editado");
		assertThat(atualizado.getAutor().getNome()).isEqualTo("Novo Autor");
	}

	@Test
	void naoPermiteExcluirLivroComEmprestimoEmAberto_eDevolerLiberaExclusao() throws Exception {
		Autor autor = autorRepository.save(new Autor("Autor Exclusao", "Brasil", 1));
		Categoria categoria = categoriaRepository.save(new Categoria("Categoria Exclusao", "desc"));
		Usuario usuario = usuarioRepository.save(new Usuario("Usuario Exclusao", "ex@e.com", "M1"));
		Livro livro = livroRepository.save(new Livro("Livro Emprestado", autor, "111111", categoria, true));

		mockMvc.perform(post("/emprestimos")
						.param("livroId", livro.getId().toString())
						.param("usuarioId", usuario.getId().toString()))
				.andExpect(status().is3xxRedirection());

		mockMvc.perform(post("/livros/" + livro.getId() + "/excluir"))
				.andExpect(status().is3xxRedirection());
		assertThat(livroRepository.findById(livro.getId())).isPresent();

		com.ger.biblioteca.bibioteca.model.Emprestimo emprestimo = emprestimoRepository
				.findByDevolvidoFalse().stream()
				.filter(e -> e.getLivro().getId().equals(livro.getId()))
				.findFirst().orElseThrow();

		mockMvc.perform(post("/emprestimos/" + emprestimo.getId() + "/devolver"))
				.andExpect(status().is3xxRedirection());

		assertThat(emprestimoRepository.findById(emprestimo.getId()).orElseThrow().isDevolvido()).isTrue();
		assertThat(livroRepository.findById(livro.getId()).orElseThrow().isDisponivel()).isTrue();

		mockMvc.perform(post("/livros/" + livro.getId() + "/excluir"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/livros"));
		assertThat(livroRepository.findById(livro.getId())).isEmpty();
	}

	@Test
	void naoPermiteEmprestarLivroJaComEmprestimoEmAberto() throws Exception {
		Autor autor = autorRepository.save(new Autor("Autor Duplo", "Brasil", 1));
		Categoria categoria = categoriaRepository.save(new Categoria("Categoria Dupla", "desc"));
		Usuario usuario1 = usuarioRepository.save(new Usuario("Usuario A", "a@e.com", "M2"));
		Usuario usuario2 = usuarioRepository.save(new Usuario("Usuario B", "b@e.com", "M3"));
		Livro livro = livroRepository.save(new Livro("Livro Unico", autor, "222222", categoria, true));

		mockMvc.perform(post("/emprestimos")
				.param("livroId", livro.getId().toString())
				.param("usuarioId", usuario1.getId().toString()));

		mockMvc.perform(post("/emprestimos")
						.param("livroId", livro.getId().toString())
						.param("usuarioId", usuario2.getId().toString()))
				.andExpect(status().is3xxRedirection());

		assertThat(emprestimoRepository.findByDevolvidoFalse().stream()
				.filter(e -> e.getLivro().getId().equals(livro.getId())).count()).isEqualTo(1);
	}

	@Test
	void naoPermiteExcluirAutorComLivrosVinculados() throws Exception {
		Autor autor = autorRepository.save(new Autor("Autor Ocupado", "Brasil", 5));
		Categoria categoria = categoriaRepository.save(new Categoria("Categoria Livre", "desc"));
		livroRepository.save(new Livro("Livro Vinculado", autor, "333333", categoria, true));

		mockMvc.perform(post("/autores/" + autor.getId() + "/excluir"))
				.andExpect(status().is3xxRedirection());

		assertThat(autorRepository.findById(autor.getId())).isPresent();
	}
}