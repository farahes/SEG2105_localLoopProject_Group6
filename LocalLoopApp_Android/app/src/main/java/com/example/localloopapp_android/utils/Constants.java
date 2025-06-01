package com.example.localloopapp_android.utils;

public class Constants {
    public static final String EXTRA_FIRST_NAME = "firstName";
    public static final String EXTRA_LAST_NAME = "lastName";
}

/**
 * Constants used throughout the app for intent extras, database keys, and reusable identifiers.
 *
 * Why use constants?
 * - ✅ Prevents bugs due to typos in key names (e.g., "firstName" vs. "fristName")
 * - ✅ Makes refactoring easier (change the key name in one place)
 * - ✅ Improves readability — makes intent extras and shared keys obvious
 *
 * Naming Conventions:
 * - EXTRA_... : Data passed between activities via Intents
 *                 *An Extra is data you attach to an Intent to pass between Activities (or Services).
 * - KEY_...   : Shared map or bundle keys
 * - PATH_...  : Firebase or server paths
 *
 * Use these to keep the codebase consistent and safe.
 *
 * And scalable !!!
 */
