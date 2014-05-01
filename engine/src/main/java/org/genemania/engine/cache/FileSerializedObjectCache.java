/**
 * This file is part of GeneMANIA.
 * Copyright (C) 2010 University of Toronto.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package org.genemania.engine.cache;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.genemania.exception.ApplicationException;

/**
 * Very simple cache implementation using java file serialization. So obviously
 * the objects being cached must be Serializable.
 * 
 * TODO: exception handling
 */
public class FileSerializedObjectCache implements IObjectCache {

    private String cacheDir;
    private boolean zipEnabled = false;

    public String getCacheDir() {
        return cacheDir;
    }

    public FileSerializedObjectCache(String cacheDir, boolean zipEnabled) {
        this.cacheDir = cacheDir;
        this.zipEnabled = zipEnabled;
    }

    public FileSerializedObjectCache(String cacheDir) {
        this(cacheDir, false);
    }

    private String getFilename(String key) {
        String name = this.cacheDir + File.separator + key + ".ser";
        if (zipEnabled) {
            name = name + ".gz";
        }
        return name;
    }

    private String getFilename(String subdir, String key) {
        String name = this.cacheDir + File.separator + subdir + File.separator + key + ".ser";
        if (zipEnabled) {
            name = name + ".gz";
        }
        return name;
    }

    public String getFilename(String [] key) throws ApplicationException {
        if (key == null || key.length == 0) {
            throw new ApplicationException("empty key");
        }

        String name = getSubdirname(key, key.length) + ".ser";
        if (zipEnabled) {
            name = name + ".gz";
        }
        return name;
    }

    /*
     * build up dir parts from array
     */
    public String getSubdirname(String [] key, int len) throws ApplicationException {
        StringBuilder path = new StringBuilder(cacheDir);

        for (int i=0; i<len; i++) {
            path.append(File.separator);
            if (key[i] == null || key[i].equals("")) {
                throw new ApplicationException("missing key part at position " + i);
            }
            path.append(key[i]);
        }

        return path.toString();
    }

    /*
     * extract key part from a file by stripping off cache location
     */
    private String[] fileToKey(File file) throws ApplicationException {
    	
    	if (!file.toString().startsWith(cacheDir)) {
    		throw new ApplicationException("unexpected cache file name");    		
    	}
    	
    	if (!file.toString().endsWith(".ser")) {
    		throw new ApplicationException("unexpected cache file name");    		
    	}

    	String keyString = file.toString().substring(cacheDir.length()+1, file.toString().length()-4); // +1 is for the seperator between cachedir and first key element

    	return keyString.split(File.separator);
    }
    
    private String getDir(String subdir) {
        return this.cacheDir + File.separator + subdir;
    }
    
    /*
     * subdir may be null
     */
    private Object getObject(String subdir, String key) throws ApplicationException {

        Object value = null;
        try {

            String filename;
            if (subdir != null) {
                filename = getFilename(subdir, key);
            }
            else {
                filename = getFilename(key);
            }
            
            InputStream fileIn = new FileInputStream(filename);
            fileIn = new BufferedInputStream(fileIn);
            if (zipEnabled) {
                fileIn = new GZIPInputStream(fileIn);
            }
            ObjectInputStream in = new ObjectInputStream(fileIn);

            value = in.readObject();

            in.close();
            fileIn.close();

        } catch (ClassNotFoundException e) {
            throw new ApplicationException(String.format("Failed to load object with key: '%s' from path '%s'",key, subdir), e);
        } catch (FileNotFoundException e) {
            throw new ApplicationException(String.format("Failed to load object with key: '%s' from path '%s'",key, subdir), e);
        } catch (IOException e) {
            throw new ApplicationException(String.format("Failed to load object with key: '%s' from path '%s'",key, subdir), e);
        }

        return value;
    }
    
    private void checkCreateDir(String subdir) {
        String dirName = this.cacheDir + File.separator + subdir;
        File dir = new File(dirName);
        if (!dir.isDirectory()) {
            dir.mkdir();
        }
    }

    /*
     * all but the last element of key should be subdirs under cacheDir.
     */
    private void checkCreateDirs(String [] key) throws ApplicationException {
        String dirName = getSubdirname(key, key.length-1);

        File dir = new File(dirName);
        if (!dir.isDirectory()) {
            dir.mkdirs();
        }
    }

    private void checkDir(String subdir) throws ApplicationException {
        String dirName = this.cacheDir + File.separator + subdir;
        File dir = new File(dirName);

        if (!dir.exists()) {
            throw new ApplicationException("directory does not exist: " + dirName);
        }

        if (!dir.isDirectory()) {
            throw new ApplicationException("not a directory: " + dirName);
        }
    }

