package dev.wakandaacademy.produdoro.tarefa.application.api;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RestController;

import dev.wakandaacademy.produdoro.config.security.service.TokenService;
import dev.wakandaacademy.produdoro.handler.APIException;
import dev.wakandaacademy.produdoro.tarefa.application.service.TarefaService;
import dev.wakandaacademy.produdoro.tarefa.domain.Tarefa;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@RestController
@Log4j2
@RequiredArgsConstructor
public class TarefaRestController implements TarefaAPI {
	private final TarefaService tarefaService;
	private final TokenService tokenService;

	@Override
	public TarefaIdResponse postNovaTarefa(TarefaRequest tarefaRequest) {
		log.info("[inicia]  TarefaRestController - postNovaTarefa  ");
		TarefaIdResponse tarefaCriada = tarefaService.criaNovaTarefa(tarefaRequest);
		log.info("[finaliza]  TarefaRestController - postNovaTarefa");
		return tarefaCriada;
	}

	@Override
	public TarefaDetalhadoResponse detalhaTarefa(String token, UUID idTarefa) {
		log.info("[inicia] TarefaRestController - detalhaTarefa");
		String usuario = getUsuarioByToken(token);
		Tarefa tarefa = tarefaService.detalhaTarefa(usuario, idTarefa);
		log.info("[finaliza] TarefaRestController - detalhaTarefa");
		return new TarefaDetalhadoResponse(tarefa);
	}

	@Override
	public void incrementaPomodoro(String token, UUID idTarefa) {
		log.info("[inicia] TarefaRestController - incrementaPomodoro");
		String emailUsuario = getUsuarioByToken(token);
		tarefaService.incrementaPomodoro(emailUsuario, idTarefa);
		log.info("[finaliza] TarefaRestController - incrementaPomodoro");
	}

	@Override
	public void concluiTarefa(String token, UUID idTarefa) {
		log.info("[inicia] TarefaRestController - concluiTarefa");
		String usuario = getUsuarioByToken(token);
		tarefaService.concluiTarefa(usuario, idTarefa);
		log.info("[finaliza] TarefaRestController - concluiTarefa");
	}

	@Override
	public void editaTarefa(String token, UUID idTarefa, EditaTarefaRequest tarefaRequest) {
		log.info("[inicia] TarefaRestController - editaTarefa");
		String emailUsuario = getUsuarioByToken(token);
		tarefaService.editaTarefa(emailUsuario, idTarefa, tarefaRequest);
		log.info("[finaliza] TarefaRestController - editaTarefa");
	}

	@Override
	public void definiTarefaComoAtiva(String token, UUID idTarefa) {
		log.info("[inicia] TarefaRestController - definiTarefaComoAtiva");
		String usuario = getUsuarioByToken(token);
		tarefaService.definiTarefaComoAtiva(usuario, idTarefa);
		log.info("[finaliza] TarefaRestController - definiTarefaComoAtiva");
	}

	@Override
	public void deletaTarefa(UUID idTarefa, String token) {
		log.info("[inicia] TarefaRestController - deletaTarefa");
		log.info("[idTarefa] {}", idTarefa);
		String usuario = getUsuarioByToken(token);
		tarefaService.deletaTarefa(idTarefa, usuario);
		log.info("[finaliza] TarefaRestController - deletaTarefa");
	}

	@Override
	public void deletaTodasAsTarefasDoUsuario(String token, UUID idUsuario) {
		log.info("[inicia] TarefaRestController - deletaTodasAsTarefasDoUsuario");
		String usuario = getUsuarioByToken(token);
		tarefaService.deletaTodasAsTarefasDoUsuario(usuario, idUsuario);
		log.info("[finaliza] TarefaRestController - deletaTodasAsTarefasDoUsuario");
	}

	@Override
	public void deletaTarefasConcluidas(String token, UUID idUsuario) {
		log.info("[inicia] TarefaRestController - deletaTarefasConcluidas");
		String email = getUsuarioByToken(token);
		tarefaService.deletaTarefasConcluidas(email, idUsuario);
		log.info("[finaliza] TarefaRestController - deletaTarefasConcluidas");
	}

	@Override
	public void mudaOrdemdaTarefa(String token, UUID idTarefa, NovaPosicaoDaTarefaRequest novaPosicaoDaTarefaRequest) {
		log.info("[inicia] TarefaRestController - mudaOrdemdaTarefa");
		String emailDoUsuario = getUsuarioByToken(token);
		tarefaService.mudaOrdemDaTarefa(emailDoUsuario, idTarefa, novaPosicaoDaTarefaRequest);
		log.info("[finaliza] TarefaRestController - mudaOrdemdaTarefa");
	}

	@Override
	public List<TarefaListResponse> listarTodasTarefas(String token, UUID idUsuario) {
		log.info("[inicia] TarefaRestController - listarTodasTarefas");
		String usuario = getUsuarioByToken(token);
		List<TarefaListResponse> tarefas = tarefaService.buscarTodasTarefas(usuario, idUsuario);
		log.info("[finaliza] TarefaRestController - listarTodasTarefas");
		return tarefas;
	}

	private String getUsuarioByToken(String token) {
		log.debug("[token] {}", token);
		String usuario = tokenService.getUsuarioByBearerToken(token)
				.orElseThrow(() -> APIException.build(HttpStatus.UNAUTHORIZED, token));
		log.info("[usuario] {}", usuario);
		return usuario;
	}
}
