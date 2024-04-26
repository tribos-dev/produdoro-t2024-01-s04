package dev.wakandaacademy.produdoro.tarefa.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import dev.wakandaacademy.produdoro.DataHelper;
import dev.wakandaacademy.produdoro.handler.APIException;
import dev.wakandaacademy.produdoro.tarefa.domain.StatusAtivacaoTarefa;
import dev.wakandaacademy.produdoro.usuario.application.repository.UsuarioRepository;
import dev.wakandaacademy.produdoro.usuario.domain.Usuario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaIdResponse;
import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaRequest;
import dev.wakandaacademy.produdoro.tarefa.application.repository.TarefaRepository;
import dev.wakandaacademy.produdoro.tarefa.domain.Tarefa;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class TarefaApplicationServiceTest {

    //	@Autowired
    @InjectMocks
    TarefaApplicationService tarefaApplicationService;

    //	@MockBean
    @Mock
    TarefaRepository tarefaRepository;

    @Mock
    UsuarioRepository usuarioRepository;

    @Test
    void deveRetornarIdTarefaNovaCriada() {
        TarefaRequest request = getTarefaRequest();
        when(tarefaRepository.salva(any())).thenReturn(new Tarefa(request));

        TarefaIdResponse response = tarefaApplicationService.criaNovaTarefa(request);

        assertNotNull(response);
        assertEquals(TarefaIdResponse.class, response.getClass());
        assertEquals(UUID.class, response.getIdTarefa().getClass());
    }

    @Test
    void deveDefinirTarefaComoAtiva(){
        Usuario usuario = DataHelper.createUsuario();
        Tarefa tarefa = DataHelper.createTarefa();
        Tarefa tarefaAtiva = getTarefaAtiva(usuario);


        when(usuarioRepository.buscaUsuarioPorEmail(any())).thenReturn(usuario);
        when(tarefaRepository.buscaTarefaPorId(tarefa.getIdTarefa())).thenReturn(Optional.of(tarefa));
        when(tarefaRepository.buscaTarefaAtivada()).thenReturn(Optional.ofNullable(tarefaAtiva));

        tarefaApplicationService.definiTarefaComoAtiva(String.valueOf(usuario), tarefa.getIdTarefa());

        verify(tarefaRepository, times(1)).salva(tarefa);
        verify(tarefaRepository, times(1)).buscaTarefaAtivada();
        verify(tarefaRepository, times(1)).salva(tarefa);
    }

    @Test
    void nãoDeveDefinirTarefaComoAtiva() {
        Usuario usuario = DataHelper.createUsuario();
        UUID idTarefaInvalido = UUID.randomUUID();

        when(usuarioRepository.buscaUsuarioPorEmail(any())).thenReturn(usuario);
        when(tarefaRepository.buscaTarefaPorId(idTarefaInvalido))
                .thenThrow(APIException.build(HttpStatus.NOT_FOUND, "Id Da Tarefa Inválido"));

        APIException e = assertThrows(APIException.class, () -> {
            tarefaApplicationService.definiTarefaComoAtiva(String.valueOf(usuario), idTarefaInvalido);
        });

        assertEquals(HttpStatus.NOT_FOUND, e.getStatusException());
        verify(tarefaRepository, never()).buscaTarefaAtivada();
        verify(tarefaRepository, never()).salva(any(Tarefa.class));
    }

    private static Tarefa getTarefaAtiva(Usuario usuario) {
        return Tarefa.builder().contagemPomodoro(1).idTarefa(UUID.fromString("4c70c27a-446c-4506-b666-1067085d8d85"))
                .idUsuario(usuario.getIdUsuario()).descricao("descricao tarefa")
                .statusAtivacao(StatusAtivacaoTarefa.ATIVA).build();
    }

    public TarefaRequest getTarefaRequest() {
        TarefaRequest request = new TarefaRequest("tarefa 1", UUID.randomUUID(), null, null, 0);
        return request;
    }
}
