package com.chamago.bison.util;

import java.io.PrintStream;

public class MathUtil
{
  public static final int ub(byte b)
  {
    return b < 0 ? 256 + b : b;
  }

  public static int toInt(String s)
  {
    return toInt(s, 10);
  }

  public static int toInt(String s, int radix)
  {
    try
    {
      return Integer.parseInt(s, radix); } catch (Exception e) {
    }
    return 0;
  }

  public static double toDouble(String s)
  {
    try
    {
      return Double.parseDouble(s); } catch (Exception e) {
    }
    return 0.0D;
  }

  public static float toFloat(String s)
  {
    try
    {
      return Float.parseFloat(s); } catch (Exception e) {
    }
    return 0.0F;
  }

  public static long toLong(String s)
  {
    try
    {
      return Long.parseLong(s); } catch (Exception e) {
    }
    return 0L;
  }

  public static short toShort(String s)
  {
    try
    {
      return Short.parseShort(s); } catch (Exception e) {
    }
    return 0;
  }

  public static boolean toBoolean(String s)
  {
    try
    {
      Boolean obj = new Boolean(s);
      return obj.booleanValue(); } catch (Exception e) {
    }
    return false;
  }

  public static byte toByte(String s)
  {
    try {
      return Byte.parseByte(s); } catch (Exception e) {
    }
    return 0;
  }

  public static int C(int m, int n)
  {
    if ((n < 0) || (m < 0) || (n < m)) {
      return 0;
    }

    if (m == n) {
      return 1;
    }

    int i = 0;
    long result = 1L;
    if (n < 2 * m) {
      m = n - m;
    }
    for (i = n; i >= n - m + 1; i--) {
      result *= i;
    }
    for (i = m; i >= 2; i--) {
      result /= i;
    }
    return (int)result;
  }

  public static void main(String[] args) {
    System.out.println(C(0, 11));
  }
}