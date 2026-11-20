package ru.yandex.practicum.filmorate;


import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Set;

@SpringBootTest
class FilmorateApplicationTests {
    private FilmController filmController;
    private UserController userController;
    @Autowired
    private Validator validator;

    @BeforeEach
    void setUp() {
        filmController = new FilmController();
        userController = new UserController();
    }

    @Test
    void contextLoads() {
    }

    private Film createValidFilm() {
        Film film = new Film();
        film.setName("Интерстеллар");
        film.setDescription("Фантастический фильм о космосе");
        film.setReleaseDate(LocalDate.of(2014, 11, 6));
        film.setDuration(169);
        return film;
    }

    private User createValidUser() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("validLogin");
        user.setName("Иван");
        user.setBirthday(LocalDate.of(1995, 5, 15));
        return user;
    }

    @Test
    void shouldCreateValidFilm() {
        Film created = filmController.create(createValidFilm());
        Assertions.assertNotNull(created.getId());
        Assertions.assertEquals(1, filmController.findAll().size());
    }

    @Test
    void shouldThrowWhenFilmIsNullOrEmpty() {
        Assertions.assertThrows(ValidationException.class, () -> filmController.create(null));
        Assertions.assertThrows(ValidationException.class, () -> filmController.create(new Film()));
    }

    @Test
    void shouldThrowWhenFilmNameIsBlank() {
        Film film = createValidFilm();
        film.setName("   ");
        Assertions.assertThrows(ValidationException.class, () -> filmController.create(film));
    }

    @Test
    void shouldValidateFilmDescriptionBoundary() {
        Film validFilm = createValidFilm();
        validFilm.setDescription("a".repeat(200));
        Assertions.assertEquals(200, filmController.create(validFilm).getDescription().length());

        Film invalidFilm = createValidFilm();
        invalidFilm.setDescription("a".repeat(201));
        Assertions.assertThrows(ValidationException.class, () -> filmController.create(invalidFilm));
    }

    @Test
    void shouldValidateFilmReleaseDateBoundary() {
        Film validFilm = createValidFilm();
        validFilm.setReleaseDate(LocalDate.of(1895, 12, 28));
        Assertions.assertEquals(LocalDate.of(1895, 12, 28), filmController.create(validFilm).getReleaseDate());

        Film invalidFilm = createValidFilm();
        invalidFilm.setReleaseDate(LocalDate.of(1895, 12, 27));
        Assertions.assertThrows(ValidationException.class, () -> filmController.create(invalidFilm));
    }

    @Test
    void shouldThrowWhenFilmDurationIsNotPositive() {
        Film zeroFilm = createValidFilm();
        zeroFilm.setDuration(0);
        Assertions.assertThrows(ValidationException.class, () -> filmController.create(zeroFilm));

        Film negativeFilm = createValidFilm();
        negativeFilm.setDuration(-10);
        Assertions.assertThrows(ValidationException.class, () -> filmController.create(negativeFilm));
    }

    @Test
    void shouldUpdateFilmAndThrowIfNotFound() {
        Film created = filmController.create(createValidFilm());
        created.setName("Новое название");
        Assertions.assertEquals("Новое название", filmController.update(created).getName());

        Film unknownFilm = createValidFilm();
        unknownFilm.setId(999L);
        Assertions.assertThrows(NotFoundException.class, () -> filmController.update(unknownFilm));
    }

    @Test
    void shouldCreateValidUser() {
        User created = userController.create(createValidUser());
        Assertions.assertNotNull(created.getId());
        Assertions.assertEquals(1, userController.findAll().size());
    }

    @Test
    void shouldThrowWhenUserIsNullOrEmpty() {
        Assertions.assertThrows(ValidationException.class, () -> userController.create(null));
        Assertions.assertThrows(ValidationException.class, () -> userController.create(new User()));
    }

    @Test
    void shouldUseLoginWhenUserNameIsBlank() {
        User user = createValidUser();
        user.setName("   ");
        Assertions.assertEquals(user.getLogin(), userController.create(user).getName());
    }

    @Test
    void shouldThrowWhenUserEmailIsInvalid() {
        User noAtUser = createValidUser();
        noAtUser.setEmail("invalid-email.com");
        Set<ConstraintViolation<User>> violationsNoAt = validator.validate(noAtUser);
        Assertions.assertFalse(violationsNoAt.isEmpty());

        User doubleAtUser = createValidUser();
        doubleAtUser.setEmail("some@@mail.ru");
        Set<ConstraintViolation<User>> violationsDoubleAt = validator.validate(doubleAtUser);
        Assertions.assertFalse(violationsDoubleAt.isEmpty());

        User blankEmailUser = createValidUser();
        blankEmailUser.setEmail(" ");
        Set<ConstraintViolation<User>> violationsBlank = validator.validate(blankEmailUser);
        Assertions.assertFalse(violationsBlank.isEmpty());
    }

    @Test
    void shouldThrowWhenUserLoginIsInvalid() {
        User blankLogin = createValidUser();
        blankLogin.setLogin(" ");
        Assertions.assertThrows(ValidationException.class, () -> userController.create(blankLogin));

        User spaceLogin = createValidUser();
        spaceLogin.setLogin("bad login");
        Assertions.assertThrows(ValidationException.class, () -> userController.create(spaceLogin));
    }

    @Test
    void shouldValidateUserBirthdayBoundary() {
        User validUser = createValidUser();
        validUser.setBirthday(LocalDate.now()); // если поставили @Past, замените на LocalDate.now().minusDays(1)
        Assertions.assertTrue(validator.validate(validUser).isEmpty());

        User invalidUser = createValidUser();
        invalidUser.setBirthday(LocalDate.now().plusDays(1));
        Assertions.assertFalse(validator.validate(invalidUser).isEmpty());
    }

    @Test
    void shouldUpdateUserAndThrowIfNotFound() {
        User created = userController.create(createValidUser());
        created.setName("Новое имя");
        Assertions.assertEquals("Новое имя", userController.update(created).getName());

        User unknownUser = createValidUser();
        unknownUser.setId(999L);
        Assertions.assertThrows(NotFoundException.class, () -> userController.update(unknownUser));
    }
}