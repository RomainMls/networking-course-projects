/*
 * Représente un message pour pouvoir stocker UID, flags (seen, deleted,...),
 * taille en octets, contenu
 * Sur disque un msg est nommé UID.msg
 * 
 */

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Message {
    private int uid;
    private Set<String> flags = new HashSet<String>();
    private String from;
    private List<String> rcpts = new ArrayList<String>();
    private String subject;
    private List<String> dataLines = new ArrayList<String>();

    /*
     * Represents an email message, including uid, from, 
     * recipients, message body and flags.
     * These are empty by default.
     */
    public Message() {
    }

    /*
     * Assign the unqiue identifier for this message
     */
    public void setUid(int uid) {
        this.uid = uid;
    }

    /*
     * Return the message's unique uid.
     */
    public int getUid() {
        return this.uid;
    }

    /*
     * Return the index of this message within the mailbox.
     * If the message is not part of the mailbox, the return value is -1.
     */
    public int getSequenceNumber(Mailbox mailbox) {
        List<Message> messages = mailbox.getAllMessages();
        for (int i = 0; i < messages.size(); i++) {
            if (messages.get(i).equals(this)) {
                return i + 1;
            }
        }
        return -1;
    }

    /*
     * Adds flag to the flag set
     */
    public void addFlag(String flag) {
        this.flags.add(flag);
    }

    /*
     * Removes flag from the flat set
     */
    public void removeFlag(String flag) {
        this.flags.remove(flag);
    }

    /*
     * Returns the set of flags.
     */
    public Set<String> getFlags() {
        return this.flags;
    }

    /*
     * Resets the set of flags to the empty set.
     */
    public void resetFlags() {
        flags = new HashSet<String>();
    }

    /*
     * Sets the "From" field.
     */
    public void setFrom(String from) {
        this.from = from;
    }

    /*
     * Return the "From" header.
     */
    public String getFrom() {
        return this.from;
    }

    /*
     * Adds a recipient to the list.
     */
    public void addRcpt(String rcpt) {
        this.rcpts.add(rcpt);
    }

    /*
     * Return the list of recipients of this message.
     */
    public List<String> getRcpts() {
        return this.rcpts;
    }

    /*
     * Clears the recipients list
     */
    public void clearRcpts() {
        this.rcpts = new ArrayList<String>();
    }

    /*
     * Sets the message subject
     */
    public void setSubject(String subject) {
        this.subject = subject;
    }

    /*
     * Return the message subject
     */
    public String getSubject() {
        return this.subject;
    }

    /*
     * Appends a line to the message body
     */
    public void addDataLine(String dataLine) {
        this.dataLines.add(dataLine);
    }

    /*
     * Return the body of the message in the form of a list of lines.
     */
    public List<String> getDataLines() {
        return this.dataLines;
    }

    /*
     * Returns the number of bytes of the body.
     */
    public int size() {
        int size = 0;
        for (String dataLine : this.dataLines) {
            size += dataLine.getBytes().length + 2; // +2 pour le CRLF
        }
        return size;
    }

    /*
     * Extracts the subject from the message body.
     */
    public void extractSubject() {
        for (String dataLine : this.getDataLines()) {
            if (dataLine.isEmpty())
                break;

            if (dataLine.startsWith("Subject:")) {
                this.setSubject(dataLine.substring("Subject:".length()).trim());
                break;
            }
        }
    }

    // Return the list of the flags in the follwing form: "Flags1 Flags2 Flags3"
    public String setFlagsToString() {
        if (flags.isEmpty()) {
            return "";
        }
        StringBuilder string = new StringBuilder();
        for (String flag : flags) {
            string.append(flag).append(" ");
        }

        string.deleteCharAt(string.length() - 1);
        return string.toString();
    }
}

