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

    /*
     * Resolves the IP address of the mail server for a domain
     */
    public static String resolveDomain(String domain){
        try{
            ProcessBuilder pb = new ProcessBuilder("dig", "+short", "MX", domain);
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            process.waitFor();
            String[] splitResponse = reader.readLine().split("\\s+");
            if(splitResponse == null)
                return null;

            if(splitResponse.length != 2) return null;

            String mailServer = splitResponse[1];

            ProcessBuilder pb2 = new ProcessBuilder("dig", "+short", mailServer);
            Process process2 = pb2.start();
            BufferedReader reader2 = new BufferedReader(new InputStreamReader(process2.getInputStream()));
            process2.waitFor();
            String finalIP = reader2.readLine();
            return finalIP;
        }
        catch(IOException | InterruptedException  e){
            System.out.println("Error while resolving for : " + domain + " " + e.getMessage());
        }
        return null;
    }
}

