//@@author danielbrzn
package seedu.address.logic.commands;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.services.people.v1.model.Person;

import javafx.application.Platform;
import javafx.concurrent.Task;
import seedu.address.commons.core.EventsCenter;
import seedu.address.commons.events.ui.CloseProgressEvent;
import seedu.address.commons.events.ui.NewResultAvailableEvent;
import seedu.address.commons.events.ui.ShowProgressEvent;
import seedu.address.commons.util.GoogleUtil;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.person.exceptions.DuplicatePersonException;

/**
 * Imports contacts from the user's specified online service
 */
public class ImportCommand extends UndoableCommand {


    public static final String COMMAND_WORD = "import";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Imports contacts from a specified online service. "
            + "Parameters: "
            + "SERVICE_NAME "
            + "Example: " + COMMAND_WORD + " "
            + "google ";

    public static final String MESSAGE_SUCCESS = "%1$s contacts imported. %2$s contacts failed to import.";
    public static final String MESSAGE_IN_PROGRESS = "Importing in progress";
    public static final String MESSAGE_INVALID_PEOPLE = "The contacts unable to be imported are: ";
    public static final String MESSAGE_FAILURE = "Failed to import contacts.";
    public static final String MESSAGE_FAILURE_EMPTY = "0 contacts imported as you have zero Google contacts.";
    public static final String MESSAGE_CONNECTION_FAILURE = "Failed to access Google. Check your internet connection or"
            + " try again in a few minutes.";
    public static final int ADDRESSBOOK_SIZE = 1000;
    private static int peopleAdded;
    private static ArrayList<String> invalidPeople;
    private static Credential credential;
    private static HttpTransport httpTransport;
    private final String service;

    /**
     * Creates an ImportCommand to add contacts from the specified service
     */
    public ImportCommand(String service) {
        this.service = service.toLowerCase();
        peopleAdded = 0;
        try {
            httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @Override
    protected CommandResult executeUndoableCommand() throws CommandException {
        // Check for connectivity to Google
        if (!GoogleUtil.isReachable()) {
            throw new CommandException(MESSAGE_CONNECTION_FAILURE);
        }
        Thread thread = new Thread(() -> {
            try {
                credential = GoogleUtil.authorize(httpTransport);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        thread.start();

        try {
            thread.join(20000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new CommandException(MESSAGE_FAILURE);
        }

        if (credential == null) {
            throw new CommandException(MESSAGE_FAILURE);
        }

        // Retrieve a list of Persons
        List<Person> connections = GoogleUtil.retrieveContacts(credential, httpTransport);

        if (connections == null) {
            throw new CommandException(MESSAGE_FAILURE_EMPTY);
        }

        // Import contacts into the application
        importContacts(connections);

        return new CommandResult(MESSAGE_IN_PROGRESS);
    }


    /**
     * Imports contacts into the application using the given {@code List<Person>}
     */
    public void importContacts(List<Person> connections) {

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                int amountToAdd = connections.size();
                invalidPeople = new ArrayList<String>();
                for (Person person : connections) {
                    seedu.address.model.person.Person toAdd = GoogleUtil.convertPerson(person);
                    if (toAdd == null) {
                        invalidPeople.add(person.getNames().get(0).getDisplayName());
                        continue;
                    }
                    try {
                        model.addPerson(toAdd);
                    } catch (DuplicatePersonException e) {
                        e.printStackTrace();
                    }
                    peopleAdded++;
                    Platform.runLater(() -> {
                        updateProgress(peopleAdded, amountToAdd);

                    });
                }
                return null;
            }
        };

        task.setOnSucceeded(t -> {
            EventsCenter.getInstance().post(new CloseProgressEvent());
            StringBuilder sb = new StringBuilder();
            if (connections.size() - peopleAdded > 0) {
                sb.append(String.format(MESSAGE_SUCCESS, peopleAdded, connections.size() - peopleAdded));
                sb.append(" ");
                sb.append(MESSAGE_INVALID_PEOPLE);
                sb.append(invalidPeople.toString());
            } else {
                sb.append(String.format(MESSAGE_SUCCESS, peopleAdded, connections.size() - peopleAdded));
            }
            EventsCenter.getInstance().post(new NewResultAvailableEvent(sb.toString(), false));
        });

        task.setOnFailed(t -> {
            EventsCenter.getInstance().post(new CloseProgressEvent());
            EventsCenter.getInstance().post(new NewResultAvailableEvent(String.format(MESSAGE_FAILURE), true));
        });

        EventsCenter.getInstance().post(new ShowProgressEvent(task.progressProperty()));
        Thread importThread = new Thread(task);
        importThread.start();
    }

    @Override
    public boolean equals(Object other) {
        return other == this // short circuit if same object
                || (other instanceof ImportCommand // instanceof handles nulls
                && service.equals(((ImportCommand) other).service));
    }

}