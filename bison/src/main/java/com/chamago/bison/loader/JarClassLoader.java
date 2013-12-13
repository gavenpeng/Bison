package com.chamago.bison.loader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

/**
 * 
 * @author Gavin.peng
 * 
 * 2013-10-27 下午04:04:55
 × bison
 */
public class JarClassLoader extends AbstractClassLoader
{
  private List<String> classRepository;
  private List<JarResources> jarsRepositoy;
  private List<String> pathRepository;

  public JarClassLoader()
  {
    super(JarClassLoader.class.getClassLoader());
    this.classRepository = new ArrayList();
    this.pathRepository = new ArrayList();
    this.jarsRepositoy = new ArrayList();
    init();
  }

  public void addClassPath(String classPath) {
    StringTokenizer tokenizer = new StringTokenizer(classPath, File.pathSeparator);
    while (tokenizer.hasMoreTokens()) {
      String ss = tokenizer.nextToken();
      if (!this.classRepository.contains(ss))
        this.classRepository.add(ss);
    }
  }

  public void init()
  {
    try {
      String ss = System.getProperty("bison.service.home") + File.separator + "service";
      this.classRepository.add(ss);
      this.classRepository.add(ss + File.separator + "classes");
      addJarPath(ss);

      ss = System.getProperty("bison.service.home") + File.separator + "plugin";
      addJarPath(ss);

      loadJarResouseFormPath();
    } catch (Exception ee) {
      ee.printStackTrace();
    }
  }

  public void addJarPath(String pathName) {
    if (!this.pathRepository.contains(pathName))
      this.pathRepository.add(pathName);
  }

  private synchronized void loadJarResouseFormPath()
  {
    try
    {
      Iterator paths = this.pathRepository.iterator();
      while (paths.hasNext()) {
        String pathName = (String)paths.next();
        loadDir(pathName);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void loadDir(String pathName)
  {
    try {
      File f = new File(pathName);
      if (f.isDirectory()) {
        File[] fs = f.listFiles();
        for (int i = 0; i < fs.length; i++) {
          File fl = fs[i];
          if (fl.isDirectory()) {
            loadDir(fl.getCanonicalPath());
          }
          else if (fl.getName().toLowerCase().endsWith(".jar"))
            this.jarsRepositoy.add(new JarResources(fl.getCanonicalPath()));
        }
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  protected synchronized byte[] loadClassBytes(String className) {
    byte[] classBytes = (byte[])null;

    String classFileName = className.replace('.', File.separatorChar);
    classFileName = classFileName + ".class";

    Iterator dirs = this.classRepository.iterator();
    while (dirs.hasNext()) {
      String dir = (String)dirs.next();
      File f = new File(dir);
      if (f.isDirectory()) {
        InputStream is = null;
        try {
          File file = new File(dir + File.separatorChar + classFileName);
          if (file.exists()) {
            is = new FileInputStream(file);

            classBytes = new byte[is.available()];
            is.read(classBytes);
            try
            {
              if (is == null) break;
              is.close();
              is = null;
            }
            catch (Exception localException)
            {
            }
          }
        }
        catch (IOException localIOException)
        {
          try
          {
            if (is != null) {
              is.close();
              is = null;
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
            if (is != null) {
              is.close();
              is = null;
            }
          }
          catch (Exception localException2) {
          }
        }
      }
    }
    if (classBytes != null) {
      return classBytes;
    }

    Iterator jars = this.jarsRepositoy.iterator();
    while (jars.hasNext()) {
      JarResources jar = (JarResources)jars.next();
      classBytes = jar.getResource(className.replace('.', '/') + ".class");
      if (classBytes != null) {
        break;
      }
    }
    return classBytes;
  }

  public synchronized void destory()
  {
  }
}