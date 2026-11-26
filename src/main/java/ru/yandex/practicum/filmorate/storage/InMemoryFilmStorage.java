package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();
    private long nextId = 1;

    @Override
    public Collection<Film> findAll() {
        log.debug("Запрос списка всех фильмов. Текущее количество: {}", films.size());
        return new ArrayList<>(films.values());
    }

    @Override
    public Optional<Film> findById(Long id) {
        if (id == null) {
            log.warn("Ошибка поиска фильма: не указан id");
            throw new ValidationException("Id фильма должен быть указан");
        }
        return Optional.ofNullable(films.get(id));
    }

    @Override
    public Film create(Film film) {
        if (film == null) {
            log.warn("Ошибка создания фильма: передан пустой запрос");
            throw new ValidationException("Фильм не может быть null");
        }
        if (film.getLikes() == null) {
            film.setLikes(new HashSet<>());
        }
        film.setId(nextId++);
        films.put(film.getId(), film);
        log.info("Добавлен новый фильм: {}", film);
        return film;
    }

    @Override
    public Film update(Film film) {
        if (film == null) {
            log.warn("Ошибка обновления фильма: передан пустой запрос");
            throw new ValidationException("Фильм не может быть null");
        }
        if (film.getId() == null) {
            log.warn("Ошибка обновления фильма: не указан id");
            throw new ValidationException("Id фильма должен быть указан");
        }
        Film existing = films.get(film.getId());
        if (existing == null) {
            log.warn("Ошибка обновления фильма: фильм с id = {} не найден", film.getId());
            throw new NotFoundException("Фильм с id = " + film.getId() + " не найден");
        }
        if (film.getLikes() == null) {
            film.setLikes(new HashSet<>(existing.getLikes()));
        } else if (film.getLikes().isEmpty() && !existing.getLikes().isEmpty()) {
            film.setLikes(new HashSet<>(existing.getLikes()));
        }
        films.put(film.getId(), film);
        log.info("Обновлён фильм: {}", film);
        return film;
    }

    @Override
    public void delete(Long id) {
        if (id == null) {
            log.warn("Ошибка удаления фильма: не указан id");
            throw new ValidationException("Id фильма должен быть указан");
        }
        if (!films.containsKey(id)) {
            log.warn("Ошибка удаления фильма: фильм с id = {} не найден", id);
            throw new NotFoundException("Фильм с id = " + id + " не найден");
        }
        films.remove(id);
        log.info("Был удалён фильм с id = {}", id);
    }
}
