package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;
import static seedu.address.logic.commands.CommandTestUtil.ADDRESS_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.EMAIL_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_AMY;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.AMY;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.GuardianPhone;
import seedu.address.model.person.HourlyRate;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

public class LogicManagerTest {
    private static final IOException DUMMY_IO_EXCEPTION = new IOException("dummy IO exception");
    private static final IOException DUMMY_AD_EXCEPTION = new AccessDeniedException("dummy access denied exception");

    @TempDir
    public Path temporaryFolder;

    private Model model = new ModelManager();
    private Logic logic;

    @BeforeEach
    public void setUp() {
        JsonAddressBookStorage addressBookStorage =
                new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        JsonUserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"));
        StorageManager storage = new StorageManager(addressBookStorage, userPrefsStorage);
        logic = new LogicManager(model, storage);
    }

    @Test
    public void execute_invalidCommandFormat_throwsParseException() {
        String invalidCommand = "uicfhmowqewca";
        assertParseException(invalidCommand, MESSAGE_UNKNOWN_COMMAND);
    }

    @Test
    public void execute_commandExecutionError_throwsCommandException() {
        String deleteCommand = "delete 9";
        assertCommandException(deleteCommand, String.format(MESSAGE_INVALID_PERSON_DISPLAYED_INDEX, "9"));
    }

    @Test
    public void execute_validCommand_success() throws Exception {
        String listCommand = ListCommand.COMMAND_WORD;
        assertCommandSuccess(listCommand, ListCommand.MESSAGE_SUCCESS, model);
    }

    @Test
    public void execute_storageThrowsIoException_throwsCommandException() {
        assertCommandFailureForExceptionFromStorage(DUMMY_IO_EXCEPTION, String.format(
                LogicManager.FILE_OPS_ERROR_FORMAT, DUMMY_IO_EXCEPTION.getMessage()));
    }

    @Test
    public void execute_storageThrowsAdException_throwsCommandException() {
        assertCommandFailureForExceptionFromStorage(DUMMY_AD_EXCEPTION, String.format(
                LogicManager.FILE_OPS_PERMISSION_ERROR_FORMAT, DUMMY_AD_EXCEPTION.getMessage()));
    }

