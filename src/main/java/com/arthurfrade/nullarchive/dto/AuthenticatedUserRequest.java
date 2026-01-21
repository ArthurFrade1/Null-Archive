package com.arthurfrade.nullarchive.dto;

public class AuthenticatedUserRequest {
        public final long id;
        public final String passwordHash;
    
        public AuthenticatedUserRequest(long id, String passwordHash) {
            this.id = id;
            this.passwordHash = passwordHash;
        }
    }
