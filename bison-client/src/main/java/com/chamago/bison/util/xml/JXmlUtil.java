package com.chamago.bison.util.xml;

import com.chamago.bison.util.StringUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.xml.sax.InputSource;

public class JXmlUtil
{
  private static Format _oneLineformat;
  private static Format _multiLineformat;

  public static Document newDocument()
  {
    return new Document();
  }

  public static final Element newDocument(String name) {
    try {
      Document doc = new Document();

      Element root = new Element(name);

      doc.addContent(root);

      return root; } catch (Exception ex) {
    }
    return null;
  }

  public static final Element newElement(Element parent, String name)
  {
    Element element = new Element(name);

    parent.addContent(element);

    return element;
  }

  public static final Element newElement(Element parent, String name, Hashtable<?, ?> attrs) {
    Element element = new Element(name);

    parent.addContent(element);

    if (attrs != null) {
      for (Iterator localIterator = attrs.keySet().iterator(); localIterator.hasNext(); ) { Object key = localIterator.next();
        String attrname = key.toString();
        String attrvalue = attrs.get(key).toString();

        element.setAttribute(attrname, attrvalue);
      }
    }

    return element;
  }

  public static final Element child(Element parent, String nameid)
  {
    List nodes = parent.getChildren(nameid);

    if ((nodes != null) && (nodes.size() == 1)) {
      return (Element)nodes.get(0);
    }

    throw new RuntimeException("指定的Xml子节点没有找到");
  }

  public static final List<Element> children(Element parent, String nameid)
  {
    return parent.getChildren(nameid);
  }

  public static String xml2String(Element root, String charset) {
    return xml2String(root, getOnelineXmlFormat(charset));
  }

  public static String xml2String(Document doc, String charset) {
    return xml2String(doc, getOnelineXmlFormat(charset));
  }

  public static String xml2String(Element root, Format format) {
    Element tmp = (Element)root.clone();

    tmp.detach();

    Document doc = new Document(tmp);

    return xml2String(doc, format);
  }

  public static String xml2String(Document doc, Format format) {
    try {
      XMLOutputter out = new XMLOutputter();

      out.setFormat(format);

      ByteArrayOutputStream os = new ByteArrayOutputStream();

      out.output(doc, os);

      return os.toString(); } catch (Throwable ex) {
    	  throw new RuntimeException("Xml文档转换--->异常", ex);
    }
    
  }

  public static Element string2Xml(String xml)
  {
    try {
      SAXBuilder builder = new SAXBuilder();

      InputSource is = new InputSource(new StringReader(xml));

      Document doc = builder.build(is);

      return doc.getRootElement(); } catch (Throwable th) {
    	  throw new RuntimeException("Xml文档分析--->异常", th);
    }
    
  }

  public static Element file2Xml(String file)
  {
    try {
      SAXBuilder builder = new SAXBuilder(false);
      Document doc = builder.build(file);
      return doc.getRootElement(); } catch (Throwable th) {
    	  throw new RuntimeException("Xml文档分析--->异常", th);
    }
    
  }

  public static Element file2Xml(File file)
  {
    try {
      SAXBuilder builder = new SAXBuilder(false);
      Document doc = builder.build(file);
      return doc.getRootElement(); } catch (Throwable th) {
    	  throw new RuntimeException("Xml文档分析--->异常", th);
    }
    
  }

  public static void xml2File(String file, Element elmRoot)
  {
    try {
      elmRoot.detach();

      Format myFmt = Format.getCompactFormat();
      myFmt.setEncoding("gbk");
      myFmt.setIndent("    ");
      myFmt.setLineSeparator("\r\n");

      XMLOutputter fmt = new XMLOutputter(myFmt);

      Document doc = new Document();
      doc.setRootElement(elmRoot);

      String ss = "";
      ss = fmt.outputString(doc);
      ss = ss.replaceAll("\n\n", "\n");

      BufferedWriter bwr = new BufferedWriter(new FileWriter(new File(file)));
      bwr.write(ss);
      bwr.close();
    } catch (Throwable th) {
      throw new RuntimeException("保存Xml文档--->异常", th);
    }
  }

