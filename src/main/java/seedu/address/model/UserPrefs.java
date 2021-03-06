package seedu.address.model;

import java.util.HashMap;
import java.util.Objects;

import seedu.address.commons.core.GuiSettings;

/**
 * Represents User's preferences.
 */
public class UserPrefs {

    private GuiSettings guiSettings;
    private String addressBookFilePath = "data/HitMeUp.xml";
    private String addressBookName = "HitMeUp";
    private String displayPicDir = "data/images/";
    private HashMap<String, String> aliasMap;

    public UserPrefs() {
        this.setGuiSettings(500, 500, 0, 0);
        aliasMap = new HashMap<>();
    }

    //@@author danielbrzn
    public void resetAlias(HashMap<String, String> prevAliasMap) {
        this.aliasMap = prevAliasMap;
    }

    //@@author
    public GuiSettings getGuiSettings() {
        return guiSettings == null ? new GuiSettings() : guiSettings;
    }

    public void updateLastUsedGuiSetting(GuiSettings guiSettings) {
        this.guiSettings = guiSettings;
    }

    public void setGuiSettings(double width, double height, int x, int y) {
        guiSettings = new GuiSettings(width, height, x, y);
    }

    public String getAddressBookFilePath() {
        return addressBookFilePath;
    }

    public void setAddressBookFilePath(String addressBookFilePath) {
        this.addressBookFilePath = addressBookFilePath;
    }

    public String getDisplayPicDir() {
        return displayPicDir;
    }

    public void setDisplayPicDir(String displayPicDir) {
        this.displayPicDir = displayPicDir;
    }

    public String getAddressBookName() {
        return addressBookName;
    }

    public void setAddressBookName(String addressBookName) {
        this.addressBookName = addressBookName;
    }

    //@@author danielbrzn
    public HashMap<String, String> getAliasMap() {
        return aliasMap;
    }

    public void addAlias(String alias, String command) {
        aliasMap.put(alias, command);
    }

    public String getAlias(String alias) {
        return aliasMap.get(alias);
    }

    //@@author
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof UserPrefs)) { //this handles null as well.
            return false;
        }

        UserPrefs o = (UserPrefs) other;

        return Objects.equals(guiSettings, o.guiSettings)
                && Objects.equals(addressBookFilePath, o.addressBookFilePath)
                && Objects.equals(addressBookName, o.addressBookName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(guiSettings, addressBookFilePath, addressBookName);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Gui Settings : " + guiSettings.toString());
        sb.append("\nLocal data file location : " + addressBookFilePath);
        sb.append("\nAddressBook name : " + addressBookName);
        return sb.toString();
    }

}
