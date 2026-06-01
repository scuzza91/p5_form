package com.formulario.config;

import com.formulario.model.Usuario;
import com.formulario.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);

    @Autowired
    private AuthService authService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.info("🔍 CustomUserDetailsService: Buscando usuario: " + username);
        
        // Buscar el usuario por username
        var usuarioOpt = authService.buscarPorUsername(username);
        
        if (usuarioOpt.isEmpty()) {
            logger.info("❌ CustomUserDetailsService: Usuario no encontrado: " + username);
            throw new UsernameNotFoundException("Usuario no encontrado: " + username);
        }
        
        Usuario usuario = usuarioOpt.get();
        logger.info("✅ CustomUserDetailsService: Usuario encontrado: " + usuario.getUsername());
        logger.info("🔐 CustomUserDetailsService: Contraseña encriptada: " + usuario.getPassword().substring(0, 20) + "...");
        logger.info("👤 CustomUserDetailsService: Usuario activo: " + usuario.isActivo());
        
        // Verificar que el usuario esté activo
        if (!usuario.isActivo()) {
            logger.info("❌ CustomUserDetailsService: Usuario inactivo: " + username);
            throw new UsernameNotFoundException("Usuario inactivo: " + username);
        }
        
        // Crear las autoridades basadas en el rol del usuario
        String rol = "ROLE_" + usuario.getRol().name();
        logger.info("🔑 CustomUserDetailsService: Rol asignado: " + rol);
        
        UserDetails userDetails = User.builder()
                .username(usuario.getUsername())
                .password(usuario.getPassword()) // La contraseña ya está encriptada
                .authorities(Collections.singletonList(new SimpleGrantedAuthority(rol)))
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(!usuario.isActivo())
                .build();
        
        logger.info("✅ CustomUserDetailsService: UserDetails creado exitosamente");
        return userDetails;
    }
} 