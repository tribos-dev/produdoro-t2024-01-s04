package dev.wakandaacademy.produdoro.tarefa.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
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
import dev.wakandaacademy.produdoro.tarefa.application.api.EditaTarefaRequest;
import dev.wakandaacademy.produdoro.tarefa.application.api.NovaPosicaoDaTarefaRequest;
import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaIdResponse;
import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaListResponse;
import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaRequest;
import dev.wakandaacademy.produdoro.tarefa.application.repository.TarefaRepository;
import dev.wakandaacademy.produdoro.tarefa.domain.StatusAtivacaoTarefa;
import dev.wakandaacademy.produdoro.tarefa.domain.StatusTarefa;
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
		when(tarefaRepository.salva(any())).thenReturn(new Tarefa(request, 0));

		TarefaIdResponse response = tarefaApplicationService.criaNovaTarefa(request);
		assertNotNull(response);
		assertEquals(TarefaIdResponse.class, response.getClass());
		assertEquals(UUID.class, response.getIdTarefa().getClass());

	}

	@Test
	void mudaOrdemDaTarefaTest() {

		Tarefa tarefa = DataHelper.createTarefa();
		List<Tarefa> tarefas = DataHelper.createListTarefa();
		Usuario usuario = DataHelper.createUsuario();
		NovaPosicaoDaTarefaRequest novaPosicao = new NovaPosicaoDaTarefaRequest(1);

		when(tarefaRepository.buscaTarefaPorId(any())).thenReturn(Optional.of(tarefa));
		when(usuarioRepository.buscaUsuarioPorEmail(any())).thenReturn(usuario);
		when(tarefaRepository.buscarTodasTarefasPorIdUsuario(tarefa.getIdUsuario())).thenReturn(tarefas);

		tarefaApplicationService.mudaOrdemDaTarefa(usuario.getEmail(), tarefa.getIdTarefa(), novaPosicao);

		verify(tarefaRepository, times(1)).defineNovaPosicaoDaTarefa(tarefa, tarefas, novaPosicao);
	}

	@Test
	void NãoMudaOrdemDAtarefaTest() {

		Usuario usuario = DataHelper.createUsuario();
		Tarefa tarefaNãoExiste = DataHelper.createTarefa();
		NovaPosicaoDaTarefaRequest novaPosicao = new NovaPosicaoDaTarefaRequest(1);

		when(tarefaRepository.buscaTarefaPorId(any())).thenReturn(Optional.empty());
		when(usuarioRepository.buscaUsuarioPorEmail(any())).thenReturn(usuario);

		assertThrows(APIException.class, () -> tarefaApplicationService.mudaOrdemDaTarefa(usuario.getEmail(),
				tarefaNãoExiste.getIdTarefa(), novaPosicao));

		verify(tarefaRepository, never()).defineNovaPosicaoDaTarefa(any(), any(), any());
	}

	@Test
	void deveDefinirTarefaComoAtiva() {
		Usuario usuario = DataHelper.createUsuario();
		Tarefa tarefa = DataHelper.createTarefa();
		Tarefa tarefaAtiva = getTarefaAtiva(usuario);

		when(usuarioRepository.buscaUsuarioPorEmail(any())).thenReturn(usuario);
		when(tarefaRepository.buscaTarefaPorId(tarefa.getIdTarefa())).thenReturn(Optional.of(tarefa));
		when(tarefaRepository.buscaTarefaAtivada()).thenReturn(Optional.ofNullable(tarefaAtiva));

		tarefaApplicationService.definiTarefaComoAtiva(String.valueOf(usuario), tarefa.getIdTarefa());

		verify(tarefaRepository, times(1)).salva(tarefa);
		verify(tarefaRepository, times(1)).buscaTarefaAtivada();
		verify(tarefaRepository, times(1)).salva(tarefa);
	}

	@Test
	void nãoDeveDefinirTarefaComoAtiva() {
		Usuario usuario = DataHelper.createUsuario();
		UUID idTarefaInvalido = UUID.randomUUID();

		when(usuarioRepository.buscaUsuarioPorEmail(any())).thenReturn(usuario);
		when(tarefaRepository.buscaTarefaPorId(idTarefaInvalido))
				.thenThrow(APIException.build(HttpStatus.NOT_FOUND, "Id Da Tarefa Inválido"));

		APIException e = assertThrows(APIException.class, () -> {
			tarefaApplicationService.definiTarefaComoAtiva(String.valueOf(usuario), idTarefaInvalido);
		});

		assertEquals(HttpStatus.NOT_FOUND, e.getStatusException());
		verify(tarefaRepository, never()).buscaTarefaAtivada();
		verify(tarefaRepository, never()).salva(any(Tarefa.class));
	}

	private static Tarefa getTarefaAtiva(Usuario usuario) {
		return Tarefa.builder().contagemPomodoro(1).idTarefa(UUID.fromString("4c70c27a-446c-4506-b666-1067085d8d85"))
				.idUsuario(usuario.getIdUsuario()).descricao("descricao tarefa")
				.statusAtivacao(StatusAtivacaoTarefa.ATIVA).build();
	}

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
		when(usuarioRepository.buscaUsuarioPorEmail(any()))
				.thenThrow(APIException.build(HttpStatus.BAD_REQUEST, "Usuario não encontrado!"));

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
		when(usuarioRepository.buscaUsuarioPorId(any()))
				.thenThrow(APIException.build(HttpStatus.BAD_REQUEST, "Usuario não encontrado!"));

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
	@DisplayName("Deleta tarefas concluidas")
	void deletaTarefasConcluidas_comDadosValidos_sucesso() {
		Usuario usuario = DataHelper.createUsuario();
		List<Tarefa> tarefasConcluidas = DataHelper.createTarefasConcluidas();
		List<Tarefa> tarefas = DataHelper.createListTarefa();
		when(usuarioRepository.buscaUsuarioPorEmail(any())).thenReturn(usuario);
		when(usuarioRepository.buscaUsuarioPorId(any())).thenReturn(usuario);
		when(tarefaRepository.buscaTarefasConcluidas(any())).thenReturn(tarefasConcluidas);
		when(tarefaRepository.buscarTodasTarefasPorIdUsuario(any())).thenReturn(tarefas);

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

	@Test
	void naoDeveBuscarTodasTarefasPorUsuario() {
		Usuario usuario = DataHelper.createUsuario();
		when(usuarioRepository.buscaUsuarioPorEmail(any()))
				.thenThrow(APIException.build(HttpStatus.BAD_REQUEST, "Usuario não encontrado!"));

		APIException e = assertThrows(APIException.class,
				() -> tarefaApplicationService.buscarTodasTarefas("emailinvalido@gmail.com", usuario.getIdUsuario()));

		assertEquals(HttpStatus.BAD_REQUEST, e.getStatusException());
		assertEquals("Usuario não encontrado!", e.getMessage());
	}

	@Test
	void deveDeletarTarefa() {
		Usuario usuario = DataHelper.createUsuario();
		Tarefa tarefa = DataHelper.createTarefa();

		when(usuarioRepository.buscaUsuarioPorEmail(any())).thenReturn(usuario);
		when(tarefaRepository.buscaTarefaPorId(any())).thenReturn(Optional.of(tarefa));
		tarefaApplicationService.deletaTarefa(tarefa.getIdTarefa(), usuario.getEmail());

		verify(tarefaRepository, times(1)).deleta(tarefa);
	}

	@Test
	void naoDeveDeletarTarefa() {
		Usuario usuario = DataHelper.createUsuario();
		String emailUsuario = "joao@gmail.com";
		UUID idTarefa = UUID.fromString("06fb5521-9d5a-861a-82fb-e67e3bedcbbb");
		Tarefa tarefa = DataHelper.createTarefa();

		APIException e = assertThrows(APIException.class,
				() -> tarefaApplicationService.deletaTarefa(tarefa.getIdTarefa(), usuario.getEmail()));

		assertNotEquals(idTarefa, tarefa.getIdTarefa());
		assertNotEquals(emailUsuario, usuario.getEmail());
		assertEquals(HttpStatus.NOT_FOUND, e.getStatusException());
	}

	@Test
	void deveEditarTarefa() {
		Usuario usuario = DataHelper.createUsuario();
		Tarefa tarefa = DataHelper.createTarefa();
		EditaTarefaRequest editaTarefaRequest = DataHelper.createEditaTarefa();
		when(usuarioRepository.buscaUsuarioPorEmail(any())).thenReturn(usuario);
		when(tarefaRepository.buscaTarefaPorId(any())).thenReturn(Optional.of(tarefa));
		tarefaApplicationService.editaTarefa(usuario.getEmail(), tarefa.getIdTarefa(), editaTarefaRequest);
		verify(usuarioRepository, times(1)).buscaUsuarioPorEmail(usuario.getEmail());
		verify(tarefaRepository, times(1)).buscaTarefaPorId(tarefa.getIdTarefa());
		assertEquals("TAREFA 2", tarefa.getDescricao());
	}

	@Test
	void naoDeveEditarTarefa() {
		UUID idTarefaInvalido = UUID.randomUUID();
		String usuario = "Allan";
		EditaTarefaRequest editaTarefaRequest = DataHelper.createEditaTarefa();
		when(tarefaRepository.buscaTarefaPorId(idTarefaInvalido)).thenReturn(Optional.empty());
		assertThrows(APIException.class,
				() -> tarefaApplicationService.editaTarefa(usuario, idTarefaInvalido, editaTarefaRequest));
		verify(tarefaRepository, times(1)).buscaTarefaPorId(idTarefaInvalido);

	}

	@Test
	void deveConcluirTarefa() {
		Usuario usuario = DataHelper.createUsuario();
		Tarefa tarefa = DataHelper.createTarefa();
		when(usuarioRepository.buscaUsuarioPorEmail(any())).thenReturn(usuario);
		when(tarefaRepository.buscaTarefaPorId(any())).thenReturn(Optional.of(tarefa));
		tarefaApplicationService.concluiTarefa(usuario.getEmail(), tarefa.getIdTarefa());
		assertEquals(tarefa.getStatus(), StatusTarefa.CONCLUIDA);
	}

	@Test
	void naoDeveConcluirTarefa() {
		Tarefa tarefa = DataHelper.createTarefa();
		when(usuarioRepository.buscaUsuarioPorEmail(any())).thenThrow(APIException.class);
		assertThrows(APIException.class,
				() -> tarefaApplicationService.concluiTarefa("emailInvalido@gmail.com", tarefa.getIdTarefa()));

	}
}