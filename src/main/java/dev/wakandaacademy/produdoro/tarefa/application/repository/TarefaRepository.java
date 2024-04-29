package dev.wakandaacademy.produdoro.tarefa.application.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import dev.wakandaacademy.produdoro.tarefa.domain.Tarefa;

public interface TarefaRepository {

	Tarefa salva(Tarefa tarefa);

	Optional<Tarefa> buscaTarefaPorId(UUID idTarefa);

	void deletaTodasAsTarefasDoUsuario(List<Tarefa> tarefasDoUsuario);

	int contarTarefas(UUID idUsuario);

	List<Tarefa> buscaTarefasConcluidas(UUID idUsuario);

	void deletaVariasTarefas(List<Tarefa> tarefasConcluidas);

	void atualizaPosicaoDasTarefas(List<Tarefa> tarefasDoUsuario);

	void salvaVariasTarefas(List<Tarefa> tarefasDoUsuario);

	List<Tarefa> buscarTodasTarefasPorIdUsuario(UUID idUsuario);
}
