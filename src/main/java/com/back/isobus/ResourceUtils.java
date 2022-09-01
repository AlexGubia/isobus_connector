package com.back.isobus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

public class ResourceUtils {
	/*
     * for all elements of java.class.path get a Collection of resources Pattern
     * pattern = Pattern.compile(".*"); gets all resources
     * 
     * @param pattern
     *            the pattern to match
     * @return the resources in the order they are found
     */
    public static Collection<String> getResources(
        final Pattern pattern){
        final ArrayList<String> retval = new ArrayList<String>();
        final String classPath = System.getProperty("java.class.path", ".");
        final String[] classPathElements = classPath.split(System.getProperty("path.separator"));
        for(final String element : classPathElements){
            retval.addAll(getResources(element, pattern));
        }
        return retval;
    }

    private static Collection<String> getResources(
        final String element,
        final Pattern pattern){
        final ArrayList<String> retval = new ArrayList<String>();
        final File file = new File(element);
        if(file.isDirectory()){
            retval.addAll(getResourcesFromDirectory(file, pattern));
        } else{
            retval.addAll(getResourcesFromJarFile(file, pattern));
        }
        return retval;
    }

    private static Collection<String> getResourcesFromJarFile(
        final File file,
        final Pattern pattern){
        final ArrayList<String> retval = new ArrayList<String>();
        ZipFile zf;
        try{
            zf = new ZipFile(file);
        } catch(final ZipException e){
            throw new Error(e);
        } catch(final IOException e){
            throw new Error(e);
        }
        @SuppressWarnings("rawtypes")
		final Enumeration e = zf.entries();
        while(e.hasMoreElements()){
            final ZipEntry ze = (ZipEntry) e.nextElement();
            final String fileName = ze.getName();
            final boolean accept = pattern.matcher(fileName).matches();
            if(accept){
                retval.add(fileName);
            }
        }
        try{
            zf.close();
        } catch(final IOException e1){
            throw new Error(e1);
        }
        return retval;
    }

    private static Collection<String> getResourcesFromDirectory(
        final File directory,
        final Pattern pattern){
        final ArrayList<String> retval = new ArrayList<String>();
        final File[] fileList = directory.listFiles();
        for(final File file : fileList){
            if(file.isDirectory()){
                retval.addAll(getResourcesFromDirectory(file, pattern));
            } else{
                try{
                    final String fileName = file.getCanonicalPath();
                    final boolean accept = pattern.matcher(fileName).matches();
                    if(accept){
                        retval.add(fileName);
                    }
                } catch(final IOException e){
                    throw new Error(e);
                }
            }
        }
        return retval;
    }
    
    private static String getResource(String fileName) {
        final Collection<String> list = ResourceUtils.getResources(Pattern.compile(".*"));
        String filePath = null;
        for(final String name : list) {
    		if (name.contains(fileName)) {
	            filePath = name;
	           // System.out.println("Match!");
	            break;
        	}
        }
		return filePath;
    }
    
    public static File getResourceAsFile(String filePath) {
    	//System.out.println("Searching for " + filePath);
    	String realFilePath = ResourceUtils.getResource(filePath);
    	File myFile = new File(realFilePath);
    	//System.out.println("got: " + realFilePath);
	    if (!myFile.isAbsolute()) {
	    	//System.out.println("not absolute");
	    	
	    	myFile = new File(CanBUS_socket.class.getClassLoader().getResource(realFilePath).getFile());
	    	//System.out.println("myFile= " + myFile);
	    	try {
		    	InputStream is = CanBUS_socket.class.getClassLoader().getResourceAsStream(realFilePath); //ClassLoader.class.getResourceAsStream(realFilePath);
		        File file = File.createTempFile("tempfiles", "temp", null);
		        //System.out.println("file= " + file);
		        OutputStream os = new FileOutputStream(file);
		        byte[] buffer = new byte[1024];
		        int length;
		  /*      if (is == null) {
		        	System.out.println("is == null");
		        }*/
		        while ((length = is.read(buffer)) != -1) {
		            os.write(buffer, 0, length);
		        }
		        is.close();
		        os.close();
		        file.deleteOnExit();
		        myFile = file;
		        //System.out.println("myFile= " + myFile);
	    	} catch (Exception e) {
	    		e.printStackTrace();
	    	} 	
		}
	    
	    //System.out.println("returning myFile= " + myFile);
	    return myFile;
    }
}  
