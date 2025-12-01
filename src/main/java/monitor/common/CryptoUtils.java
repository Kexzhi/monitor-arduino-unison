package monitor.common;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Utilidad sencilla para cifrar/descifrar mensajes usando AES.
 * Ambos programas (Cliente y Servidor) comparten la misma clave.
 */
public class CryptoUtils {

    // EXACTAMENTE 16 caracteres (128 bits) → si no, truena
    private static final String SECRET_KEY = "UNISON-MONITOR-1"; // 16 chars
    private static final String ALGORITHM = "AES";

    private static SecretKeySpec getKey() {
        return new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), ALGORITHM);
    }

    public static String encrypt(String plainText) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, getKey());
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("Error cifrando mensaje", e);
        }
    }

    public static String decrypt(String encryptedBase64) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, getKey());
            byte[] decoded = Base64.getDecoder().decode(encryptedBase64);
            byte[] original = cipher.doFinal(decoded);
            return new String(original, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Error descifrando mensaje", e);
        }
    }
}
