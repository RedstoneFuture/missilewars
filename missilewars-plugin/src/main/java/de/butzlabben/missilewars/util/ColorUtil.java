package de.butzlabben.missilewars.util;

import org.bukkit.Color;

public class ColorUtil {
    
    /**
     * This method lightens a given org.bukkit.Color by a specified fraction.
     *
     * @param color the original color to be lightened
     * @param fraction the fraction by which to lighten the color (e.g., 0.3 for 30%)
     * @return a new Color object representing the lightened color
     */
    public static Color lightenColor(Color color, double fraction) {
        int red = color.getRed();
        int green = color.getGreen();
        int blue = color.getBlue();

        // Calculate the new lighter values for each color component
        red = lightenColorComponent(red, fraction);
        green = lightenColorComponent(green, fraction);
        blue = lightenColorComponent(blue, fraction);

        // Create and return the new lightened color
        return Color.fromRGB(red, green, blue);
    }

    /**
     * This method darkens a given org.bukkit.Color by a specified fraction.
     *
     * @param color the original color to be darkened
     * @param fraction the fraction by which to darken the color (e.g., 0.3 for 30%)
     * @return a new Color object representing the darkened color
     */
    public static Color darkenColor(Color color, double fraction) {
        int red = color.getRed();
        int green = color.getGreen();
        int blue = color.getBlue();

        // Calculate the new darker values for each color component
        red = darkenColorComponent(red, fraction);
        green = darkenColorComponent(green, fraction);
        blue = darkenColorComponent(blue, fraction);

        // Create and return the new darkened color
        return Color.fromRGB(red, green, blue);
    }

    /**
     * This method lightens a single color component (R/G/B) by a specified percentage.
     *
     * @param colorComponent the original value of the color component (0-255)
     * @param fraction the fraction by which to lighten the color component (e.g., 0.3 for 30%)
     * @return the new lightened value of the color component, ensuring it stays within the valid range (0-255)
     */
    private static int lightenColorComponent(int colorComponent, double fraction) {
        // Increase the color component by the given fraction towards 255
        int newValue = (int) (colorComponent + (255 - colorComponent) * fraction);

        // Ensure the new value is within the valid range (0-255)
        return Math.min(255, Math.max(0, newValue));
    }

    /**
     * This method darkens a single color component (R/G/B) by a specified percentage.
     *
     * @param colorComponent the original value of the color component (0-255)
     * @param fraction the fraction by which to darken the color component (e.g., 0.3 for 30%)
     * @return the new darkened value of the color component, ensuring it stays within the valid range (0-255)
     */
    private static int darkenColorComponent(int colorComponent, double fraction) {
        // Decrease the color component by the given fraction towards 0
        int newValue = (int) (colorComponent - (colorComponent * fraction));

        // Ensure the new value is within the valid range (0-255)
        return Math.min(255, Math.max(0, newValue));
    }
    
}