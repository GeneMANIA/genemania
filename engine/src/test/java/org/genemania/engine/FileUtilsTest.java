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

package org.genemania.engine;
import java.io.Reader;
import java.io.StringReader;
import java.util.Vector;

import org.genemania.engine.utils.FileUtils;

import org.junit.Test;
import static org.junit.Assert.*;

public class FileUtilsTest {

    @Test
    public void testLoadBatchQueries() throws Exception {
        Reader reader = new StringReader("# comment line\nATUBA1\tAT3G48560\tPBC1\tAT1G58230\nSTE7\tSTE5\tSTE14\tSTE12\tPHD1\n");
        Vector<String[]> queries = FileUtils.loadRecords(reader, '\t', '#');

        assertNotNull(queries);
        assertEquals(2, queries.size());
        assertEquals(4, queries.get(0).length);
        assertEquals(5, queries.get(1).length);

        assertEquals("ATUBA1", queries.get(0)[0]);
        assertEquals("PHD1", queries.get(1)[4]);

    }
}