    @Test
    public void getFilteredPersonList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> logic.getFilteredPersonList().remove(0));
    }

    @Test
    public void execute_guardianAndRate_saveReloadAndNoChange() throws Exception {
        Person student = new PersonBuilder().withName("Tutor Student").build();
        model.addPerson(student);

        CommandResult guardianResult = logic.execute("guardian 1 g/+65 6777 8899");
        assertEquals("Guardian contact set for Tutor Student: +6567778899.", guardianResult.getFeedbackToUser());
        assertEquals(1, guardianResult.getSelectedPersonIndex().orElseThrow());
        assertEquals("Guardian contact for Tutor Student is already +6567778899.",
                logic.execute("guardian 1 g/6777-8899").getFeedbackToUser());
        assertEquals("Guardian contact updated for Tutor Student: +6567778899 -> +6591234567.",
                logic.execute("guardian 1 g/91234567").getFeedbackToUser());

        CommandResult rateResult = logic.execute("rate 1 r/80");
        assertEquals("Hourly rate set for Tutor Student: S$80.00.", rateResult.getFeedbackToUser());
        assertEquals(1, rateResult.getSelectedPersonIndex().orElseThrow());
        assertEquals("Hourly rate for Tutor Student is already S$80.00.", logic.execute("rate 1 r/80.00")
                .getFeedbackToUser());
        assertEquals("Hourly rate updated for Tutor Student: S$80.00 -> S$95.50.",
                logic.execute("rate 1 r/95.5").getFeedbackToUser());

        Person updated = logic.getFilteredPersonList().getFirst();
        assertEquals(java.util.Optional.of(new GuardianPhone("+6591234567")), updated.getGuardianPhone());
        assertEquals(java.util.Optional.of(new HourlyRate(new java.math.BigDecimal("95.50"))), updated.getHourlyRate());
        assertEquals(student.getPhone(), updated.getPhone());
        assertEquals(student.getAddress(), updated.getAddress());
        assertEquals(student.getEmail(), updated.getEmail());

        JsonAddressBookStorage reloadedStorage = new JsonAddressBookStorage(
                temporaryFolder.resolve("addressBook.json"));
        Person reloaded = reloadedStorage.readAddressBook().orElseThrow().getPersonList().getFirst();
        assertEquals(updated, reloaded);
    }

    @Test
    public void execute_guardianSaveFailure_preservesPerson() {
        Person student = new PersonBuilder().withName("Tutor Student").build();
        model.addPerson(student);
        JsonAddressBookStorage failingStorage = new JsonAddressBookStorage(temporaryFolder.resolve("failure.json")) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                throw DUMMY_IO_EXCEPTION;
            }
        };
        StorageManager failingStorageManager = new StorageManager(failingStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("failure-prefs.json")));
        logic = new LogicManager(model, failingStorageManager);

        assertThrows(CommandException.class, LogicManager.MESSAGE_ATOMIC_SAVE_FAILURE, () -> logic.execute(
                "guardian 1 g/91234567"));
        assertEquals(student, model.getFilteredPersonList().getFirst());
    }

    @Test
    public void execute_rateSaveFailure_preservesExistingRate() {
        Person base = new PersonBuilder().withName("Tutor Student").build();
        Person student = new Person(base.getName(), base.getPhone(), base.getEmail(), base.getAddress(), base.getTags(),
                java.util.Optional.empty(), java.util.Optional.of(new HourlyRate(new java.math.BigDecimal("80.00"))));
        model.addPerson(student);
        JsonAddressBookStorage failingStorage = new JsonAddressBookStorage(
                temporaryFolder.resolve("rate-failure.json")) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                throw DUMMY_IO_EXCEPTION;
            }
        };
        StorageManager failingStorageManager = new StorageManager(failingStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("rate-failure-prefs.json")));
        logic = new LogicManager(model, failingStorageManager);

        assertThrows(CommandException.class, LogicManager.MESSAGE_ATOMIC_SAVE_FAILURE, () -> logic.execute(
                "rate 1 r/95.50"));
        assertEquals(student, model.getFilteredPersonList().getFirst());
    }

    /**
     * Executes the command and confirms that
     * - no exceptions are thrown <br>
     * - the feedback message is equal to {@code expectedMessage} <br>
     * - the internal model manager state is the same as that in {@code expectedModel} <br>
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandSuccess(String inputCommand, String expectedMessage,
            Model expectedModel) throws CommandException, ParseException {
        CommandResult result = logic.execute(inputCommand);
        assertEquals(expectedMessage, result.getFeedbackToUser());
        assertEquals(expectedModel, model);
    }

    /**
     * Executes the command, confirms that a ParseException is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertParseException(String inputCommand, String expectedMessage) {
        assertCommandFailure(inputCommand, ParseException.class, expectedMessage);
    }

    /**
     * Executes the command, confirms that a CommandException is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandException(String inputCommand, String expectedMessage) {
        assertCommandFailure(inputCommand, CommandException.class, expectedMessage);
    }

    /**
     * Executes the command, confirms that the exception is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandFailure(String inputCommand, Class<? extends Throwable> expectedException,
            String expectedMessage) {
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        assertCommandFailure(inputCommand, expectedException, expectedMessage, expectedModel);
    }

    /**
     * Executes the command and confirms that
     * - the {@code expectedException} is thrown <br>
     * - the resulting error message is equal to {@code expectedMessage} <br>
     * - the internal model manager state is the same as that in {@code expectedModel} <br>
     * @see #assertCommandSuccess(String, String, Model)
     */
    private void assertCommandFailure(String inputCommand, Class<? extends Throwable> expectedException,
            String expectedMessage, Model expectedModel) {
        assertThrows(expectedException, expectedMessage, () -> logic.execute(inputCommand));
        assertEquals(expectedModel, model);
    }

    /**
     * Tests the Logic component's handling of an {@code IOException} thrown by the Storage component.
     *
     * @param e the exception to be thrown by the Storage component
     * @param expectedMessage the message expected inside exception thrown by the Logic component
     */
    private void assertCommandFailureForExceptionFromStorage(IOException e, String expectedMessage) {
        Path prefPath = temporaryFolder.resolve("ExceptionUserPrefs.json");

        // Inject LogicManager with a JsonAddressBookStorage that throws the IOException e when saving
        JsonAddressBookStorage addressBookStorage = new JsonAddressBookStorage(prefPath) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                throw e;
            }
        };

        JsonUserPrefsStorage userPrefsStorage =
                new JsonUserPrefsStorage(temporaryFolder.resolve("ExceptionUserPrefs.json"));
        StorageManager storage = new StorageManager(addressBookStorage, userPrefsStorage);

        logic = new LogicManager(model, storage);

        // Triggers the saveAddressBook method by executing an add command
        String addCommand = AddCommand.COMMAND_WORD + NAME_DESC_AMY + PHONE_DESC_AMY
                + EMAIL_DESC_AMY + ADDRESS_DESC_AMY;
        Person expectedPerson = new PersonBuilder(AMY).withTags().build();
        ModelManager expectedModel = new ModelManager();
        expectedModel.addPerson(expectedPerson);
        assertCommandFailure(addCommand, CommandException.class, expectedMessage, expectedModel);
    }
}
