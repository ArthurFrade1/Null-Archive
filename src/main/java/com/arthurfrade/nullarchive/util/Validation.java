package com.arthurfrade.nullarchive.util;

import com.arthurfrade.nullarchive.dto.LoginRequest;
import com.arthurfrade.nullarchive.dto.RegisterRequest;

public class Validation {

    public static String validateRegister(RegisterRequest req) {
        if (req.username == null || req.username.isBlank()) return "Username obrigatório";
        if (req.email == null || req.email.isBlank()) return "Email obrigatório";
        if (!req.email.contains("@")) return "Email inválido";
        if (req.password == null || req.password.length() < 6) return "Senha deve ter ao menos 6 caracteres";
        return null;
    }

    public static String validateLogin(LoginRequest req) {
        if (req.email == null || req.email.isBlank()) return "Email obrigatório";
        if (!req.email.contains("@")) return "Email inválido";
        if (req.password == null || req.password.isBlank()) return "Senha obrigatória";
        return null;
    }
}
