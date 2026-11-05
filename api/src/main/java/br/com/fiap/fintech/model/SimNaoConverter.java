package br.com.fiap.fintech.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/** Booleano Java <-> CHAR(1) 'S'/'N' do banco. */
@Converter
public class SimNaoConverter implements AttributeConverter<Boolean, String> {

    @Override
    public String convertToDatabaseColumn(Boolean valor) {
        return valor == null ? null : (valor ? "S" : "N");
    }

    @Override
    public Boolean convertToEntityAttribute(String valor) {
        return valor == null ? null : "S".equals(valor.trim());
    }
}
