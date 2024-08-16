package net.datto.dciservice.utils;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

@Configuration
@ConfigurationProperties(prefix = "aes.codec")
public class AESCodec {
    private static final Logger logger = LoggerFactory.getLogger(AESCodec.class);
    private static SecretKey secret;
    private static final String salt = "1AJS0E(p&()!*R*)";

    private String password;

    public void setPassword(String password) {
        this.password = password;
    }

    @PostConstruct
    public void aesCodec() {
        KeySpec keySpec = null;
        try {
            keySpec = new PBEKeySpec(password.toCharArray(), salt.getBytes("UTF-8"), 1024, 128);
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            secret = new SecretKeySpec(keyFactory.generateSecret(keySpec).getEncoded(), "AES");
        } catch (UnsupportedEncodingException | InvalidKeySpecException | NoSuchAlgorithmException e) {
            logger.error("Initialization failed. Exception: ", e);
        }
    }

    public String encode(String str) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

            cipher.init(Cipher.ENCRYPT_MODE, secret);
            AlgorithmParameters params = cipher.getParameters();

            byte[] ivBytes = params.getParameterSpec(IvParameterSpec.class).getIV();
            byte[] textBytes = cipher.doFinal(str.getBytes("UTF-8"));
            String iv = Base64.getEncoder().encodeToString(ivBytes);
            String text = Base64.getEncoder().encodeToString(textBytes);
            return iv + ";" + text;
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidParameterSpecException
                | IllegalBlockSizeException | BadPaddingException | UnsupportedEncodingException e) {
            logger.error("Encode failed.");
        }
        return "";
    }

    public String decode(String str) {
        String firstHalf = str.substring(0, str.indexOf(';'));
        String secondHald = str.substring(str.indexOf(';') + 1, str.length());
        byte[] iv = Base64.getDecoder().decode(firstHalf);
        byte[] text = Base64.getDecoder().decode(secondHald);
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(iv));
            return new String(cipher.doFinal(text), "UTF-8");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException
                | InvalidAlgorithmParameterException | UnsupportedEncodingException | IllegalBlockSizeException
                | BadPaddingException e) {
            logger.error("Decode failed.");
        }
        return "";
    }
}
