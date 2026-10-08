package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.parser.ParserUtil.MESSAGE_INVALID_GUARDIAN_PHONE;
import static seedu.address.logic.parser.ParserUtil.MESSAGE_INVALID_HOURLY_RATE;
import static seedu.address.logic.parser.ParserUtil.MESSAGE_INVALID_INDEX;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.GuardianCommand;
import seedu.address.logic.commands.RateCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.GuardianPhone;
import seedu.address.model.person.HourlyRate;

class GuardianAndRateCommandParserTest {
    private final GuardianCommandParser guardianParser = new GuardianCommandParser();
    private final RateCommandParser rateParser = new RateCommandParser();

    @Test
    void parseGuardian_acceptsAndCanonicalizesSingaporePhone() throws Exception {
        GuardianCommand command = guardianParser.parse(" 1   g/+65 6777-8899 ");
        assertEquals(new GuardianCommand(ParserUtil.parseIndex("1"), new GuardianPhone("+6567778899")), command);
    }

    @Test
    void parseGuardian_reportsMissingRepeatedAndInvalidValues() {
        assertParseFailure(guardianParser, "1", "Error: Missing required parameter: g/PHONE.");
        assertParseFailure(guardianParser, "1 g/91234567 g/91234568",
                "Error: Parameter g/PHONE may be specified only once.");
        assertParseFailure(guardianParser, "1 g/71234567", MESSAGE_INVALID_GUARDIAN_PHONE);
    }

    @Test
    void parseGuardian_checksIndexSyntaxBeforePhoneValue() {
        assertParseFailure(guardianParser, "01 g/not-a-phone", MESSAGE_INVALID_INDEX);
    }

    @Test
    void parseRate_acceptsDecimalBoundariesAndStoresTwoPlaces() throws Exception {
        assertEquals(new RateCommand(ParserUtil.parseIndex("1"), new HourlyRate(new java.math.BigDecimal("1.00"))),
                rateParser.parse("1 r/1"));
        assertEquals(new HourlyRate(new java.math.BigDecimal("80.00")),
                ParserUtil.parseHourlyRate("80"));
        assertEquals(new HourlyRate(new java.math.BigDecimal("1000.00")),
                ParserUtil.parseHourlyRate("1000"));
    }

    @Test
    void parseRate_reportsMissingRepeatedAndInvalidValues() {
        assertParseFailure(rateParser, "1", "Error: Missing required parameter: r/RATE.");
        assertParseFailure(rateParser, "1 r/80 r/90", "Error: Parameter r/RATE may be specified only once.");
        for (String invalid : new String[] {"0.99", "1000.01", "01", "1.001", "$80", "1e2", "+80", "1,000"}) {
            assertParseFailure(rateParser, "1 r/" + invalid, MESSAGE_INVALID_HOURLY_RATE);
        }
    }

    @Test
    void parseRate_checksIndexSyntaxBeforeRateValue() {
        assertParseFailure(rateParser, "01 r/not-a-rate", MESSAGE_INVALID_INDEX);
    }

    private void assertParseFailure(Parser<?> parser, String arguments, String expectedMessage) {
        assertThrows(ParseException.class, expectedMessage, () -> parser.parse(arguments));
    }
}
