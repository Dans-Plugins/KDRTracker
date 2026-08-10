package dansplugins.kdrtracker.commands;

import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Daniel McCoy Stephenson
 *
 * Regression coverage for the K/D ratio shown to players being rounded for
 * display, rather than printed at full floating-point precision.
 */
class InfoCommandTest {

    @Test
    void formatRatio_roundsRecurringDecimalDown() {
        assertEquals("0.33", InfoCommand.formatRatio(1.0 / 3.0));
    }

    @Test
    void formatRatio_roundsRecurringDecimalUp() {
        assertEquals("0.67", InfoCommand.formatRatio(2.0 / 3.0));
    }

    @Test
    void formatRatio_padsWholeNumbersToTwoPlaces() {
        assertEquals("5.00", InfoCommand.formatRatio(5.0));
    }

    @Test
    void formatRatio_formatsZero() {
        assertEquals("0.00", InfoCommand.formatRatio(0.0));
    }

    @Test
    void formatRatio_usesAPeriodAsTheDecimalSeparatorRegardlessOfTheDefaultLocale() {
        Locale defaultLocale = Locale.getDefault();
        try {
            Locale.setDefault(Locale.GERMANY);
            assertEquals("1.50", InfoCommand.formatRatio(1.5));
        } finally {
            Locale.setDefault(defaultLocale);
        }
    }
}
