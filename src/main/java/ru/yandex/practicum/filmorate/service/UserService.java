package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public Collection<User> findAll() {
        return userStorage.findAll();
    }

    public User findById(Long id) {
        return userStorage.findById(id)
                .orElseThrow(() -> {
                    log.warn("Пользователь с id = {} не найден", id);
                    return new NotFoundException("Пользователь с id = " + id + " не найден");
                });
    }

    public User create(User user) {
        validate(user);
        return userStorage.create(user);
    }

    public User update(User user) {
        validate(user);
        return userStorage.update(user);
    }

    public void addFriend(Long userId, Long friendId) {
        User user = findById(userId);
        User friend = findById(friendId);
        if (userId.equals(friendId)) {
            log.warn("Ошибка добавления в друзья: пользователь с id = {} пытается добавить сам себя", userId);
            throw new ValidationException("Пользователь не может добавить сам себя в друзья");
        }
        user.getFriends().add(friendId);
        friend.getFriends().add(userId);
        log.info("Пользователи с id = {} и id = {} теперь друзья", userId, friendId);
    }

    public void removeFriend(Long userId, Long friendId) {
        User user = findById(userId);
        User friend = findById(friendId);
        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);
        log.info("Пользователи с id = {} и id = {} удалены из друзей друг друга", userId, friendId);
    }

    public List<User> getFriends(Long userId) {
        User user = findById(userId);
        log.debug("Запрос списка друзей пользователя с id = {}", userId);
        return user.getFriends().stream()
                .map(this::findById)
                .toList();
    }

    public List<User> getCommonFriends(Long userId, Long otherId) {
        User user = findById(userId);
        User other = findById(otherId);
        Set<Long> otherFriends = other.getFriends();
        log.debug("Запрос общих друзей пользователей с id = {} и id = {}", userId, otherId);
        return user.getFriends().stream()
                .filter(otherFriends::contains)
                .map(this::findById)
                .toList();
    }

    private void validate(User user) {
        if (user == null) {
            log.warn("Ошибка валидации пользователя: передан пустой запрос");
            throw new ValidationException("Пользователь не может быть null");
        }
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            log.warn("Ошибка валидации пользователя: некорректный логин '{}'", user.getLogin());
            throw new ValidationException("Логин не может быть пустым и содержать пробелы");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.debug("Имя пользователя не задано, использован логин: {}", user.getLogin());
        }
    }
}
