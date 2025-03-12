package com.lozaine.rwr.utils;

import org.bukkit.Bukkit;

public class CustomLogger {
    private static final String PLUGIN_PREFIX = "[ResourceWorldResetter] ";
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_CYAN = "\u001B[36m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_BLUE = "\u001B[34m";

    public static void info(String message) {
        log(ANSI_GREEN + "INFO" + ANSI_RESET + " " + message);
    }

    public static void warn(String message) {
        log(ANSI_YELLOW + "WARN" + ANSI_RESET + " " + message);
    }

    public static void error(String message) {
        log(ANSI_RED + "ERROR" + ANSI_RESET + " " + message);
    }

    private static synchronized void log(String message) {
        Bukkit.getConsoleSender().sendMessage(PLUGIN_PREFIX + message);
    }

    public static void startupBanner() {
        String[] bannerLines = {
                ANSI_CYAN + "================================",
                ANSI_BLUE + " Resource World Resetter v1.2  ", // Updated version
                ANSI_BLUE + "    Developed by Lozaine       ",
                ANSI_CYAN + "================================" + ANSI_RESET
        };
        for (String line : bannerLines) {
            Bukkit.getConsoleSender().sendMessage(line);
        }
    }

    public static void shutdownBanner() {
        String[] bannerLines = {
                ANSI_CYAN + "================================",
                ANSI_RED + " Resource World Resetter Disabled ",
                ANSI_CYAN + "================================" + ANSI_RESET
        };
        for (String line : bannerLines) {
            Bukkit.getConsoleSender().sendMessage(line);
        }
    }

    public static void logInterval(long intervalHours) {
        info("Reset interval set to " + intervalHours + " hours.");
    }
}
