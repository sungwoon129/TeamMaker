package io.wauction.core.common.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class EncryptService {
    public static String createSalt() throws NoSuchAlgorithmException {
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        byte [] bytes = new byte[16];
        random.nextBytes(bytes);


        String salt = new String(Base64.getEncoder().encode(bytes));


        return salt;
    }

    public static String encrypt(String plainText, String salt) {
        try {

            MessageDigest md = MessageDigest.getInstance("SHA-256");
            String rawAndSalt = plainText + salt;



            md.update(rawAndSalt.getBytes());


            byte[] byteData = md.digest();

            StringBuffer hexString = new StringBuffer();
            for(int i = 0; i < byteData.length; ++i) {
                String hex = Integer.toHexString(255 & byteData[i]);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();

        } catch (Exception ex) {
            ex.fillInStackTrace();
            throw new RuntimeException();
        }
    }
}
