package com.chamago.bison.util;

import java.io.PrintStream;

public class CommonUtil
{
  public static String convertMoneyToLarge(String s)
  {
    if (s.length() == 0) {
      return "";
    }
    if (s.trim().equalsIgnoreCase("0")) {
      return "零元整";
    }

    for (int i = s.length() - 1; i >= 0; i--) {
      s = StringUtil.replaceString(s, ",", "");
      s = StringUtil.replaceString(s, " ", "");
      s = StringUtil.replaceString(s, "￥", "");
    }

    String part0 = "";
    String part1 = "";

    int pos = s.indexOf(".");
    if (s.indexOf(".") != -1) {
      part0 = s.substring(0, pos);
      part1 = s.substring(pos + 1);
    } else {
      part0 = s;
      part1 = "";
    }

    String newchar = "";

    for (int i = part0.length() - 1; i >= 0; i--) {
      String tmpnewchar = "";
      char perchar = part0.charAt(i);
      switch (perchar) {
      case '0':
        tmpnewchar = "零" + tmpnewchar;
        break;
      case '1':
        tmpnewchar = "壹" + tmpnewchar;
        break;
      case '2':
        tmpnewchar = "贰" + tmpnewchar;
        break;
      case '3':
        tmpnewchar = "叁" + tmpnewchar;
        break;
      case '4':
        tmpnewchar = "肆" + tmpnewchar;
        break;
      case '5':
        tmpnewchar = "伍" + tmpnewchar;
        break;
      case '6':
        tmpnewchar = "陆" + tmpnewchar;
        break;
      case '7':
        tmpnewchar = "柒" + tmpnewchar;
        break;
      case '8':
        tmpnewchar = "捌" + tmpnewchar;
        break;
      case '9':
        tmpnewchar = "玖" + tmpnewchar;
      }

      switch (part0.length() - i - 1) {
      case 0:
        tmpnewchar = tmpnewchar + "元";
        break;
      case 1:
        if (perchar == '0') break;
        tmpnewchar = tmpnewchar + "拾";

        break;
      case 2:
        if (perchar == '0') break;
        tmpnewchar = tmpnewchar + "佰";

        break;
      case 3:
        if (perchar == '0') break;
        tmpnewchar = tmpnewchar + "仟";

        break;
      case 4:
        tmpnewchar = tmpnewchar + "万";
        break;
      case 5:
        if (perchar == '0') break;
        tmpnewchar = tmpnewchar + "拾";

        break;
      case 6:
        if (perchar == '0') break;
        tmpnewchar = tmpnewchar + "佰";

        break;
      case 7:
        if (perchar == '0') break;
        tmpnewchar = tmpnewchar + "仟";

        break;
      case 8:
        tmpnewchar = tmpnewchar + "亿";
        break;
      case 9:
        tmpnewchar = tmpnewchar + "拾";
      }

      newchar = tmpnewchar + newchar;
    }

    if (s.indexOf(".") != -1) {
      if (part1.length() > 2) {
        part1 = part1.substring(0, 2);
      }
      for (int i = 0; i < part1.length(); i++) {
        String tmpnewchar = "";
        char perchar = part1.charAt(i);
        switch (perchar) {
        case '0':
          tmpnewchar = "零" + tmpnewchar;
          break;
        case '1':
          tmpnewchar = "壹" + tmpnewchar;
          break;
        case '2':
          tmpnewchar = "贰" + tmpnewchar;
          break;
        case '3':
          tmpnewchar = "叁" + tmpnewchar;
          break;
        case '4':
          tmpnewchar = "肆" + tmpnewchar;
          break;
        case '5':
          tmpnewchar = "伍" + tmpnewchar;
          break;
        case '6':
          tmpnewchar = "陆" + tmpnewchar;
          break;
        case '7':
          tmpnewchar = "柒" + tmpnewchar;
          break;
        case '8':
          tmpnewchar = "捌" + tmpnewchar;
          break;
        case '9':
          tmpnewchar = "玖" + tmpnewchar;
        }

        if (i == 0) {
          tmpnewchar = tmpnewchar + "角";
        }
        if (i == 1) {
          tmpnewchar = tmpnewchar + "分";
        }
        newchar = newchar + tmpnewchar;
      }
    }

    while (newchar.indexOf("零零") != -1)
      newchar = StringUtil.replaceString(newchar, "零零", "零");
    while (newchar.indexOf("零亿") != -1)
      newchar = StringUtil.replaceString(newchar, "零亿", "亿");
    while (newchar.indexOf("亿万") != -1)
      newchar = StringUtil.replaceString(newchar, "亿万", "亿");
    while (newchar.indexOf("零万") != -1)
      newchar = StringUtil.replaceString(newchar, "零万", "万");
    while (newchar.indexOf("零元") != -1)
      newchar = StringUtil.replaceString(newchar, "零元", "元");
    while (newchar.indexOf("零角") != -1)
      newchar = StringUtil.replaceString(newchar, "零角", "");
    while (newchar.indexOf("零角") != -1) {
      newchar = StringUtil.replaceString(newchar, "零分", "");
    }

    if ((newchar.indexOf("元") == -1) || (newchar.indexOf("角") == -1)) {
      newchar = newchar + "整";
    }

    while (newchar.indexOf("亿万") != -1) {
      newchar = StringUtil.replaceString(newchar, "亿万", "亿");
    }
    return newchar;
  }

