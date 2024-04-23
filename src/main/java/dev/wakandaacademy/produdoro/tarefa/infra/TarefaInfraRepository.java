package dev.wakandaacademy.produdoro.tarefa.infra;

import dev.wakandaacademy.produdoro.handler.APIException;
import dev.wakandaacademy.produdoro.tarefa.application.api.NovaPosicaoDaTarefaRequest;
import dev.wakandaacademy.produdoro.tarefa.application.repository.TarefaRepository;
import dev.wakandaacademy.produdoro.tarefa.domain.Tarefa;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@Log4j2
@RequiredArgsConstructor
public class TarefaInfraRepository implements TarefaRepository {

    private final TarefaSpringMongoDBRepository tarefaSpringMongoDBRepository;

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
    public void defineNovaPosicaoDatarefa(Tarefa tarefa, List<Tarefa> tarefas, NovaPosicaoDaTarefaRequest novaPosicaoDaTarefaRequest) {
        log.info("[inicia] TarefaInfraRepository - defineNovaPosicaoDatarefa");
        
        log.info("[finaliza] TarefaInfraRepository - defineNovaPosicaoDatarefa");
    }
}
