// vérifier user/password, domaine
// Dire oui ou non

public class User {
    private static final String[] allowedUsers = { "dcd@gembloux.uliege.be", "vj@gembloux.uliege.be",
            "dcd@info.uliege.be",
            "vj@info.uliege.be", "dcd@uliege.be", "vj@uliege.be" };
    private String userName; // email address

    public User(String userName) {
        if(!userName.contains("@")){
            userName += "@" + MailServer.getDomain();
        }
        this.userName = userName;
    }

    public boolean userExists() {
        for (String email : allowedUsers) {
            if (userName.equals(email))
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
        String[] split = userName.split("@");
        if(split == null)
            return null;

        return split[1];
    }

    public String getUserName() {
        return userName.split("@")[0];
    }

    @Override
    public String toString() {
        return userName;
    }
}
