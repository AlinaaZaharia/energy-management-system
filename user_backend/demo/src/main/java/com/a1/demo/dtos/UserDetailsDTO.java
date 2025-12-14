package com.a1.demo.dtos;


import com.a1.demo.dtos.validators.annotation.AgeLimit;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public class UserDetailsDTO {

    private UUID id;

    @NotBlank(message = "name is required")
    private String name;

    @NotBlank(message = "username is required")
    private String username;

    @Email(message = "email must be valid")
    @NotBlank(message="email is required")
    private String email;

    @NotBlank(message = "city is required")
    private String city;

    @NotNull(message="date of birth is required")
    @Past(message = "date of birth must be in the past")
    @AgeLimit(value = 18, message="user must be at least 18 years old")
    private LocalDate dateOfBirth;

    public UserDetailsDTO() {
    }

    public UserDetailsDTO(String name, String username, String email, String city, LocalDate dateOfBirth) {
        this.name = name;
        this.username = username;
        this.email = email;
        this.city = city;
        this.dateOfBirth = dateOfBirth;
    }

    public UserDetailsDTO(UUID id, String name, String username, String email, String city, LocalDate dateOfBirth) {
        this.id = id;
        this.name = name;
        this.username = username;
        this.email = email;
        this.city = city;
        this.dateOfBirth = dateOfBirth;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserDetailsDTO that = (UserDetailsDTO) o;
        return Objects.equals(name, that.name)
                && Objects.equals(username, that.username)
                && Objects.equals(email, that.email)
                && Objects.equals(city, that.city)
                && Objects.equals(dateOfBirth, that.dateOfBirth);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, username, email, city, dateOfBirth);
    }
}