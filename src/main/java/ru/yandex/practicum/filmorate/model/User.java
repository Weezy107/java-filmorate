package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class User {
    private Long id;
    @NotBlank(message = "Ошибка валидации пользователя: передан пустой email")
    @Email(message = "Ошибка валидации пользователя: некорректный email")
    private String email;
    private String login;
    private String name;
    @NotNull(message = "Дата рождения должна быть указана")
    @PastOrPresent(message = "Дата рождения не может быть в будущем")
    private LocalDate birthday;
    private Set<Long> friends = new HashSet<>();
}
