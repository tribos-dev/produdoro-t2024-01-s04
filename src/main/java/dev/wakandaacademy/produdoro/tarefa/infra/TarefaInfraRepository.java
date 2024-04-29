package dev.wakandaacademy.produdoro.tarefa.infra;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;

import dev.wakandaacademy.produdoro.handler.APIException;
import dev.wakandaacademy.produdoro.tarefa.application.repository.TarefaRepository;
import dev.wakandaacademy.produdoro.tarefa.domain.StatusTarefa;
import dev.wakandaacademy.produdoro.tarefa.domain.Tarefa;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Repository
@Log4j2
@RequiredArgsConstructor
public class TarefaInfraRepository implements TarefaRepository {

	private final TarefaSpringMongoDBRepository tarefaSpringMongoDBRepository;
	private final MongoTemplate mongoTemplate;

	@Override
	public Tarefa salva(Tarefa tarefa) {
		log.info("[inicia] TarefaInfraRepository - salva");
		try {
			tarefaSpringMongoDBRepository.save(tarefa);
		} catch (DataIntegrityViolationException e) {
			throw APIException.build(HttpStatus.BAD_REQUEST, "Tarefa já cadastrada", e);
		}
		log.info("[finaliza] TarefaInfraRepository - salva");
		return tarefa;
	}

	@Override
	public Optional<Tarefa> buscaTarefaPorId(UUID idTarefa) {
		log.info("[inicia] TarefaInfraRepository - buscaTarefaPorId");
		Optional<Tarefa> tarefaPorId = tarefaSpringMongoDBRepository.findByIdTarefa(idTarefa);
		log.info("[finaliza] TarefaInfraRepository - buscaTarefaPorId");
		return tarefaPorId;
	}

	@Override
	public int contarTarefas(UUID idUsuario) {
		log.info("[inicia] TarefaInfraRepository - contarTarefas");
		List<Tarefa> tarefasDoUsuario = buscarTodasTarefasPorIdUsuario(idUsuario);
		int novaPosicao = tarefasDoUsuario.size();
		log.info("[finaliza] TarefaInfraRepository - contarTarefas");
		return novaPosicao;
	}

	@Override
	public List<Tarefa> buscaTarefasConcluidas(UUID idUsuario) {
		log.info("[inicia] TarefaInfraRepository - buscaTarefasConcluidas");
		Query query = new Query();
		query.addCriteria(Criteria.where("idUsuario").is(idUsuario)
				.and("status").is(StatusTarefa.CONCLUIDA));
		List<Tarefa> tarefasConcluidas = mongoTemplate.find(query, Tarefa.class);
		log.info("[finaliza] TarefaInfraRepository - buscaTarefasConclidas");
		return tarefasConcluidas;
	}

	@Override
	public void deletaVariasTarefas(List<Tarefa> tarefasConcluidas) {
		log.info("[inicia] TarefaInfraRepository - deletaVariasTarefas");
		tarefaSpringMongoDBRepository.deleteAll(tarefasConcluidas);
		log.info("[finaliza] TarefaInfraRepository - deletaVariasTarefas");
	}

	@Override
	public void atualizaPosicaoDasTarefas(List<Tarefa> tarefasDoUsuario) {
		log.info("[inicia] TarefaInfraRepository - atualizaPosicaoDasTarefas");
		int tamanhoDaLista = tarefasDoUsuario.size();
		List<Tarefa> tarefasAtualizadas = IntStream.range(0, tamanhoDaLista)
			.mapToObj(i -> atualizaTarefaComNovaPosicao(tarefasDoUsuario.get(i), i))
			.collect(Collectors.toList());
			salvaVariasTarefas(tarefasAtualizadas);
		log.info("[finaliza] TarefaInfraRepository - atualizaPosicaoDasTarefas");
	}
	
	private Tarefa atualizaTarefaComNovaPosicao(Tarefa tarefa, int novaPosicao) {
		log.info("[inicia] TarefaInfraRepository - atualizaTarefaComNovaPosicao");
		tarefa.atualizaPosicao(novaPosicao);
		log.info("[finaliza] TarefaInfraRepository - atualizaTarefaComNovaPosicao");
		return tarefa;
	}
	
	@Override
	public void salvaVariasTarefas(List<Tarefa> tarefasDoUsuario) {
		log.info("[inicia] TarefaInfraRepository - salvaVariasTarefas");
		tarefaSpringMongoDBRepository.saveAll(tarefasDoUsuario);
		log.info("[finaliza] TarefaInfraRepository - salvaVariasTarefas");
	}

	@Override
	public List<Tarefa> buscarTodasTarefasPorIdUsuario(UUID idUsuario) {
		log.info("[inicia] TarefaInfraRepository - buscarTodasTarefasPorIdUsuario");
		List<Tarefa> todasTarefas = tarefaSpringMongoDBRepository.findAllByIdUsuario(idUsuario);
		log.info("[finaliza] TarefaInfraRepository - buscarTodasTarefasPorIdUsuario");
		return todasTarefas;
	}
}
