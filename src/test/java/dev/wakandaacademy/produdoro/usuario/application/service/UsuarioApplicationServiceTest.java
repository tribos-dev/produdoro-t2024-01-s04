package dev.wakandaacademy.produdoro.usuario.application.service;

import dev.wakandaacademy.produdoro.DataHelper;
import dev.wakandaacademy.produdoro.handler.APIException;
import dev.wakandaacademy.produdoro.usuario.application.repository.UsuarioRepository;
import dev.wakandaacademy.produdoro.usuario.domain.Usuario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import javax.xml.crypto.Data;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioApplicationServiceTest {

    // @Autowired
    @InjectMocks
    UsuarioApplicationService usuarioApplicationService;
    // @MockBean
    @Mock
    UsuarioRepository usuarioRepository;

    @Test
    void UsuarioMudaStatusPausaLongaSucesso() {
        Usuario usuario = DataHelper.createUsuario();
        when(usuarioRepository.buscaUsuarioPorEmail(any())).thenReturn(usuario);
        usuarioApplicationService.mudaStatusPausaLonga(usuario.getEmail(), usuario.getIdUsuario());
        verify(usuarioRepository, times(1)).salva(any());
    }

    @Test
    void UsuarioMudaStatusPausaLongaFalha() {
        Usuario usuario = DataHelper.createUsuario();
        when(usuarioRepository.buscaUsuarioPorEmail(any())).thenReturn(usuario);
        APIException ex = assertThrows(APIException.class,
                () -> usuarioApplicationService.mudaStatusPausaLonga(usuario.getEmail(), UUID.randomUUID()));
        assertEquals(APIException.class, ex.getClass());
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusException());
        assertEquals("Credencial de autenticação não é válida!", ex.getMessage());
    }

}