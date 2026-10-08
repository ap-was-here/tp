package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.Objects;
import java.util.Optional;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.GuardianPhone;
import seedu.address.model.person.Person;

/** Sets the guardian phone number of a person selected by displayed index. */
public class GuardianCommand extends Command implements AtomicCommand {

    public static final String COMMAND_WORD = "guardian";
    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Sets a student's guardian contact.\n"
            + "Parameters: INDEX g/PHONE\n"
            + "Example: " + COMMAND_WORD + " 1 g/91234567";

    public static final String MESSAGE_SET_SUCCESS = "Guardian contact set for %1$s: %2$s.";
    public static final String MESSAGE_UPDATE_SUCCESS = "Guardian contact updated for %1$s: %2$s -> %3$s.";
    public static final String MESSAGE_NO_CHANGE = "Guardian contact for %1$s is already %2$s.";

    private final Index index;
    private final GuardianPhone guardianPhone;

    /** Creates a command that sets the guardian phone for the displayed student index. */
    public GuardianCommand(Index index, GuardianPhone guardianPhone) {
        requireNonNull(index);
        requireNonNull(guardianPhone);
        this.index = index;
        this.guardianPhone = guardianPhone;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        Person target = VisibleIndexResolver.resolvePerson(index, model);
        Optional<GuardianPhone> previous = target.getGuardianPhone();
        String name = target.getName().toString();
        if (previous.isPresent() && previous.get().equals(guardianPhone)) {
            return new CommandResult(String.format(MESSAGE_NO_CHANGE, name, guardianPhone));
        }

        Person updated = new Person(target.getName(), target.getPhone(), target.getEmail(), target.getAddress(),
                target.getTags(), Optional.of(guardianPhone), target.getHourlyRate());
        model.setPerson(target, updated);
        String message = previous.isEmpty()
                ? String.format(MESSAGE_SET_SUCCESS, name, guardianPhone)
                : String.format(MESSAGE_UPDATE_SUCCESS, name, previous.get(), guardianPhone);
        return new CommandResult(message, index.getOneBased());
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof GuardianCommand otherCommand
                && index.equals(otherCommand.index) && guardianPhone.equals(otherCommand.guardianPhone);
    }

    @Override
    public int hashCode() {
        return Objects.hash(index, guardianPhone);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("index", index).add("guardianPhone", guardianPhone).toString();
    }
}
