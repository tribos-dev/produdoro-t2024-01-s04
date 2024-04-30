package dev.wakandaacademy.produdoro.usuario.application.service;

import java.util.UUID;

import dev.wakandaacademy.produdoro.usuario.application.api.UsuarioCriadoResponse;
import dev.wakandaacademy.produdoro.usuario.application.api.UsuarioNovoRequest;

public interface UsuarioService {
	UsuarioCriadoResponse criaNovoUsuario(UsuarioNovoRequest usuarioNovo);

	void mudaStatusParaPausaCurta(UUID idUsuario, String usuarioEmail);

	UsuarioCriadoResponse buscaUsuarioPorId(UUID idUsuario);

	void mudaStatusParaFoco(String usuario, UUID idUsuario);

	void mudaStatusPausaLonga(String usuario, UUID idUsuario);
}
