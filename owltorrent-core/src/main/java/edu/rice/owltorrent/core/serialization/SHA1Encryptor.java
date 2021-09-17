package edu.rice.owltorrent.core.serialization;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/** SHA1 Encryptor */
public class SHA1Encryptor {

  /**
   * Encrypt the input string using SHA1 algorithm
   *
   * @return the raw 20-byte SHA1 hash
   */
  public static byte[] encrypt(String input) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-1");

      return md.digest(input.getBytes());
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Check if the input string has the same SHA1 hash value as the expected one
   *
   * @param input The input string
   * @param expectedHash The expected correct hash
   * @return True if two hash values match, vice versa.
   */
  public static boolean isSHA1HashEqual(String input, byte[] expectedHash) {
    return Arrays.equals(encrypt(input), expectedHash);
  }
}
