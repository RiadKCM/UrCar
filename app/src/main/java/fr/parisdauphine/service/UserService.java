package fr.parisdauphine.service;

import fr.parisdauphine.entity.User;
import org.springframework.security.crypto.bcrypt.BCrypt;
import fr.parisdauphine.repository.UserRepository;

import java.util.Optional;

public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String register(User user) {
        if (!ValidationService.isValidName(user.getNom()) || !ValidationService.isValidName(user.getPrenom())) {
            return "Nom/Prénom invalide.";
        }
        if (!ValidationService.isValidEmail(user.getEmail())) {
            return "Email invalide.";
        }
        if (!ValidationService.isValidPassword(user.getMotDePasse())) {
            return "Mot de passe invalide.";
        }
        if (!ValidationService.isValidPhoneNumber(user.getTelephone())) {
            return "Numéro de téléphone invalide.";
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            return "Email déjà utilisé.";
        }
        if (userRepository.existsByPhoneNumber(user.getTelephone())) {
            return "Téléphone déjà utilisé.";
        }

        String hashedPassword = BCrypt.hashpw(user.getMotDePasse(), BCrypt.gensalt());
        user.setMotDePasse(hashedPassword);

        userRepository.save(user);
        return "OK";
    }

    public Optional<User> login(String email, String password) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        return userOpt.filter(user -> BCrypt.checkpw(password, user.getMotDePasse()));
    }
}
