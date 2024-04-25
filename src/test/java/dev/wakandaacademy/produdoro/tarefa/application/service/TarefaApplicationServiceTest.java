package dev.wakandaacademy.produdoro.tarefa.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import dev.wakandaacademy.produdoro.DataHelper;
import dev.wakandaacademy.produdoro.handler.APIException;
import dev.wakandaacademy.produdoro.tarefa.application.api.NovaPosicaoDaTarefaRequest;
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

        when(tarefaRepository.salva(any())).thenReturn(new Tarefa(request,0));

        TarefaIdResponse response = tarefaApplicationService.criaNovaTarefa(request);

        assertNotNull(response);
        assertEquals(TarefaIdResponse.class, response.getClass());
        assertEquals(UUID.class, response.getIdTarefa().getClass());
    }


    @Test
    void mudaOrdemDAtarefaTest(){

        Tarefa tarefa = DataHelper.createTarefa();
        List<Tarefa> tarefas = DataHelper.createListTarefa();
        Usuario usuario = DataHelper.createUsuario();
        NovaPosicaoDaTarefaRequest novaPosicao = new NovaPosicaoDaTarefaRequest(1);

        when(tarefaRepository.buscaTarefaPorId(any())).thenReturn(Optional.of(tarefa));
        when(usuarioRepository.buscaUsuarioPorEmail(any())).thenReturn(usuario);
        when(tarefaRepository.buscaTodasAsTarefasDoUsuario(tarefa.getIdUsuario())).thenReturn(tarefas);

        tarefaApplicationService.mudaOrdemDatarefa(usuario.getEmail(),tarefa.getIdTarefa(),novaPosicao);

        verify(tarefaRepository, times(1)).defineNovaPosicaoDatarefa(tarefa,tarefas,novaPosicao);
    }

    @Test
    void NãoMudaOrdemDAtarefaTest(){

        Usuario usuario = DataHelper.createUsuario();
        Tarefa tarefaNãoExiste = DataHelper.createTarefa();
        NovaPosicaoDaTarefaRequest novaPosicao = new NovaPosicaoDaTarefaRequest(1);

        when(tarefaRepository.buscaTarefaPorId(any())).thenReturn(Optional.empty());
        when(usuarioRepository.buscaUsuarioPorEmail(any())).thenReturn(usuario);

        assertThrows(APIException.class, () -> tarefaApplicationService.mudaOrdemDatarefa(usuario.getEmail(),
                    tarefaNãoExiste.getIdTarefa(), novaPosicao));

        verify(tarefaRepository, never()).defineNovaPosicaoDatarefa(any(), any(), any());
        }



    public TarefaRequest getTarefaRequest() {
        TarefaRequest request = new TarefaRequest("tarefa 1", UUID.randomUUID(), null, null, 0);
        return request;
    }
}
