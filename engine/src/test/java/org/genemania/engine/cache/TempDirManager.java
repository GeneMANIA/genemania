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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.genemania.engine.cache;

import java.io.File;

/**
 * Use to manage creation & cleanup of a temporary folder
 * required by test cases.
 *
 *   public class MyJUnitTest extends TestCase {
 *
 *      public static TempDirManager tempDir = new TempDirManager();
 *
 *      public void setup() {
 *          tempDir.setup();
 *          ...
 *      }
 *
 *      public void tearDown() {
 *          tempDir.tearDown();
 *      }
 *
 *      ...
 *
 *  }
 *
 * Could make this class extend testcase, and then have your testcases
 * extend this, but direct compositional usage suits me well enough.
 */
public class TempDirManager {
    private static String DEFAULT_TEMP_DIR = "target/temp_test_dir";
    private String tempDir = DEFAULT_TEMP_DIR;

    /*
     * just use the default temp location
     */
    public TempDirManager() {
    }

    /*
     * use specified temp location
     */
    public TempDirManager(String tempDir) {
        this.tempDir = tempDir;
    }

    public void setUp() {
        createTempDir();
    }

    public void tearDown() {
        deleteTempDir();
    }

    /**
     * @return the temp dir
     */
    public String getTempDir() {
        return tempDir;
    }

    public void setTempDir(String tempDir) {
        this.tempDir = tempDir;
    }

    /**
     *  recursively delete a directory and any files in it. 
     */
    public static void deleteDir(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isFile()) {
                    files[i].delete();
                }
                else if (files[i].isDirectory()) {
                    deleteDir(files[i]);
                }
            }
        }

        path.delete();
    }

    public void createTempDir() {
        File path = new File(getTempDir());
        if (path.exists()) { // previous test run may have failed ... cleanup
            deleteDir(path);
        }
        path.mkdir();
    }

    public void deleteTempDir() {
        File path = new File(getTempDir());
        deleteDir(path);
    }
}
