package com.chamago.bison.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class ZipUtil
{
  public static void zip(String zipFileName, File inputFile)
    throws Exception
  {
    ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFileName));
    zip(out, inputFile, "");
    out.close();
  }

  private static void zip(ZipOutputStream out, File f, String base) throws Exception {
    if (f.isDirectory()) {
      base = base + f.getName() + "/";
      out.putNextEntry(new ZipEntry(base));
      File[] fl = f.listFiles();
      for (int i = 0; i < fl.length; i++)
        zip(out, fl[i], base);
    }
    else {
      base = base + f.getName();
      out.putNextEntry(new ZipEntry(base));
      FileInputStream in = new FileInputStream(f);

      byte[] bb = new byte[4096];
      while (true) {
        int num = in.read(bb, 0, 4096);
        if (num < 0) {
          break;
        }
        out.write(bb, 0, num);
      }

      in.close();
    }
  }

  public static void unZip(String zipFileName, String destDir) throws Exception {
    byte[] b = new byte[8092];
    ZipFile zippy = new ZipFile(zipFileName);
    Enumeration all = zippy.entries();
    while (all.hasMoreElements()) {
      ZipEntry e = (ZipEntry)all.nextElement();

      String zipName = e.getName();

      if (!e.isDirectory()) {
        if (zipName.startsWith("/")) {
          zipName = zipName.substring(1);
        }
        int ix = zipName.lastIndexOf('/');
        if (ix > 0) {
          String dirName = zipName.substring(0, ix);
          File d = new File(destDir, dirName);
          if ((!d.exists()) || (!d.isDirectory())) {
            d.mkdirs();
          }
        }

        FileOutputStream os = new FileOutputStream(new File(destDir, zipName));
        InputStream is = zippy.getInputStream(e);
        int n = 0;
        while ((n = is.read(b)) > 0)
          os.write(b, 0, n);
        is.close();
        os.close();
      } else {
        File d = new File(destDir, zipName);
        if (!d.exists())
          d.mkdirs();
      }
    }
  }

  public static void processFile(File f, String destDir) throws Exception
  {
    if (f.isDirectory()) {
      File dir = new File(destDir, f.getName());
      if (!dir.exists()) {
        dir.mkdirs();
      }
      File[] files = f.listFiles();
      for (int i = 0; i < files.length; i++) {
        processFile(files[i], dir.getCanonicalPath());
      }
    }
    else if (f.getName().endsWith("zip")) {
      unZip(f.getCanonicalPath(), destDir);
    }
  }

  public static byte[] ZipData(byte[] sByte)
  {
    try {
      ByteArrayOutputStream bout = new ByteArrayOutputStream();
      GZIPOutputStream out = new GZIPOutputStream(bout);

      out.write(sByte, 0, sByte.length);

      out.flush();
      out.close();
      byte[] b = bout.toByteArray();
      bout.close();
      return b; } catch (Exception e) {
    }
    return sByte;
  }

  public static byte[] UnzipData(byte[] dByte)
  {
    BufferedInputStream in = null;
    ByteArrayOutputStream bout = null;

    byte[] b1 = (byte[])null;
    try {
      in = new BufferedInputStream(
        new GZIPInputStream(
        new ByteArrayInputStream(dByte)));
      bout = new ByteArrayOutputStream();
      int c;
      while ((c = in.read()) != -1)
      {
        bout.write(c);
      }
    }
    catch (EOFException localEOFException) {
    }
    catch (Exception localException1) {
    }
    try {
      if (bout != null) {
        b1 = bout.toByteArray();
        bout.close();
      }
      in.close();
    } catch (Exception e2) {
      b1 = dByte;
    }
    return b1;
  }

  public static byte[] ZipObject(Object obj, boolean flag)
  {
    ByteArrayOutputStream o = null;
    GZIPOutputStream gzout = null;
    ObjectOutputStream out = null;
    byte[] data_ = (byte[])null;
    try
    {
      o = new ByteArrayOutputStream();
      if (flag) {
        gzout = new GZIPOutputStream(o);
        out = new ObjectOutputStream(gzout);
      } else {
        out = new ObjectOutputStream(o);
      }
      out.writeObject(obj);
      out.flush();

      out.close();
      out = null;
      if (gzout != null) {
        gzout.close();
        gzout = null;
      }

      data_ = o.toByteArray();
    } catch (IOException e) {
      e.printStackTrace();
      try
      {
        if (o != null) {
          o.close();
          o = null;
        }
        if (gzout != null) {
          gzout.close();
          gzout = null;
        }
        if (out != null) {
          out.close();
          out = null;
        }
      }
      catch (Exception localException)
      {
      }
    }
    finally
    {
      try
      {
        if (o != null) {
          o.close();
          o = null;
        }
        if (gzout != null) {
          gzout.close();
          gzout = null;
        }
        if (out != null) {
          out.close();
          out = null;
        }
      } catch (Exception localException1) {
      }
    }
    return data_;
  }

  public static Object UnzipObject(byte[] data_, int off, int len, ClassLoader loader, boolean flag)
  {
    ByteArrayInputStream bin = null;
    ObjectInputStream in = null;
    GZIPInputStream gzin = null;
    Object object_ = null;
    try
    {
      bin = new ByteArrayInputStream(data_, off, len);
      if (flag) {
        gzin = new GZIPInputStream(bin);
        if (loader != null)
          in = new BisonObjectInputStream(gzin, loader);
        else {
          in = new ObjectInputStream(gzin);
        }
      }
      else if (loader != null) {
        in = new BisonObjectInputStream(bin, loader);
      } else {
        in = new ObjectInputStream(bin);
      }

      object_ = in.readObject();

      bin.close();
      bin = null;
      if (gzin != null) {
        gzin.close();
        gzin = null;
      }
      in.close();
      in = null;
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
      System.out.println(e);
      try
      {
        if (bin != null) {
          bin.close();
          bin = null;
        }
        if (gzin != null) {
          gzin.close();
          gzin = null;
        }
        if (in != null) {
          in.close();
          in = null;
        }
      }
      catch (Exception localException)
      {
      }
    }
    catch (IOException e)
    {
      e.printStackTrace();
      System.out.println(e);
      try
      {
        if (bin != null) {
          bin.close();
          bin = null;
        }
        if (gzin != null) {
          gzin.close();
          gzin = null;
        }
        if (in != null) {
          in.close();
          in = null;
        }
      }
      catch (Exception localException1)
      {
      }
    }
    finally
    {
      try
      {
        if (bin != null) {
          bin.close();
          bin = null;
        }
        if (gzin != null) {
          gzin.close();
          gzin = null;
        }
        if (in != null) {
          in.close();
          in = null;
        }
      } catch (Exception localException2) {
      }
    }
    return object_;
  }
}