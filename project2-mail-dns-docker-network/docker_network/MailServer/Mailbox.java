/*
 * Représente la mailbox d'un utilisateur (au minimum INBOX par utilisateur)
 * Autrement dit, représente tous les msg d'un user
 * 
 * UIDVALIDITY, nextUID, listes des messages (références vers Message)
 * ajouter, supprimer, marquer msg
 * retrouver un msg par seq number pour POP3 (RETR 1, RETR 2,...)
 * pour POP3, numéro msg par arrivée, le premier mail est num 1,.. attention
 * quand un mail est suppr, les num changent
 * retrouver un msg par UID pour IMAP
 * Un fichier metadata par Mailbox pour stocker l'UIDVALIDITY de la mailbox, le
 * nextUID, UID des msg et leur flag + taille
 */

import java.util.ArrayList;
import java.util.List;

public class Mailbox {
    private List<Message> messages = new ArrayList<Message>();
    private String name; // INBOX by default
    private int nextUID = 1;
    private int UIDVALIDITY;

    /*
     * Initializes a mailbox with the given name and an empty list of messages
     */
    public Mailbox(String name) {
        this.name = name;
    }

    /*
     * Sets the UID validity of the mailbox
     */
    public void setUidValidity(int uidValidity) {
        this.UIDVALIDITY = uidValidity;
    }

    /*
     * Sets the next UID to assign to the next message
     */
    public void setNextUid(int nextUid) {
        this.nextUID = nextUid;
    }

    /*
     * Return the UID validity
     */
    public int getUidValidity() {
        return UIDVALIDITY;
    }

    /*
     * Return the UID that will be assigned to the next message
     */
    public int getUidNext() {
        return nextUID;
    }

    /*
     * Return the name of the mailbox
     */
    public String getName() {
        return name;
    }

    /*
     * Return all messages currently in the mailbox in the form of a list.
     */
    public List<Message> getAllMessages() {
        return this.messages;
    }

    /*
     * Adds a message to the list of messages in the list
     */
    public void addMessage(Message message) {
        message.setUid(nextUID);
        nextUID++;
        messages.add(message);
    }

    /*
     * adds externally loaded message without modifying UID sequence
     */
    public void addLoadedMessage(Message message) {
        messages.add(message);
    }

    /*
     * Return the message which has the corresponding uid
     */
    public Message getMessageByUid(int uid) {
        for (Message message : messages) {
            if (uid == message.getUid())
                return message;
        }
        return null;
    }

    /*
     * Gets the message at position 'index' of the list of messages
     */
    public Message getMessageByIndex(int index) {
        return messages.get(index - 1);
    }

    // Renvoit une liste d'entier pour les index de séquence pour le IMAP expunge
    public List<Integer> expunge() {
        List<Integer> sequenceIndex = new ArrayList<Integer>();
        int nbRemove = 0;
        for (int i = 0; i < messages.size(); i++) {
            if (messages.get(i).getFlags().contains("\\Deleted")) {
                sequenceIndex.add(i - nbRemove);
                nbRemove++;
            }
        }

        for (int i = messages.size() - 1; i >= 0; i--) {
            if (messages.get(i).getFlags().contains("\\Deleted"))
                messages.remove(i);
        }

        return sequenceIndex;
    }

    public int getExistsCount() {
        return messages.size();
    }
}

