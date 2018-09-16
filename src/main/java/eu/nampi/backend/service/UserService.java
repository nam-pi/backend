package eu.nampi.backend.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.nampi.backend.domain.User;
import eu.nampi.backend.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User createUser(String userName, String email) {
        User user = new User();
        user.setUserName(userName);
        user.setEmail(email);
        return userRepository.create(user);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

}