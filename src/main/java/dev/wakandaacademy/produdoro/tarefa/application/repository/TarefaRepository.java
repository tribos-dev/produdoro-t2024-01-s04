package dev.wakandaacademy.produdoro.tarefa.application.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import dev.wakandaacademy.produdoro.tarefa.domain.Tarefa;
import dev.wakandaacademy.produdoro.usuario.domain.Usuario;

public interface TarefaRepository {

    Tarefa salva(Tarefa tarefa);
    Optional<Tarefa> buscaTarefaPorId(UUID idTarefa);
	int tamanhoDaLista(UUID idUsuario);
	List<Tarefa> buscaTarefasDoUsuario(UUID idUsuario);

}
