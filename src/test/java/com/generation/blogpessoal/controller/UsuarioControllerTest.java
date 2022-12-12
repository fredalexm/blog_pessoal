package com.generation.blogpessoal.controller;

import com.generation.blogpessoal.model.Usuario;
import com.generation.blogpessoal.model.UsuarioLogin;
import com.generation.blogpessoal.repository.UsuarioRepository;
import com.generation.blogpessoal.service.UsuarioService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UsuarioControllerTest {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @BeforeAll
    void start(){

        usuarioRepository.deleteAll();

        usuarioService.cadastrarUsuario( new Usuario(
                0L, "Root", "root@root.com", "rootroot", " "));
    }

    @Test
    @DisplayName("Cadastrar Um Usuário")
    public void deveCriarUmUsuario(){

        HttpEntity<Usuario> corpoRequisicao = new HttpEntity<Usuario>(
                new Usuario(0L, "Paulo Antunes", "paulo_antunes@email.com", "12345678", "imagem_foto_do_paulo")
        );

        ResponseEntity<Usuario> corpoResposta = testRestTemplate
                .exchange("/usuarios/cadastrar", HttpMethod.POST, corpoRequisicao, Usuario.class);

        assertEquals(HttpStatus.CREATED, corpoResposta.getStatusCode());
        assertEquals(corpoRequisicao.getBody().getNome(), corpoResposta.getBody().getNome());
        assertEquals(corpoRequisicao.getBody().getUsuario(), corpoResposta.getBody().getUsuario());
    }

    @Test
    @DisplayName("Não deve permitir duplicação do Usuário")
    public void naoDeveDuplicarUsuario(){

        usuarioService.cadastrarUsuario(new Usuario(
                0L, "Maria da Silva", "maria_silva@email.com", "12345678", "http://endereco.imagem"));

        HttpEntity<Usuario> corpoRequisicao = new HttpEntity<Usuario>(new Usuario(
                0L, "Maria da Silva", "maria_silva@email.com", "12345678", "http://endereco.imagem"
        ));

        ResponseEntity<Usuario> corpoResposta = testRestTemplate
                .exchange("/usuarios/cadastrar", HttpMethod.POST, corpoRequisicao, Usuario.class);

        assertEquals(HttpStatus.BAD_REQUEST, corpoResposta.getStatusCode());
    }

    @Test
    @DisplayName("Atualizar um Usuário")
    public void deveAtualizarUmUsuario(){

        Optional<Usuario> usuarioCadastrado = usuarioService.cadastrarUsuario(new Usuario(
                0L, "Juliana Andrews", "juliana@email.com", "juliana123", "http://endereco.imagem"
        ));

        Usuario usuarioUpadate = new Usuario(usuarioCadastrado.get().getId(),
                "Juliana Andrews", "juliana@email.com", "juliana123", "http://endereco.imagem");

        HttpEntity<Usuario> corpoRequisicao = new HttpEntity<Usuario>(usuarioUpadate);

        ResponseEntity<Usuario> corpoResposta = testRestTemplate
                .withBasicAuth("root@root.com", "rootroot")
                .exchange("/usuarios/atualizar", HttpMethod.PUT, corpoRequisicao, Usuario.class);

        assertEquals(HttpStatus.OK, corpoResposta.getStatusCode());
        assertEquals(corpoRequisicao.getBody().getNome(), corpoResposta.getBody().getNome());
        assertEquals(corpoRequisicao.getBody().getUsuario(), corpoResposta.getBody().getUsuario());
    }
    @Test
    @DisplayName("Listar todos os Usuarios")
    public void deveMostrarTodosUsuarios(){

        usuarioService.cadastrarUsuario(new Usuario(0L, "Sabrina", "paulo_antunes@email.com", "12345678", " "));

        usuarioService.cadastrarUsuario(new Usuario(0L, "Paulo Ricardo", "paulo_antunes@email.com", "12345678", " "));

        ResponseEntity<String> resposta = testRestTemplate
                .withBasicAuth("root@root.com", "rootroot")
                .exchange("/usuarios/all", HttpMethod.GET, null, String.class);

        assertEquals(HttpStatus.OK, resposta.getStatusCode());
    }

    @Test
    @DisplayName("Buscar Usuarios por id")
    public void buscarUsuariosId(){

        Optional<Usuario> novoUsuario = usuarioService.cadastrarUsuario(new Usuario(
                0L, "Juliana Andrews", "juliana@email.com", "juliana123", "http://endereco.imagem"
        ));

        ResponseEntity<Usuario> resposta = testRestTemplate
                .withBasicAuth("root@root.com", "rootroot")
                .exchange("/usuarios/"+novoUsuario.get(), HttpMethod.GET, null, Usuario.class);

        assertEquals(HttpStatus.OK, resposta.getStatusCode());
    }

    @Test
    @DisplayName("Login Usuario")
    public void logarUsuarios(){

        HttpEntity<UsuarioLogin> corpoRequisicao = new HttpEntity<UsuarioLogin>(
                new UsuarioLogin("root@root.com", "rootroot")
        );

        ResponseEntity<Usuario> corpoResposta = testRestTemplate.exchange(
                "/usuarios/logar", HttpMethod.POST, corpoRequisicao, Usuario.class
        );

        assertEquals(HttpStatus.OK, corpoResposta.getStatusCode());
    }

}
