package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.GuardianPhone;
import seedu.address.model.person.HourlyRate;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

class PersonCardTest {
    @Test
    void formatOptionalFields_showsEmDashWhenUnset() {
        Person person = new PersonBuilder().build();
        assertEquals("Guardian: —", PersonCard.formatGuardianPhone(person));
        assertEquals("Hourly rate: —", PersonCard.formatHourlyRate(person));
    }

    @Test
    void formatOptionalFields_showsCanonicalValues() {
        Person base = new PersonBuilder().build();
        Person person = new Person(base.getName(), base.getPhone(), base.getEmail(), base.getAddress(), base.getTags(),
                Optional.of(new GuardianPhone("+6591234567")),
                Optional.of(new HourlyRate(new BigDecimal("80"))));
        assertEquals("Guardian: +6591234567", PersonCard.formatGuardianPhone(person));
        assertEquals("Hourly rate: S$80.00", PersonCard.formatHourlyRate(person));
    }
}
