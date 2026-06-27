package com.mycompany.proton.util;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.prefs.Preferences;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import org.mindrot.jbcrypt.BCrypt;

public class Seguranca {

    // Gera um hash seguro para uma senha em texto puro
    public static String hashSenha(String senhaPura) {
        return BCrypt.hashpw(senhaPura, BCrypt.gensalt(12)); // O fator 12 é o custo computacional, muito seguro
    }

    // Verifica se a senha digitada pelo usuário confere com o hash salvo no banco
    public static boolean verificarSenha(String senhaPura, String hashSalvo) {
        try {
            return BCrypt.checkpw(senhaPura, hashSalvo);
        } catch (Exception e) {
            return false;
        }
    }
    /**
     * Obtém a chave mestra do Registro da Máquina (Windows/Linux/Mac).
     * Se não existir, gera uma chave aleatória de 128-bits e salva no registro.
     */
    private static SecretKeySpec getChaveMestra() throws Exception{
        // Acessa o nó de registro exclusivo para esta classe
        Preferences prefs = Preferences.userNodeForPackage(Seguranca.class);
        
        // Tenta ler a chave salva. Retorna null se for a primeira vez rodando no PC
        String chaveBase64 = prefs.get("proton_aes_mestra", null);
        
        byte[] keyBytes;
        
        if (chaveBase64 == null) {
            // 1. Gera uma chave aleatória de alta segurança (16 bytes = 128 bits)
            keyBytes = new byte[16];
            new SecureRandom().nextBytes(keyBytes);
            // 2. Converte para String e salva no Registro do Windows de forma oculta
            prefs.put("proton_aes_mestra", Base64.getEncoder().encodeToString(keyBytes));
        } else {
            // 3. Se a chave já existir no registro, apenas lê e decodifica
            keyBytes = Base64.getDecoder().decode(chaveBase64);
        }
        return new SecretKeySpec(keyBytes, "AES");
    }
    
    public static String criptografar(String valor) {
        if (valor == null || valor.isEmpty()) return valor;
        try {
            SecretKeySpec skeySpec = getChaveMestra(); // Puxa a chave dinâmica do SO
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
            byte[] criptografado = cipher.doFinal(valor.getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(criptografado); 
        } catch (Exception ex) {
            System.out.println("Erro ao criptografar: " + ex.getMessage());
            return valor;
        }
    }
    public static String descriptografar(String valorCriptografado) {
        if (valorCriptografado == null || valorCriptografado.isEmpty()) return valorCriptografado;
        try {
            SecretKeySpec skeySpec = getChaveMestra(); // Puxa a chave dinâmica do SO
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec);
            byte[] original = cipher.doFinal(Base64.getDecoder().decode(valorCriptografado));
            return new String(original, "UTF-8"); 
        } catch (Exception ex) {
            // Se o arquivo antigo estiver em texto puro, cai aqui e devolve o próprio texto!
            return valorCriptografado; 
        }
    }
}