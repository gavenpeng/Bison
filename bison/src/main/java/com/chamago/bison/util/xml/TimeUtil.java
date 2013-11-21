package com.chamago.bison.util.xml;

import java.io.PrintStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;

public final class TimeUtil
{
  public static final SimpleDateFormat DateFormater = new SimpleDateFormat("yyyy-MM-dd");

  public static final SimpleDateFormat TimeFormater = new SimpleDateFormat("HH:mm:ss");

  public static final SimpleDateFormat DateTimeFormater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

  private static final Hashtable<String, SimpleDateFormat> CustomFormats = new Hashtable();

  private static final String[] DaysOfWeek = { "日", "一", "二", "三", "四", "五", "六" };

  private static final ThreadLocal<Long> threadTimes = new ThreadLocal();

  public static String currentDate()
  {
    return DateFormater.format(new Date());
  }

  public static String currentTime()
  {
    return TimeFormater.format(new Date());
  }

  public static String currentDateTime()
  {
    return DateTimeFormater.format(new Date());
  }

  public static String currentDateTime(String format)
  {
    try
    {
      synchronized (CustomFormats) {
        if (!CustomFormats.containsKey(format)) {
          CustomFormats.put(format, new SimpleDateFormat(format));
        }
      }

      SimpleDateFormat formater = (SimpleDateFormat)CustomFormats.get(format);

      return formater.format(new Date()); } catch (Exception ex) {
    	  throw new RuntimeException("时间格式(" + format + ")错误", ex);
    }
    
  }

  public static String weekOfDay()
  {
    return weekOfDay(Calendar.getInstance());
  }

  public static String weekOfDay(Calendar date) {
    return DaysOfWeek[(date.get(7) - 1)];
  }

  public static String customDateTime(Date date)
  {
    return DateTimeFormater.format(date);
  }

  public static String customDateTime(Date time, String format)
  {
    try
    {
      synchronized (CustomFormats) {
        if (!CustomFormats.containsKey(format)) {
          CustomFormats.put(format, new SimpleDateFormat(format));
        }
      }

      SimpleDateFormat formater = (SimpleDateFormat)CustomFormats.get(format);

      return formater.format(time); } catch (Exception ex) {
    	  throw new RuntimeException("日期时间格式(" + format + ")错误", ex);
    }
   
  }

  public static String customDateTime(Calendar time, String format)
  {
    try {
      synchronized (CustomFormats) {
        if (!CustomFormats.containsKey(format)) {
          CustomFormats.put(format, new SimpleDateFormat(format));
        }
      }

      SimpleDateFormat formater = (SimpleDateFormat)CustomFormats.get(format);

      return formater.format(time); } catch (Exception ex) {
    	  throw new RuntimeException("日期时间格式(" + format + ")错误", ex);
    }
   
  }

  public static Date parserDateTime(String datetime)
  {
    try
    {
      return DateTimeFormater.parse(datetime); } catch (ParseException ex) {
    	  throw new RuntimeException("解析时间错误", ex);
    }
   
  }

  public static Date parserDateTime(String datetime, String format)
  {
    try
    {
      synchronized (CustomFormats) {
        if (!CustomFormats.containsKey(format)) {
          CustomFormats.put(format, new SimpleDateFormat(format));
        }
      }

      SimpleDateFormat formater = (SimpleDateFormat)CustomFormats.get(format);

      return formater.parse(datetime); } catch (ParseException ex) {
    	  throw new RuntimeException("解析时间错误", ex);
    }
   
  }

  public static String convert(String time, String format)
  {
    return customDateTime(parserDateTime(time), format);
  }

  public static String convert(String time, String oformat, String nformat) {
    return customDateTime(parserDateTime(time, oformat), nformat);
  }

  public static long timeDiff(String time) {
    return parserDateTime(time).getTime() - new Date().getTime();
  }

  public static long timeDiff(String time, String format) {
    return parserDateTime(time, format).getTime() - new Date().getTime();
  }

  public static long timeDiff(String atime, String btime, String format) {
    return parserDateTime(atime, format).getTime() - parserDateTime(btime, format).getTime();
  }

  public static long timeDiff(String atime, String aformat, String btime, String bformat) {
    return parserDateTime(atime, aformat).getTime() - parserDateTime(btime, bformat).getTime();
  }

  static void beginTimer() {
    threadTimes.set(Long.valueOf(System.currentTimeMillis()));
  }

  public static long updateTimer() {
    Long current = (Long)threadTimes.get();

    threadTimes.set(Long.valueOf(System.currentTimeMillis()));

    if (current == null) {
      return -1L;
    }
    return System.currentTimeMillis() - current.longValue();
  }

  public static long fetchTimer()
  {
    return fetchTimer(true);
  }

  public static long fetchTimer(boolean remove) {
    Long current = (Long)threadTimes.get();

    if (current != null) {
      if (remove) {
        threadTimes.remove();
      }

      return System.currentTimeMillis() - current.longValue();
    }

    return -1L;
  }

  public static void main(String[] args) {
    System.out.println(System.currentTimeMillis());

    System.out.println(new Date().getTime());
  }
}