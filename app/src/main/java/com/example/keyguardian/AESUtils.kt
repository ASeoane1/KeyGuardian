import android.util.Base64
import java.security.SecureRandom
import java.security.spec.KeySpec
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object AESUtils {

    private const val AES_MODE = "AES/CBC/PKCS5Padding"
    private const val KEY_SIZE = 256
    private const val ITERATION_COUNT = 65536
    private const val SALT = "your_fixed_salt_here"

    fun generateKeyFromPassword(password: String): SecretKey {
        val saltBytes = SALT.toByteArray()
        val spec: KeySpec = PBEKeySpec(password.toCharArray(), saltBytes, ITERATION_COUNT, KEY_SIZE)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val keyBytes = factory.generateSecret(spec).encoded
        return SecretKeySpec(keyBytes, "AES")
    }

    fun encrypt(data: String, password: String): String {
        val secretKey = generateKeyFromPassword(password)
        val cipher = Cipher.getInstance(AES_MODE)

        val iv = ByteArray(cipher.blockSize)
        SecureRandom().nextBytes(iv)
        val ivSpec = IvParameterSpec(iv)

        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec)
        val encryptedData = cipher.doFinal(data.toByteArray())

        val encryptedWithIv = iv + encryptedData
        return Base64.encodeToString(encryptedWithIv, Base64.DEFAULT)
    }

    fun decrypt(encryptedData: String, password: String): String {
        val secretKey = generateKeyFromPassword(password)
        val cipher = Cipher.getInstance(AES_MODE)

        val encryptedBytes = Base64.decode(encryptedData, Base64.DEFAULT)

        val iv = encryptedBytes.copyOfRange(0, cipher.blockSize)
        val actualEncryptedData = encryptedBytes.copyOfRange(cipher.blockSize, encryptedBytes.size)

        val ivSpec = IvParameterSpec(iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec)
        val decryptedData = cipher.doFinal(actualEncryptedData)

        return String(decryptedData)
    }
}
