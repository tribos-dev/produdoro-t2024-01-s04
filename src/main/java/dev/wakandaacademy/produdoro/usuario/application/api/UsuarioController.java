package dev.wakandaacademy.produdoro.usuario.application.api;

import java.util.UUID;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import dev.wakandaacademy.produdoro.config.security.service.TokenService;
import dev.wakandaacademy.produdoro.handler.APIException;
import dev.wakandaacademy.produdoro.usuario.application.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@RestController
@Validated
@Log4j2
@RequiredArgsConstructor
public class UsuarioController implements UsuarioAPI {
	private final UsuarioService usuarioAppplicationService;
	private final TokenService tokenService;

	@Override
	public UsuarioCriadoResponse postNovoUsuario(@Valid UsuarioNovoRequest usuarioNovo) {
		log.info("[inicia] UsuarioController - postNovoUsuario");
		UsuarioCriadoResponse usuarioCriado = usuarioAppplicationService.criaNovoUsuario(usuarioNovo);
		log.info("[finaliza] UsuarioController - postNovoUsuario");
		return usuarioCriado;
	}

	@Override
	public UsuarioCriadoResponse buscaUsuarioPorId(UUID idUsuario) {
		log.info("[inicia] UsuarioController - buscaUsuarioPorId");
		log.info("[idUsuario] {}", idUsuario);
		UsuarioCriadoResponse buscaUsuario = usuarioAppplicationService.buscaUsuarioPorId(idUsuario);
		log.info("[finaliza] UsuarioController - buscaUsuarioPorId");
		return buscaUsuario;
	}

	@Override
	public void mudaStatusParaPausaCurta(UUID idUsuario, String token) {
		log.info("[inicia] UsuarioController - mudaStatusParaPausaCurta");
		log.info("[idUsuario] {}", idUsuario);
		String usuarioEmail = tokenService.getUsuarioByBearerToken(token)
				.orElseThrow(() -> APIException.build(HttpStatus.UNAUTHORIZED, token));
		usuarioAppplicationService.mudaStatusParaPausaCurta(idUsuario, usuarioEmail);
		log.info("[finaliza] UsuarioController - mudaStatusParaPausaCurta");
	}

	@Override
	public void mudaStatusParaFoco(String token, UUID idUsuario) {
		log.info("[inicia] UsuarioController - mudaStatusParaFoco");
		log.info("[idUsuario] {}", idUsuario);
		String usuario = validaTokenUsuario(token);
		usuarioAppplicationService.mudaStatusParaFoco(usuario, idUsuario);
		log.info("[finaliza] UsuarioController - mudaStatusParaFoco");
	}

	@Override
	public void mudaStatusPausaLonga(String token, UUID idUsuario) {
		log.info("[inicia] TarefaRestController - mudaStatusPausaLonga");
		log.info("[idUsuario] {}", idUsuario);
		String usuarioT = validaTokenUsuario(token);
		usuarioAppplicationService.mudaStatusPausaLonga(usuarioT, idUsuario);
		log.info("[finaliza] TarefaRestController - mudaStatusPausaLonga");
	}

	private String validaTokenUsuario(String token) {
		String usuario = tokenService.getUsuarioByBearerToken(token).orElseThrow(
				() -> APIException.build(HttpStatus.UNAUTHORIZED, "Credencial de autenticação não é valida."));
		return usuario;
	}

}
