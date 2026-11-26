package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();
    private long nextId = 1;

    @Override
    public Collection<User> findAll() {
        log.debug("Запрос списка всех пользователей. Текущее количество: {}", users.size());
        return new ArrayList<>(users.values());
    }

    @Override
    public Optional<User> findById(Long id) {
        if (id == null) {
            log.warn("Ошибка поиска пользователя: не указан id");
            throw new ValidationException("Id пользователя должен быть указан");
        }
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public User create(User user) {
        if (user == null) {
            log.warn("Ошибка создания пользователя: передан пустой запрос");
            throw new ValidationException("Пользователь не может быть null");
        }
        if (user.getFriends() == null) {
            user.setFriends(new HashSet<>());
        }
        user.setId(nextId++);
        users.put(user.getId(), user);
        log.info("Добавлен новый пользователь: {}", user);
        return user;
    }

    @Override
    public User update(User user) {
        if (user == null) {
            log.warn("Ошибка обновления пользователя: передан пустой запрос");
            throw new ValidationException("Пользователь не может быть null");
        }
        if (user.getId() == null) {
            log.warn("Ошибка обновления пользователя: не указан id");
            throw new ValidationException("Id пользователя должен быть указан");
        }
        User existing = users.get(user.getId());
        if (existing == null) {
            log.warn("Ошибка обновления пользователя: пользователь с id = {} не найден", user.getId());
            throw new NotFoundException("Пользователь с id = " + user.getId() + " не найден");
        }
        if (user.getFriends() == null) {
            user.setFriends(new HashSet<>(existing.getFriends()));
        } else if (user.getFriends().isEmpty() && !existing.getFriends().isEmpty()) {
            user.setFriends(new HashSet<>(existing.getFriends()));
        }
        users.put(user.getId(), user);
        log.info("Обновлён пользователь: {}", user);
        return user;
    }

    @Override
    public void delete(Long id) {
        if (id == null) {
            log.warn("Ошибка удаления пользователя: не указан id");
            throw new ValidationException("Id пользователя должен быть указан");
        }
        if (!users.containsKey(id)) {
            log.warn("Ошибка удаления пользователя: пользователь с id = {} не найден", id);
            throw new NotFoundException("Пользователь с id = " + id + " не найден");
        }
        users.remove(id);
        log.info("Был удалён пользователь с id = {}", id);
    }
}
