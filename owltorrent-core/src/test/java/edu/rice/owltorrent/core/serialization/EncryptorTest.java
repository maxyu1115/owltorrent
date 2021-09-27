package edu.rice.owltorrent.core.serialization;

import static org.junit.Assert.*;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.junit.Test;

public class EncryptorTest {
  static String testString = "testString";
  static String expectedHexHash = "956265657D0B637EF65B9B59F9F858EECF55ED6A";

  @Test
  public void testSHA1EncryptString() {
    byte[] result = SHA1Encryptor.encrypt(testString);
    String actualHexHash = new BigInteger(1, result).toString(16);

    assertEquals(expectedHexHash.toLowerCase(), actualHexHash.toLowerCase());
  }

  @Test
  public void testSHA1EncryptByte() {
    byte[] testInput = testString.getBytes();

    byte[] result = SHA1Encryptor.encrypt(testInput);
    String actualHexHash = new BigInteger(1, result).toString(16);

    assertEquals(expectedHexHash.toLowerCase(), actualHexHash.toLowerCase());
  }

  @Test
  public void testIsEqualWithEqualStrings() {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-1");

      byte[] messageDigest = md.digest(testString.getBytes(StandardCharsets.US_ASCII));

      assertTrue(SHA1Encryptor.isSHA1HashEqual(testString, messageDigest));
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  public void testIsEqualWithNotEqualStrings() {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-1");

      byte[] messageDigest = md.digest(testString.getBytes(StandardCharsets.US_ASCII));

      assertFalse(SHA1Encryptor.isSHA1HashEqual(testString + "random", messageDigest));
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  public void testIsEqualWithEqualBytes() {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-1");
      byte[] testBytes = testString.getBytes(StandardCharsets.US_ASCII);
      byte[] messageDigest = md.digest(testBytes);

      assertTrue(SHA1Encryptor.isSHA1HashEqual(testBytes, messageDigest));
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  public void testIsEqualWithNotEqualBytes() {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-1");
      byte[] testBytes = testString.getBytes(StandardCharsets.US_ASCII);
      byte[] messageDigest = md.digest(testBytes);
      String diffString = testString + "random";

      assertFalse(SHA1Encryptor.isSHA1HashEqual(diffString.getBytes(), messageDigest));
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }
}
