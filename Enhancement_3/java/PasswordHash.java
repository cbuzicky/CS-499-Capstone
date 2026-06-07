package com.zybooks.c499_buzicky_cheryl;

import android.util.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordHash {

    // Hash password using SHA-256
    public static String hashPassword(String password) {

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            // Convert password to bytes and hash it
            byte[] hashedBytes = md.digest(password.getBytes());

            // Convert bytes to hexadecimal
            StringBuilder sb = new StringBuilder();

            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }

            return sb.toString();

        } catch (NoSuchAlgorithmException e) {
            Log.e("PasswordUtils", "SHA-256 hashing error", e);
            return null;
        }
    }
}