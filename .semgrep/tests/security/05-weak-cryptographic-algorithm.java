public class CryptoTest {
    public void test() throws Exception {
        // ruleid: 05-weak-cryptographic-algorithm
        MessageDigest md5 = MessageDigest.getInstance("MD5");

        // ruleid: 05-weak-cryptographic-algorithm
        MessageDigest sha1 = MessageDigest.getInstance("SHA1");

        // ruleid: 05-weak-cryptographic-algorithm
        Cipher des = Cipher.getInstance("DES");

        // ruleid: 05-weak-cryptographic-algorithm
        Cipher rc4 = Cipher.getInstance("RC4");
    }
}

public class CryptoNegativeTest {
    public void test() throws Exception {
        // ok: 05-weak-cryptographic-algorithm
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
    }
}
