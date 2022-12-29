/*
 * Emircan Görkem ECE - 210303049
 * Ebrar Esila Mutlu - 190303066
 */

public class Encryption {
    public String encrypt(String decoded) {
        if(decoded.equals("")){
            return decoded;
        }

        String[] splitted = decoded.split("");
        StringBuilder sb = new StringBuilder();

        for (int i=0;i<splitted.length;i++) {
            splitted[i] = Character.toString(((splitted[i].charAt(0)) - 19) * 2);
        }

        for (String str: splitted) {
            sb.append(str);
        }

        return sb.toString();
    }
    public String decrypt(String encoded) {
        if (encoded.equals("")) {
            return encoded;
        }

        String[] splitted = encoded.split("");
        StringBuilder sb = new StringBuilder();

        for (int i=0;i<splitted.length;i++) {
            splitted[i] = Character.toString(((splitted[i].charAt(0)) / 2) + 19);
        }

        for (String str: splitted) {
            sb.append(str);
        }

        return sb.toString();
    }
}