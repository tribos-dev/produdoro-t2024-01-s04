package dev.wakandaacademy.produdoro.tarefa.infra;

import dev.wakandaacademy.produdoro.handler.APIException;
import dev.wakandaacademy.produdoro.tarefa.application.api.NovaPosicaoDaTarefaRequest;
import dev.wakandaacademy.produdoro.tarefa.application.repository.TarefaRepository;
import dev.wakandaacademy.produdoro.tarefa.domain.Tarefa;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


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
    public int contarTarefasDoUsuario(UUID idUsuario) {
        log.info("[inicia] TarefaInfraRepository - contarTarefasDoUsuario");
        int contarTarefas = tarefaSpringMongoDBRepository.countByIdUsuario(idUsuario);
        log.info("[finaliza] TarefaInfraRepository - contarTarefasDoUsuario");
        return contarTarefas;
    }

    @Override
    public void defineNovaPosicaoDaTarefa(Tarefa tarefa, List<Tarefa> tarefas, NovaPosicaoDaTarefaRequest novaPosicaoDaTarefa) {
        log.info("[inicia] TarefaInfraRepository - defineNovaPosicaoDatarefa");
        validaNovaPosicao(tarefas,tarefa,novaPosicaoDaTarefa);
        int posicaoAntiga = tarefa.getPosicao();
        int novaPosicao = novaPosicaoDaTarefa.getNovaPosicao();
        List<Tarefa> tarefasComPosicaoAtualizada = IntStream
                .range(Math.min(novaPosicao, posicaoAntiga), Math.max(novaPosicao, posicaoAntiga))
                .mapToObj(i -> atualizaPosicaoTarefas(tarefas.get(i), novaPosicao < posicaoAntiga, tarefas))
                .collect(Collectors.toList());
        tarefa.setPosicao(novaPosicao);
        salvaVariasTarefas(tarefasComPosicaoAtualizada);
        salva(tarefa);
    }

    @Override
    public void salvaVariasTarefas(List<Tarefa> tarefasComPosicaoAtualizada) {
        log.info("[inicia] TarefaInfraRepository - salvaVariasTarefas");
        tarefaSpringMongoDBRepository.saveAll(tarefasComPosicaoAtualizada);
        log.info("[finaliza] TarefaInfraRepository - salvaVariasTarefas");
    }


    private Tarefa atualizaPosicaoTarefas(Tarefa tarefa, Boolean incrementa, List<Tarefa> tarefas) {
        if (incrementa)
            tarefa.incrementaPosicao(tarefas.size());
        else
           tarefa.decrementaPosicao();
        Query queryAtualizacao = new Query(Criteria.where("idTarefa").is(tarefa.getIdTarefa()));
        Update updateAtualizacao = new Update().set("posicao", tarefa.getPosicao());
        mongoTemplate.updateFirst(queryAtualizacao, updateAtualizacao, Tarefa.class);
        return tarefa;
    }


    private void validaNovaPosicao(List<Tarefa> tarefas, Tarefa tarefa, NovaPosicaoDaTarefaRequest novaPosicaoDaTarefa) {
        int posicaoAntiga = tarefa.getPosicao();
        int tamanhoDalistaDeTarefas = tarefas.size();

        if (novaPosicaoDaTarefa.getNovaPosicao() >= tamanhoDalistaDeTarefas || novaPosicaoDaTarefa.getNovaPosicao().equals(posicaoAntiga)) {
            String mensagem = novaPosicaoDaTarefa.getNovaPosicao() >= tamanhoDalistaDeTarefas
                    ?"A posição da tarefa não pode ser maior, nem igual a quantidade de tarefas do usuario"
                    :"A posição enviada é igual a posição atual da tarefa, insira uma nova posição";
            throw APIException.build(HttpStatus.BAD_REQUEST, mensagem);
        }
    }
    @Override
    public List<Tarefa> buscarTodasTarefasPorIdUsuario(UUID idUsuario) {
        log.info("[inicia] TarefaInfraRepository - buscarTodasTarefasPorIdUsuario");
        List<Tarefa> todasTarefas = tarefaSpringMongoDBRepository.findAllByIdUsuarioOrderByPosicaoAsc(idUsuario);
        log.info("[finaliza] TarefaInfraRepository - buscarTodasTarefasPorIdUsuario");
        return todasTarefas;
    }
}
