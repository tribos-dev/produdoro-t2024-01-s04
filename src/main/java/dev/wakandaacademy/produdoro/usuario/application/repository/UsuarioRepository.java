package dev.wakandaacademy.produdoro.usuario.application.repository;

import java.util.UUID;

import dev.wakandaacademy.produdoro.usuario.domain.Usuario;

public interface UsuarioRepository {
	Usuario salva(Usuario usuario);
	Usuario buscaUsuarioPorId(UUID idUsuario);
	Usuario buscaUsuarioPorEmail(String emailUsuario);
}
