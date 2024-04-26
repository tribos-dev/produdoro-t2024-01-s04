package dev.wakandaacademy.produdoro.tarefa.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import dev.wakandaacademy.produdoro.DataHelper;
import dev.wakandaacademy.produdoro.handler.APIException;
import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaIdResponse;
import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaListResponse;
import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaRequest;
import dev.wakandaacademy.produdoro.tarefa.application.repository.TarefaRepository;
import dev.wakandaacademy.produdoro.tarefa.domain.Tarefa;
import dev.wakandaacademy.produdoro.usuario.application.repository.UsuarioRepository;
import dev.wakandaacademy.produdoro.usuario.domain.Usuario;

@ExtendWith(MockitoExtension.class)
class TarefaApplicationServiceTest {

	// @Autowired
	@InjectMocks
	TarefaApplicationService tarefaApplicationService;

	// @MockBean
	@Mock
	TarefaRepository tarefaRepository;
	@Mock
	UsuarioRepository usuarioRepository;

    @Test
    void deveRetornarIdTarefaNovaCriada() {
        TarefaRequest request = getTarefaRequest();
        when(tarefaRepository.salva(any())).thenReturn(new Tarefa(request));

		TarefaIdResponse response = tarefaApplicationService.criaNovaTarefa(request);

		assertNotNull(response);
		assertEquals(TarefaIdResponse.class, response.getClass());
		assertEquals(UUID.class, response.getIdTarefa().getClass());
	}

	public TarefaRequest getTarefaRequest() {
		TarefaRequest request = new TarefaRequest("tarefa 1", UUID.randomUUID(), null, null, 0);
		return request;
	}

	@Test
	@DisplayName("Deleta todas as tarefas do usuario com sucesso")
	void deletaTodasAsTarefasDoUsuario_comDadosValidos_sucesso() {
		Usuario usuario = DataHelper.createUsuario();
		List<Tarefa> tarefasDoUsuario = DataHelper.createListTarefa();
		String email = usuario.getEmail();
		UUID idUsuario = usuario.getIdUsuario();
		when(usuarioRepository.buscaUsuarioPorEmail(any())).thenReturn(usuario);
		when(usuarioRepository.buscaUsuarioPorId(any())).thenReturn(usuario);
		when(tarefaRepository.buscarTodasTarefasPorIdUsuario(any())).thenReturn(tarefasDoUsuario);

		tarefaApplicationService.deletaTodasAsTarefasDoUsuario(email, idUsuario);

		verify(tarefaRepository, times(1)).deletaTodasAsTarefasDoUsuario(tarefasDoUsuario);
	}

	@Test
    @DisplayName("Deleta todas as tarefas do usuario quando emailUsuario for inexistente")
    void deletaTodasAsTarefasDoUsuario_comEmailInexistente_retornaAPIException() {
    	when(usuarioRepository.buscaUsuarioPorEmail(any())).thenThrow(APIException.build(HttpStatus.BAD_REQUEST, "Usuario não encontrado!"));

    	assertThrows(APIException.class, 
    			() -> tarefaApplicationService.deletaTodasAsTarefasDoUsuario("exemplo@gmail.com", UUID.randomUUID()));
 
    	verify(usuarioRepository, times(1)).buscaUsuarioPorEmail(any());
    }
	
	@Test
    @DisplayName("Deleta todas as tarefas do usuario quando idUsuario for inexistente")
    void deletaTodasAsTarefasDoUsuario_comIdUsuarioInexistente_retornaAPIException() {
		Usuario usuario = DataHelper.createUsuario();
		String email = usuario.getEmail();
		UUID idInexistente = UUID.randomUUID();
		when(usuarioRepository.buscaUsuarioPorEmail(any())).thenReturn(usuario);
		when(usuarioRepository.buscaUsuarioPorId(any())).thenThrow(APIException.build(HttpStatus.BAD_REQUEST, "Usuario não encontrado!"));

    	assertThrows(APIException.class, 
    			() -> tarefaApplicationService.deletaTodasAsTarefasDoUsuario(email, idInexistente));
 
    	verify(usuarioRepository, times(1)).buscaUsuarioPorEmail(any());
    	verify(usuarioRepository, times(1)).buscaUsuarioPorId(any());
    }
	
