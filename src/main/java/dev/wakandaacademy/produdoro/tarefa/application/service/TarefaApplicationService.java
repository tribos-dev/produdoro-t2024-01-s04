package dev.wakandaacademy.produdoro.tarefa.application.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import dev.wakandaacademy.produdoro.handler.APIException;
import dev.wakandaacademy.produdoro.tarefa.application.api.EditaTarefaRequest;
import dev.wakandaacademy.produdoro.tarefa.application.api.NovaPosicaoDaTarefaRequest;
import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaIdResponse;
import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaListResponse;
import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaRequest;
import dev.wakandaacademy.produdoro.tarefa.application.repository.TarefaRepository;
import dev.wakandaacademy.produdoro.tarefa.domain.Tarefa;
import dev.wakandaacademy.produdoro.usuario.application.repository.UsuarioRepository;
import dev.wakandaacademy.produdoro.usuario.domain.Usuario;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
@RequiredArgsConstructor
public class TarefaApplicationService implements TarefaService {
	private final TarefaRepository tarefaRepository;
	private final UsuarioRepository usuarioRepository;

	@Override
	public TarefaIdResponse criaNovaTarefa(TarefaRequest tarefaRequest) {
		log.info("[inicia] TarefaApplicationService - criaNovaTarefa");
		int novaPosicao = tarefaRepository.contarTarefas(tarefaRequest.getIdUsuario());
		Tarefa tarefaCriada = tarefaRepository.salva(new Tarefa(tarefaRequest, novaPosicao));
		log.info("[finaliza] TarefaApplicationService - criaNovaTarefa");
		return TarefaIdResponse.builder().idTarefa(tarefaCriada.getIdTarefa()).build();
	}

	@Override
	public Tarefa detalhaTarefa(String usuario, UUID idTarefa) {
		log.info("[inicia] TarefaApplicationService - detalhaTarefa");
		Usuario usuarioPorEmail = usuarioRepository.buscaUsuarioPorEmail(usuario);
		log.info("[usuarioPorEmail] {}", usuarioPorEmail);
		Tarefa tarefa = tarefaRepository.buscaTarefaPorId(idTarefa)
				.orElseThrow(() -> APIException.build(HttpStatus.NOT_FOUND, "Tarefa não encontrada!"));
		tarefa.pertenceAoUsuario(usuarioPorEmail);
		log.info("[finaliza] TarefaApplicationService - detalhaTarefa");
		return tarefa;
	}

	@Override
	public void incrementaPomodoro(String emailUsuario, UUID idTarefa) {
		log.info("[inicia] TarefaApplicationService - incrementaPomodoro");
		Tarefa tarefa = detalhaTarefa(emailUsuario, idTarefa);
		Usuario usuario = usuarioRepository.buscaUsuarioPorEmail(emailUsuario);
		tarefa.incrementaPomodoro(usuario);
		tarefaRepository.salva(tarefa);
		tarefaRepository.processaStatusEContadorPomodoro(usuario);
		log.info("[finaliza] TarefaApplicationService - incrementaPomodoro");
	}

	@Override
	public void deletaTarefasConcluidas(String email, UUID idUsuario) {
		log.info("[inicia] TarefaApplicationService - detalhaTarefa");
		Usuario usuarioPorEmail = usuarioRepository.buscaUsuarioPorEmail(email);
		log.info("[usuarioPorEmail] {}", usuarioPorEmail);
		Usuario usuario = usuarioRepository.buscaUsuarioPorId(idUsuario);
		usuario.pertenceAoUsuario(usuarioPorEmail);
		List<Tarefa> tarefasConcluidas = tarefaRepository.buscaTarefasConcluidas(usuario.getIdUsuario());
		if (tarefasConcluidas.isEmpty()) {
			throw APIException.build(HttpStatus.BAD_REQUEST, "Usúario não possui nenhuma tarefa concluída!");
		}
		tarefaRepository.deletaVariasTarefas(tarefasConcluidas);
		List<Tarefa> tarefasDoUsuario = tarefaRepository.buscarTodasTarefasPorIdUsuario(usuario.getIdUsuario());
		tarefaRepository.atualizaPosicaoDasTarefas(tarefasDoUsuario);
		log.info("[finaliza] TarefaApplicationService - detalhaTarefa");
	}