  public static void xml2FileEx(String file, JXmlWapper wapper) {
    try {
      Element elmRoot = wapper.getXmlRoot();
      elmRoot.detach();

      Format myFmt = Format.getCompactFormat();
      myFmt.setEncoding("gbk");
      myFmt.setIndent("   ");
      myFmt.setLineSeparator("\r\n");

      XMLOutputter fmt = new XMLOutputter(myFmt);

      Document doc = new Document();
      doc.setRootElement(elmRoot);

      String ss = "";
      ss = fmt.outputString(doc);
      ss = ss.replaceAll("\n\n", "\n");

      BufferedWriter bwr = new BufferedWriter(new FileWriter(new File(file)));
      bwr.write(ss);
      bwr.close();
    } catch (Throwable th) {
      throw new RuntimeException("保存Xml文档--->异常", th);
    }
  }

  public static Element url2Xml(String url, String data) throws Exception {
    return url2Xml(url, data, 10);
  }

  public static Element url2Xml(String url, String data, int timeout) throws Exception {
    URL murl = new URL(url);
    HttpURLConnection con = (HttpURLConnection)murl.openConnection();
    con.setConnectTimeout(1000 * timeout);
    con.setReadTimeout(1000 * timeout);

    con.setDoInput(true);
    con.setDoOutput(true);
    con.setAllowUserInteraction(false);

    DataOutputStream posts = new DataOutputStream(con.getOutputStream());
    posts.writeBytes(data);
    posts.flush();

    BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
    StringBuilder b = new StringBuilder();
    String line;
    while ((line = reader.readLine()) != null)
    {
     // String line;
      b.append(line);
      b.append("\r\n");
    }

    reader.close();
    posts.close();

    con.disconnect();
    reader = null;
    con = null;
    posts = null;

    SAXBuilder builder = new SAXBuilder(false);
    Reader in = new StringReader(new String(b));
    Document doc = builder.build(in);
    return doc.getRootElement();
  }

  public static Element url2Xml(String url) throws Exception {
    return url2Xml(url, 10);
  }

  public static Element url2Xml(String url, int timeout)
    throws Exception
  {
    URL murl = new URL(url);
    HttpURLConnection con = (HttpURLConnection)murl.openConnection();
    con.setConnectTimeout(1000 * timeout);
    con.setReadTimeout(1000 * timeout);

    BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
    StringBuilder b = new StringBuilder();
    String line;
    while ((line = reader.readLine()) != null)
    {
      //String line;
      b.append(line);
      b.append("\r\n");
    }

    reader.close();
    con.disconnect();
    reader = null;
    con = null;

    SAXBuilder builder = new SAXBuilder(false);
    Reader in = new StringReader(new String(b));
    Document doc = builder.build(in);
    return doc.getRootElement();
  }

  public static Format getOnelineXmlFormat(String charset)
  {
    if (_oneLineformat == null) {
      _oneLineformat = Format.getRawFormat();
      _oneLineformat.setEncoding(charset);
      _oneLineformat.setLineSeparator("\r\n");
    }
    return _oneLineformat;
  }

  public static Format getMultilineXmlFormat(String charset)
  {
    if (_multiLineformat == null) {
      _multiLineformat = Format.getRawFormat();
      _multiLineformat.setEncoding("gbk");

      _multiLineformat.setIndent("  ");
    }

    return _multiLineformat;
  }

  public static String decode(String strSource)
  {
    if (strSource == null) {
      return "";
    }
    String strDest = strSource;
    strDest = StringUtil.replaceString(strDest, "&lt;", "<");
    strDest = StringUtil.replaceString(strDest, "&gt;", ">");
    strDest = StringUtil.replaceString(strDest, "&amp;", "&");
    strDest = StringUtil.replaceString(strDest, "&quot;", "\"");

    return strDest;
  }

  public static String encode(String strSource)
  {
    if (strSource == null) {
      return "";
    }
    String strDest = strSource;
    strDest = StringUtil.replaceString(strDest, "&", "&amp;");
    strDest = StringUtil.replaceString(strDest, "<", "&lt;");
    strDest = StringUtil.replaceString(strDest, ">", "&gt;");
    strDest = StringUtil.replaceString(strDest, "\"", "&quot;");

    return strDest;
  }

  public static String createTagXml(String tagName, String tagValue)
  {
    StringBuffer sb = new StringBuffer("");
    sb.append("<");
    sb.append(encode(tagName));
    if (tagValue.length() == 0) {
      sb.append("/>");
    } else {
      sb.append(">");
      sb.append(encode(tagValue));
      sb.append("</");
      sb.append(tagName);
      sb.append(">");
    }
    return new String(sb);
  }

  public static String createAttrXml(String attrName, String attrValue) {
    StringBuffer sb = new StringBuffer("");
    sb.append(encode(attrName));
    sb.append("=\"");
    sb.append(encode(attrValue));
    sb.append("\" ");
    return new String(sb);
  }
}