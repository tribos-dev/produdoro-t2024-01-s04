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
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;

import dev.wakandaacademy.produdoro.handler.APIException;
import dev.wakandaacademy.produdoro.tarefa.application.api.NovaPosicaoDaTarefaRequest;
import dev.wakandaacademy.produdoro.tarefa.application.repository.TarefaRepository;
import dev.wakandaacademy.produdoro.tarefa.domain.StatusAtivacaoTarefa;
import dev.wakandaacademy.produdoro.tarefa.domain.StatusTarefa;
import dev.wakandaacademy.produdoro.tarefa.domain.Tarefa;
import dev.wakandaacademy.produdoro.usuario.domain.StatusUsuario;
import dev.wakandaacademy.produdoro.usuario.domain.Usuario;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Repository
@Log4j2
@RequiredArgsConstructor
public class TarefaInfraRepository implements TarefaRepository {

	private final TarefaSpringMongoDBRepository tarefaSpringMongoDBRepository;
	private final MongoTemplate mongoTemplate;
	private Integer contagemPomodoroPausaCurta = 0;

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
	public int contarTarefasDoUsuario(UUID idUsuario) {
		log.info("[inicia] TarefaInfraRepository - contarTarefasDoUsuario");
		int contarTarefas = tarefaSpringMongoDBRepository.countByIdUsuario(idUsuario);
		log.info("[finaliza] TarefaInfraRepository - contarTarefasDoUsuario");
		return contarTarefas;
	}

	@Override
	public void defineNovaPosicaoDaTarefa(Tarefa tarefa, List<Tarefa> tarefas, NovaPosicaoDaTarefaRequest novaPosicao) {
		validaNovaPosicao(tarefas, tarefa, novaPosicao);
		int posicaoAtualTarefa = tarefa.getPosicao();
		int novaPosicaoTarefa = novaPosicao.getNovaPosicao();

		if (novaPosicaoTarefa < posicaoAtualTarefa) {
			IntStream.range(novaPosicaoTarefa, posicaoAtualTarefa)
					.forEach(i -> atualizaPosicaoTarefa(tarefas.get(i), i + 1));

		} else if (novaPosicaoTarefa > posicaoAtualTarefa) {
			IntStream.range(posicaoAtualTarefa + 1, novaPosicaoTarefa + 1)
					.forEach(i -> atualizaPosicaoTarefa(tarefas.get(i), i - 1));
		}
		tarefa.atualizaPosicao(novaPosicaoTarefa);
		atualizaPosicaoTarefa(tarefa, novaPosicaoTarefa);
	}

	private void atualizaPosicaoTarefa(Tarefa tarefa, int novaPosicao) {
		Query query = new Query(Criteria.where("idTarefa").is(tarefa.getIdTarefa()));
		Update update = new Update().set("posicao", novaPosicao);
		mongoTemplate.updateFirst(query, update, Tarefa.class);
	}

	private void validaNovaPosicao(List<Tarefa> tarefas, Tarefa tarefa,
			NovaPosicaoDaTarefaRequest novaPosicaoDaTarefa) {
		int posicaoAntiga = tarefa.getPosicao();
		int tamanhoDalistaDeTarefas = tarefas.size();

		if (novaPosicaoDaTarefa.getNovaPosicao() >= tamanhoDalistaDeTarefas
				|| novaPosicaoDaTarefa.getNovaPosicao().equals(posicaoAntiga)) {
			String mensagem = novaPosicaoDaTarefa.getNovaPosicao() >= tamanhoDalistaDeTarefas
					? "A posição da tarefa não pode ser maior, nem igual a quantidade de tarefas do usuario"
					: "A posição enviada é igual a posição atual da tarefa, insira uma nova posição";
			throw APIException.build(HttpStatus.BAD_REQUEST, mensagem);
		}
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
		query.addCriteria(Criteria.where("idUsuario").is(idUsuario).and("status").is(StatusTarefa.CONCLUIDA));
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
				.mapToObj(i -> atualizaTarefaComNovaPosicao(tarefasDoUsuario.get(i), i)).collect(Collectors.toList());
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
	public void deletaTodasAsTarefasDoUsuario(List<Tarefa> tarefasDoUsuario) {
		log.info("[inicia] TarefaInfraRepository - deletaTodasAsTarefasDoUsuario");
		tarefaSpringMongoDBRepository.deleteAll(tarefasDoUsuario);
		log.info("[finaliza] TarefaInfraRepository - deletaTodasAsTarefasDoUsuario");
	}

	@Override
	public Optional<Tarefa> buscaTarefaAtivada() {
		log.info("[inicia] TarefaInfraRepository - buscarTarefaAtivada");
		Optional<Tarefa> tarefa = tarefaSpringMongoDBRepository.findFirstByStatusAtivacao(StatusAtivacaoTarefa.ATIVA);
		log.info("[finaliza] TarefaInfraRepository - buscarTarefaAtivada");
		return tarefa;
	}

	@Override
	public List<Tarefa> buscarTodasTarefasPorIdUsuario(UUID idUsuario) {
		log.info("[inicia] TarefaInfraRepository - buscarTodasTarefasPorIdUsuario");
		List<Tarefa> todasTarefas = tarefaSpringMongoDBRepository.findAllByIdUsuarioOrderByPosicaoAsc(idUsuario);
		log.info("[finaliza] TarefaInfraRepository - buscarTodasTarefasPorIdUsuario");
		return todasTarefas;
	}

	@Override
	public void deleta(Tarefa tarefa) {
		log.info("[inicia] TarefaInfraRepository - deleta");
		tarefaSpringMongoDBRepository.delete(tarefa);
		log.info("[finaliza] TarefaInfraRepository - deleta");
	}

	@Override
	public void processaStatusEContadorPomodoro(Usuario usuarioStatus) {
		log.info("[inicia] TarefaInfraRepository - modificaStatusUsuarioIncrementa");
		if (usuarioStatus.getStatus().equals(StatusUsuario.FOCO)) {
			if (this.contagemPomodoroPausaCurta < 3) {
				usuarioStatus.mudaStatusParaPausaCurta();
			} else {
				usuarioStatus.mudaStatusPausaLonga();
				this.contagemPomodoroPausaCurta = 0;
			}
		} else {
			usuarioStatus.alteraStatusParaFoco(usuarioStatus.getIdUsuario());
			this.contagemPomodoroPausaCurta++;
			;
		}
		Query query = Query.query(Criteria.where("idUsuario").is(usuarioStatus.getIdUsuario()));
		Update updateUsuario = Update.update("status", usuarioStatus.getStatus());
		mongoTemplate.updateMulti(query, updateUsuario, Usuario.class);
		log.info("[finaliza] TarefaInfraRepository - modificaStatusUsuarioIncrementa");
	}

}
