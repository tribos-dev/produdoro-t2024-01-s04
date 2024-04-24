package dev.wakandaacademy.produdoro.tarefa.application.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;

@Value
public class NovaPosicaoDaTarefaRequest {
    @PositiveOrZero
    @NotNull
    private Integer novaPosicao;

    @JsonCreator
    public NovaPosicaoDaTarefaRequest(@JsonProperty("posicao") Integer novaPosicao) {
        this.novaPosicao = novaPosicao;
    }

}
