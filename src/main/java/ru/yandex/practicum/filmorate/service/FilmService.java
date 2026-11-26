package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
public class FilmService {
    private static final int MAX_DESCRIPTION_LENGTH = 200;
    private static final LocalDate CINEMA_BIRTHDAY = LocalDate.of(1895, 12, 28);

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film findById(Long id) {
        return filmStorage.findById(id)
                .orElseThrow(() -> {
                    log.warn("Фильм с id = {} не найден", id);
                    return new NotFoundException("Фильм с id = " + id + " не найден");
                });
    }

    public Film create(Film film) {
        validate(film);
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        validate(film);
        return filmStorage.update(film);
    }

    public void addLike(Long filmId, Long userId) {
        Film film = findById(filmId);
        verifyUserExists(userId);
        film.getLikes().add(userId);
        log.info("Пользователь с id = {} поставил лайк фильму с id = {}", userId, filmId);
    }

    public void removeLike(Long filmId, Long userId) {
        Film film = findById(filmId);
        verifyUserExists(userId);
        film.getLikes().remove(userId);
        log.info("Пользователь с id = {} удалил лайк у фильма с id = {}", userId, filmId);
    }

    public List<Film> getPopular(int count) {
        if (count <= 0) {
            log.warn("Ошибка получения популярных фильмов: некорректный count = {}", count);
            throw new ValidationException("Параметр count должен быть положительным числом");
        }
        log.debug("Запрос {} наиболее популярных фильмов", count);
        return filmStorage.findAll().stream()
                .sorted(Comparator.comparingInt((Film film) -> film.getLikes().size()).reversed())
                .limit(count)
                .toList();
    }

    private void verifyUserExists(Long userId) {
        userStorage.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Пользователь с id = {} не найден", userId);
                    return new NotFoundException("Пользователь с id = " + userId + " не найден");
                });
    }

    private void validate(Film film) {
        if (film == null) {
            log.warn("Ошибка валидации фильма: передан пустой запрос");
            throw new ValidationException("Фильм не может быть null");
        }
        if (film.getName() == null || film.getName().isBlank()) {
            log.warn("Ошибка валидации фильма: пустое название");
            throw new ValidationException("Название фильма не может быть пустым");
        }
        if (film.getDescription() != null && film.getDescription().length() > MAX_DESCRIPTION_LENGTH) {
            log.warn("Ошибка валидации фильма: длина описания {} превышает {}",
                    film.getDescription().length(), MAX_DESCRIPTION_LENGTH);
            throw new ValidationException("Максимальная длина описания — 200 символов");
        }
        if (film.getReleaseDate() == null || film.getReleaseDate().isBefore(CINEMA_BIRTHDAY)) {
            log.warn("Ошибка валидации фильма: некорректная дата релиза {}", film.getReleaseDate());
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
        if (film.getDuration() == null || film.getDuration() <= 0) {
            log.warn("Ошибка валидации фильма: некорректная продолжительность {}", film.getDuration());
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }
    }
}
