package com.chamago.bison.loader;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * 
 * @author Gavin.peng
 * 
 * 2013-10-27 下午04:05:44
 × bison
 */
public final class JarResources
{
  public boolean debugOn = false;

  private Hashtable<String, Integer> htSizes = new Hashtable<String,Integer>();

  private Hashtable<String, byte[]> htJarContents = new Hashtable<String, byte[]>();
  private String jarFileName;
  private long lastModify;

  public JarResources(String jarFileName)
  {
    this.jarFileName = jarFileName;
    init();
  }

  public byte[] getResource(String name)
  {
    return (byte[])this.htJarContents.get(name);
  }

  private void init()
  {
    FileInputStream fis = null;
    try {
      File file = new File(this.jarFileName);
      this.lastModify = file.lastModified();

      ZipFile zf = new ZipFile(this.jarFileName);

      Enumeration e = zf.entries();
      while (e.hasMoreElements()) {
        ZipEntry ze = (ZipEntry)e.nextElement();

        if (this.debugOn) {
          System.out.println(dumpZipEntry(ze));
        }
        this.htSizes.put(ze.getName(), new Integer((int)ze.getSize()));
      }
      zf.close();

      fis = new FileInputStream(this.jarFileName);
      BufferedInputStream bis = new BufferedInputStream(fis);
      ZipInputStream zis = new ZipInputStream(bis);
      ZipEntry ze = null;
      while ((ze = zis.getNextEntry()) != null) {
        if (ze.isDirectory())
        {
          continue;
        }
        if (this.debugOn) {
          System.out.println("ze.getName()=" + ze.getName() + "," + "getSize()=" + ze.getSize());
        }

        int size = (int)ze.getSize();

        if (size == -1) {
          size = ((Integer)this.htSizes.get(ze.getName())).intValue();
        }

        byte[] b = new byte[size];
        int rb = 0;
        int chunk = 0;
        while (size - rb > 0) {
          chunk = zis.read(b, rb, size - rb);
          if (chunk == -1) {
            break;
          }
          rb += chunk;
        }

        this.htJarContents.put(ze.getName(), b);
        if (this.debugOn)
          System.out.println(ze.getName() + "  rb=" + rb + ",size=" + size + ",csize=" + ze.getCompressedSize());
      }
    }
    catch (NullPointerException e) {
      System.out.println("done.");
      try
      {
        if (fis != null) {
          fis.close();
          fis = null;
        }
      }
      catch (Exception localException)
      {
      }
    }
    catch (FileNotFoundException e)
    {
      e.printStackTrace();
      try
      {
        if (fis != null) {
          fis.close();
          fis = null;
        }
      }
      catch (Exception localException1)
      {
      }
    }
    catch (IOException e)
    {
      e.printStackTrace();
      try
      {
        if (fis != null) {
          fis.close();
          fis = null;
        }
      }
      catch (Exception localException2)
      {
      }
    }
    finally
    {
      try
      {
        if (fis != null) {
          fis.close();
          fis = null;
        }
      }
      catch (Exception localException3)
      {
      }
    }
  }

  private String dumpZipEntry(ZipEntry ze)
  {
    StringBuffer sb = new StringBuffer();
    if (ze.isDirectory())
      sb.append("d ");
    else {
      sb.append("f ");
    }

    if (ze.getMethod() == 0)
      sb.append("stored   ");
    else {
      sb.append("defalted ");
    }

    sb.append(ze.getName());
    sb.append("\t");
    sb.append(ze.getSize());
    if (ze.getMethod() == 8) {
      sb.append("/" + ze.getCompressedSize());
    }

    return sb.toString();
  }

  public void destory() {
    this.htJarContents.clear();
    this.htSizes.clear();
  }

  public boolean isChanged()
  {
    File file = new File(this.jarFileName);
    if (this.lastModify != file.lastModified()) {
      this.lastModify = file.lastModified();
      return true;
    }
    return false;
  }

  public Enumeration<String> getAllResourceName()
  {
    return this.htJarContents.keys();
  }
}