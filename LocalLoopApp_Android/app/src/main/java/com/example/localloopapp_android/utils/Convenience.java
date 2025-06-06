package com.example.localloopapp_android.utils;

import android.text.TextUtils;
import android.widget.EditText;

import java.util.function.Predicate;

public class Convenience {

    /**
     * Validates a single text field using the given predicate.  If invalid,
     * sets an error message and returns false.
     */
    public static boolean validateField(EditText field, String value, Predicate<String> isValid, String errorMsg) {
        if (TextUtils.isEmpty(value) || !isValid.test(value)) {
            field.setError(errorMsg);
            field.requestFocus();
            return false;
        }
        return true;
    }

    /**
     * Converts user-inputted text into a simple, trimmed string.
     */
    public static String getTrimmedString(EditText input) {
        if (input.getText() == null) return "";
        return input.getText().toString().trim();
    }

    /**
     * Returns a String with the first letter capitalized (if applicable).
     */
    public static String capitalize(String input) {
        if (input == null || input.isEmpty()) return input;
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }

}