    public void putObject(String subdir, String key, Object value) throws ApplicationException {
        try {

            String filename;
            if (subdir != null) {
                checkCreateDir(subdir);
                filename = getFilename(subdir, key);
            }
            else {
                filename = getFilename(key);
            }
            
            FileOutputStream fileOut = new FileOutputStream(filename);
            OutputStream out = new BufferedOutputStream(fileOut);
            if (zipEnabled) {
                out = new GZIPOutputStream(out);
            }
            ObjectOutputStream objout = new ObjectOutputStream(out);

            objout.writeObject(value);
            
            objout.flush();            
            fileOut.getFD().sync();
            
            objout.close();
            fileOut.close();

        } catch (FileNotFoundException e) {
            throw new ApplicationException("Failed to save object with key: " + key, e);
        } catch (IOException e) {
            throw new ApplicationException("Failed to save object with key: " + key, e);
        }
    }

    public void put(String [] key, Object value, boolean isVolatile) throws ApplicationException {
        try {

            checkCreateDirs(key);
            String filename = getFilename(key);

            FileOutputStream fileOut = new FileOutputStream(filename);
            OutputStream out = new BufferedOutputStream(fileOut);
            if (zipEnabled) {
                out = new GZIPOutputStream(out);
            }
            ObjectOutputStream objout = new ObjectOutputStream(out);

            objout.writeObject(value);
            
            objout.flush();            
            fileOut.getFD().sync();

            objout.close();
            fileOut.close();

        } catch (FileNotFoundException e) {
            throw new ApplicationException("Failed to save object with key: " + key, e);
        } catch (IOException e) {
            throw new ApplicationException("Failed to save object with key: " + key, e);
        }
    }

    public Object get(String [] key, boolean isVolatile) throws ApplicationException {
        Object value = null;
        String filename = null;
        try {
            filename = getFilename(key);
            value = deserialize(filename);
        } catch (ClassNotFoundException e) {
            throw new ApplicationException(String.format("Failed to load object with key: '%s'", filename), e);
        } catch (FileNotFoundException e) {
            throw new ApplicationException(String.format("Failed to load object with key: '%s'", filename), e);
        } catch (IOException e) {
            throw new ApplicationException(String.format("Failed to load object with key: '%s'", filename), e);
        }

        return value;
    }

    protected Object deserialize(String filename) throws IOException, ClassNotFoundException {
        InputStream fileIn = new FileInputStream(filename);
        fileIn = new BufferedInputStream(fileIn);
        if (zipEnabled) {
            fileIn = new GZIPInputStream(fileIn);
        }
        ObjectInputStream in = new ObjectInputStream(fileIn);

        Object value = in.readObject();

        in.close();
        fileIn.close();
        
        return value;
	}

	/*
     * remove file or directory matching key
     */
    public void remove(String [] key) throws ApplicationException {
        if (key == null || key.length == 0) {
            throw new ApplicationException("directory not given");
        }

        // check if key is a file, just remove it and exist
        String filename = getFilename(key);
        File file = new File(filename);

        if (file.exists() && file.isFile()) {
            file.delete();
            return;
        }

        // maybe the key matches a dir, recursively
        // delete the tree rooted there
        String dirName = getSubdirname(key, key.length);
        file = new File(dirName);
        if (file.isDirectory()) {
            recursiveDeleteDir(file);
        }
    }

    private static void recursiveDeleteDir(File dir) {

        File[] entries = dir.listFiles();

        for (File entry: entries) {
            if (entry.isFile()) {
                entry.delete();
            }
            else if (entry.isDirectory()) {
                recursiveDeleteDir(entry);
            }
        }

        dir.delete();
    }

	public boolean exists(String[] key) throws ApplicationException {
        if (key == null || key.length == 0) {
            throw new ApplicationException("null or empty object key");
        }

        String filename = getFilename(key);
        File file = new File(filename);
		
        return file.exists();
	}

	public List<String[]> list(String[] key) throws ApplicationException {
        if (key == null || key.length == 0) {
            throw new ApplicationException("null or empty object key");
        }

        List<String[]> result = new ArrayList<String[]>();

//        String filename = getFilename(key);
//        File file = new File(filename);
//        
//        
//        // should we throw an error if its not a dir?
//        if (!file.isDirectory()) {
//        	return result;
//        }
        
        String dirName = getSubdirname(key, key.length);
        File dir = new File(dirName);
        
        if (!dir.isDirectory()) {
        	return result;        	
        }
        
        File[] entries = dir.listFiles();

        for (File entry: entries) {
        	result.add(fileToKey(entry));
        }
        
        return result;
    } 
}