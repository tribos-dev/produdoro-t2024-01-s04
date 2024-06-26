package dev.wakandaacademy.produdoro.tarefa.infra;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.mongodb.repository.MongoRepository;

import dev.wakandaacademy.produdoro.tarefa.domain.StatusAtivacaoTarefa;
import dev.wakandaacademy.produdoro.tarefa.domain.Tarefa;

public interface TarefaSpringMongoDBRepository extends MongoRepository<Tarefa, UUID> {
	Optional<Tarefa> findByIdTarefa(UUID idTarefa);

	Optional<Tarefa> findFirstByStatusAtivacao(StatusAtivacaoTarefa status);

	int countByIdUsuario(UUID idUsuario);

	List<Tarefa> findAllByIdUsuarioOrderByPosicaoAsc(UUID idUsuario);
}
