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

    public Message(int uid) {
        this.uid = uid;
    }

    public int getUid() {
        return this.uid;
    }

    public void addFlag(String flag) {
        this.flags.add(flag);
    }

    public void removeFlag(String flag) {
        this.flags.remove(flag);
    }

    public Set<String> getFlags() {
        return this.flags;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getFrom() {
        return this.from;
    }

    public void addRcpt(String rcpt) {
        this.rcpts.add(rcpt);
    }

    public List<String> getRcpts() {
        return this.rcpts;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getSubject() {
        return this.subject;
    }

    public void addDataLine(String dataLine) {
        this.dataLines.add(dataLine);
    }

    public List<String> getDataLines() {
        return this.dataLines;
    }

    public int size() {
        int size = 0;
        for (String dataLine : this.dataLines) {
            size += dataLine.getBytes().length + 2; // +2 pour le CRLF
        }
        return size;
    }
}