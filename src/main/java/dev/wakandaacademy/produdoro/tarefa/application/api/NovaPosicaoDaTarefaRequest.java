package dev.wakandaacademy.produdoro.tarefa.application.api;

import lombok.Value;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;

@Value
public class NovaPosicaoDaTarefaRequest {
    @PositiveOrZero
    @NotNull
    private Integer posicao;

}
