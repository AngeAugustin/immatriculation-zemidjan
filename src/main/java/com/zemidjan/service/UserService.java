package com.zemidjan.service;

import com.zemidjan.model.ERole;
import com.zemidjan.model.Role;
import com.zemidjan.model.User;
import com.zemidjan.repository.RoleRepository;
import com.zemidjan.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public User createUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional
    public User updateUser(User user) {
        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
    
    @Transactional
    public User addRoleToUser(User user, ERole roleName) {
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Erreur: Rôle non trouvé"));
        
        Set<Role> userRoles = user.getRoles();
        userRoles.add(role);
        user.setRoles(userRoles);
        
        return userRepository.save(user);
    }
    
    @Transactional
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
    
    @PostConstruct
    @Transactional
    public void initialiseRolesAndUsers() {
        // Création des rôles s'ils n'existent pas
        Arrays.stream(ERole.values()).forEach(role -> {
            if (roleRepository.findByName(role).isEmpty()) {
                roleRepository.save(new Role(role));
            }
        });
        
        // Création d'un utilisateur administrateur par défaut s'il n'existe pas
        if (!userRepository.existsByEmail("admin@zemidjan.com")) {
            User admin = new User();
            admin.setNom("Admin");
            admin.setPrenom("Système");
            admin.setEmail("admin@zemidjan.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setCommune("Cotonou");
            admin.setActive(true);
            
            Set<Role> adminRoles = new HashSet<>();
            roleRepository.findByName(ERole.ROLE_ADMIN).ifPresent(adminRoles::add);
            admin.setRoles(adminRoles);
            
            userRepository.save(admin);
        }
        
        // Création d'un utilisateur modérateur par défaut
        if (!userRepository.existsByEmail("moderateur@zemidjan.com")) {
            User moderateur = new User();
            moderateur.setNom("Modérateur");
            moderateur.setPrenom("Système");
            moderateur.setEmail("moderateur@zemidjan.com");
            moderateur.setPassword(passwordEncoder.encode("moderateur123"));
            moderateur.setCommune("Cotonou");
            moderateur.setActive(true);
            
            Set<Role> moderateurRoles = new HashSet<>();
            roleRepository.findByName(ERole.ROLE_MODERATEUR).ifPresent(moderateurRoles::add);
            moderateur.setRoles(moderateurRoles);
            
            userRepository.save(moderateur);
        }
        
        // Création d'un utilisateur standard par défaut
        if (!userRepository.existsByEmail("user@zemidjan.com")) {
            User user = new User();
            user.setNom("Utilisateur");
            user.setPrenom("Standard");
            user.setEmail("user@zemidjan.com");
            user.setPassword(passwordEncoder.encode("user123"));
            user.setCommune("Cotonou");
            user.setActive(true);
            
            Set<Role> userRoles = new HashSet<>();
            roleRepository.findByName(ERole.ROLE_USER).ifPresent(userRoles::add);
            user.setRoles(userRoles);
            
            userRepository.save(user);
        }
    }
}