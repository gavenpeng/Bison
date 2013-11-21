package com.chamago.bison.util;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.security.MessageDigest;

public class MD5Util
{
  public static char[] hexChar = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

  public static String compute(String str)
    throws Exception
  {
    MessageDigest md5 = MessageDigest.getInstance("MD5");
    char[] charArray = str.toCharArray();
    byte[] byteArray = new byte[charArray.length];

    for (int i = 0; i < charArray.length; i++) {
      byteArray[i] = (byte)charArray[i];
    }
    byte[] md5Bytes = md5.digest(byteArray);

    StringBuffer hexValue = new StringBuffer();

    for (int i = 0; i < md5Bytes.length; i++) {
      int val = md5Bytes[i] & 0xFF;
      if (val < 16)
        hexValue.append("0");
      hexValue.append(Integer.toHexString(val));
    }

    return hexValue.toString();
  }

  public static String getHash(String fileName) throws Exception
  {
    int count = 0;
    InputStream fis = new FileInputStream(fileName);
    byte[] buffer = new byte[1024];
    MessageDigest md5 = MessageDigest.getInstance("MD5");
    int numRead = 0;
    while ((numRead = fis.read(buffer)) > 0) {
      md5.update(buffer, 0, numRead);
      count++;
    }

    fis.close();
    return toHexString(md5.digest());
  }

  public static String toHexString(byte[] b) {
    StringBuffer sb = new StringBuffer(b.length * 2);

    for (int i = 0; i < b.length; i++) {
      sb.append(hexChar[((b[i] & 0xF0) >>> 4)]);
      sb.append(hexChar[(b[i] & 0xF)]);
    }
    return sb.toString();
  }

  public static void main(String[] args) throws Exception {
    System.out.println(compute("1234567890").toUpperCase());
    System.out.println("conf.dir=" + System.getenv("conf.dir"));
  }
}