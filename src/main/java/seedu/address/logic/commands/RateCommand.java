package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.Objects;
import java.util.Optional;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.HourlyRate;
import seedu.address.model.person.Person;

/** Sets a person's hourly rate using the rate's displayed index. */
public class RateCommand extends Command implements AtomicCommand {

    public static final String COMMAND_WORD = "rate";
    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Sets a student's hourly rate in SGD.\n"
            + "Parameters: INDEX r/RATE\n"
            + "Example: " + COMMAND_WORD + " 1 r/80";

    public static final String MESSAGE_SET_SUCCESS = "Hourly rate set for %1$s: S$%2$s.";
    public static final String MESSAGE_UPDATE_SUCCESS = "Hourly rate updated for %1$s: S$%2$s -> S$%3$s.";
    public static final String MESSAGE_NO_CHANGE = "Hourly rate for %1$s is already S$%2$s.";

    private final Index index;
    private final HourlyRate hourlyRate;

    /** Creates a command that sets the hourly rate for the displayed student index. */
    public RateCommand(Index index, HourlyRate hourlyRate) {
        requireNonNull(index);
        requireNonNull(hourlyRate);
        this.index = index;
        this.hourlyRate = hourlyRate;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        Person target = VisibleIndexResolver.resolvePerson(index, model);
        Optional<HourlyRate> previous = target.getHourlyRate();
        String name = target.getName().toString();
        if (previous.isPresent() && previous.get().equals(hourlyRate)) {
            return new CommandResult(String.format(MESSAGE_NO_CHANGE, name, hourlyRate));
        }

        Person updated = new Person(target.getName(), target.getPhone(), target.getEmail(), target.getAddress(),
                target.getTags(), target.getGuardianPhone(), Optional.of(hourlyRate));
        model.setPerson(target, updated);
        String message = previous.isEmpty()
                ? String.format(MESSAGE_SET_SUCCESS, name, hourlyRate)
                : String.format(MESSAGE_UPDATE_SUCCESS, name, previous.get(), hourlyRate);
        return new CommandResult(message, index.getOneBased());
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof RateCommand otherCommand
                && index.equals(otherCommand.index) && hourlyRate.equals(otherCommand.hourlyRate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(index, hourlyRate);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("index", index).add("hourlyRate", hourlyRate).toString();
    }
}
