package com.a1.demo.dtos.builders;

import com.a1.demo.dtos.UserDTO;
import com.a1.demo.dtos.UserDetailsDTO;
import com.a1.demo.entities.User;

import java.time.LocalDate;
import java.time.Period;

public class UserBuilder {

    private UserBuilder() {
    }

    private static int computeAge(LocalDate dateOfBirth){
        if(dateOfBirth==null)
            return 0;
        else
            return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }

    public static UserDTO toUserDTO(User user) {
        return new UserDTO(
                user.getId(),
                user.getUsername(),
                computeAge(user.getDateOfBirth()),
                user.getEmail());
    }

    public static UserDetailsDTO toUserDetailsDTO(User user) {
        return new UserDetailsDTO(
                user.getId(),
                user.getName(),
                user.getUsername(),
                user.getEmail(),
                user.getCity(),
                user.getDateOfBirth());
    }

    public static User toEntity(UserDetailsDTO dto) {
        return new User(
                dto.getName(),
                dto.getUsername(),
                dto.getEmail(),
                dto.getCity(),
                dto.getDateOfBirth());
    }
}