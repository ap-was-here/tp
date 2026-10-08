package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_MISSING_REQUIRED_PARAMETER;
import static seedu.address.logic.Messages.MESSAGE_PARAMETER_SPECIFIED_ONLY_ONCE;
import static seedu.address.logic.Messages.MESSAGE_UNEXPECTED_TEXT;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_PARAMETER;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses an index followed by exactly one prefixed value, with shared error precedence. */
public final class IndexedParameterParserUtil {

    private static final Pattern PREFIX_TOKEN_PATTERN = Pattern.compile("(?:^|\\s)([A-Za-z][A-Za-z0-9]*/)");

    private IndexedParameterParserUtil() {}

    /** Parsed index and raw prefixed value. */
    public record ParsedParameter(Index index, String value) {}

    /**
     * Parses {@code INDEX prefix/VALUE}. Unknown prefixes and extra preamble text are reported before
     * required/repeated parameter errors; index syntax is checked before the returned value is validated.
     */
    public static ParsedParameter parse(String arguments, Prefix requiredPrefix, String parameterLabel)
            throws ParseException {
        requireNonNull(arguments);
        requireNonNull(requiredPrefix);
        requireNonNull(parameterLabel);

        String normalized = Normalizer.normalize(arguments, Normalizer.Form.NFKC);
        int leadingWhitespace = 0;
        while (leadingWhitespace < normalized.length()
                && Character.isWhitespace(normalized.charAt(leadingWhitespace))) {
            leadingWhitespace++;
        }
        normalized = normalized.substring(leadingWhitespace);
        int firstWhitespace = firstWhitespaceIndex(normalized);
        String indexText = firstWhitespace < 0 ? normalized : normalized.substring(0, firstWhitespace);
        String remainder = firstWhitespace < 0 ? "" : normalized.substring(firstWhitespace).trim();

        List<PrefixToken> prefixes = findPrefixTokens(normalized);
        String expectedPrefix = requiredPrefix.getPrefix();
        for (PrefixToken token : prefixes) {
            if (!token.prefix().equals(expectedPrefix)) {
                throw new ParseException(String.format(MESSAGE_UNKNOWN_PARAMETER, token.prefix()));
            }
        }

        if (prefixes.isEmpty()) {
            if (!remainder.isEmpty()) {
                throw new ParseException(MESSAGE_UNEXPECTED_TEXT);
            }
            throw new ParseException(String.format(MESSAGE_MISSING_REQUIRED_PARAMETER, parameterLabel));
        }

        if (prefixes.size() > 1) {
            throw new ParseException(String.format(MESSAGE_PARAMETER_SPECIFIED_ONLY_ONCE, parameterLabel));
        }

        PrefixToken prefix = prefixes.getFirst();
        if (!normalized.substring(firstWhitespace < 0 ? normalized.length() : firstWhitespace,
                prefix.startPosition()).trim().isEmpty()) {
            throw new ParseException(MESSAGE_UNEXPECTED_TEXT);
        }

        Index index = ParserUtil.parseIndex(indexText);
        return new ParsedParameter(index, normalized.substring(prefix.valueStartPosition()));
    }

    private static int firstWhitespaceIndex(String value) {
        for (int i = 0; i < value.length(); i++) {
            if (Character.isWhitespace(value.charAt(i))) {
                return i;
            }
        }
        return -1;
    }

    private static List<PrefixToken> findPrefixTokens(String value) {
        List<PrefixToken> tokens = new ArrayList<>();
        Matcher matcher = PREFIX_TOKEN_PATTERN.matcher(value);
        while (matcher.find()) {
            String prefix = matcher.group(1);
            tokens.add(new PrefixToken(prefix, matcher.start(1), matcher.end(1)));
        }
        return tokens;
    }

    private record PrefixToken(String prefix, int startPosition, int valueStartPosition) {}
}
