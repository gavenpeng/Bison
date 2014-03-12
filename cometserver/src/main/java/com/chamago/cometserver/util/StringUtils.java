package com.chamago.cometserver.util;

/**
 * 字符串工具类。
 * 
 * @author carver.gu
 * @since 1.0, Sep 12, 2009
 */
public abstract class StringUtils {

	private StringUtils() {}

	/**
	 * 检查指定的字符串是否为空。
	 * <ul>
	 * <li>SysUtils.isEmpty(null) = true</li>
	 * <li>SysUtils.isEmpty("") = true</li>
	 * <li>SysUtils.isEmpty("   ") = true</li>
	 * <li>SysUtils.isEmpty("abc") = false</li>
	 * </ul>
	 * 
	 * @param value 待检查的字符串
	 * @return true/false
	 */
	public static boolean isEmpty(String value) {
		int strLen;
		if (value == null || (strLen = value.length()) == 0) {
			return true;
		}
		for (int i = 0; i < strLen; i++) {
			if ((Character.isWhitespace(value.charAt(i)) == false)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 检查对象是否为数字型字符串,包含负数开头的。
	 */
	public static boolean isNumeric(Object obj) {
		if (obj == null) {
			return false;
		}
		char[] chars = obj.toString().toCharArray();
		int length = chars.length;
		if(length < 1)
			return false;
		
		int i = 0;
		if(length > 1 && chars[0] == '-')
			i = 1;
		
		for (; i < length; i++) {
			if (!Character.isDigit(chars[i])) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 检查指定的字符串列表是否不为空。
	 */
	public static boolean areNotEmpty(String... values) {
		boolean result = true;
		if (values == null || values.length == 0) {
			result = false;
		} else {
			for (String value : values) {
				result &= !isEmpty(value);
			}
		}
		return result;
	}

	/**
	 * 把通用字符编码的字符串转化为汉字编码。
	 */
	public static String unicodeToChinese(String unicode) {
		StringBuilder out = new StringBuilder();
		if (!isEmpty(unicode)) {
			for (int i = 0; i < unicode.length(); i++) {
				out.append(unicode.charAt(i));
			}
		}
		return out.toString();
	}

	public static String toUnderlineStyle(String name) {
		StringBuilder newName = new StringBuilder();
		for (int i = 0; i < name.length(); i++) {
			char c = name.charAt(i);
			if (Character.isUpperCase(c)) {
				if (i > 0) {
					newName.append("_");
				}
				newName.append(Character.toLowerCase(c));
			} else {
				newName.append(c);
			}
		}
		return newName.toString();
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
}
