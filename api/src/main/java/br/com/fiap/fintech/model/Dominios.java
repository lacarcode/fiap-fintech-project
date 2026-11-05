package br.com.fiap.fintech.model;

import java.text.Normalizer;
import java.util.Locale;

/** Normaliza valores de domínio para o formato aceito pelos CHECKs do banco (D-04). */
public final class Dominios {

    private Dominios() {
    }

    /** "Variável" -> "VARIAVEL"; "Cheque especial" -> "CHEQUE_ESPECIAL"; null -> null. */
    public static String normalizar(String valor) {
        if (valor == null) {
            return null;
        }
        String semAcento = Normalizer.normalize(valor.trim(), Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return semAcento.toUpperCase(Locale.ROOT).replace(' ', '_');
    }
}