	@Test
    @DisplayName("Deleta todas as tarefas do usuario quando usuarioEmail nao pertence ao usuario")
    void deletaTodasAsTarefasDoUsuario_comUsuarioEmailNaoPertenceAoUsuario_retornaAPIException() {
		Usuario usuario = DataHelper.createUsuario();
		Usuario usuarioTeste = DataHelper.createUsuarioTeste();
		String email = usuarioTeste.getEmail();
		UUID idUsuario = usuario.getIdUsuario();
		when(usuarioRepository.buscaUsuarioPorEmail(any())).thenReturn(usuarioTeste);
		when(usuarioRepository.buscaUsuarioPorId(any())).thenReturn(usuario);

		APIException ex = assertThrows(APIException.class, 
    			() -> tarefaApplicationService.deletaTodasAsTarefasDoUsuario(email, idUsuario));
    	
    	assertEquals("Usúario(a) não autorizado(a) para a requisição solicitada!", ex.getMessage());
    	assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusException());
	}
	
	@Test
    @DisplayName("Deleta todas as tarefas do usuario quando usuario nao possui tarefas cadastradas")
    void deletaTodasAsTarefasDoUsuario_quandoUsuarioNaoPossuiTarefaCadastrada_retornaAPIException() {
		Usuario usuario = DataHelper.createUsuario();
		String email = usuario.getEmail();
		UUID idUsuario = usuario.getIdUsuario();
		List<Tarefa> listaVazia = List.of();
		when(usuarioRepository.buscaUsuarioPorEmail(any())).thenReturn(usuario);
		when(usuarioRepository.buscaUsuarioPorId(any())).thenReturn(usuario);
		when(tarefaRepository.buscarTodasTarefasPorIdUsuario(any())).thenReturn(listaVazia);
		
		APIException ex = assertThrows(APIException.class, 
    			() -> tarefaApplicationService.deletaTodasAsTarefasDoUsuario(email, idUsuario));

    	assertEquals("Usúario não possui nenhuma tarefa cadastrada!", ex.getMessage());
    	assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusException());
    }

	
    @Test
    void deveListarTodasAsTarefas() {
    	// Dado
    	Usuario usuario = DataHelper.createUsuario();
    	List<Tarefa> tarefas = DataHelper.createListTarefa();
    	// Quando
    	when(usuarioRepository.buscaUsuarioPorEmail(any())).thenReturn(usuario);
    	when(usuarioRepository.buscaUsuarioPorId(any())).thenReturn(usuario);
    	when(tarefaRepository.buscarTodasTarefasPorIdUsuario(any())).thenReturn(tarefas);
    	
    	List<TarefaListResponse> resultado = tarefaApplicationService.buscarTodasTarefas(usuario.getEmail(), 
    			usuario.getIdUsuario());
    	
    	// Então
    	verify(usuarioRepository, times(1)).buscaUsuarioPorEmail(usuario.getEmail());
    	verify(usuarioRepository, times(1)).buscaUsuarioPorId(usuario.getIdUsuario());
    	verify(tarefaRepository, times(1)).buscarTodasTarefasPorIdUsuario(usuario.getIdUsuario());
    	assertEquals(resultado.size(), 8);
    }

    @Test
    void naoDeveBuscarTodasTarefasPorUsuario() {
    	Usuario usuario = DataHelper.createUsuario();

		when(usuarioRepository.buscaUsuarioPorEmail(any()))
				.thenThrow(APIException.build(HttpStatus.BAD_REQUEST, "Usuario não encontrado!"));

		APIException e = assertThrows(APIException.class, () -> tarefaApplicationService
				.buscarTodasTarefas("emailinvalido@gmail.com", usuario.getIdUsuario()));

		assertEquals(HttpStatus.BAD_REQUEST, e.getStatusException());
		assertEquals("Usuario não encontrado!", e.getMessage());
    }
    
}
