package dev.wakandaacademy.produdoro.usuario.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import dev.wakandaacademy.produdoro.DataHelper;
import dev.wakandaacademy.produdoro.handler.APIException;
import dev.wakandaacademy.produdoro.usuario.application.repository.UsuarioRepository;
import dev.wakandaacademy.produdoro.usuario.domain.Usuario;

@ExtendWith(MockitoExtension.class)
class UsuarioApplicationServiceTest {

	@InjectMocks
	UsuarioApplicationService usuarioApplicationService;

	@Mock
	UsuarioRepository usuarioRepository;

	@Test
	void deveMudarStatusUsuarioParaFoco() {
		Usuario usuario = DataHelper.createUsuario();
		UUID idUsuario = UUID.fromString("a713162f-20a9-4db9-a85b-90cd51ab18f4");

		when(usuarioRepository.buscaUsuarioPorEmail(anyString())).thenReturn(usuario);
		when(usuarioRepository.buscaUsuarioPorId(idUsuario)).thenReturn(usuario);
		usuarioApplicationService.mudaStatusParaFoco(usuario.getEmail(), idUsuario);

		verify(usuarioRepository, times(1)).salva(any());
	}

	@Test
	void mudaStatusPausaLongaPositivo() {
		Usuario usuario = DataHelper.createUsuario();

		when(usuarioRepository.buscaUsuarioPorEmail(any())).thenReturn(usuario);
		usuarioApplicationService.mudaStatusPausaLonga(usuario.getEmail(), usuario.getIdUsuario());
		verify(usuarioRepository, times(1)).salva(any());
	}

	@Test
	void mudaStatusPausaLongaNegativo() {
		Usuario usuario = DataHelper.createUsuario();

		when(usuarioRepository.buscaUsuarioPorEmail(any())).thenReturn(usuario);
		APIException e = assertThrows(APIException.class,
				() -> usuarioApplicationService.mudaStatusPausaLonga(usuario.getEmail(), UUID.randomUUID()));

		assertEquals(APIException.class, e.getClass());
		assertEquals(HttpStatus.UNAUTHORIZED, e.getStatusException());
		assertEquals("Credencial de autenticação não é válida.", e.getMessage());
	}
}