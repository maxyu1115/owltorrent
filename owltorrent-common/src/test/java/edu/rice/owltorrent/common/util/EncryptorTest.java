package edu.rice.owltorrent.common.util;

import static org.junit.Assert.*;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.junit.Test;

public class EncryptorTest {
  static String testString = "testString";
  static String expectedHexHash = "956265657D0B637EF65B9B59F9F858EECF55ED6A";
  static String expectedHexHash2 = "2250c974e979ed47b06e821fcaad27de497a87de";

  @Test
  public void testSHA1EncryptString() {
    byte[] result = SHA1Encryptor.encrypt(testString);
    String actualHexHash = new BigInteger(1, result).toString(16);

    assertEquals(expectedHexHash.toLowerCase(), actualHexHash.toLowerCase());
  }

  @Test
  public void testSHA1EncryptByte() {
    byte[] testInput = testString.getBytes();

    byte[] head = new String("d6:pieces1:").getBytes();
    byte[] bytes = new byte[] {(byte) 0xec};
    byte[] c = new byte[head.length + bytes.length + 1];
    System.arraycopy(head, 0, c, 0, head.length);
    System.arraycopy(bytes, 0, c, head.length, bytes.length);
    System.arraycopy(new String("e").getBytes(), 0, c, head.length + bytes.length, 1);
    byte[] result = SHA1Encryptor.encrypt(c);
    String actualHexHash = new BigInteger(1, result).toString(16);
    assertEquals(expectedHexHash2.toLowerCase(), actualHexHash.toLowerCase());
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
