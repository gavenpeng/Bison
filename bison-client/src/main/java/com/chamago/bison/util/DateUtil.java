package com.chamago.bison.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtil
{
  public static String getCurrentDate()
  {
    try
    {
      SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd");
      return formater.format(new Date());
    } catch (Exception e) {
    }
    return "";
  }

  public static String getCurrentFormatDate(String sFmt)
  {
    try
    {
      SimpleDateFormat formater = new SimpleDateFormat(sFmt);
      return formater.format(new Date());
    } catch (Exception e) {
    }
    return "";
  }

  public static String getCurrentTime1(int timeType, int num)
  {
    try
    {
      SimpleDateFormat formater = new SimpleDateFormat("HH:mm:ss");
      Calendar cal = Calendar.getInstance();
      cal.add(timeType, num);

      return formater.format(cal.getTime());
    } catch (Exception e) {
    }
    return "";
  }

  public static String getCurrentDate(int num)
  {
    try
    {
      SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd");
      Calendar cal = Calendar.getInstance();
      cal.add(5, num);

      return formater.format(cal.getTime());
    } catch (Exception e) {
    }
    return "";
  }

  public static String getDateTime(int dateField, int num)
  {
    try
    {
      SimpleDateFormat formater = new SimpleDateFormat(
        "yyyy-MM-dd HH:mm:ss");
      Calendar cal = Calendar.getInstance();
      cal.add(dateField, num);

      return formater.format(cal.getTime());
    } catch (Exception e) {
    }
    return "";
  }

  public static String getCurrentDate(String num)
  {
    try
    {
      SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd");
      Calendar cal = Calendar.getInstance();
      cal.add(5, Integer.parseInt(num));

      return formater.format(cal);
    } catch (Exception e) {
    }
    return "";
  }

  public static String getCurrentTime()
  {
    try
    {
      SimpleDateFormat formater = new SimpleDateFormat("HH:mm:ss");
      return formater.format(new Date());
    } catch (Exception e) {
    }
    return "";
  }

  public static String getCurrentDateTime()
  {
    try
    {
      SimpleDateFormat formater = new SimpleDateFormat(
        "yyyy-MM-dd HH:mm:ss");
      return formater.format(new Date());
    } catch (Exception e) {
    }
    return "";
  }

  public static String ConvertToTime(String s)
  {
    String strReturn = "";
    try {
      if (s.length() == 5) {
        strReturn = "0" + s.substring(0, 1) + ":" + s.substring(1, 3) + 
          ":" + s.substring(3);
      }
      else
        strReturn = s.substring(0, 2) + ":" + s.substring(2, 4) + ":" + 
          s.substring(4);
    }
    catch (Exception e)
    {
      strReturn = "";
    }
    return strReturn;
  }

  public static String ConvertToTime1(String s)
  {
    String strReturn = "";
    try {
      if (s.length() == 5) {
        strReturn = "0" + s.substring(0, 1) + "时" + s.substring(1, 3) + 
          "分" + s.substring(3) + "秒";
      }
      else
        strReturn = s.substring(0, 2) + "时" + s.substring(2, 4) + "分" + 
          s.substring(4, 6) + "秒";
    }
    catch (Exception e)
    {
      strReturn = "";
    }
    return strReturn;
  }

  public static String ConvertToDate(String s)
  {
    String strReturn = "";
    try {
      if (s.length() == 6) {
        s = "20" + s;
      }
      if (s.length() == 8)
        strReturn = s.substring(0, 4) + "-" + s.substring(4, 6) + "-" + 
          s.substring(6);
    }
    catch (Exception e)
    {
      strReturn = "";
    }
    return strReturn;
  }

  public static String ConvertToDate1(String s)
  {
    String strReturn = "";
    try {
      if (s.length() == 6) {
        s = "20" + s;
      }
      if (s.length() == 8)
        strReturn = s.substring(0, 4) + "年" + s.substring(4, 6) + "月" + 
          s.substring(6) + "日";
    }
    catch (Exception e)
    {
      strReturn = "";
    }
    return strReturn;
  }

  public static String getCurrentDate1()
  {
    try
    {
      SimpleDateFormat formater = new SimpleDateFormat("yyyy年MM月dd日");
      return formater.format(new Date());
    } catch (Exception e) {
    }
    return "";
  }

  public static Date parserDate(String sDate)
  {
    Date d = null;
    try {
      SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
      d = fmt.parse(sDate);
    }
    catch (Exception e) {
      d = null;
    }
    return d;
  }

  public static Date parserDateTime(String sDate)
  {
    Date d = null;
    try {
      SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      d = fmt.parse(sDate);
    }
    catch (Exception e) {
      e.printStackTrace();
      d = null;
    }
    return d;
  }

  public static String getDateTime(long lTime) {
    try {
      SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      return formater.format(new Date(lTime));
    } catch (Exception e) {
    }
    return "";
  }

  public static String getDateTime(long lTime, String fmt)
  {
    try {
      SimpleDateFormat formater = new SimpleDateFormat(fmt);
      return formater.format(new Date(lTime));
    } catch (Exception e) {
    }
    return "";
  }
}