package dev.wakandaacademy.produdoro.tarefa.application.api;

import java.util.List;
import java.util.UUID;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/tarefa")
public interface TarefaAPI {
	@PostMapping
	@ResponseStatus(code = HttpStatus.CREATED)
	TarefaIdResponse postNovaTarefa(@RequestBody @Valid TarefaRequest tarefaRequest);

	@GetMapping("/{idTarefa}")
	@ResponseStatus(code = HttpStatus.OK)
	TarefaDetalhadoResponse detalhaTarefa(@RequestHeader(name = "Authorization", required = true) String token,
			@PathVariable UUID idTarefa);

	@PatchMapping("/{idTarefa}/incrementa-pomodoro")
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	void incrementaPomodoro(@RequestHeader(name = "Authorization") String token, @PathVariable UUID idTarefa);

	@GetMapping("/listar-tarefa/{idUsuario}")
	@ResponseStatus(code = HttpStatus.OK)
	List<TarefaListResponse> listarTodasTarefas(@RequestHeader(name = "Authorization", required = true) String token,
			@PathVariable UUID idUsuario);

	@PostMapping("/muda-ordem/{idTarefa}")
	@ResponseStatus(code = HttpStatus.ACCEPTED)
	void mudaOrdemdaTarefa(@RequestHeader(name = "Authorization") String token, @PathVariable UUID idTarefa,
			@RequestBody @Valid NovaPosicaoDaTarefaRequest novaPosicaoDaTarefaRequest);

	@PatchMapping("/concluiTarefa/{idTarefa}")
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	void concluiTarefa(@RequestHeader(name = "Authorization", required = true) String token,
			@PathVariable UUID idTarefa);

	@PatchMapping("/{idTarefa}/ativa")
	@ResponseStatus(code = HttpStatus.OK)
	void definiTarefaComoAtiva(@RequestHeader(name = "Authorization", required = true) String token,
			@PathVariable UUID idTarefa);

	@DeleteMapping(value = "/deleta-tarefa/{idTarefa}")
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	void deletaTarefa(@PathVariable UUID idTarefa,
			@RequestHeader(name = "Authorization", required = true) String token);

	@DeleteMapping("/usuario/{idUsuario}/limpar-todas-as-tarefas")
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	void deletaTodasAsTarefasDoUsuario(@RequestHeader(name = "Authorization", required = true) String token,
			@PathVariable UUID idUsuario);

	@PatchMapping("/editaTarefa/{idTarefa}")
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	void editaTarefa(@RequestHeader(name = "Authorization", required = true) String token, @PathVariable UUID idTarefa,
			@RequestBody @Valid EditaTarefaRequest tarefaRequest);

	@DeleteMapping("/usuario/{idUsuario}/deleta-tarefas-concluidas")
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	void deletaTarefasConcluidas(@RequestHeader(name = "Authorization", required = true) String token,
			@PathVariable UUID idUsuario);
}
