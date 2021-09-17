package edu.rice.owltorrent.core;

import static org.junit.Assert.*;

import edu.rice.owltorrent.core.serialization.SHA1Encryptor;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.junit.Test;

public class EncryptorTest {
  @Test
  public void testSHA1Encrypt() {
    String testInput = "testString";
    String expectedHexHash = "956265657D0B637EF65B9B59F9F858EECF55ED6A";

    byte[] result = SHA1Encryptor.encrypt(testInput);
    String actualHexHash = new BigInteger(1, result).toString(16);

    assertEquals(expectedHexHash.toLowerCase(), actualHexHash.toLowerCase());
  }

  @Test
  public void testIsEqualWithEqualStrings() {
    String testString = "testString";
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-1");

      byte[] messageDigest = md.digest(testString.getBytes());

      assertTrue(SHA1Encryptor.isSHA1HashEqual(testString, messageDigest));
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  public void testIsEqualWithNotEqualStrings() {
    String testString = "testString";
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-1");

      byte[] messageDigest = md.digest(testString.getBytes());

      assertFalse(SHA1Encryptor.isSHA1HashEqual(testString + "random", messageDigest));
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }
}
