package com.alexeymirniy.remindertelezhkabot.dao;

import com.alexeymirniy.remindertelezhkabot.entity.User;
import com.alexeymirniy.remindertelezhkabot.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserDao {

    private final UserRepository userRepository;

    @Autowired
    public UserDao(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User findByUserId(long id) {
        return userRepository.findById(id);
    }

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    public void removeUser(User user) {
        userRepository.delete(user);
    }

    public void saveUser(User user) {
        userRepository.save(user);
    }

    public boolean isExist(long id) {
        User user = findByUserId(id);
        return user != null;
    }
}
