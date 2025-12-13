/*
 * Gestion du stockage de tt les mailbox sur disque
 * 
 * "ouvre" les mailbox
 * charge et sauvegarde les metadata
 * crée des nouveaux msg
 * charger msg
 * créer mailbox si inexsistante
 * suppr des msg (flag deleted)
 * 
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.ArrayList;
/*
 *
    metadata.txt
        UIDVALIDITY: <nombre>
        NEXTUID: <nombre>
        <uid> <flag1> <flag2> ... <size>
        <uid> <flag1> <flag2> ... <size>
        ...

    .msg
        FROM: <from>
        RCPTS: <rcpt1> <rcpt2> ...
        SUBJECT: <subject>

        <body line 1>
        <body line 2>
        <body line 3>
        ...
*/
/*
 * Romain
 *     INBOX
 *         metadata.txt
 *         uid1.msg
 *         uid2.msg
 *         ...
 *     POUBELLE
 *         metadata.txt
 *         uid1.msg
 *         uid2.msg
 *         ...
 * Baptiste
 *     INBOX
 */

public class MailStore {

    /*
     *
     */
    public static Mailbox loadMailbox(User user, String mailboxName) {
        String path = createPathMailboxFromUser(user, mailboxName);
        createMailboxIfNotExists(user, mailboxName);
        Mailbox mailbox = new Mailbox(mailboxName);
        try (BufferedReader reader = new BufferedReader(new FileReader(path.concat("metadata.txt")))) {

            mailbox = readMetadata(mailbox, reader);

        } catch (IOException e) {
            System.out.println("Error while reading metadata file : " + e.getMessage());
        }

        for (Message message : mailbox.getAllMessages()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(path + message.getUid() + ".msg"))) {

                readMessage(message, reader);

            } catch (IOException e) {
                System.out.println("Error while reading message in file : " + e.getMessage());
            }
        }
        return mailbox;
    }

    /*
     * Stores the mailbox and all of the messages on the filesystem.
     * Writes metadata file containing UID validity, next UID.
     * Per message file with the name "<uid>.msg".
     * It overwrites previous stored messages and metadata file.
     */
    public static void saveMailbox(User user, Mailbox mailbox) {
        String mailboxName = mailbox.getName();
        String path = createPathMailboxFromUser(user, mailboxName);
        new File(path).mkdir();

        StringBuilder content = new StringBuilder();

        content.append("UIDVALIDITY: " + mailbox.getUidValidity() + "\n");
        content.append("NEXTUID: " + mailbox.getUidNext() + "\n");
        for (Message message : mailbox.getAllMessages()) {
            content.append(message.getUid() + " " + message.setFlagsToString() + " " + message.size() + "\n");

            String messagePath = path + message.getUid() + ".msg";

            StringBuilder messageContent = new StringBuilder();
            for (String dataLine : message.getDataLines())
                messageContent.append(dataLine + "\n");
            try (BufferedWriter messageWriter = new BufferedWriter(new FileWriter(messagePath))) {
                messageWriter.write(messageContent.toString());
            } catch (IOException e) {
                System.err.println("Error while writing message in file : " + e.getMessage());
            }
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path.concat("metadata.txt")))) {
            writer.write(content.toString());
        } catch (IOException e) {
            System.err.println("Error while writing mailbox : " + e.getMessage());
        }
    }

    /*
     * Returns the list of mailboxes names for this specific user
     */
    public static String[] getListMailboxes(User user) {
        File folder = new File(createPathFromUser(user));

        File[] directories = folder.listFiles(f -> f.isDirectory());

        if (directories == null) {
            return null;
        }
        String[] mailboxes = new String[directories.length];
        for (int i = 0; i < directories.length; i++)
            mailboxes[i] = directories[i].getName();
        return mailboxes;
    }

    /*
     * Return the list of messages file name of the mailbox
     */
    public static List<String> getListMails(User user, String mailboxName) {
        File folder = new File(createPathMailboxFromUser(user, mailboxName));

        File[] file = folder.listFiles(f -> f.isFile());

        if (file == null) {
            return null;
        }
        List<String> files = new ArrayList<String>();
        for (int i = 0; i < file.length; i++)
            if (file[i].getName().endsWith(".msg")) {
                files.add(file[i].getName());
            }
        return files;
    }

    /*
     * Creates a new mailbox for user.
     * Creates the directory of the file system if not existant.
     * Initializes metadata.
     */
    public static void createMailbox(User user, String mailboxName) {
        String path = createPathMailboxFromUser(user, mailboxName);
        File directory = new File(path);

        if (!directory.exists())
            directory.mkdirs();

        StringBuilder content = new StringBuilder();
        int uidValidity = (int) (System.currentTimeMillis() / 1000);
        content.append("UIDVALIDITY: " + uidValidity + "\n");
        content.append("NEXTUID: 1\n"); // nextUid

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path.concat("metadata.txt")))) {
            writer.write(content.toString());
        } catch (IOException e) {
            System.out.println("Error while writing mailbox : " + e.getMessage());
        }

    }

    /*
     * Deletes a mailbox from the filesystem
     */
    public static void deleteMailbox(User user, String mailboxName) {
        String[] listMaiboxes = getListMailboxes(user);
        for (String mailboxes : listMaiboxes) {
            if (mailboxName.equals(mailboxes)) {
                String path = createPathMailboxFromUser(user, mailboxName);
                deleteDirectory(path);
                break;
            }
        }
    }

    /*
     * Renames a mailbox on the filesystem
     */
    public static void renameMailbox(User user, String oldName, String newName) {
        String oldPath = createPathMailboxFromUser(user, oldName);
        String newPath = createPathMailboxFromUser(user, newName);
        try {
            Files.move(Paths.get(oldPath), Paths.get(newPath));
        } catch (IOException e) {
            System.out.println("Error while renaling mailbox : " + e.getMessage());
        }
    }

    /*
     * Synchronization between a RAM mailbox and a filesystem mailbox for the expunge function.
     */
    public static void expungeMailbox(User user, Mailbox mailbox) {
        String mailboxName = mailbox.getName();
        List<String> mailFiles = getListMails(user, mailboxName);
        List<Message> allMessages = mailbox.getAllMessages();

        for (String mailFile : mailFiles) {
            if(mailFile == null){

                continue;
            }
            boolean exists = false;
            String name = mailFile;
            if(name == null || name.length() == 0){
                System.err.println("Error in MailStore: null or empty file name");
                continue;
            }
            for (Message message : allMessages) {
                String[] split = name.split("\\.");
                if(split.length != 2){
                    System.err.println("Error in MailStore.expungeMailbox: incorrect split of filename: " + name);
                    continue;
                }
                if (Integer.parseInt(split[0]) == message.getUid()) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                File file = new File(createPathMailboxFromUser(user, mailboxName).concat(mailFile));
                file.delete();
            }
            StringBuilder content = new StringBuilder();

            content.append("UIDVALIDITY: " + mailbox.getUidValidity() + "\n");
            content.append("NEXTUID: " + mailbox.getUidNext() + "\n");
            for (Message message : mailbox.getAllMessages())
                content.append(
                        message.getUid() + " " + message.setFlagsToString() + " " + message.size() + "\n");

            try (BufferedWriter messageWriter = new BufferedWriter(
                    new FileWriter(createPathMailboxFromUser(user, mailboxName).concat("metadata.txt")))) {
                messageWriter.write(content.toString());
            } catch (IOException e) {
                System.out.println("Error while writing message in file : " + e.getMessage());
            }
        }
    }

    /*
     * Creates a mailbox if the directory for this mailbox does not exist. 
     */
    public static void createMailboxIfNotExists(User user, String mailboxName) {
        File file = new File(createPathMailboxFromUser(user, mailboxName));
        if (!file.exists()) {
            createMailbox(user, mailboxName);
        }
    }

    /*
     * Creates the path for this user for the filesystem
     */
    private static String createPathFromUser(User user) {
        return "mailstore/".concat(user.getUserDomain()).concat("/").concat(user.getUserName().concat("/"));
    }

    /*
     * Creates the path for this mailbox of user for the filesystem
     */
    private static String createPathMailboxFromUser(User user, String mailboxName) {
        return createPathFromUser(user).concat(mailboxName).concat("/");
    }

    /*
     * Deletes a directory from the filesystem.
     */
    private static boolean deleteDirectory(String path) {
        File toDelete = new File(path);

        File[] contents = toDelete.listFiles();
        if (contents != null) {
            for (File file : contents) {
                deleteDirectory(file.getPath());
            }
        }
        return toDelete.delete();
    }

    /*
     * Reads the metadata file on the filesystem to create the corresponding mailbox.
     */
    private static Mailbox readMetadata(Mailbox mailbox, BufferedReader reader) throws IOException {
        String line;
        line = reader.readLine();
        if (line != null && line.startsWith("UIDVALIDITY:")) {
            String[] parts = line.trim().split("\\s+");
            if (parts.length == 2)
                mailbox.setUidValidity(Integer.parseInt(parts[1].trim()));
        }

        line = reader.readLine();
        if (line != null && line.startsWith("NEXTUID:")) {
            String[] parts = line.split(":");
            if (parts.length == 2)
                mailbox.setNextUid(Integer.parseInt(parts[1].trim()));
        }

        while ((line = reader.readLine()) != null) {
            String[] parts = line.trim().split("\\s+");
            if (parts.length < 2)
                continue;

            Message message = new Message();
            message.setUid(Integer.parseInt(parts[0])); // load pas la taille, pas un attribut stocké
                                                        // mais calculé dans l'objet Message
            for (int i = 1; i < parts.length - 1; i++)
                message.addFlag(parts[i]);

            mailbox.addLoadedMessage(message);
        }
        return mailbox;
    }

    /*
     * Loads the all the contents of a message from memory
     */
    private static Message readMessage(Message message, BufferedReader reader) throws IOException {
        String line;

        while ((line = reader.readLine()) != null) {
            message.addDataLine(line);
        }
        message.extractSubject();
        return message;
    }
}

