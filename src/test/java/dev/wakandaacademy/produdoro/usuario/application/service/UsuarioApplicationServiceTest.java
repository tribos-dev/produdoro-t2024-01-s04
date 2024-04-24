package dev.wakandaacademy.produdoro.usuario.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import static org.mockito.Mockito.times;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import dev.wakandaacademy.produdoro.DataHelper;
import dev.wakandaacademy.produdoro.handler.APIException;
import dev.wakandaacademy.produdoro.usuario.application.repository.UsuarioRepository;
import dev.wakandaacademy.produdoro.usuario.domain.StatusUsuario;
import dev.wakandaacademy.produdoro.usuario.domain.Usuario;

@ExtendWith(MockitoExtension.class)
class UsuarioApplicationServiceTest {
	
	@InjectMocks
	private UsuarioApplicationService usuarioApplicationService;
	
	@Mock
	private UsuarioRepository usuarioRepository;
	
	@Test
	void deveAlterarStatusParaPausaCurta() {
		// Dado que
		Usuario usuario = DataHelper.createUsuario();
		
		// Quando
		when(usuarioRepository.buscaUsuarioPorEmail(any())).thenReturn(usuario);
		when(usuarioRepository.buscaUsuarioPorId(any())).thenReturn(usuario);
		usuarioApplicationService.mudaStatusParaPausaCurta(usuario.getIdUsuario(), usuario.getEmail());
		
		// Então
		verify(usuarioRepository,times(1)).salva(usuario);
		assertEquals(StatusUsuario.PAUSA_CURTA, usuario.getStatus());
	}
	
	@Test
	void naoDeveMudarStatusParaPausaCurta() {
	    Usuario usuario = DataHelper.createUsuario();
	    UUID idUsuario = UUID.fromString("b92ee6fa-9ae9-45ac-afe0-fb8e4460d839");
	    when(usuarioRepository.buscaUsuarioPorEmail(anyString())).thenReturn(usuario);
	    APIException e = assertThrows(APIException.class,
	          () -> usuarioApplicationService.mudaStatusParaPausaCurta(idUsuario, usuario.getEmail()));
	    assertEquals(HttpStatus.UNAUTHORIZED, e.getStatusException());
	}

}