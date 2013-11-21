package com.chamago.bison.loader;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Hashtable;

/**
 * 
 * @author Gavin.peng
 * 
 * 2013-10-27 下午04:04:50
 × bison
 */
public abstract class AbstractClassLoader extends URLClassLoader
{
  private Hashtable<String, Class<?>> classes = new Hashtable<String, Class<?>>();
  private char classNameReplacementChar;

  public AbstractClassLoader()
  {
    super(new URL[0]);
  }

  public AbstractClassLoader(ClassLoader classLoader)
  {
    super(new URL[0], classLoader);
  }

  public Class<?> loadClass(String className) throws ClassNotFoundException
  {
    return loadClass(className, true);
  }

  public Class<?> loadClass(String className, boolean resolveIt)
    throws ClassNotFoundException
  {
    Class result = (Class)this.classes.get(className);
    if (result != null)
      return result;
    try
    {
      result = super.findSystemClass(className);
    } catch (Exception e) {
      result = null;
    }
    if (result != null) {
      return result;
    }

    byte[] classBytes = loadClassBytes(className);
    if (classBytes != null) {
      result = defineClass(className, classBytes, 0, classBytes.length);
      if (result == null) {
        throw new ClassFormatError();
      }
      if (resolveIt) resolveClass(result);
      this.classes.put(className, result);
      return result;
    }
    throw new ClassNotFoundException();
  }

  public void setClassNameReplacementChar(char replacement)
  {
    this.classNameReplacementChar = replacement;
  }
  protected abstract byte[] loadClassBytes(String paramString);

  protected String formatClassName(String className) {
    if (this.classNameReplacementChar == 0) {
      return className.replace('.', '/') + ".class";
    }
    return className.replace('.', this.classNameReplacementChar) + ".class";
  }
}