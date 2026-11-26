package ru.yandex.practicum.filmorate;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class FilmorateApplicationTests {
    private FilmController filmController;
    private UserController userController;
    @Autowired
    private Validator validator;
    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        InMemoryFilmStorage filmStorage = new InMemoryFilmStorage();
        InMemoryUserStorage userStorage = new InMemoryUserStorage();
        FilmService filmService = new FilmService(filmStorage, userStorage);
        UserService userService = new UserService(userStorage);
        filmController = new FilmController(filmService);
        userController = new UserController(userService);
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
        Assertions.assertEquals(created.getId(), filmController.findById(created.getId()).getId());
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
        Assertions.assertEquals(created.getId(), userController.findById(created.getId()).getId());
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
        validUser.setBirthday(LocalDate.now());
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

    @Test
    void shouldManageMutualFriendsAndCommonFriends() {
        User u1 = userController.create(createValidUser());
        User u2 = createValidUser();
        u2.setEmail("u2@example.com");
        u2.setLogin("user2");
        u2 = userController.create(u2);

        User common = createValidUser();
        common.setEmail("common@example.com");
        common.setLogin("common");
        common = userController.create(common);

        userController.addFriend(u1.getId(), common.getId());
        userController.addFriend(u2.getId(), common.getId());

        Assertions.assertEquals(1, userController.getFriends(u1.getId()).size());
        Assertions.assertEquals(2, userController.getFriends(common.getId()).size());

        List<User> commonFriends = userController.getCommonFriends(u1.getId(), u2.getId());
        Assertions.assertEquals(1, commonFriends.size());
        Assertions.assertEquals(common.getId(), commonFriends.get(0).getId());

        userController.removeFriend(u1.getId(), common.getId());
        Assertions.assertTrue(userController.getFriends(u1.getId()).isEmpty());
        Assertions.assertEquals(1, userController.getFriends(common.getId()).size());

        Long u1Id = u1.getId();
        Assertions.assertThrows(ValidationException.class, () -> userController.addFriend(u1Id, u1Id));
        Assertions.assertThrows(NotFoundException.class, () -> userController.addFriend(u1Id, 999L));
    }

    @Test
    void shouldManageLikesAndReturnPopularFilms() {
        User u1 = userController.create(createValidUser());
        User u2 = createValidUser();
        u2.setEmail("u2@example.com");
        u2.setLogin("user2");
        u2 = userController.create(u2);

        Film f1 = filmController.create(createValidFilm());
        Film f2 = createValidFilm();
        f2.setName("Начало");
        f2 = filmController.create(f2);

        filmController.addLike(f2.getId(), u1.getId());
        filmController.addLike(f2.getId(), u2.getId());
        filmController.addLike(f2.getId(), u2.getId());
        filmController.addLike(f1.getId(), u1.getId());

        List<Film> popular = filmController.getPopular(10);
        Assertions.assertEquals(2, popular.size());
        Assertions.assertEquals(f2.getId(), popular.get(0).getId());
        Assertions.assertEquals(1, filmController.getPopular(1).size());

        filmController.removeLike(f2.getId(), u1.getId());
        filmController.removeLike(f2.getId(), u2.getId());
        Assertions.assertEquals(f1.getId(), filmController.getPopular(10).get(0).getId());

        Long f1Id = f1.getId();
        Assertions.assertThrows(NotFoundException.class, () -> filmController.addLike(f1Id, 999L));
        Assertions.assertThrows(NotFoundException.class, () -> filmController.addLike(999L, u1.getId()));
        Assertions.assertThrows(ValidationException.class, () -> filmController.getPopular(0));
    }

    @Test
    void shouldReturnExpectedHttpStatusCodesFromErrorHandler() throws Exception {
        mockMvc.perform(get("/users/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());

        mockMvc.perform(get("/films/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"bad-email\",\"login\":\"ok\",\"birthday\":\"1995-05-15\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"description\":\"desc\",\"releaseDate\":\"2000-01-01\",\"duration\":100}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }
}
