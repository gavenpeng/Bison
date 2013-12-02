package com.chamago.bison.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

public class BisonObjectInputStream extends ObjectInputStream
{
  private ClassLoader loader;

  public BisonObjectInputStream(InputStream in, ClassLoader loader)
    throws IOException
  {
    super(in);
    this.loader = loader;
  }

  protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
    try {
      String name = desc.getName();
      return Class.forName(name, false, this.loader); } catch (Exception e) {
    }
    return super.resolveClass(desc);
  }
}