package com.chamago.bison.util;

import java.lang.reflect.Method;

/**
 * 
 * @author Gavin.peng
 * 
 * 2013-10-16 下午04:21:41
 × bison-client
 */
public final class ByteUtil
{
  public static final int DATA_LENGTH08 = 8;
  public static final int DATA_LENGTH04 = 4;
  public static final int DATA_LENGTH02 = 2;
  public static final int DATA_LENGTH01 = 1;
  public static byte[] ZEROS = new byte[16384];

  public static int read(byte[] data, int off) {
    return data[off] & 0xFF;
  }

  public static int write(byte[] data, int off, int val) {
    data[off] = (byte)(val & 0xFF);
    return 1;
  }

  public static int readInt(byte[] data, int off) {
    int ch1 = (data[(off + 0)] & 0xFF) << 24;
    int ch2 = (data[(off + 1)] & 0xFF) << 16;
    int ch3 = (data[(off + 2)] & 0xFF) << 8;
    int ch4 = (data[(off + 3)] & 0xFF) << 0;
    return ch1 + ch2 + ch3 + ch4;
  }

  public static int writeInt(byte[] data, int off, int val) {
    data[(off + 0)] = (byte)(val >>> 24);
    data[(off + 1)] = (byte)(val >>> 16);
    data[(off + 2)] = (byte)(val >>> 8);
    data[(off + 3)] = (byte)(val >>> 0);
    return 4;
  }

  public static int readInt(byte[] data, int off, int len) {
    int value = 0;
    for (int idx = 0; idx < len; idx++) {
      value += ((data[(off + idx)] & 0xFF) << 8 * (len - idx - 1));
    }
    return value;
  }

  public static int writeInt(byte[] data, int off, int val, int len) {
    for (int idx = 0; idx < len; idx++) {
      data[(off + idx)] = (byte)(val >>> 8 * (len - idx - 1));
    }
    return len;
  }

  public static int readShort(byte[] data, int off) {
    int ch1 = (data[(off + 0)] & 0xFF) << 8;
    int ch2 = (data[(off + 1)] & 0xFF) << 0;
    return ch1 + ch2;
  }

  public static int writeShort(byte[] data, int off, int val) {
    data[(off + 0)] = (byte)(val >>> 8);
    data[(off + 1)] = (byte)(val >>> 0);
    return 2;
  }

  public static boolean readBool(byte[] data, int off) {
    return data[off] == 84;
  }

  public static int writeBool(byte[] data, int off, boolean val) {
    data[off] = (byte)(val ? 84 : 70);
    return 1;
  }

  public static int readBits(byte[] data, int off, int start, int end) {
    return (0xFF & data[off] << 8 - end) >>> 8 - end + start;
  }

  public static int writeBits(byte[] data, int off, int start, int end, int bits) {
    byte ptmp = (byte)((0xFF & 255 << 8 - end) >>> start + 8 - end << start ^ 0xFFFFFFFF);
    byte ntmp = (byte)((0xFF & bits << 8 - (end - start)) >>> 8 - (end - start) << start);
    data[start] = (byte)(data[start] & ptmp | ntmp);
    return 0;
  }

  public static long readLong(byte[] data, int off) {
    long ch1 = data[(off + 0)] << 56;
    long ch2 = (data[(off + 1)] & 0xFF) << 48;
    long ch3 = (data[(off + 2)] & 0xFF) << 40;
    long ch4 = (data[(off + 3)] & 0xFF) << 32;
    long ch5 = (data[(off + 4)] & 0xFF) << 24;
    int ch6 = (data[(off + 5)] & 0xFF) << 16;
    int ch7 = (data[(off + 6)] & 0xFF) << 8;
    int ch8 = (data[(off + 7)] & 0xFF) << 0;
    return ch1 + ch2 + ch3 + ch4 + ch5 + ch6 + ch7 + ch8;
  }

  public static int writeLong(byte[] data, int off, long val) {
    data[(off + 0)] = (byte)(int)(val >>> 56);
    data[(off + 1)] = (byte)(int)(val >>> 48);
    data[(off + 2)] = (byte)(int)(val >>> 40);
    data[(off + 3)] = (byte)(int)(val >>> 32);
    data[(off + 4)] = (byte)(int)(val >>> 24);
    data[(off + 5)] = (byte)(int)(val >>> 16);
    data[(off + 6)] = (byte)(int)(val >>> 8);
    data[(off + 7)] = (byte)(int)(val >>> 0);
    return 8;
  }

  public static double readDouble(byte[] data, int off) {
    return Double.longBitsToDouble(readLong(data, off));
  }

  public static int writeDouble(byte[] data, int off, double val) {
    return writeLong(data, off, Double.doubleToLongBits(val));
  }

  public static String readString(byte[] data, int off) {
    int realen = zeroRange(data, off);
    return new String(data, off, realen);
  }

  public static int writeString(byte[] data, int off, String str) {
    byte[] strbits = str.getBytes();
    int realen = Math.min(strbits.length, data.length - off);
    data[(off + realen)] = 0;
    System.arraycopy(strbits, 0, data, off, realen);
    return realen + 1;
  }

  public static String readString(byte[] data, int off, int len) {
    int realen = Math.min(len, zeroRange(data, off));
    return new String(data, off, realen);
  }

  public static int writeString(byte[] data, int off, String str, int len) {
    byte[] strbits = str.getBytes();
    int realen = Math.min(strbits.length, len);
    realen = Math.min(realen, data.length - off);
    System.arraycopy(strbits, 0, data, off, realen);
    return realen;
  }

  private static int zeroRange(byte[] data, int off)
  {
    for (int idx = off; idx < data.length; idx++) {
      if (data[idx] == 0) {
        return idx - off;
      }
    }
    return data.length - off;
  }

  public static void CopyBeanToBean(Object src, Object dest) throws Exception
  {
    Method[] method1 = src.getClass().getMethods();
    Method[] method2 = dest.getClass().getMethods();

    for (int i = 0; i < method1.length; i++) {
      String methodName1 = method1[i].getName();
      String methodFix1 = methodName1.substring(3, methodName1.length());

      if (methodName1.startsWith("get"))
        for (int j = 0; j < method2.length; j++) {
          String methodName2 = method2[j].getName();
          String methodFix2 = methodName2.substring(3, methodName2.length());
          if ((!methodName2.startsWith("set")) || 
            (!methodFix2.equals(methodFix1))) continue;
          Object[] objs1 = new Object[0];
          Object[] objs2 = new Object[1];
          objs2[0] = method1[i].invoke(src, objs1);
          method2[j].invoke(dest, objs2);
          objs1 = (Object[])null;
          objs2 = (Object[])null;
          break;
        }
    }
  }
}