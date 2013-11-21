package com.chamago.bison.util.xml;

import com.chamago.bison.util.StringUtil;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import org.jdom2.Attribute;
import org.jdom2.Element;

public class JXmlWapper
  implements Serializable
{
  private static final long serialVersionUID = 1965559298207194831L;
  private Element xmlroot;

  public JXmlWapper(String base)
  {
    this.xmlroot = new Element(base);
  }

  public JXmlWapper(Element element) {
    this.xmlroot = element;
  }

  public Element getXmlRoot() {
    return getXmlRoot(false);
  }

  public Element getXmlRoot(boolean top) {
    if (!top) {
      return this.xmlroot;
    }
    Element tmpnode = this.xmlroot;
    while (tmpnode.getParentElement() != null) {
      tmpnode = tmpnode.getParentElement();
    }
    return tmpnode;
  }

  public String toXmlString()
  {
    return toXmlString("gbk");
  }

  public String toXmlString(String charset) {
    return toXmlString(this, charset);
  }

  public static JXmlWapper parse(String xml) {
    Element element = JXmlUtil.string2Xml(xml);
    if (element != null) {
      return new JXmlWapper(element);
    }
    return null;
  }

  public static JXmlWapper parse(File xmlFile)
  {
    Element element = JXmlUtil.file2Xml(xmlFile);
    if (element != null) {
      return new JXmlWapper(element);
    }
    return null;
  }

  public static JXmlWapper parseUrl(String url) throws Exception
  {
    return parseUrl(url, 10);
  }

  public static JXmlWapper parseUrl(String url, int timeout) throws Exception {
    Element element = JXmlUtil.url2Xml(url, timeout);
    if (element != null) {
      return new JXmlWapper(element);
    }
    return null;
  }

  public static JXmlWapper parseUrl(String url, String data) throws Exception
  {
    return parseUrl(url, data, 10);
  }

  public static JXmlWapper parseUrl(String url, String data, int timeout) throws Exception {
    Element element = JXmlUtil.url2Xml(url, data, timeout);
    if (element != null) {
      return new JXmlWapper(element);
    }
    return null;
  }

  public static String toXmlString(JXmlWapper wapper, String charset)
  {
    return JXmlUtil.xml2String(wapper.getXmlRoot(), charset);
  }

  public void addValue(String path, int value)
  {
    addValue(path, String.valueOf(value));
  }

  public void addValue(String path, long value) {
    addValue(path, String.valueOf(value));
  }

  public void addValue(String path, double value) {
    addValue(path, String.valueOf(value));
  }

  public void addValue(String path, Date date) {
    addValue(path, TimeUtil.customDateTime(date));
  }

  public void addValue(String path, boolean value) {
    addValue(path, String.valueOf(value));
  }

  public void addStringValue(String path, String value) {
    addValue(path, value);
  }
  public void addValue(String path, String value) {
    PHelper helper = pathDecode(path);
    Element element = makeXmlElement(helper);
    if (element == null) {
      throw new RuntimeException("指定路径" + path + "的节点创建失败");
    }

    if (helper.check) {
      element.setAttribute(helper.attr, value);
    } else {
      List list = element.removeContent();
      element.setText(value);
      for (int idx = 0; idx < list.size(); idx++)
        if ((list.get(idx) instanceof Element))
          element.addContent((Element)list.get(idx));
    }
  }

  public void setValue(String path, int value)
  {
    setValue(path, String.valueOf(value));
  }

  public void setValue(String path, long value) {
    setValue(path, String.valueOf(value));
  }

  public void setValue(String path, double value) {
    setValue(path, String.valueOf(value));
  }

  public void setValue(String path, Date date) {
    setValue(path, TimeUtil.customDateTime(date));
  }

  public void setValue(String path, boolean value) {
    setValue(path, String.valueOf(value));
  }

  public void setValue(String path, String value) {
    PHelper helper = pathDecode(path);
    Element element = findXmlElement(helper, 0);
    if (element == null) {
      throw new RuntimeException("指定路径" + path + "的节点未找到");
    }
    if (helper.check) {
      element.setAttribute(helper.attr, value);
    } else {
      List list = element.removeContent();
      element.setText(value);
      for (int idx = 0; idx < list.size(); idx++)
        if ((list.get(idx) instanceof Element))
          element.addContent((Element)list.get(idx));
    }
  }

  public int getIntValue(String path)
  {
    return Integer.parseInt(getStringValue(path));
  }

  public long getLongValue(String path) {
    return Long.parseLong(getStringValue(path));
  }

  public short getShortValue(String path) {
    return Short.parseShort(getStringValue(path));
  }

  public double getDoubleValue(String path) {
    return Double.parseDouble(getStringValue(path));
  }

  public Date getDateValue(String path) {
    return TimeUtil.parserDateTime(getStringValue(path));
  }

  public boolean getBoolValue(String path) {
    return Boolean.parseBoolean(getStringValue(path));
  }

  public int getIntValue(String path, int defvalue) {
    String value = getStringValue(path, String.valueOf(defvalue));
    if ((value == null) || (value.length() == 0)) {
      return defvalue;
    }
    return Integer.parseInt(value);
  }

  public long getLongValue(String path, long defvalue)
  {
    String value = getStringValue(path, String.valueOf(defvalue));
    if ((value == null) || (value.length() == 0)) {
      return defvalue;
    }
    return Long.parseLong(value);
  }

  public short getShortValue(String path, short defvalue)
  {
    String value = getStringValue(path, String.valueOf(defvalue));
    if ((value == null) || (value.length() == 0)) {
      return defvalue;
    }
    return Short.parseShort(value);
  }

  public double getDoubleValue(String path, double defvalue)
  {
    String value = getStringValue(path, String.valueOf(defvalue));
    if ((value == null) || (value.length() == 0)) {
      return defvalue;
    }
    return Double.parseDouble(value);
  }

  public Date getDateValue(String path, String defvalue)
  {
    String value = getStringValue(path, defvalue);
    if ((value == null) || (value.length() == 0)) {
      value = defvalue;
    }
    return TimeUtil.parserDateTime(value);
  }

  public boolean getBoolValue(String path, boolean defvalue) {
    String value = getStringValue(path, String.valueOf(defvalue));
    if ((value == null) || (value.length() == 0)) {
      return defvalue;
    }
    return Boolean.parseBoolean(value);
  }

  public String getStringValue(String path)
  {
    return getStringValue(path, null);
  }

  public String getStringValue(String path, String defvalue) {
    PHelper helper = pathDecode(path);
    Element element = findXmlElement(helper, 0);
    if (element != null) {
      if (helper.check) {
        Attribute attr = element.getAttribute(helper.attr);
        if (attr != null)
          return attr.getValue();
      }
      else {
        return element.getText();
      }
    }
    return defvalue;
  }

  public Map<String, String> getXmlAttrs(String path)
  {
    PHelper helper = pathDecode(path);
    if (helper.check) {
      throw new RuntimeException("当前路径<" + path + ">为属性路径");
    }
    Element element = findXmlElement(helper, 0);
    if (element == null) {
      throw new RuntimeException("指定路径" + path + "的节点未找到");
    }
    Map table = new Hashtable();
    List attrs = element.getAttributes();
    for (int idx = 0; idx < attrs.size(); idx++) {
      table.put(((Attribute)attrs.get(idx)).getName(), ((Attribute)attrs.get(idx)).getValue());
    }
    return table;
  }

  public JXmlWapper getXmlNode(String path) {
    PHelper helper = pathDecode(path);
    if (helper.check) {
      throw new RuntimeException("当前路径<" + path + ">为属性路径");
    }
    Element element = findXmlElement(helper, 0);
    if (element == null)
    {
      return null;
    }
    if (element == this.xmlroot) {
      return this;
    }
    return new JXmlWapper(element);
  }

  public JXmlWapper addXmlNode(String path)
  {
    PHelper helper = pathDecode(path);
    if (helper.check) {
      throw new RuntimeException("当前路径<" + path + ">为属性路径");
    }
    Element element = makeXmlElement(helper);
    if (element == null) {
      throw new RuntimeException("指定路径" + path + "的节点未创建成功");
    }
    if (element == this.xmlroot) {
      return this;
    }
    return new JXmlWapper(element);
  }

  public List<JXmlWapper> getXmlNodeList(String path)
  {
    PHelper helper = pathDecode(path);
    if (helper.check) {
      throw new RuntimeException("当前路径<" + path + ">为属性路径");
    }
    Element element = findXmlElement(helper, 1);
    if (element == null) {
      throw new RuntimeException("指定路径" + path + "的节点未找到");
    }
    List volist = new ArrayList();
    List elmlist = element.getChildren(helper.last());
    for (int idx = 0; idx < elmlist.size(); idx++) {
      volist.add(new JXmlWapper((Element)elmlist.get(idx)));
    }
    return volist;
  }

  public int countXmlNodes(String path) {
    PHelper helper = pathDecode(path);
    if (helper.check) {
      throw new RuntimeException("当前路径<" + path + ">为属性路径");
    }
    Element element = findXmlElement(helper, 1);
    if (element == null) {
      return 0;
    }

    return element.getChildren(helper.last()).size();
  }

  public boolean remove(String path) {
    PHelper helper = pathDecode(path);
    Element element = findXmlElement(helper, 0);
    if (element == null) {
      return true;
    }

    if (helper.check) {
      return element.removeAttribute(helper.attr);
    }
    Element elmParent = element.getParentElement();
    if (elmParent != null) {
      List lst = elmParent.getChildren();
      if ((lst != null) && (lst.size() > 0)) {
        lst.remove(element);
        return true;
      }
      return false;
    }

    return false;
  }

  private Element findXmlElement(PHelper phelp, int left)
  {
    Element curnode = getXmlRoot();
    for (int idx = 0; idx < phelp.nodes.length - left; idx++) {
      NHelper nhelp = nodeDecode(phelp.nodes[idx]);
      List subs = curnode.getChildren(nhelp.node);
      if (subs.size() == 0) {
        return null;
      }
      if (nhelp.check) {
        if (nhelp.index >= subs.size()) {
          return null;
        }
        curnode = (Element)subs.get(nhelp.index);
      }
      else {
        if (subs.size() > 1) {
          return null;
        }
        curnode = (Element)subs.get(0);
      }
    }

    return curnode;
  }

  private Element makeXmlElement(PHelper phelp)
  {
    Element curnode = getXmlRoot();
    for (int idx = 0; idx < phelp.nodes.length; idx++) {
      NHelper nhelp = nodeDecode(phelp.nodes[idx]);
      List subs = curnode.getChildren(nhelp.node);
      if (nhelp.check) {
        if (nhelp.index == subs.size()) {
          Element newnode = new Element(nhelp.node);
          curnode.addContent(newnode);
          curnode = newnode;
        } else if (nhelp.index < subs.size()) {
          curnode = (Element)subs.get(nhelp.index);
        } else {
          return null;
        }
      }
      else if (subs.size() == 0) {
        Element newnode = new Element(nhelp.node);
        curnode.addContent(newnode);
        curnode = newnode;
      }
      else if (idx < phelp.nodes.length - 1) {
        if (subs.size() == 1)
          curnode = (Element)subs.get(0);
        else
          return null;
      }
      else {
        Element newnode = new Element(nhelp.node);
        curnode.addContent(newnode);
        curnode = newnode;
      }

    }

    return curnode;
  }

  private NHelper nodeDecode(String path) {
    NHelper helper = new NHelper();
    if (path.endsWith("]")) {
      int pos = path.indexOf("[");
      helper.check = true;
      helper.node = path.substring(0, pos);
      helper.index = Integer.parseInt(path.substring(pos + 1, path.length() - 1));
    } else {
      helper.node = path;
      helper.check = false;
    }
    return helper;
  }

  private PHelper pathDecode(String path)
  {
    PHelper helper = new PHelper();
    if (path.equals(".")) {
      helper.check = false;
      helper.nodes = new String[0];
    }
    else {
      String[] nodes = StringUtil.splitter(path, ".");
      int acount = nodes.length; int ncount = nodes.length - 1;
      if (nodes[ncount].startsWith("@")) {
        helper.check = true;
        helper.attr = nodes[ncount].substring(1);
        helper.nodes = new String[ncount];
        System.arraycopy(nodes, 0, helper.nodes, 0, ncount);
      } else {
        helper.check = false;
        helper.nodes = new String[acount];
        System.arraycopy(nodes, 0, helper.nodes, 0, acount);
      }
    }
    return helper;
  }

  class NHelper
  {
    public int index;
    public String node;
    public boolean check;

    NHelper()
    {
    }
  }

  class PHelper
  {
    public String attr;
    public boolean check;
    public String[] nodes;

    PHelper()
    {
    }

    public String last()
    {
      return this.nodes[(this.nodes.length - 1)];
    }
  }
}