	@Override
	public void deletaTodasAsTarefasDoUsuario(String usuarioEmail, UUID idUsuario) {
		log.info("[inicia] TarefaApplicationService - deletaTodasAsTarefasDoUsuario");
		Usuario usuarioPorEmail = usuarioRepository.buscaUsuarioPorEmail(usuarioEmail);
		log.info("[usuarioPorEmail] {}", usuarioPorEmail);
		Usuario usuario = usuarioRepository.buscaUsuarioPorId(idUsuario);
		usuario.pertenceAoUsuario(usuarioPorEmail);
		List<Tarefa> tarefasDoUsuario = tarefaRepository.buscarTodasTarefasPorIdUsuario(usuario.getIdUsuario());
		if (tarefasDoUsuario.isEmpty()) {
			throw APIException.build(HttpStatus.BAD_REQUEST, "Usúario não possui nenhuma tarefa cadastrada!");
		}
		tarefaRepository.deletaTodasAsTarefasDoUsuario(tarefasDoUsuario);
		log.info("[finaliza] TarefaApplicationService - deletaTodasAsTarefasDoUsuario");
	}

	@Override
	public void definiTarefaComoAtiva(String usuario, UUID idTarefa) {
		log.info("[inicia] TarefaApplicationService - definirTarefaComoAtiva");
		Usuario usuarioEmail = usuarioRepository.buscaUsuarioPorEmail(usuario);

		validarTarefa(idTarefa, usuarioEmail);

		Optional<Tarefa> tarefaAtivaOptional = tarefaRepository.buscaTarefaAtivada();

		tarefaAtivaOptional.ifPresent(tarefaAtiva -> {
			tarefaAtiva.definirComoInativa();
			tarefaRepository.salva(tarefaAtiva);
		});

		Tarefa novaTarefa = tarefaRepository.buscaTarefaPorId(idTarefa)
				.orElseThrow(() -> APIException.build(HttpStatus.NOT_FOUND, "Tarefa não encontrada."));
		novaTarefa.definirComoAtiva();
		tarefaRepository.salva(novaTarefa);
		log.info("[finaliza] TarefaApplicationService - definirTarefaComoAtiva");
	}

	private Tarefa validarTarefa(UUID idTarefa, Usuario usuarioEmail) {
		Tarefa tarefa = tarefaRepository.buscaTarefaPorId(idTarefa)
				.orElseThrow(() -> APIException.build(HttpStatus.NOT_FOUND, "Id Da Tarefa Inválido"));
		tarefa.pertenceAoUsuario(usuarioEmail);
		return tarefa;
	}

	@Override
	public void mudaOrdemDaTarefa(String emailDoUsuario, UUID idTarefa,
			NovaPosicaoDaTarefaRequest novaPosicaoDaTarefaRequest) {
		log.info("[inicia] TarefaApplicationService - mudaOrdemDatarefa");
		Tarefa tarefa = detalhaTarefa(emailDoUsuario, idTarefa);
		List<Tarefa> tarefas = tarefaRepository.buscarTodasTarefasPorIdUsuario(tarefa.getIdUsuario());
		tarefaRepository.defineNovaPosicaoDaTarefa(tarefa, tarefas, novaPosicaoDaTarefaRequest);
		log.info("[finaliza] TarefaApplicationService - mudaOrdemDatarefa");
	}

	@Override
	public List<TarefaListResponse> buscarTodasTarefas(String usuario, UUID idUsuario) {
		log.info("[inicia] TarefaApplicationService - buscarTodasTarefas");
		Usuario usuarioPorEmail = usuarioRepository.buscaUsuarioPorEmail(usuario);
		usuarioRepository.buscaUsuarioPorId(idUsuario);
		usuarioPorEmail.validaUsuario(idUsuario);
		List<Tarefa> tarefas = tarefaRepository.buscarTodasTarefasPorIdUsuario(idUsuario);
		log.info("[finaliza] TarefaApplicationService - buscarTodasTarefas");
		return TarefaListResponse.converter(tarefas);
	}

	@Override
	public void deletaTarefa(UUID idTarefa, String usuario) {
		log.info("[inicia] TarefaApplicationService - deletaTarefa");
		Tarefa tarefa = detalhaTarefa(usuario, idTarefa);
		tarefaRepository.deleta(tarefa);
		log.info("[finaliza] TarefaApplicationService - deletaTarefa");
	}

	@Override
	public void editaTarefa(String emailUsuario, UUID idTarefa, EditaTarefaRequest tarefaRequest) {
		log.info("[inicia] TarefaApplicationService - editaTarefa");
		Tarefa tarefa = detalhaTarefa(emailUsuario, idTarefa);
		tarefa.edita(tarefaRequest);
		tarefaRepository.salva(tarefa);
		log.info("[finaliza] TarefaApplicationService - editaTarefa");
	}

	@Override
	public void concluiTarefa(String usuario, UUID idTarefa) {
		log.info("[inicia] TarefaApplicationService - concluiTarefa");
		Tarefa tarefa = detalhaTarefa(usuario, idTarefa);
		tarefa.concluiTarefa();
		tarefaRepository.salva(tarefa);
		log.info("[finaliza] TarefaApplicationService - concluiTarefa");
	}
}
