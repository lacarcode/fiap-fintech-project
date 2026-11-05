package br.com.fiap.fintech.service;

import br.com.fiap.fintech.exception.RegraNegocioException;
import br.com.fiap.fintech.model.Usuario;

/** Regra comum: um registro (conta ou lançamento) não muda de dono num PUT. */
final class Posse {

    private Posse() {
    }

    static void garantirMesmoDono(Usuario donoAtual, Long usuarioIdPedido) {
        if (donoAtual != null && !donoAtual.getId().equals(usuarioIdPedido)) {
            throw new RegraNegocioException("Não é permitido transferir o registro para outro usuário");
        }
    }
}
