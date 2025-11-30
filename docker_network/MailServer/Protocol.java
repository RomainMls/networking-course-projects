enum Protocol {
    SMTP,
    POP3,
    IMAP;

    public int getPort() {
        switch (this) {
            case SMTP:
                return 25;
            case POP3:
                return 110;
            case IMAP:
                return 143;
            default:
                return -1;
        }
    }
}
