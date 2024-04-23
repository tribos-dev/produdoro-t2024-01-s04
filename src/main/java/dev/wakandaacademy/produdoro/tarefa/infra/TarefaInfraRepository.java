package dev.wakandaacademy.produdoro.tarefa.infra;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
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
	public int tamanhoDaLista(UUID idUsuario) {
		log.info("[inicia] TarefaInfraRepository - tamanhoDaLista");
		List<Tarefa> tarefasDoUsuario = buscaTarefasDoUsuario(idUsuario);
		int tamanhoDaLista = tarefasDoUsuario.size();
		log.info("[finaliza] TarefaInfraRepository - tamanhoDaLista");
		return tamanhoDaLista;
	}

	@Override
	public List<Tarefa> buscaTarefasDoUsuario(UUID idUsuario) {
		log.info("[inicia] TarefaInfraRepository - buscaTarefasDoUsuario");
		Query query = new Query();
		query.addCriteria(Criteria.where("idUsuario").is(idUsuario));
		query.with(Sort.by(Sort.Direction.ASC, "posicao"));
		List<Tarefa> tarefasDoUsuario = mongoTemplate.find(query, Tarefa.class);
		log.info("[finaliza] TarefaInfraRepository - buscaTarefasDoUsuario");
		return tarefasDoUsuario;
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
		int tamanhaDalista = tarefasDoUsuario.size();
		IntStream.range(0, tamanhaDalista)
			.mapToObj(i -> atualizaTarefaComNovaPosicao(tarefasDoUsuario.get(i), i));
		log.info("[finaliza] TarefaInfraRepository - atualizaPosicaoDasTarefas");
	}
	
	private Tarefa atualizaTarefaComNovaPosicao(Tarefa tarefa, int novaPosicao) {
		log.info("[inicia] TarefaInfraRepository - atualizaPosicaoDasTarefas");
		tarefa.atualizaPosicao(novaPosicao);
		Query query = new Query();
		query.addCriteria(Criteria.where("idTarefa").is(tarefa.getIdTarefa()));
		
		Update update = new Update();
		update.set("posicao", tarefa.getPosicao());
		
		mongoTemplate.updateFirst(query, update, Tarefa.class);
		log.info("[finaliza] TarefaInfraRepository - atualizaPosicaoDasTarefas");
		return tarefa;
	}

}
