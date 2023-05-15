
import java.nio.charset.StandardCharsets;
import java.security.*;
import javax.crypto.*;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;

public class MAC {

    public static String mac_encrypt(byte[] key, String str) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));

            byte[] bytes = str.getBytes();
            byte[] macResult = mac.doFinal(bytes);

            return new String(macResult, StandardCharsets.US_ASCII);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            System.out.println("Cryptography Error: " + e.toString());
        }

        return null;
    }
}
