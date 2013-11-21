package com.chamago.bison.util;

import java.io.PrintStream;
import java.util.Hashtable;

public class StringUtil
{
  public static boolean isEmpty(String str)
  {
    return (str == null) || ("".equals(str)) || (str.trim().length() == 0);
  }

  public static String getStringNoNull(String str)
  {
    if (str == null) {
      return "";
    }
    return str;
  }

  public static String getNullString(String strTemp)
  {
    return strTemp == null ? "" : strTemp.trim();
  }

  public static String getNullString(String strTemp, String defValue) {
    return strTemp == null ? defValue : strTemp.trim();
  }

  public static String getNullString(Object objTemp) {
    return objTemp == null ? "" : objTemp.toString().trim();
  }

  public static int getNullInt(String strTemp) {
    try {
      return getNullString(strTemp).length() == 0 ? 0 : Integer.parseInt(strTemp.trim()); } catch (Exception e) {
    }
    return 0;
  }

  public static int getNullInt(Object objTemp)
  {
    try {
      return objTemp == null ? 0 : Integer.parseInt(objTemp.toString().trim()); } catch (Exception e) {
    }
    return 0;
  }

  public static long getNullLong(String strTemp)
  {
    try {
      return getNullString(strTemp).length() == 0 ? 0L : Long.parseLong(strTemp.trim()); } catch (Exception e) {
    }
    return 0L;
  }

  public static long getNullLong(Object objTemp)
  {
    try {
      return objTemp == null ? 0L : Long.parseLong(objTemp.toString().trim()); } catch (Exception e) {
    }
    return 0L;
  }

  public static double getNullDouble(String strTemp)
  {
    try {
      return getNullString(strTemp).length() == 0 ? 0.0D : Double.parseDouble(strTemp.trim()); } catch (Exception e) {
    }
    return 0.0D;
  }

  public static double getNullDouble(Object objTemp)
  {
    try {
      return objTemp == null ? 0.0D : Double.parseDouble(objTemp.toString().trim()); } catch (Exception e) {
    }
    return 0.0D;
  }

  public static boolean getNullBoolean(String strTemp)
  {
    return (getNullString(strTemp).length() > 0) && ((strTemp.toString().equalsIgnoreCase("True")) || (strTemp.toString().equalsIgnoreCase("1")));
  }

  public static boolean getNullBoolean(Object objTemp)
  {
    return objTemp == null ? false : ((Boolean)objTemp).booleanValue();
  }

  public static String getValueFromHashtable(Hashtable<?, ?> h, String key) {
    String sRet = "";
    try {
      sRet = (String)h.get(key);
      if (sRet == null)
        sRet = "";
    }
    catch (Exception e) {
      sRet = "";
    }
    return sRet;
  }

  public static String replaceString(String strSource, String strFind, String strReplace)
  {
    String strTemp = strSource;
    StringBuffer sb = new StringBuffer();

    if ((strTemp != null) && (strFind != null) && (strReplace != null))
    {
      int pos;
      while ((pos = strTemp.indexOf(strFind)) != -1)
      {
       // int pos;
        sb.append(strTemp.substring(0, pos));
        sb.append(strReplace);
        strTemp = strTemp.substring(pos + strFind.length());
      }
      sb.append(strTemp);
      return new String(sb);
    }
    return strSource;
  }

  public static String replaceStringNoCase(String strSource, String strFind, String strReplace)
  {
    StringBuffer sb = new StringBuffer();

    if ((strSource != null) && (strFind != null) && (strReplace != null)) {
      String strTemp = strSource;
      String strTemp1 = strSource.toLowerCase();
      String strFind1 = strFind.toLowerCase();
      int pos;
      while ((pos = strTemp1.indexOf(strFind1)) != -1)
      {
        //int pos;
        sb.append(strTemp.substring(0, pos));
        sb.append(strReplace);
        strTemp1 = strTemp1.substring(pos + strFind1.length());
        strTemp = strTemp.substring(pos + strFind.length());
      }
      sb.append(strTemp);
      return new String(sb);
    }
    return strSource;
  }

  public static String replaceChar(String strSource, char chFind, String strReplace)
  {
    String strFind = String.valueOf(chFind);
    return replaceString(strSource, strFind, strReplace);
  }

  public static final String CreateUniqID(String prefix)
  {
    String s = String.valueOf(System.currentTimeMillis());
    return prefix + s;
  }

  public static String LeftPad(String s, String pad, int len) {
    int l = len - s.getBytes().length;
    String ss = s;
    for (int i = 0; i < l; i++) {
      ss = pad + ss;
    }
    return ss;
  }

  public static String RightPad(String s, String pad, int len) {
    int l = len - s.getBytes().length;
    String ss = s;
    for (int i = 0; i < l; i++) {
      ss = ss + pad;
    }
    return ss;
  }

  public static int[] SplitterInt(String code, String delim)
  {
    int size = CountStrNum(code, delim);
    return SplitterInt(code, delim, size);
  }

  public static int[] SplitterInt(String code, String delim, int length) {
    int pos = -1;
    int begin = 0;
    int[] s = new int[length];
    int count = 0;
    while ((pos = code.indexOf(delim, pos + 1)) != -1) {
      s[count] = Integer.parseInt(code.substring(begin, pos));
      begin = pos + 1;
      count++;
    }
    s[count] = Integer.parseInt(code.substring(begin, code.length()));
    count++;
    return s;
  }

  public static String[] splitter(String code, String delim)
  {
    int size = CountStrNum(code, delim);
    return splitter(code, delim, size);
  }

  public static String[] splitter(String code, String delim, int length) {
    int pos = -1;
    int begin = 0;
    String[] s = new String[length];
    int count = 0;
    while ((pos = code.indexOf(delim, pos + 1)) != -1) {
      s[count] = code.substring(begin, pos);
      begin = pos + 1;
      count++;
    }
    s[count] = code.substring(begin, code.length());
    count++;
    return s;
  }

  public static int CountStrNum(String source, String delim)
  {
    int pos = -1;
    int begin = 0;
    int count = 1;
    while ((pos = source.indexOf(delim, begin)) >= 0) {
      count++;
      begin = pos + 1;
    }
    return count;
  }

  public static void main(String[] args) {
    String s = " asdfk create dddd Create fff cReate ddd crEate ccc ";
    System.out.println(replaceStringNoCase(s, "create", "----------"));
  }
}