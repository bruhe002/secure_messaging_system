import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.*;
import java.security.spec.EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.security.KeyPair;


import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.BadPaddingException;

public class RSA {
    public static byte[] rsa_encrypt_aes(final PublicKey pu, final String aes_key) {
        try {
            Cipher encryptCipher = Cipher.getInstance("RSA");
            // Use Public key to encrypt AES key
            encryptCipher.init(Cipher.ENCRYPT_MODE, pu);
            byte[] aes_key_byte = aes_key.getBytes(StandardCharsets.UTF_8);
            return encryptCipher.doFinal(aes_key_byte);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException
                | BadPaddingException e) {
            e.printStackTrace();
        } catch(Exception e) {
            System.out.println("Cryptography Error: " + e.toString());
        }

        return null;
    }

    public static String rsa_decrypt_aes(final PrivateKey pr, final byte[] aes_key) {
        try {
            Cipher decryptCipher = Cipher.getInstance("RSA");
            // Use Private key to decrypt AES key
            decryptCipher.init(Cipher.DECRYPT_MODE, pr);
            byte[] decrypted_aes_byte = decryptCipher.doFinal(aes_key);
            return new String(decrypted_aes_byte, StandardCharsets.UTF_8);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException
                | BadPaddingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("Cryptography Error: " + e.toString());
        }
        return null;
    }
}
