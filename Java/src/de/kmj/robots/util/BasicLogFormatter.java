package de.kmj.robots.util;

import java.util.Locale;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * Extracts the minimum necessary information from a
 * {@link java.util.logging.LogRecord}.
 *
 * The output String has the pattern
 * <code>[&lt;short class name&gt;] &lt;level&gt;: &lt;message&gt;</code>.
 *
 * @author Kathrin Janowski
 */
public class BasicLogFormatter extends Formatter {

    public BasicLogFormatter() {
        super();
    }

    @Override
    public String format(LogRecord record) {
        String fullMessage = formatMessage(record);

        String shortName = record.getLoggerName();
        int dotIdx = shortName.lastIndexOf(".");
        if ((dotIdx > -1) && (shortName.length() > dotIdx)) {
            shortName = shortName.substring(dotIdx + 1);
        }

        String output = String.format(Locale.US, "[%s] %s: %s\n", shortName, record.getLevel().getName(), fullMessage);

        return output;
    }

}
