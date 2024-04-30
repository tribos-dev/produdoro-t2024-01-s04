package dev.wakandaacademy.produdoro.tarefa.application.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import dev.wakandaacademy.produdoro.tarefa.application.api.NovaPosicaoDaTarefaRequest;
import dev.wakandaacademy.produdoro.tarefa.domain.Tarefa;

public interface TarefaRepository {

	Tarefa salva(Tarefa tarefa);

	Optional<Tarefa> buscaTarefaPorId(UUID idTarefa);

	int contarTarefasDoUsuario(UUID idUsuario);

	void defineNovaPosicaoDaTarefa(Tarefa tarefa, List<Tarefa> tarefas,
			NovaPosicaoDaTarefaRequest novaPosicaoDaTarefaRequest);

	void salvaVariasTarefas(List<Tarefa> tarefasComNovasPosicoes);

	List<Tarefa> buscarTodasTarefasPorIdUsuario(UUID idUsuario);

	Optional<Tarefa> buscaTarefaAtivada();

	void deletaTodasAsTarefasDoUsuario(List<Tarefa> tarefasDoUsuario);

	int contarTarefas(UUID idUsuario);

	List<Tarefa> buscaTarefasConcluidas(UUID idUsuario);

	void deletaVariasTarefas(List<Tarefa> tarefasConcluidas);

	void atualizaPosicaoDasTarefas(List<Tarefa> tarefasDoUsuario);

	void deleta(Tarefa tarefa);
}
