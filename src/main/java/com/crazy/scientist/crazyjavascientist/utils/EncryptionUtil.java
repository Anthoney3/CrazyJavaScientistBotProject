//package com.crazy.scientist.crazyjavascientist.utils;
//
//import org.springframework.beans.factory.annotation.Value;
//
//import javax.crypto.BadPaddingException;
//import javax.crypto.Cipher;
//import javax.crypto.IllegalBlockSizeException;
//import javax.crypto.NoSuchPaddingException;
//import java.io.*;
//import java.nio.charset.StandardCharsets;
//import java.nio.file.Files;
//import java.security.*;
//import java.security.spec.*;
//import java.util.Arrays;
//import java.util.Base64;
//
//public class EncryptionUtil {
//
//    private static KeyFactory keyFactory;
//
//    static {
//        try {
//            keyFactory = KeyFactory.getInstance("RSA");
//        } catch (NoSuchAlgorithmException e) {
//            throw new RuntimeException(e);
//        }
//    }
//    private static KeySpec keySpec;
//    private static PrivateKey privateKey;
//
//    @Value("${twilio.private.key.location}")
//    private static String privateKeyLocation;
//
//    @Value("${twilio.auth.id}")
//    private static String authID;
//
//
//
//    public EncryptionUtil() throws NoSuchAlgorithmException {
//    }
//
//    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
//
//        FileInputStream fis = new FileInputStream(privateKeyLocation);
//        String sanitizedPrivateKey = Arrays.toString(fis.readAllBytes()).replaceAll("\\n","").replaceAll("-----BEGIN PUBLIC KEY-----","").replaceAll("-----END PUBLIC KEY-----","").trim();
//        keySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(sanitizedPrivateKey.getBytes(StandardCharsets.UTF_8)));
//        privateKey = keyFactory.generatePrivate(keySpec);
//
//        String auth_id = null;
//
//        try{
//            final Cipher cipher = Cipher.getInstance("RSA");
//            cipher.init(Cipher.DECRYPT_MODE,privateKey);
//
//            byte[] decoded = Base64.getDecoder().decode(authID);
//            byte[] decrypted = cipher.doFinal(decoded);
//            auth_id = new String(decrypted,StandardCharsets.UTF_8);
//
//            System.out.printf("The Auth Id Decrypted is : %s", auth_id);
//
//        } catch (NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
//            throw new RuntimeException(e);
//        }
//
//    }
//
//}
