package com.github.b3kt.aviation.application.helper;

import java.math.BigDecimal;

public class CoordinateHelper {

    /** Private constructor to prevent instantiation. */
    private CoordinateHelper() {}

    /**
     * Parses a coordinate in the format "DDMM.MMMM" (e.g. 5112.3456) into a decimal value.
     * @param value String representation of the coordinate
     * @return double decimal value
     */
    public static BigDecimal parseFromSeconds(String value) {
        char direction = value.charAt(value.length() - 1);
        double seconds = Double.parseDouble(value.substring(0, value.length() - 1));

        double decimal = seconds / 3600.0;

        if (direction == 'S' || direction == 'W') {
            decimal = -decimal;
        }
        return BigDecimal.valueOf(decimal);
    }

    /**
     * Parses a coordinate in the format "DD-MM-SS.SSSS" (e.g. 0823231.3700) into a decimal value.
     * @param value String representation of the coordinate
     * @return double decimal value
     */
    public static BigDecimal parseFaaLatLon(String value) {
        // Example: 35-26-09.9980N or 082-32-31.3700W
        String[] parts = value.split("-");
        String secondsPart = parts[2];

        double seconds = Double.parseDouble(secondsPart.substring(0, secondsPart.length() - 1));
        char direction = secondsPart.charAt(secondsPart.length() - 1);

        int degrees = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);

        double decimal = degrees + (minutes / 60.0) + (seconds / 3600.0);

        if (direction == 'S' || direction == 'W') {
            decimal = -decimal;
        }
        return BigDecimal.valueOf(decimal);
    }
}
