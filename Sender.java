import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.*;
import java.security.spec.EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.security.KeyPair;

public class Sender {
    static boolean exitFlag = false; // A flag to know when to exit the program
    // Constant shared key for MAC encoding
    private static final byte[] BYTE_MAC_KEY = "AEB908AA1CEDFFDEA1F255640A05EEF6".getBytes(StandardCharsets.UTF_8);

    public static void main(String[] args) {
        try {
            // Erase The Files
            // Start a new clean slate when a new session is created
            FileWriter erase_rec_file = new FileWriter("receiver_rsa_pub_key.txt");
            FileWriter erase_sen_file = new FileWriter("sender_rsa_pub_key.txt");
            FileWriter erase_data_file = new FileWriter("Transmitted_Data.txt");
            erase_rec_file.write("");
            erase_sen_file.write("");
            erase_data_file.write("");
            erase_rec_file.close();
            erase_sen_file.close();
            erase_data_file.close();
        }catch (IOException e) {
            System.out.println("Error Opening File: " + e.toString());
        }

        try {
            // Generates key pair for RSA
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            KeyPair pair = generator.generateKeyPair();
            PublicKey publicKey = pair.getPublic();     // Set public key
            PrivateKey privateKey = pair.getPrivate();  // Set private key

            // Send public key to the file
            FileOutputStream send_pub_key_reader = new FileOutputStream("sender_rsa_pub_key.txt");
            send_pub_key_reader.write(publicKey.getEncoded());
            send_pub_key_reader.close();

            // Wait for the receiver to accept the connection (run its code)
            File file_read = new File("receiver_rsa_pub_key.txt");
            System.out.println("Waiting for receiver...");
            while(file_read.length() == 0) {}       // If file is empty, the receiver isn't online yet
            // Loop will break once the file is not empty

            Scanner input = new Scanner(System.in);
            System.out.println("Write a Key");      // Prompt the user to enter AES key
            String aes_key = input.nextLine();

            do {
                // Get receivers public key
                File rec_pub_key_reader = new File("receiver_rsa_pub_key.txt");
                byte[] pub_bytes = Files.readAllBytes(rec_pub_key_reader.toPath());
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(pub_bytes);
                PublicKey pub_key = keyFactory.generatePublic(publicKeySpec);

                // Set up File Writer to write data to file
                FileWriter messageWriter = new FileWriter("Transmitted_Data.txt");

                // Use Public key to encrypt AES key
                String encodeMessage = Base64.getEncoder().encodeToString(RSA.rsa_encrypt_aes(pub_key, aes_key));

                // Send AES to the file Encrypted
                messageWriter.write(encodeMessage);

                // Type Message
                System.out.print("Send a message: ");
                String message = input.nextLine();

                // Encrypt Message
                if(message.equalsIgnoreCase("!quit")) {     // If message is !quit, the loop will break
                    exitFlag = true;
                } else {
                    String enc_message = AES.encrypt(message, aes_key);     // Encrypt message with AES key
                    String mac_message = MAC.mac_encrypt(BYTE_MAC_KEY, message);    // create MAC
                    messageWriter.append("\n" + enc_message);       // Append to file
                    messageWriter.append("\n" + mac_message);       // Append to file
                    messageWriter.close();

                    System.out.println("Waiting for receiver to send a message...");
                    Thread.sleep(1000);     // Necessary to wait for the receiver to empty the file
                    File file = new File("Transmitted_Data.txt");
                    while (file.length() == 0) {        // Wait for the sender to fill the file up
                        File rec_file = new File("receiver_rsa_pub_key.txt");
                        if(rec_file.length() == 0) {    // If the file is ever zero while waiting, the user left
                            System.out.println("Receiver has left the chat\nExiting Code...");
                            exitFlag = true;            // End the program
                            break;
                        }
                    }
                    if(!exitFlag) {                     // If the flag was false from the loop
                        // Decrypt AES Key
                        Scanner fileReader = new Scanner(file);
                        String aes_text = fileReader.nextLine();
                        byte[] decoded_aes = Base64.getDecoder().decode(aes_text);
                        String dec_aes_key = RSA.rsa_decrypt_aes(privateKey, decoded_aes);

                        // Decrypt Message
                        String encoded_msg = fileReader.nextLine();
                        String decrypted_msg = AES.decrypt(encoded_msg, dec_aes_key); // Decrypt the message

                        // Check MAC authentication
                        String received_mac = fileReader.nextLine();        // Obtain MAC from file
                        FileWriter fw = new FileWriter("Transmitted_Data.txt");
                        String made_mac = MAC.mac_encrypt(BYTE_MAC_KEY, decrypted_msg);     // Create a new MAC from the decrypted message
                        // THIS IS NEEDED TO FIX ASCII VS UTF-8 ERROR
                        fw.write(made_mac);     // Write it to the file
                        fw.close();
                        fileReader = new Scanner(file);
                        String test_mac = fileReader.nextLine();    // Read the new MAC
                        if (received_mac.equals(test_mac)) {        // Compare the new MAC with the original MAC
                            System.out.println("Receiver --> " + decrypted_msg);    // If match display message

                        } else {        // If no match, display error
                            System.out.println("WARNING: AUTHENTICATION FAILURE! UNTRUSTED SOURCE");
                        }

                        // Clear contents of file
                        FileWriter erase_file = new FileWriter("Transmitted_Data.txt");
                        erase_file.write("");
                        erase_file.close();
                    }
                }

            } while (!exitFlag);

            // Erase rsa public key after exiting the code
            FileWriter erase_file = new FileWriter("sender_rsa_pub_key.txt");
            erase_file.write("");
            erase_file.close();
        } catch (IOException e) {
            System.out.println("Error Opening File: " + e.toString());
        } catch(Exception e) {
            System.out.println("Cryptography Error: " + e.toString());
        }

        // Needed in case an error is caught, still need to empty public key file
        try {
            // Erase rsa public key
            FileWriter erase_file = new FileWriter("sender_rsa_pub_key.txt");
            erase_file.write("");
            erase_file.close();
        }catch (IOException e) {
            System.out.println("Error Opening File: " + e.toString());
        }
    }
}
