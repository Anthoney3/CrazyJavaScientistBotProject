package com.crazy.scientist.crazyjavascientist.security;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Service;

@Service
public class EncryptorAESGCM {

  /**
   * AES-GCM inputs - 12 bytes IV, need the same IV and secret keys for encryption and decryption.
   * <p>
   * The output consist of iv, password's salt, encrypted content and auth tag in the following format:
   * output = byte[] {i i i s s s c c c c c c ...}
   * <p>
   * i = IV bytes
   * s = Salt bytes
   * c = content bytes (encrypted content)
   */

  private final String ENCRYPT_ALGO = "AES/GCM/NoPadding";

  private final int TAG_LENGTH_BIT = 128; // must be one of {128, 120, 112, 104, 96}
  private final int IV_LENGTH_BYTE = 12;
  private final int SALT_LENGTH_BYTE = 16;
  private final Charset UTF_8 = StandardCharsets.UTF_8;

  // return a base64 encoded AES encrypted text
  public String encrypt(byte[] pText, String password) throws Exception {
    // 16 bytes salt
    byte[] salt = getRandomNonce(SALT_LENGTH_BYTE);

    // GCM recommended 12 bytes iv?
    byte[] iv = getRandomNonce(IV_LENGTH_BYTE);

    // secret key from password
    SecretKey aesKeyFromPassword = getAESKeyFromPassword(
      password.toCharArray(),
      salt
    );

    Cipher cipher = Cipher.getInstance(ENCRYPT_ALGO);

    // ASE-GCM needs GCMParameterSpec
    cipher.init(
      Cipher.ENCRYPT_MODE,
      aesKeyFromPassword,
      new GCMParameterSpec(TAG_LENGTH_BIT, iv)
    );

    byte[] cipherText = cipher.doFinal(pText);

    // prefix IV and Salt to cipher text
    byte[] cipherTextWithIvSalt = ByteBuffer
      .allocate(iv.length + salt.length + cipherText.length)
      .put(iv)
      .put(salt)
      .put(cipherText)
      .array();

    // string representation, base64, send this string to other for decryption.
    return Base64.getEncoder().encodeToString(cipherTextWithIvSalt);
  }

  // we need the same password, salt and iv to decrypt it
  public String decrypt(String cText, String password) throws Exception {
    byte[] decode = Base64.getDecoder().decode(cText.getBytes(UTF_8));

    // get back the iv and salt from the cipher text
    ByteBuffer bb = ByteBuffer.wrap(decode);

    byte[] iv = new byte[IV_LENGTH_BYTE];
    bb.get(iv);

    byte[] salt = new byte[SALT_LENGTH_BYTE];
    bb.get(salt);

    byte[] cipherText = new byte[bb.remaining()];
    bb.get(cipherText);

    // get back the aes key from the same password and salt
    SecretKey aesKeyFromPassword = getAESKeyFromPassword(
      password.toCharArray(),
      salt
    );

    Cipher cipher = Cipher.getInstance(ENCRYPT_ALGO);

    cipher.init(
      Cipher.DECRYPT_MODE,
      aesKeyFromPassword,
      new GCMParameterSpec(TAG_LENGTH_BIT, iv)
    );

    byte[] plainText = cipher.doFinal(cipherText);

    return new String(plainText, UTF_8);
  }

  /*public static void main(String[] args) throws Exception {

            String OUTPUT_FORMAT = "%-30s:%s";
            String PASSWORD = "this is a password";
            String pText = "AES-GSM Password-Bases encryption!";

            String encryptedTextBase64 = EncryptorAesGcmPassword.encrypt(pText.getBytes(UTF_8), PASSWORD);

            System.out.println("\n------ AES GCM Password-based Encryption ------");
            System.out.println(String.format(OUTPUT_FORMAT, "Input (plain text)", pText));
            System.out.println(String.format(OUTPUT_FORMAT, "Encrypted (base64) ", encryptedTextBase64));

            System.out.println("\n------ AES GCM Password-based Decryption ------");
            System.out.println(String.format(OUTPUT_FORMAT, "Input (base64)", encryptedTextBase64));

            String decryptedText = EncryptorAesGcmPassword.decrypt(encryptedTextBase64, PASSWORD);
            System.out.println(String.format(OUTPUT_FORMAT, "Decrypted (plain text)", decryptedText));

        }*/

  private byte[] getRandomNonce(int numBytes) {
    byte[] nonce = new byte[numBytes];
    new SecureRandom().nextBytes(nonce);
    return nonce;
  }

  // AES secret key
  private SecretKey getAESKey(int keysize) throws NoSuchAlgorithmException {
    KeyGenerator keyGen = KeyGenerator.getInstance("AES");
    keyGen.init(keysize, SecureRandom.getInstanceStrong());
    return keyGen.generateKey();
  }

  // Password derived AES 256 bits secret key
  private SecretKey getAESKeyFromPassword(char[] password, byte[] salt)
    throws NoSuchAlgorithmException, InvalidKeySpecException {
    SecretKeyFactory factory = SecretKeyFactory.getInstance(
      "PBKDF2WithHmacSHA256"
    );
    // iterationCount = 65536
    // keyLength = 256
    KeySpec spec = new PBEKeySpec(password, salt, 65536, 256);
    return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
  }

  // hex representation
  private String hex(byte[] bytes) {
    StringBuilder result = new StringBuilder();
    for (byte b : bytes) {
      result.append(String.format("%02x", b));
    }
    return result.toString();
  }

  // print hex with block size split
  private String hexWithBlockSize(byte[] bytes, int blockSize) {
    String hex = hex(bytes);

    // one hex = 2 chars
    blockSize = blockSize * 2;

    // better idea how to print this?
    List<String> result = new ArrayList<>();
    int index = 0;
    while (index < hex.length()) {
      result.add(
        hex.substring(index, Math.min(index + blockSize, hex.length()))
      );
      index += blockSize;
    }

    return result.toString();
  }
}
