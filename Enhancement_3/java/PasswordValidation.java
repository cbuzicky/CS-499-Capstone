package com.zybooks.c499_buzicky_cheryl;

public class PasswordValidation {

    public static boolean isValidPassword(String password) {

        return password.matches(
                "^(?=.*[A-Z])(?=.*\\d).{8,}$"
        );
    }


}
