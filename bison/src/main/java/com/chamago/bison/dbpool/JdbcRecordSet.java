package com.chamago.bison.dbpool;

import com.chamago.bison.util.DateUtil;
import com.chamago.bison.util.xml.JXmlUtil;

import java.io.ByteArrayOutputStream;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Serializable;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Vector;

public class JdbcRecordSet
  implements Serializable
{
  private static final long serialVersionUID = -7801233758248522450L;
  private ArrayList<String[]> pArray;
  private Vector<String> vFields;
  private int pos;

  public JdbcRecordSet(ResultSet rs)
    throws SQLException
  {
    this.vFields = new Vector();
    this.pArray = new ArrayList();
    this.pos = -1;
    cacheResultSet(rs);
  }

  public JdbcRecordSet(Vector<String> vFields) {
    this.pos = -1;
    this.vFields = vFields;
    this.pArray = new ArrayList();
  }

  private void cacheResultSet(ResultSet rs) throws SQLException {
    ResultSetMetaData mData = rs.getMetaData();
    int colCount = mData.getColumnCount();
    for (int j = 1; j < colCount + 1; j++) {
      this.vFields.add(mData.getColumnName(j).toLowerCase());
    }

    while (rs.next()) {
      String[] ss = new String[colCount];
      for (int i = 1; i < colCount + 1; i++) {
        int fieldType = mData.getColumnType(i);

        String fieldValue = null;
        switch (fieldType)
        {
        case -2:
          InputStream is = rs.getBinaryStream(i);
          ByteArrayOutputStream bos = new ByteArrayOutputStream();
          byte[] buffer = new byte[512];
          int len = 0;
          try {
            while ((len = is.read(buffer)) != -1) {
              bos.write(buffer, 0, len);
            }
            bos.flush();
            fieldValue = new String(bos.toByteArray());

            is.close();
            bos.close();
            is = null;
            bos = null;
          } catch (IOException localIOException) {
          }
          finally {
            if (is != null) {
              try {
                is.close();
              } catch (IOException localIOException3) {
              }
              is = null;
            }
            if (bos != null) {
              try {
                bos.close();
              } catch (IOException localIOException4) {
              }
              bos = null;
            }
            buffer = (byte[])null;
          }
          break;
        case -7:
          if (rs.getBoolean(i))
            fieldValue = "true";
          else {
            fieldValue = "false";
          }
          break;
        case 2004:
          Blob blob = rs.getBlob(i);
          if (blob == null) break;
          InputStream is2004 = blob.getBinaryStream();
          ByteArrayOutputStream bos2004 = new ByteArrayOutputStream();
          byte[] buffer2004 = new byte[512];
          len = 0;
          try {
            while ((len = is2004.read(buffer2004)) != -1) {
              bos2004.write(buffer2004, 0, len);
            }
            bos2004.flush();
            fieldValue = new String(bos2004.toByteArray());

            is2004.close();
            bos2004.close();
            is2004 = null;
            bos2004 = null;
          } catch (IOException localIOException14) {
          }
          finally {
            if (is2004 != null) {
              try {
                is2004.close();
              } catch (IOException localIOException10) {
              }
              is2004 = null;
            }
            if (bos2004 != null) {
              try {
                bos2004.close();
              } catch (IOException localIOException11) {
              }
              bos2004 = null;
            }
            buffer2004 = (byte[])null;
          }

          break;
        case 16:
          if (rs.getBoolean(i))
            fieldValue = "true";
          else {
            fieldValue = "false";
          }
          break;
        case 2005:
          Reader reader = rs.getCharacterStream(i);
          CharArrayWriter writer = new CharArrayWriter();
          try {
            char[] buffer2005 = new char[512];
            int len2005 = 0;
            while ((len2005 = reader.read(buffer2005)) != -1) {
              writer.write(buffer2005, 0, len2005);
            }
            writer.flush();
            fieldValue = new String(writer.toCharArray());
            reader.close();
            reader = null;
            writer.close();
            writer = null;
            buffer2005 = (char[])null;
          } catch (Exception localException) {
          }
          finally {
            if (reader != null) {
              try {
                reader.close();
              } catch (Exception localException9) {
              }
              reader = null;
            }

            if (writer != null) {
              try {
                writer.close();
              } catch (Exception localException10) {
              }
              writer = null;
            }
          }
          break;
        case 91:
          SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
          if (rs.getTimestamp(i) == null)
            fieldValue = "";
          else {
            fieldValue = formater.format(rs.getTimestamp(i));
          }

          break;
        case 93:
          SimpleDateFormat formater93 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
          if (rs.getTimestamp(i) == null)
            fieldValue = "";
          else {
            fieldValue = formater93.format(rs.getTimestamp(i));
          }
          break;
        default:
          fieldValue = rs.getString(i);
        }

        fieldValue = fieldValue == null ? "" : fieldValue;

        ss[(i - 1)] = fieldValue;
      }
      this.pArray.add(ss);
    }
  }

  public int size()
  {
    return this.pArray.size();
  }

  public String[] getRecord(int index)
  {
    if ((index >= 0) && (index < this.pArray.size())) {
      return (String[])this.pArray.get(index);
    }
    return null;
  }

  public String[] getCurrentRecord()
  {
    return (String[])this.pArray.get(this.pos);
  }

  public String get(String fieldName) {
    int idx = this.vFields.indexOf(fieldName.toLowerCase());
    if (idx < 0) {
      return "";
    }
    String s = getCurrentRecord()[idx];
    return s == null ? "" : s;
  }

  public int getInt(String fieldName) {
    return Integer.parseInt(get(fieldName));
  }

  public long getLong(String fieldName) {
    return Long.parseLong(get(fieldName));
  }

  public Date getDate(String fieldName) {
    return DateUtil.parserDateTime(get(fieldName));
  }

  public String get(String fieldName, int pos) {
    int idx = this.vFields.indexOf(fieldName.toLowerCase());
    if (idx < 0) {
      return "";
    }
    String s = ((String[])this.pArray.get(pos))[idx];
    return s == null ? "" : s;
  }

  public int getInt(String fieldName, int pos) {
    return Integer.parseInt(get(fieldName, pos));
  }

  public long getLong(String fieldName, int pos) {
    return Long.parseLong(get(fieldName, pos));
  }

  public Date getDate(String fieldName, int pos) {
    return DateUtil.parserDateTime(get(fieldName, pos));
  }

  public String[] getFileds() {
    String[] ss = new String[this.vFields.size()];
    for (int i = 0; i < this.vFields.size(); i++) {
      ss[i] = ((String)this.vFields.get(i));
    }
    return ss;
  }

  public boolean next()
  {
    this.pos += 1;

    return this.pos <= this.pArray.size() - 1;
  }

  public boolean previous()
  {
    this.pos -= 1;

    return this.pos >= 0;
  }

  public boolean first()
  {
    if (this.pArray.size() == 0) {
      return false;
    }
    this.pos = 0;
    return true;
  }

  public boolean last()
  {
    if (this.pArray.size() <= 0) {
      return false;
    }
    this.pos = (this.pArray.size() - 1);
    return true;
  }

  public void beforeFirst()
  {
    this.pos = -1;
  }

  public void afterLast()
  {
    this.pos = this.pArray.size();
  }

  public long getPostion() {
    return this.pos;
  }

  public boolean move(int pos) {
    if ((pos >= 0) && (pos <= size())) {
      this.pos = pos;
      return true;
    }
    return false;
  }

  public void addRecod(String[] ss)
  {
    this.pArray.add(ss);
  }

  public synchronized void clear() {
    this.vFields.clear();
    this.pArray.clear();
  }

  public String toXmlString() {
    int o_pos = this.pos;
    StringBuffer sb = new StringBuffer();
    sb.append("<xml ");
    sb.append(JXmlUtil.createAttrXml("rows", String.valueOf(this.pArray.size())));
    sb.append(JXmlUtil.createAttrXml("cols", String.valueOf(this.vFields.size())));
    sb.append(">\n");
    for (int i = 0; i < this.pArray.size(); i++) {
      sb.append("<row ");
      for (int j = 0; j < this.vFields.size(); j++) {
        sb.append(JXmlUtil.createAttrXml((String)this.vFields.get(j), ((String[])this.pArray.get(i))[j]));
      }
      sb.append("/>\n");
    }
    sb.append("</xml>");
    this.pos = o_pos;
    return new String(sb);
  }

  public String toRawXmlString(String tag) {
    int o_pos = this.pos;
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < this.pArray.size(); i++) {
      sb.append("<" + tag + " ");
      for (int j = 0; j < this.vFields.size(); j++) {
        sb.append(JXmlUtil.createAttrXml((String)this.vFields.get(j), ((String[])this.pArray.get(i))[j]));
      }
      sb.append("/>\n");
    }
    this.pos = o_pos;
    return new String(sb);
  }
}