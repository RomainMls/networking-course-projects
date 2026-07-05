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

    /*
     * true iff the user corresponds to an existing account
     */
    public boolean userExists() {
        for (String email : allowedUsers) {
            if (userName.equals(email))
                return true;
        }
        return false;
    }

    /*
     * true iff password is correct for this user
     */
    public boolean checkPassword(String password) {
        return password.equals("password");
    }

    /*
     * true iff the user domain is this server's domain
     */
    public boolean checkDomain() {
        return getUserDomain().equals(MailServer.getDomain());
    }

    /*
     * returns the domain of this user mail adress
     */
    public String getUserDomain() {
        String[] split = userName.split("@");
        if(split == null)
            return null;

        return split[1];
    }

    /*
     * get the username of the email address
     */
    public String getUserName() {
        return userName.split("@")[0];
    }

    @Override
    public String toString() {
        return userName;
    }
}

