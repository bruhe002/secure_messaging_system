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

public class Receiver {
    static boolean exitFlag = false;
    private static final byte[] BYTE_MAC_KEY = "AEB908AA1CEDFFDEA1F255640A05EEF6".getBytes(StandardCharsets.UTF_8);

    public static void main(String[] args) {
        File file_read = new File("sender_rsa_pub_key.txt");
        if(file_read.length() == 0) {
            System.out.println("Sender has not opened communication!");
            System.out.println("Exiting Code...");
        } else {
            System.out.println("Waiting for sender...");
            while(file_read.length() == 0) {}

            Scanner input = new Scanner(System.in);
            System.out.println("Write a Key");
            String rec_aes_key = input.nextLine();

            try {
//            System.out.println("BLOCK ONE");
                KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
                KeyPair pair = generator.generateKeyPair();
                PublicKey publicKey = pair.getPublic();
                PrivateKey privateKey = pair.getPrivate();
                FileOutputStream pub_key_reader = new FileOutputStream("receiver_rsa_pub_key.txt");
                pub_key_reader.write(publicKey.getEncoded());
                pub_key_reader.close();

                do {
//                System.out.println("BLOCK TWO");
                    System.out.println("Waiting for sender to send a message...");
                    Thread.sleep(1000);
                    File file = new File("Transmitted_Data.txt");
                    while(file.length() == 0) {}

                    // Decrypt AES Key
                    Scanner fileReader = new Scanner(file);
                    String sender_aes_text = fileReader.nextLine();
                    byte[] decoded_aes = Base64.getDecoder().decode(sender_aes_text);
                    String dec_aes_key = RSA.rsa_decrypt_aes(privateKey, decoded_aes);

                    // Decrypt Message
                    String encoded_msg = fileReader.nextLine();
                    String decrypted_msg = AES.decrypt(encoded_msg, dec_aes_key);
                    String received_mac = fileReader.nextLine();
                    String made_mac = MAC.mac_encrypt(BYTE_MAC_KEY, decrypted_msg);
                    FileWriter fw = new FileWriter("Transmitted_Data.txt");
                    fw.write(made_mac);
                    fw.close();
                    fileReader = new Scanner(file);
                    String test_mac = fileReader.nextLine();
                    System.out.println(test_mac.equals(received_mac));
                    if(received_mac.equals(test_mac)) {
                        System.out.println("Sender --> " + decrypted_msg);

//                    System.out.println("BLOCK FIVE");
                        // Clear contents of file
                        FileWriter erase_file = new FileWriter("Transmitted_Data.txt");
                        erase_file.write("");
                        erase_file.close();

                    } else {
                        System.out.println("WARNING: AUTHENTICATION FAILURE! UNTRUSTED SOURCE");
                        // Clear contents of file
                        FileWriter erase_file = new FileWriter("Transmitted_Data.txt");
                        erase_file.write("");
                        erase_file.close();
                    }

                    // Get Sender public key
                    File rec_pub_key_reader = new File("sender_rsa_pub_key.txt");
                    byte[] pub_bytes = Files.readAllBytes(rec_pub_key_reader.toPath());
                    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                    EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(pub_bytes);
                    PublicKey pub_key = keyFactory.generatePublic(publicKeySpec);

                    // Use Public key to encrypt aes key
                    String encodeAES = Base64.getEncoder().encodeToString(RSA.rsa_encrypt_aes(pub_key, rec_aes_key));

                    // Send AES to the file
                    FileWriter messageWriter = new FileWriter("Transmitted_Data.txt");
                    messageWriter.write(encodeAES);
                    System.out.print("Send a message: ");
                    String message = input.nextLine();
                    if(message.equalsIgnoreCase("!quit")) {
                        exitFlag = true;
                    } else {
                        String enc_message = AES.encrypt(message, rec_aes_key);
                        String mac_message = MAC.mac_encrypt(BYTE_MAC_KEY, message);
                        messageWriter.append("\n" + enc_message);
                        messageWriter.append("\n" + mac_message);
                        messageWriter.close();
                    }
                } while(!exitFlag);

            } catch (IOException e) {
                System.out.println("Error Opening File: " + e.toString());
            } catch (Exception e) {
                System.out.print("Problems with Cryptography: " + e.toString());
            }

            try {
                // Erase rsa public key
                FileWriter erase_file = new FileWriter("receiver_rsa_pub_key.txt");
                erase_file.write("");
                erase_file.close();
            }catch (IOException e) {
                System.out.println("Error Opening File: " + e.toString());
            }
        }
    }
}
