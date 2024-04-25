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
	private UsuarioRepository usuarioRepository;

	@Test
	void deveRetornarIdTarefaNovaCriada() {
		TarefaRequest request = getTarefaRequest();
		when(tarefaRepository.salva(any())).thenReturn(new Tarefa(request, 0));

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
	@DisplayName("Deleta tarefas concluidas")
	void deletaTarefasConcluidas_comDadosValidos_sucesso() {
		Usuario usuario = DataHelper.createUsuario();
		List<Tarefa> tarefasConcluidas = DataHelper.createTarefasConcluidas();
		List<Tarefa> tarefas = DataHelper.createListTarefa();
		when(usuarioRepository.buscaUsuarioPorEmail(any())).thenReturn(usuario);
		when(usuarioRepository.buscaUsuarioPorId(any())).thenReturn(usuario);
		when(tarefaRepository.buscaTarefasConcluidas(any())).thenReturn(tarefasConcluidas);
		when(tarefaRepository.buscaTarefasDoUsuario(any())).thenReturn(tarefas);

		tarefaApplicationService.deletaTarefasConcluidas(usuario.getEmail(), usuario.getIdUsuario());

		verify(tarefaRepository, times(1)).deletaVariasTarefas(tarefasConcluidas);
		verify(tarefaRepository, times(1)).atualizaPosicaoDasTarefas(tarefas);
	}

	@Test
	@DisplayName("Deleta tarefas concluidas quando email for inexistente")
	void deletaTarefasConcluidas_comEmailInexistente_retornaAPIException() {
		String email = "emailinvalido@gmail.com";
		when(usuarioRepository.buscaUsuarioPorEmail(any()))
				.thenThrow(APIException.build(HttpStatus.BAD_REQUEST, "Usuario não encontrado!"));

		assertThrows(APIException.class,
				() -> tarefaApplicationService.deletaTarefasConcluidas(email, UUID.randomUUID()));

		verify(usuarioRepository, times(1)).buscaUsuarioPorEmail(email);
	}

	@Test
	@DisplayName("Deleta tarefas concluidas quando idUsuario for inexistente")
	void deletaTarefasConcluidas_comIdUsuarioInexistente_retornaAPIException() {
		Usuario usuario = DataHelper.createUsuario();
		String email = usuario.getEmail();
		UUID idInvalido = UUID.randomUUID();
		when(usuarioRepository.buscaUsuarioPorEmail(any())).thenReturn(usuario);
		when(usuarioRepository.buscaUsuarioPorId(any()))
				.thenThrow(APIException.build(HttpStatus.BAD_REQUEST, "Usuario não encontrado!"));

		assertThrows(APIException.class, () -> tarefaApplicationService.deletaTarefasConcluidas(email, idInvalido));

		verify(usuarioRepository, times(1)).buscaUsuarioPorEmail(email);
		verify(usuarioRepository, times(1)).buscaUsuarioPorId(idInvalido);
	}

	@Test
	@DisplayName("Deleta tarefas concluidas quando emailUsuario não pertence ao usuario")
	void deletaTarefasConcluidas_quandoEmailUsuarioNaoPertenceAoUsuario_retornaAPIException() {
		Usuario usuario = DataHelper.createUsuario();
		UUID idUsuario = usuario.getIdUsuario();
		Usuario usuarioTeste = DataHelper.createUsuarioTeste();
		String email = usuarioTeste.getEmail();
		when(usuarioRepository.buscaUsuarioPorEmail(any())).thenReturn(usuarioTeste);
		when(usuarioRepository.buscaUsuarioPorId(any())).thenReturn(usuario);

		APIException ex = assertThrows(APIException.class,
				() -> tarefaApplicationService.deletaTarefasConcluidas(email, idUsuario));

		assertEquals("Usúario(a) não autorizado(a) para a requisição solicitada!", ex.getMessage());
		assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusException());
	}

	@Test
	@DisplayName("Deleta tarefas concluidas quando usuario nao possui nenhuma tarefa cadastrada")
	void deletaTarefasConcluidas_quandoUsuarioNaoPossuiNenhumaTarefaConcluida_retornaAPIException() {
		Usuario usuario = DataHelper.createUsuario();
		String email = usuario.getEmail();
		UUID idUsuario = usuario.getIdUsuario();
		List<Tarefa> tarefasConcluidas = List.of();

		when(usuarioRepository.buscaUsuarioPorEmail(any())).thenReturn(usuario);
		when(usuarioRepository.buscaUsuarioPorId(any())).thenReturn(usuario);
		when(tarefaRepository.buscaTarefasConcluidas(any())).thenReturn(tarefasConcluidas);

		APIException ex = assertThrows(APIException.class,
				() -> tarefaApplicationService.deletaTarefasConcluidas(email, idUsuario));

		assertEquals("Usúario não possui nenhuma tarefa concluída!", ex.getMessage());
		assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusException());
	}

}
