package dev.wakandaacademy.produdoro.usuario.application.service;

import java.util.UUID;

import javax.validation.Valid;

import org.springframework.stereotype.Service;

import dev.wakandaacademy.produdoro.credencial.application.service.CredencialService;
import dev.wakandaacademy.produdoro.pomodoro.application.service.PomodoroService;
import dev.wakandaacademy.produdoro.usuario.application.api.UsuarioCriadoResponse;
import dev.wakandaacademy.produdoro.usuario.application.api.UsuarioNovoRequest;
import dev.wakandaacademy.produdoro.usuario.application.repository.UsuarioRepository;
import dev.wakandaacademy.produdoro.usuario.domain.Usuario;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
@RequiredArgsConstructor
public class UsuarioApplicationService implements UsuarioService {
	private final PomodoroService pomodoroService;
	private final CredencialService credencialService;
	private final UsuarioRepository usuarioRepository;

	@Override
	public UsuarioCriadoResponse criaNovoUsuario(@Valid UsuarioNovoRequest usuarioNovo) {
		log.info("[inicia] UsuarioApplicationService - criaNovoUsuario");
		var configuracaoPadrao = pomodoroService.getConfiguracaoPadrao();
		credencialService.criaNovaCredencial(usuarioNovo);
		var usuario = new Usuario(usuarioNovo, configuracaoPadrao);
		usuarioRepository.salva(usuario);
		log.info("[finaliza] UsuarioApplicationService - criaNovoUsuario");
		return new UsuarioCriadoResponse(usuario);
	}

	@Override
	public UsuarioCriadoResponse buscaUsuarioPorId(UUID idUsuario) {
		log.info("[inicia] UsuarioApplicationService - buscaUsuarioPorId");
		Usuario usuario = usuarioRepository.buscaUsuarioPorId(idUsuario);
		log.info("[finaliza] UsuarioApplicationService - buscaUsuarioPorId");
		return new UsuarioCriadoResponse(usuario);
	}

	@Override
	public void mudaStatusParaFoco(String usuario, UUID idUsuario) {
		log.info("[inicia] UsuarioApplicationService - mudaStatusParaFoco");
		Usuario usuarioEmail = usuarioRepository.buscaUsuarioPorEmail(usuario);
		usuarioRepository.buscaUsuarioPorId(idUsuario);
		usuarioEmail.alteraStatusParaFoco(idUsuario);
		usuarioRepository.salva(usuarioEmail);
		log.info("[finaliza] UsuarioApplicationService - mudaStatusParaFoco");
	}

	@Override
	public void mudaStatusPausaLonga(String usuarioT, UUID idUsuario) {
		log.info("[inicia] UsuarioApplicationService - mudaStatusPausaLonga");
		usuarioRepository.buscaUsuarioPorId(idUsuario);
		Usuario usuario = usuarioRepository.buscaUsuarioPorEmail(usuarioT);
		usuario.validaUsuario(idUsuario);
		usuario.mudaStatusPausaLonga();
		usuarioRepository.salva(usuario);
		log.info("[finaliza] UsuarioApplicationService - mudaStatusPausaLonga");
	}
}
