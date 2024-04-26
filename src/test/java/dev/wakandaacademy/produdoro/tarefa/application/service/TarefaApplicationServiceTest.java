package dev.wakandaacademy.produdoro.tarefa.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import dev.wakandaacademy.produdoro.DataHelper;
import dev.wakandaacademy.produdoro.handler.APIException;
import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaIdResponse;
import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaListResponse;
import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaRequest;
import dev.wakandaacademy.produdoro.tarefa.application.repository.TarefaRepository;
import dev.wakandaacademy.produdoro.tarefa.domain.StatusAtivacaoTarefa;
import dev.wakandaacademy.produdoro.tarefa.domain.Tarefa;
import dev.wakandaacademy.produdoro.usuario.application.repository.UsuarioRepository;
import dev.wakandaacademy.produdoro.usuario.domain.StatusUsuario;
import dev.wakandaacademy.produdoro.usuario.domain.Usuario;

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
    void deveListarTodasAsTarefas() {
    	// Dado
    	Usuario usuario = DataHelper.createUsuario();
    	List<Tarefa> tarefas = DataHelper.createListTarefa();
    	// Quando
    	when(usuarioRepository.buscaUsuarioPorEmail(any())).thenReturn(usuario);
    	when(usuarioRepository.buscaUsuarioPorId(any())).thenReturn(usuario);
    	when(tarefaRepository.buscarTodasTarefasPorIdUsuario(any())).thenReturn(tarefas);
    	
    	List<TarefaListResponse> resultado = tarefaApplicationService.buscarTodasTarefas(usuario.getEmail(), 
    			usuario.getIdUsuario());
    	
    	// Então
    	verify(usuarioRepository, times(1)).buscaUsuarioPorEmail(usuario.getEmail());
    	verify(usuarioRepository, times(1)).buscaUsuarioPorId(usuario.getIdUsuario());
    	verify(tarefaRepository, times(1)).buscarTodasTarefasPorIdUsuario(usuario.getIdUsuario());
    	assertEquals(resultado.size(), 8);
    }

    @Test
    void naoDeveBuscarTodasTarefasPorUsuario() {
    	Usuario usuario = DataHelper.createUsuario();

		when(usuarioRepository.buscaUsuarioPorEmail(any()))
				.thenThrow(APIException.build(HttpStatus.BAD_REQUEST, "Usuario não encontrado!"));

		APIException e = assertThrows(APIException.class, () -> tarefaApplicationService
				.buscarTodasTarefas("emailinvalido@gmail.com", usuario.getIdUsuario()));

		assertEquals(HttpStatus.BAD_REQUEST, e.getStatusException());
		assertEquals("Usuario não encontrado!", e.getMessage());
    }
    
    public TarefaRequest getTarefaRequest() {
        TarefaRequest request = new TarefaRequest("tarefa 1", UUID.randomUUID(), null, null, 0);
        return request;
    }
    
    @Test
    void deveDeletarTarefa() {
    	Usuario usuario = DataHelper.createUsuario();
    	Tarefa tarefa = DataHelper.createTarefa();
    	
    	when(usuarioRepository.buscaUsuarioPorEmail(any())).thenReturn(usuario);
    	when(tarefaRepository.buscaTarefaPorId(any())).thenReturn(Optional.of(tarefa));
    	tarefaApplicationService.deletaTarefa(tarefa.getIdTarefa(), usuario.getEmail());
    	
    	verify(tarefaRepository, times(1)).deleta(tarefa);
    }
    
    @Test
    void naoDeveDeletarTarefa() {
    	Usuario usuario = DataHelper.createUsuario();
    	String emailUsuario = "joao@gmail.com";
    	UUID idTarefa = UUID.fromString("06fb5521-9d5a-861a-82fb-e67e3bedcbbb");
    	Tarefa tarefa = DataHelper.createTarefa();
    	
		APIException e = assertThrows(APIException.class, () -> tarefaApplicationService
				.deletaTarefa(tarefa.getIdTarefa(),usuario.getEmail()));
		
		assertNotEquals(idTarefa, tarefa.getIdTarefa());
		assertNotEquals(emailUsuario, usuario.getEmail());
		assertEquals(HttpStatus.NOT_FOUND, e.getStatusException());
    }  
}
