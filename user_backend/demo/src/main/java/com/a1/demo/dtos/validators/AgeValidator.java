package com.a1.demo.dtos.validators;


import com.a1.demo.dtos.validators.annotation.AgeLimit;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;
import java.time.Period;

public class AgeValidator implements ConstraintValidator<AgeLimit, LocalDate> {
    private int min;

    @Override
    public void initialize(AgeLimit ann) {
        this.min = ann.value();
    }

    @Override
    public boolean isValid(LocalDate dateOfBirth, ConstraintValidatorContext ctx) {
        if (dateOfBirth == null) return true;
        int age = Period.between(dateOfBirth, LocalDate.now()).getYears();
        return age >= min;
    }
}