/*
 * lookup MX
 * lookup A
 * déterminer l'ip de la cible
 * décider des chemins, direct, relai, erreur
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class DomainResolver {
    public static String resolve(String domain, String typeRecord) {
        if (!typeRecord.equals("MX") && !typeRecord.equals("A"))
            return null;
        else
            try {
                ProcessBuilder pb = new ProcessBuilder("dig", "+short", typeRecord, domain);
                Process process = pb.start();
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line = reader.readLine();
                System.err.println("dig response for domain '" + domain + "': " + line);

                if (line != null) {
                    line = line.trim();
                    String[] parts = line.split("\\s+");
                    String host;

                    if (parts.length == 2)
                        host = parts[1];
                    else
                        host = line;

                    if (host.endsWith("."))
                        host = host.substring(0, host.length() - 1);
                    return host;
                }
            } catch (IOException e) {
                System.out.println("Error while resolving " + typeRecord + " for : " + domain + " " + e.getMessage());
            }
        return null;
    }

    public static String resolveSmtpServer(String domain) {
        if (domain.equals(MailServer.getDomain()))
            return null;

        else {
            String mx = resolve(domain, "MX");
            if (mx != null)
                return mx;
            else {
                String a = resolve(domain, "A");
                if (a != null)
                    return a;
            }
        }
        return null;
    }
}
