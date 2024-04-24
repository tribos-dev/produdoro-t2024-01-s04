package dev.wakandaacademy.produdoro.tarefa.application.repository;

import dev.wakandaacademy.produdoro.tarefa.application.api.NovaPosicaoDaTarefaRequest;
import dev.wakandaacademy.produdoro.tarefa.domain.Tarefa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TarefaRepository {

    Tarefa salva(Tarefa tarefa);
    Optional<Tarefa> buscaTarefaPorId(UUID idTarefa);
    int contarTarefasDoUsuario(UUID idUsuario);
    void defineNovaPosicaoDatarefa(Tarefa tarefa, List<Tarefa> tarefas, NovaPosicaoDaTarefaRequest novaPosicaoDaTarefaRequest);
    void salvaVariasTarefas(List<Tarefa> tarefasComNovasPosicoes);
    List<Tarefa> buscaTodasAsTarefasDoUsuario(UUID idUsuario);
}
