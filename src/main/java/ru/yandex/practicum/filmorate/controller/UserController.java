package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final Map<Long, User> users = new HashMap<>();
    private long nextId = 1;

    @GetMapping
    public Collection<User> findAll() {
        log.info("Получен запрос на список всех пользователей. Текущее количество: {}", users.size());
        return users.values();
    }

    @PostMapping
    public User create(@RequestBody @Valid User user) {
        validate(user);
        user.setId(nextId++);
        users.put(user.getId(), user);
        log.info("Добавлен новый пользователь: {}", user);
        return user;
    }

    @PutMapping
    public User update(@RequestBody @Valid User user) {
        if (user == null) {
            log.warn("Ошибка обновления пользователя: передан пустой запрос");
            throw new ValidationException("Пользователь не может быть null");
        }
        if (user.getId() == null) {
            log.warn("Ошибка обновления пользователя: не указан id");
            throw new ValidationException("Id пользователя должен быть указан");
        }
        if (!users.containsKey(user.getId())) {
            log.warn("Ошибка обновления пользователя: пользователь с id = {} не найден", user.getId());
            throw new NotFoundException("Пользователь с id = " + user.getId() + " не найден");
        }
        validate(user);
        users.put(user.getId(), user);
        log.info("Обновлён пользователь: {}", user);
        return user;
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