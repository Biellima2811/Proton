package com.mycompany.proton.util;

import java.util.regex.Pattern;

public class Validator {

    public static boolean isCpfCnpjValido(String valor) {
        String numeros = valor.replaceAll("\\D", "");
        if (numeros.length() == 11) return isValidCPF(numeros);
        if (numeros.length() == 14) return isValidCNPJ(numeros);
        return false;
    }

    private static boolean isValidCPF(String cpf) {
        // Algoritmo de validação CPF
        if (cpf.length() != 11) return false;
        if (cpf.chars().allMatch(c -> c == cpf.charAt(0))) return false;
        int[] digitos = new int[11];
        for (int i = 0; i < 11; i++) digitos[i] = Character.getNumericValue(cpf.charAt(i));
        int soma = 0;
        for (int i = 0; i < 9; i++) soma += digitos[i] * (10 - i);
        int resto = 11 - (soma % 11);
        int dv1 = (resto == 10 || resto == 11) ? 0 : resto;
        if (dv1 != digitos[9]) return false;
        soma = 0;
        for (int i = 0; i < 10; i++) soma += digitos[i] * (11 - i);
        resto = 11 - (soma % 11);
        int dv2 = (resto == 10 || resto == 11) ? 0 : resto;
        return dv2 == digitos[10];
    }

    private static boolean isValidCNPJ(String cnpj) {
        // Algoritmo de validação CNPJ (similar, implementar)
        return true; // placeholder
    }

    public static boolean isEmailValido(String email) {
        String regex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return Pattern.matches(regex, email);
    }

    public static boolean isTelefoneValido(String telefone) {
        String numeros = telefone.replaceAll("\\D", "");
        return numeros.length() >= 10 && numeros.length() <= 11;
    }
}