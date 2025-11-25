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
import java.util.Set;
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
    public static Mailbox loadMailbox(User user, String mailboxName) {
        String path = createPathMailboxFromUser(user, mailboxName);
        createIfNotExists(user, mailboxName);
        Mailbox mailbox = new Mailbox();
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

    public static void saveMailbox(User user, String mailboxName, Mailbox mailbox) {
        String path = createPathMailboxFromUser(user, mailboxName);
        new File(path).mkdir();

        StringBuilder content = new StringBuilder();

        content.append("UIDVALIDITY: " + mailbox.getUidValidity() + "\n");
        content.append("NEXTUID: " + mailbox.getUidNext() + "\n");
        for (Message message : mailbox.getAllMessages()) {
            content.append(message.getUid() + " " + setFlagsToString(message.getFlags()) + message.size() + "\n");

            String messagePath = path + message.getUid() + ".msg";

            StringBuilder messageContent = new StringBuilder();
            messageContent.append("FROM: " + message.getFrom() + "\n");
            messageContent.append("RCPTS: " + listToString(message.getRcpts()) + "\n");
            messageContent.append("SUBJECT: " + message.getSubject() + "\n");
            messageContent.append("\n");
            for (String dataLine : message.getDataLines())
                messageContent.append(dataLine + "\n");
            try (BufferedWriter messageWriter = new BufferedWriter(new FileWriter(messagePath))) {
                messageWriter.write(messageContent.toString());
            } catch (IOException e) {
                System.out.println("Error while writing message in file : " + e.getMessage());
            }
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path.concat("metadata.txt")))) {
            writer.write(content.toString());
        } catch (IOException e) {
            System.out.println("Error while writing mailbox : " + e.getMessage());
        }
    }

    public static String[] getListMailboxes(User user) {
        File folder = new File(createPathFromUser(user));

        File[] directories = folder.listFiles(f -> f.isDirectory());

        if (directories == null) {
            return new String[0];
        }
        String[] mailboxes = new String[directories.length];
        for (int i = 0; i < directories.length; i++)
            mailboxes[i] = directories[i].getName();
        return mailboxes;
    }

    public static String[] getListMails(User user, String mailboxName) {
        File folder = new File(createPathFromUser(user) + "/" + mailboxName);

        File[] file = folder.listFiles(f -> f.isFile());

        if (file == null) {
            return new String[0];
        }
        String[] files = new String[file.length];
        int counter = 0;
        for (int i = 0; i < file.length; i++)
            if (file[i].getName().endsWith(".msg")) {
                files[counter] = file[i].getName();
                counter++;
            }
        return files;
    }

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

    public static void renameMailbox(User user, String oldName, String newName) {
        String oldPath = createPathMailboxFromUser(user, oldName);
        String newPath = createPathMailboxFromUser(user, newName);
        try {
            Files.move(Paths.get(oldPath), Paths.get(newPath));
        } catch (IOException e) {
            System.out.println("Error while renaling mailbox : " + e.getMessage());
        }
    }

    public static void expungeMailbox(User user, String mailboxName, Mailbox mailbox) {
        String[] mailFiles = getListMails(user, mailboxName);
        List<Message> allMessages = mailbox.getAllMessages();

        for (String mailFile : mailFiles) {
            boolean exists = false;
            String name = mailFile.split("\\.")[0];
            for (Message message : allMessages) {
                if (Integer.parseInt(name) == message.getUid())
                    exists = true;
                break;
            }
            if (!exists) {
                File file = new File(createPathMailboxFromUser(user, mailboxName).concat(mailFile));
                file.delete();
            }
        }
    }

    private static String createPathFromUser(User user) {
        return "mailstore/".concat(user.getUserDomain()).concat("/").concat(user.getUserName().concat("/"));
    }

    private static String createPathMailboxFromUser(User user, String mailboxName) {
        return createPathFromUser(user).concat(mailboxName).concat("/");
    }

    private static String setFlagsToString(Set<String> flags) {
        if (flags.isEmpty()) { // Flags1 Flags2 Flags
            return "";
        }
        StringBuilder string = new StringBuilder();
        for (String flag : flags) {
            string.append(flag).append(" ");
        }
        return string.toString();
    }

    private static String listToString(List<String> list) {
        if (list.isEmpty()) {
            return "";
        }
        StringBuilder string = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            string.append(list.get(i));
            if (i < list.size() - 1) // No space after the last
                string.append(" ");
        }
        return string.toString();
    }

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

    private static Message readMessage(Message message, BufferedReader reader) throws IOException {
        String line;

        line = reader.readLine();
        if (line != null && line.startsWith("FROM:")) {
            String from = line.substring("FROM:".length()).trim();
            message.setFrom(from);
        }
        line = reader.readLine();
        if (line != null && line.startsWith("RCPTS:")) {
            String rcpts = line.substring("RCPTS:".length()).trim();
            if (!rcpts.isEmpty()) {
                String[] rctpsTab = rcpts.split("\\s+");
                for (String rcpt : rctpsTab) {
                    message.addRcpt(rcpt);
                }
            }
        }

        line = reader.readLine();
        if (line != null && line.startsWith("SUBJECT:")) {
            String subject = line.substring("SUBJECT:".length()).trim();
            message.setSubject(subject);
        }

        reader.readLine(); // Read the empty line

        while ((line = reader.readLine()) != null) {
            message.addDataLine(line);
        }
        return message;
    }

    private static void createIfNotExists(User user, String mailboxName) {
        String path = createPathFromUser(user);
        File file = new File(path);
        if(!file.exists()){
            try{
                file.createNewFile();
            }
            catch(IOException e){
                System.out.println("Error while creating new file :" + e.getMessage());
            }
        }
        String path2 = createPathMailboxFromUser(user, mailboxName);
        File file2 = new File(path2);
        if(!file2.exists()){
            try{
                file2.createNewFile();
            }
            catch(IOException e){
                System.out.println("Error while creating new file :" + e.getMessage());
            }
            createMailbox(user, mailboxName);
        }
    }
}
