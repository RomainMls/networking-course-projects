// vérifier user/password, domaine
// Dire oui ou non

public class User {
    private static final String[] allowedUsers = { "dcd@gembloux.uliege.be", "vj@gembloux.uliege.be",
            "dcd@info.uliege.be",
            "vj@info.uliege.be", "dcd@uliege.be", "vj@uliege.be" };
    private String user; // email address

    public User(String user) {
        this.user = user;
    }

    public boolean userExists() {
        for (String email : allowedUsers) {
            if (this.user.equals(email))
                return true;
        }
        return false;
    }

    public boolean checkPassword(String password) {
        return password.equals("password");
    }

    public boolean checkDomain() {
        return getUserDomain().equals(MailServer.getDomain());
    }

    public String getUserDomain() {
        return user.split("@")[1];
    }

    public String getUserName() {
        return user.split("@")[0];
    }

    @Override
    public String toString() {
        return user;
    }
}
