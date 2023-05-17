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

public class Receiver {
    static boolean exitFlag = false; // A flag to know when to exit the program
    // Constant shared key for MAC encoding
    private static final byte[] BYTE_MAC_KEY = "AEB908AA1CEDFFDEA1F255640A05EEF6".getBytes(StandardCharsets.UTF_8);

    public static void main(String[] args) {

        File file_read = new File("sender_rsa_pub_key.txt");
        if(file_read.length() == 0) {       // If sender public key file is empty, the sender has not opened communication
            System.out.println("Sender has not opened communication!");
            System.out.println("Exiting Code...");      // Exit code if file is empty
        } else {
            Scanner input = new Scanner(System.in);
            System.out.println("Write a Key");      // Prompt the user to enter AES key
            String rec_aes_key = input.nextLine();

            try {
                // Generates key pair for RSA
                KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
                KeyPair pair = generator.generateKeyPair();
                PublicKey publicKey = pair.getPublic();         // Set public key
                PrivateKey privateKey = pair.getPrivate();      // Set private key

                // Send public key to the file
                FileOutputStream pub_key_reader = new FileOutputStream("receiver_rsa_pub_key.txt");
                pub_key_reader.write(publicKey.getEncoded());
                pub_key_reader.close();

                do {
                    // Wait for sender to send message
                    System.out.println("Waiting for sender to send a message...");
                    Thread.sleep(1000);         // Necessary to wait for the receiver to empty the file
                    File file = new File("Transmitted_Data.txt");
                    while(file.length() == 0) {         // Wait for the receiver to fill the file up
                        File rec_file = new File("sender_rsa_pub_key.txt");
                        if (rec_file.length() == 0) {   // If the file is ever zero while waiting, the user left
                            System.out.println("Sender has left the chat\nExiting Code...");
                            exitFlag = true;            // End the program
                            break;
                        }
                    }
                    if(!exitFlag) {                     // If the flag was false from the loop
                        // Decrypt AES Key
                        Scanner fileReader = new Scanner(file);
                        String sender_aes_text = fileReader.nextLine();
                        byte[] decoded_aes = Base64.getDecoder().decode(sender_aes_text);
                        String dec_aes_key = RSA.rsa_decrypt_aes(privateKey, decoded_aes);

                        // Decrypt Message
                        String encoded_msg = fileReader.nextLine();
                        String decrypted_msg = AES.decrypt(encoded_msg, dec_aes_key);

                        // Check MAC authentication
                        String received_mac = fileReader.nextLine();        // Obtain MAC from file
                        String made_mac = MAC.mac_encrypt(BYTE_MAC_KEY, decrypted_msg);
                        FileWriter fw = new FileWriter("Transmitted_Data.txt");     // Create a new MAC from the decrypted message
                        // THIS IS NEEDED TO FIX ASCII VS UTF-8 ERROR
                        fw.write(made_mac);     // Write it to the file
                        fw.close();
                        fileReader = new Scanner(file);
                        String test_mac = fileReader.nextLine();    // Read the new MAC
                        if(received_mac.equals(test_mac)) {         // Compare the new MAC with the original MAC
                            System.out.println("Sender --> " + decrypted_msg);  // If match display message
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
                            String enc_message = AES.encrypt(message, rec_aes_key);     // Encrypt message with AES
                            String mac_message = MAC.mac_encrypt(BYTE_MAC_KEY, message);    // Create MAC
                            messageWriter.append("\n" + enc_message);       // Append Ciphertext to file
                            messageWriter.append("\n" + mac_message);       // Append MAC to file
                            messageWriter.close();
                        }
                    }

                } while(!exitFlag);

                // Erase rsa public key after exiting the code
                FileWriter erase_file = new FileWriter("receiver_rsa_pub_key.txt");
                erase_file.write("");
                erase_file.close();
            } catch (IOException e) {
                System.out.println("Error Opening File: " + e.toString());
            } catch (Exception e) {
                System.out.print("Problems with Cryptography: " + e.toString());
            }

            // Needed in case an error is caught, still need to empty public key file
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