  public static String convertNumToLarge(String s)
  {
    if (s.length() == 0) {
      return "";
    }
    if (s.trim().equalsIgnoreCase("0")) {
      return "零份";
    }

    for (int i = s.length() - 1; i >= 0; i--) {
      s = StringUtil.replaceString(s, ",", "");
      s = StringUtil.replaceString(s, " ", "");
      s = StringUtil.replaceString(s, "￥", "");
    }

    String part0 = "";
    String part1 = "";

    int pos = s.indexOf(".");
    if (s.indexOf(".") != -1) {
      part0 = s.substring(0, pos);
      part1 = s.substring(pos + 1);
    } else {
      part0 = s;
      part1 = "";
    }

    String newchar = "";

    for (int i = part0.length() - 1; i >= 0; i--) {
      String tmpnewchar = "";
      char perchar = part0.charAt(i);
      switch (perchar) {
      case '0':
        tmpnewchar = "零" + tmpnewchar;
        break;
      case '1':
        tmpnewchar = "壹" + tmpnewchar;
        break;
      case '2':
        tmpnewchar = "贰" + tmpnewchar;
        break;
      case '3':
        tmpnewchar = "叁" + tmpnewchar;
        break;
      case '4':
        tmpnewchar = "肆" + tmpnewchar;
        break;
      case '5':
        tmpnewchar = "伍" + tmpnewchar;
        break;
      case '6':
        tmpnewchar = "陆" + tmpnewchar;
        break;
      case '7':
        tmpnewchar = "柒" + tmpnewchar;
        break;
      case '8':
        tmpnewchar = "捌" + tmpnewchar;
        break;
      case '9':
        tmpnewchar = "玖" + tmpnewchar;
      }

      switch (part0.length() - i - 1)
      {
      case 1:
        if (perchar == '0') break;
        tmpnewchar = tmpnewchar + "拾";

        break;
      case 2:
        if (perchar == '0') break;
        tmpnewchar = tmpnewchar + "佰";

        break;
      case 3:
        if (perchar == '0') break;
        tmpnewchar = tmpnewchar + "仟";

        break;
      case 4:
        tmpnewchar = tmpnewchar + "万";
        break;
      case 5:
        if (perchar == '0') break;
        tmpnewchar = tmpnewchar + "拾";

        break;
      case 6:
        if (perchar == '0') break;
        tmpnewchar = tmpnewchar + "佰";

        break;
      case 7:
        if (perchar == '0') break;
        tmpnewchar = tmpnewchar + "仟";

        break;
      case 8:
        tmpnewchar = tmpnewchar + "亿";
        break;
      case 9:
        tmpnewchar = tmpnewchar + "拾";
      }

      newchar = tmpnewchar + newchar;
    }

    if (s.indexOf(".") != -1) {
      if (part1.length() > 2) {
        part1 = part1.substring(0, 2);
      }
      newchar = newchar + "点";
      for (int i = 0; i < part1.length(); i++) {
        String tmpnewchar = "";
        char perchar = part1.charAt(i);
        switch (perchar) {
        case '0':
          tmpnewchar = "零" + tmpnewchar;
          break;
        case '1':
          tmpnewchar = "壹" + tmpnewchar;
          break;
        case '2':
          tmpnewchar = "贰" + tmpnewchar;
          break;
        case '3':
          tmpnewchar = "叁" + tmpnewchar;
          break;
        case '4':
          tmpnewchar = "肆" + tmpnewchar;
          break;
        case '5':
          tmpnewchar = "伍" + tmpnewchar;
          break;
        case '6':
          tmpnewchar = "陆" + tmpnewchar;
          break;
        case '7':
          tmpnewchar = "柒" + tmpnewchar;
          break;
        case '8':
          tmpnewchar = "捌" + tmpnewchar;
          break;
        case '9':
          tmpnewchar = "玖" + tmpnewchar;
        }

        newchar = newchar + tmpnewchar;
      }
    }

    while (newchar.indexOf("零零") != -1)
      newchar = StringUtil.replaceString(newchar, "零零", "零");
    while (newchar.indexOf("零亿") != -1)
      newchar = StringUtil.replaceString(newchar, "零亿", "亿");
    while (newchar.indexOf("亿万") != -1)
      newchar = StringUtil.replaceString(newchar, "亿万", "亿");
    while (newchar.indexOf("零万") != -1)
      newchar = StringUtil.replaceString(newchar, "零万", "万");
    while (newchar.indexOf("零份") != -1)
      newchar = StringUtil.replaceString(newchar, "零份", "份");
    while (newchar.indexOf("零角") != -1)
      newchar = StringUtil.replaceString(newchar, "零角", "");
    while (newchar.indexOf("零角") != -1) {
      newchar = StringUtil.replaceString(newchar, "零分", "");
    }

    while (newchar.indexOf("亿万") != -1) {
      newchar = StringUtil.replaceString(newchar, "亿万", "亿");
    }
    return newchar;
  }

  public static String delimMoney(String myStr)
  {
    if (myStr.length() == 0) {
      return "";
    }

    int pos = myStr.indexOf(".");
    String str = "";
    String str1 = "";

    if (pos == -1) {
      str = myStr;
      str1 = "";
    } else {
      str = myStr.substring(0, pos);
      str1 = myStr.substring(pos);
    }
    for (int i = str.length() - 1; i >= 0; i--) {
      str = StringUtil.replaceString(str, ",", "");
      str = StringUtil.replaceString(str, " ", "");
    }
    int len = str.length();
    if (len == 0) {
      return "";
    }
    String s = "";
    for (int i = 0; i < len; i++) {
      String ch = str.substring(len - i - 1, len - i);
      s = ch + s;
      if (((i + 1) % 3 != 0) || 
        (i == len - 1)) continue;
      s = "," + s;
    }

    return s + str1;
  }

  public static void main(String[] argv) {
    System.out.println(convertMoneyToLarge("10334554000870.89"));
    System.out.println(delimMoney("10334554000870.89"));
  }
}