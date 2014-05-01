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

package org.genemania.engine.core.evaluation;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

import org.genemania.engine.Utils;
import org.genemania.engine.core.KHeap;
import org.genemania.engine.core.evaluation.correlation.CorrelationFactory.CorrelationType;
import org.genemania.exception.ApplicationException;

import org.junit.Test;
import static org.junit.Assert.*;

public class ProfileToNetworkDriverTest {
    private static double testTolerance = .00001d;

    private static String namingFile =  
        "gm:00000,GENE0\n" +
        "gm:00001,GENE1\n" +
        "gm:00002,GENE2\n" +
        "gm:00003,GENE3\n" +
        "gm:00004,GENE4\n" +
        "gm:00005,GENE5\n" +
        "gm:00006,GENE6\n" +
        "gm:00004,GENE4_syn1\n" +
        "gm:00004,GENE4_syn2\n";

    /**
     * if you haven't tested that something is right, at least test
     * that it isn't changing
     * 
     * @throws Exception
     */
    @Test
    public void testRegressionPearson() throws Exception {
        String data = "header,feature1,feature2,feature3\n" +
        "gene1,0.5,0.5,0.5\n" +  // note this row has zero var, should not appear in output
        "gene2,0.2,0.1,0.3\n" +
        "gene3,0.8,0.9,0.8\n" +
        "gene4,0.1,0.3,0.4\n" +
        "gene5,0.8,0.2,0.3\n";

        StringReader reader = new StringReader(data);
        StringReader synonymSource = new StringReader(namingFile);
        StringWriter writer = new StringWriter();

        ProfileToNetworkDriver loader = new ProfileToNetworkDriver();
        loader.setCorrelationType( CorrelationType.PEARSON );
        loader.setK( 3 );
        loader.setSepChar(',');
        loader.setSynIdColumn(0);
        loader.setSynNameColumn(1);
        loader.setSynSepChar(',');
        loader.setSynReader(synonymSource);
        loader.setThreshold(ProfileToNetworkDriver.THRESHOLD_OFF);         
        loader.process(reader, writer);

        System.out.println(writer.toString());
        Map<String, Double> networkMap = Utils.networkToMap(new StringReader(writer.toString()), '\t');
        assertEquals(12, networkMap.size());
        assertEquals(0.327326d, networkMap.get("GENE4-GENE2"), testTolerance);
        assertEquals(0.155542d, networkMap.get("GENE5-GENE2"), testTolerance);
        assertEquals(0.188982d, networkMap.get("GENE4-GENE3"), testTolerance);
        assertEquals(-0.628618d, networkMap.get("GENE5-GENE3"), testTolerance);
        assertEquals(0.327326d, networkMap.get("GENE2-GENE4"), testTolerance);
        assertEquals(0.188982d, networkMap.get("GENE3-GENE4"), testTolerance);
        assertEquals(0.155542d, networkMap.get("GENE2-GENE5"), testTolerance);
        assertEquals(-0.628618d, networkMap.get("GENE3-GENE5"), testTolerance);
        assertEquals(-0.8825d, networkMap.get("GENE4-GENE5"), testTolerance);
        assertEquals(-0.8825d, networkMap.get("GENE5-GENE4"), testTolerance);
        assertEquals(-0.86603d, networkMap.get("GENE2-GENE3"), testTolerance);
        assertEquals(-0.86603d, networkMap.get("GENE3-GENE2"), testTolerance);
    }

    /*
     * negatives should be filtered out
     */
    @Test
    public void testRegressionPearsonThreshold() throws Exception {
        String data = "header,feature1,feature2,feature3\n" +
        "gene1,0.5,0.5,0.5\n" +
        "gene2,0.2,0.1,0.3\n" +
        "gene3,0.8,0.9,0.8\n" +
        "gene4,0.1,0.3,0.4\n" +
        "gene5,0.8,0.2,0.3\n";

        StringReader reader = new StringReader(data);
        StringReader synonymSource = new StringReader(namingFile);
        StringWriter writer = new StringWriter();

        ProfileToNetworkDriver loader = new ProfileToNetworkDriver();
        loader.setCorrelationType( CorrelationType.PEARSON );
        loader.setK( 3 );
        loader.setSepChar(',');
        loader.setSynIdColumn(0);
        loader.setSynNameColumn(1);
        loader.setSynSepChar(',');
        loader.setSynReader(synonymSource);
        loader.setThreshold(ProfileToNetworkDriver.THRESHOLD_AUTO);         
        loader.process(reader, writer);

        System.out.println(writer.toString());
        Map<String, Double> networkMap = Utils.networkToMap(new StringReader(writer.toString()), '\t');
        assertEquals(0.327326d, networkMap.get("GENE4-GENE2"), testTolerance);
        assertEquals(0.155542d, networkMap.get("GENE5-GENE2"), testTolerance);
        assertEquals(0.188982d, networkMap.get("GENE4-GENE3"), testTolerance);
        assertNull(networkMap.get("GENE5-GENE3"));
        assertEquals(0.327326d, networkMap.get("GENE2-GENE4"), testTolerance);
        assertEquals(0.188982d, networkMap.get("GENE3-GENE4"), testTolerance);
        assertEquals(0.155542d, networkMap.get("GENE2-GENE5"), testTolerance);
        assertNull(networkMap.get("GENE3-GENE5"));
    }

    @Test
    public void testNoIdentifierMapping() throws Exception {
        String data = "header,feature1,feature2,feature3\n" +
        "gene1,0.5,0.5,0.5\n" +
        "gene2,0.2,0.1,0.3\n" +
        "gene3,0.8,0.9,0.8\n" +
        "gene4,0.1,0.3,0.4\n" +
        "gene5,0.8,0.2,0.3\n";

        StringReader reader = new StringReader(data);
        StringWriter writer = new StringWriter();

        ProfileToNetworkDriver loader = new ProfileToNetworkDriver();
        loader.setCorrelationType( CorrelationType.PEARSON );
        loader.setK( 3 );
        loader.setSepChar(',');
        try {
            loader.process(reader, writer);
            fail("Should throw an ApplicationException because synFile isn't specified.");
        } catch( ApplicationException e ){
        }

    }

    @Test
    public void testMissingDataWithNames() throws Exception {
        String data = "header,feature1,feature2,feature3,feature4\n" +
        "GENE1,0.5,0.5,0.5,0.5\n" +
        "GENE2,0.2,0.1,0.3,0.4\n" +
        "GENE3,0.8,0.9,0.8,0.7\n" +
        "GENE4,0.1,0.3,0.4,0.5\n" +
        "GENE5,0.8,0.2,0.3,null\n";

        StringReader reader = new StringReader(data);
        StringWriter writer = new StringWriter();

        StringReader synonymSource = new StringReader(namingFile);

        ProfileToNetworkDriver loader = new ProfileToNetworkDriver();
        loader.setCorrelationType( CorrelationType.PEARSON );
        loader.setK(5);
        loader.setMaxMissingPercentage(90);
        loader.setSepChar(',');
        loader.setSynIdColumn(0);
        loader.setSynNameColumn(1);
        loader.setSynSepChar(',');
        loader.setSynReader(synonymSource);
        loader.setThreshold(ProfileToNetworkDriver.THRESHOLD_OFF);       
        loader.process(reader, writer);

        System.out.println(writer.toString());
        System.out.println(Utils.getMD5asHEXString(writer.toString()));
        Map<String, Double> networkMap = Utils.networkToMap(new StringReader(writer.toString()), '\t');
        assertEquals(6.80336e-01, networkMap.get("GENE4-GENE2"), testTolerance);
        assertEquals(-4.78091e-01, networkMap.get("GENE4-GENE3"), testTolerance);

        // the true correlations for gene5, with the NaN column ommitted, are below
        // however these are NOT our results. instead, since we first normalize
        // each gene not taking into account possible NaN's in the gene with which
        // it is paired in the correlations, the results differ		
        //assertEquals(1.555427542095638e-01, networkMap.get("GENE5-GENE2"), testTolerance);
        //assertEquals(-6.286185570937121e-01, networkMap.get("GENE5-GENE3"), testTolerance);
        //assertEquals(-8.824975032927697e-01, networkMap.get("GENE5-GENE4"), testTolerance);
        assertEquals(9.83738e-02, networkMap.get("GENE5-GENE2"), testTolerance);
        assertEquals(-3.62933e-01, networkMap.get("GENE5-GENE3"), testTolerance);
        assertEquals(-6.44485e-01, networkMap.get("GENE5-GENE4"), testTolerance);

        // check symmetry
        assertEquals(networkMap.get("GENE4-GENE5"), networkMap.get("GENE5-GENE4"));
        assertEquals(networkMap.get("GENE2-GENE5"), networkMap.get("GENE5-GENE2"));
        assertEquals(networkMap.get("GENE3-GENE5"), networkMap.get("GENE5-GENE3"));
        assertEquals(networkMap.get("GENE2-GENE4"), networkMap.get("GENE4-GENE2"));
        assertEquals(networkMap.get("GENE3-GENE4"), networkMap.get("GENE4-GENE3"));	
    }

    @Test
    public void testBinaryRegressionPearson() throws Exception {
        String data = 
            "header,feature1,feature2,feature3,feature4\n" +
            "GENE1,1,4,5\n" +
            "GENE2,2,3,4\n" +
            "GENE3,1,2,4,5\n" +
            "GENE4,0,3,5\n" +
            "GENE5,0,1,2,4,5\n";

        StringReader reader = new StringReader(data);
        StringReader synonymSource = new StringReader(namingFile);
        StringWriter writer = new StringWriter();

        ProfileToNetworkDriver loader = new ProfileToNetworkDriver();
        loader.setCorrelationType( CorrelationType.PEARSON_BIN_LOG_NO_NORM );
        loader.setK( 5 );
        loader.setSepChar(',');
        loader.setProfileType(ProfileToNetworkDriver.BINARY);
        loader.setSynIdColumn(0);
        loader.setSynNameColumn(1);
        loader.setSynSepChar(',');
        loader.setSynReader(synonymSource);
        loader.setThreshold(ProfileToNetworkDriver.THRESHOLD_OFF);
        loader.process(reader, writer);

        System.out.println("Sparse Binary - Pearson");
        System.out.println(writer);

        Map<String, Double> networkMap = Utils.networkToMap(new StringReader(writer.toString()), '\t');
        assertEquals(20, networkMap.size());

        assertEquals(4.143497580133637e-01, networkMap.get("GENE3-GENE1"), testTolerance);
        assertEquals(-8.739482955705147e-01, networkMap.get("GENE4-GENE1"), testTolerance);
        assertEquals(-3.146578445431611e-01, networkMap.get("GENE5-GENE1"), testTolerance);
        assertEquals(-7.235865804365513e-01, networkMap.get("GENE3-GENE2"), testTolerance);
        assertEquals(2.477645851518018e-02, networkMap.get("GENE4-GENE2"), testTolerance);
        assertEquals(4.143497580133637e-01, networkMap.get("GENE5-GENE3"), testTolerance);
        assertEquals(-8.739482955705151e-01, networkMap.get("GENE5-GENE4"), testTolerance);

        // symmetry
        assertEquals(networkMap.get("GENE1-GENE3"), networkMap.get("GENE3-GENE1"), testTolerance);
        assertEquals(networkMap.get("GENE1-GENE4"), networkMap.get("GENE4-GENE1"), testTolerance);
        assertEquals(networkMap.get("GENE1-GENE5"), networkMap.get("GENE5-GENE1"), testTolerance);
        assertEquals(networkMap.get("GENE2-GENE3"), networkMap.get("GENE3-GENE2"), testTolerance);
        assertEquals(networkMap.get("GENE2-GENE4"), networkMap.get("GENE4-GENE2"), testTolerance);
        assertEquals(networkMap.get("GENE3-GENE5"), networkMap.get("GENE5-GENE3"), testTolerance);
        assertEquals(networkMap.get("GENE4-GENE5"), networkMap.get("GENE5-GENE4"), testTolerance);
    }

    @Test
    public void testRegressionSpearman() throws Exception {

        String data = "header,feature1,feature2,feature3\n" +
        "gene1,0.5,0.5,0.5\n" +
        "gene2,0.2,0.1,0.3\n" +
        "gene3,0.8,0.9,0.8\n" +
        "gene4,0.1,0.3,0.4\n" +
        "gene5,0.8,0.2,0.3\n";

        StringReader reader = new StringReader(data);
        StringReader synonymSource = new StringReader(namingFile);
        StringWriter writer = new StringWriter();

        ProfileToNetworkDriver loader = new ProfileToNetworkDriver();
        loader.setCorrelationType( CorrelationType.SPEARMAN );
        loader.setK( 5 );
        loader.setSepChar(',');
        loader.setSynIdColumn(0);
        loader.setSynNameColumn(1);
        loader.setSynSepChar(',');
        loader.setSynReader(synonymSource);
        loader.setThreshold(ProfileToNetworkDriver.THRESHOLD_OFF);
        loader.process(reader, writer);

        System.out.println(writer.toString());
        Map<String, Double> networkMap = Utils.networkToMap(new StringReader(writer.toString()), '\t');
        assertEquals(-0.866025d, networkMap.get("GENE3-GENE2"), testTolerance);
        assertEquals(0.50d, networkMap.get("GENE4-GENE2"), testTolerance);
        assertEquals(0.50d, networkMap.get("GENE5-GENE2"), testTolerance);
        assertEquals(-0.866025d, networkMap.get("GENE2-GENE3"), testTolerance);
        assertEquals(-0.866025d, networkMap.get("GENE5-GENE3"), testTolerance);
        assertEquals(0.50d, networkMap.get("GENE2-GENE4"), testTolerance);
        assertEquals(-0.50d, networkMap.get("GENE5-GENE4"), testTolerance);
        assertEquals(0.50d, networkMap.get("GENE2-GENE5"), testTolerance);
        assertEquals(-0.866025d, networkMap.get("GENE3-GENE5"), testTolerance);
        assertEquals(-0.50d, networkMap.get("GENE4-GENE5"), testTolerance);
    }

    @Test
    public void testRegressionPearsonColRank() throws Exception {

        String data = "header,feature1,feature2,feature3\n" +
        "gene1,0.5,0.5,0.5\n" +
        "gene2,0.2,0.1,0.3\n" +
        "gene3,0.8,0.9,0.8\n" +
        "gene4,0.1,0.3,0.4\n" +
        "gene5,0.8,0.2,0.3\n";

        StringReader reader = new StringReader(data);
        StringReader synonymSource = new StringReader(namingFile);
        StringWriter writer = new StringWriter();

        ProfileToNetworkDriver loader = new ProfileToNetworkDriver();
        loader.setCorrelationType( CorrelationType.PEARSON_RANK );
        loader.setK( 5 );
        loader.setSepChar(',');
        loader.setSynIdColumn(0);
        loader.setSynNameColumn(1);
        loader.setSynSepChar(',');
        loader.setSynReader(synonymSource);
        loader.setThreshold(ProfileToNetworkDriver.THRESHOLD_OFF);		
        loader.process(reader, writer);

        System.out.println(writer.toString());
        Map<String, Double> networkMap = Utils.networkToMap(new StringReader(writer.toString()), '\t');
        assertEquals(-0.866025d, networkMap.get("GENE2-GENE1"), testTolerance);
        assertEquals(1.0d, networkMap.get("GENE3-GENE1"), testTolerance);
        assertEquals(1.0d, networkMap.get("GENE4-GENE1"), testTolerance);
        assertEquals(-0.987829d, networkMap.get("GENE5-GENE1"), testTolerance);
        assertEquals(-0.866025d, networkMap.get("GENE1-GENE2"), testTolerance);
        assertEquals(-0.866025d, networkMap.get("GENE3-GENE2"), testTolerance);
        assertEquals(-0.866025d, networkMap.get("GENE4-GENE2"), testTolerance);
        assertEquals(0.777713d, networkMap.get("GENE5-GENE2"), testTolerance);
        assertEquals(1.0d, networkMap.get("GENE1-GENE3"), testTolerance);
        assertEquals(-0.866025d, networkMap.get("GENE2-GENE3"), testTolerance);
        assertEquals(1.0d, networkMap.get("GENE4-GENE3"), testTolerance);
        assertEquals(-0.987829d, networkMap.get("GENE5-GENE3"), testTolerance);
        assertEquals(1.0d, networkMap.get("GENE1-GENE4"), testTolerance);
        assertEquals(-0.866025d, networkMap.get("GENE2-GENE4"), testTolerance);
        assertEquals(1.0d, networkMap.get("GENE3-GENE4"), testTolerance);
        assertEquals(-0.987829d, networkMap.get("GENE5-GENE4"), testTolerance);
        assertEquals(-0.987829d, networkMap.get("GENE1-GENE5"), testTolerance);
        assertEquals(0.777714d, networkMap.get("GENE2-GENE5"), testTolerance);
        assertEquals(-0.987829d, networkMap.get("GENE3-GENE5"), testTolerance);
        assertEquals(-0.987829d, networkMap.get("GENE4-GENE5"), testTolerance);
    }

    @Test
    public void testRegressionMI() throws Exception {
        String data = "header,feature1,feature2,feature3\n" +
        "gene1,0,4,0,8,0,4,4,0,4,0,4,0,8,4,0,0,4,0,4,0\n" +
        "gene2,0,4,4,8,0,4,4,4,4,0,4,4,8,4,0,0,4,0,4,0\n" +
        "gene3,8,8,8,0,4,4,8,8,0,8,4,4,0,4,8,4,8,8,4,8\n";

        StringReader reader = new StringReader(data);
        StringReader synonymSource = new StringReader(namingFile);
        StringWriter writer = new StringWriter();

        ProfileToNetworkDriver loader = new ProfileToNetworkDriver();
        loader.setCorrelationType( CorrelationType.MUTUAL_INFORMATION );
        loader.setK( 5 );
        loader.setSepChar(',');
        loader.setSynIdColumn(0);
        loader.setSynNameColumn(1);
        loader.setSynSepChar(',');
        loader.setSynReader(synonymSource);
        loader.setThreshold(ProfileToNetworkDriver.THRESHOLD_OFF);		
        loader.process(reader, writer);

        System.out.println("MI");
        System.out.println(writer.toString());
        Map<String, Double> networkMap = Utils.networkToMap(new StringReader(writer.toString()), '\t');
        assertEquals(6, networkMap.size()); 
        assertEquals(0.437744d, networkMap.get("GENE3-GENE1"), testTolerance);
        assertEquals(0.437744d, networkMap.get("GENE1-GENE3"), testTolerance);
        assertEquals(0.896021d, networkMap.get("GENE1-GENE2"), testTolerance);
        assertEquals(0.896021d, networkMap.get("GENE2-GENE1"), testTolerance);
        assertEquals(0.396830d, networkMap.get("GENE2-GENE3"), testTolerance);
        assertEquals(0.396829d, networkMap.get("GENE3-GENE2"), testTolerance);
    }

    @Test
    public void testRegressionMI_Rank() throws Exception {

        String data = "header,feature1,feature2,feature3\n" +
        "gene0,6,5,9,13,11,1,15,4,19,7,10,18,16,2,14,17,8,12,3,0\n" +
        "gene1,18,15,16,7,13,8,6,19,12,14,4,2,10,5,17,1,0,3,11,9\n" +
        "gene2,6,18,12,11,7,17,15,14,8,1,9,19,5,16,0,10,13,4,2,3\n";

        StringReader reader = new StringReader(data);
        StringReader synonymSource = new StringReader(namingFile);
        StringWriter writer = new StringWriter();

        ProfileToNetworkDriver loader = new ProfileToNetworkDriver();
        loader.setCorrelationType( CorrelationType.MUTUAL_INFORMATION );
        loader.setK( 2 );
        loader.setSepChar(',');
        loader.setSynIdColumn(0);
        loader.setSynNameColumn(1);
        loader.setSynSepChar(',');
        loader.setSynReader(synonymSource);
        loader.setEqualElementBin(true);
        loader.setThreshold(ProfileToNetworkDriver.THRESHOLD_OFF);		
        loader.process(reader, writer);

        System.out.println(writer.toString());
        Map<String, Double> networkMap = Utils.networkToMap(new StringReader(writer.toString()), '\t');
        assertEquals(6, networkMap.size()); 
        assertEquals(0.453561d, networkMap.get("GENE0-GENE1"), testTolerance);
        assertEquals(0.453561d, networkMap.get("GENE1-GENE0"), testTolerance);
        assertEquals(0.453561d, networkMap.get("GENE1-GENE2"), testTolerance);
        assertEquals(0.453561d, networkMap.get("GENE2-GENE1"), testTolerance);
        assertEquals(0.378072d, networkMap.get("GENE2-GENE0"), testTolerance);
        assertEquals(0.378072d, networkMap.get("GENE0-GENE2"), testTolerance);
    }

    @Test
    public void testRegressionMI_Binary() throws Exception {

        String data = "header,feature1,feature2,feature3\n" +
        "gene0,2,4,6,7,9,11\n" +
        "gene1,1,2,3,5,6,7,8,10,11,12\n";
        //		"gene0,0,1,0,1,0,1,1,0,1,0,1,0\n" +
        //		"gene1,1,1,1,0,1,1,1,1,0,1,1,1\n";

        StringReader reader = new StringReader(data);
        StringReader synonymSource = new StringReader(namingFile);
        StringWriter writer = new StringWriter();

        ProfileToNetworkDriver loader = new ProfileToNetworkDriver();
        loader.setCorrelationType( CorrelationType.MUTUAL_INFORMATION );
        loader.setK( 5 );
        loader.setSepChar(',');
        loader.setSynIdColumn(0);
        loader.setSynNameColumn(1);
        loader.setSynSepChar(',');
        loader.setSynReader(synonymSource);
        loader.setProfileType("bin");
        loader.setThreshold(ProfileToNetworkDriver.THRESHOLD_OFF);		
        loader.process(reader, writer);

        System.out.println(writer.toString());
        Map<String, Double> networkMap = Utils.networkToMap(new StringReader(writer.toString()), '\t');
        assertEquals(2, networkMap.size()); 
        assertEquals(0.190875d, networkMap.get("GENE0-GENE1"), testTolerance);
        assertEquals(0.190875d, networkMap.get("GENE1-GENE0"), testTolerance);
    }

    @Test
    public void testRegressionPearsonRank_Binary() throws Exception {

        String dataSparse = "header,feature1,feature2,feature3\n" +
        "gene1,1,2,3,4,5,6\n" +
        "gene0,2,4,6\n";

        String dataDense = "header,feature1,feature2,feature3\n" +
        "gene1,1,1,1,1,1,1\n" +
        "gene0,0,1,0,1,0,1\n";

        String dataDensePearson = "header,feature1,feature2,feature3\n" +
        "gene1,2,1.5,2,1.5,2,1.5\n" +
        "gene0,1,1.5,1,1.5,1,1.5\n";

        StringReader readerD = new StringReader(dataDense);
        StringReader synonymSourceD = new StringReader(namingFile);
        StringWriter writerD = new StringWriter();

        ProfileToNetworkDriver loaderD = new ProfileToNetworkDriver();
        loaderD.setCorrelationType( CorrelationType.PEARSON_RANK );
        loaderD.setK( 5 );
        loaderD.setSepChar(',');
        loaderD.setSynIdColumn(0);
        loaderD.setSynNameColumn(1);
        loaderD.setSynSepChar(',');
        loaderD.setSynReader(synonymSourceD);
        loaderD.setThreshold(ProfileToNetworkDriver.THRESHOLD_OFF);		
        loaderD.process(readerD, writerD);

        System.out.println(writerD.toString());
        Map<String, Double> networkMapD = Utils.networkToMap(new StringReader(writerD.toString()), '\t');


        StringReader readerS = new StringReader(dataSparse);
        StringReader synonymSourceS = new StringReader(namingFile);
        StringWriter writerS = new StringWriter();

        ProfileToNetworkDriver loaderS = new ProfileToNetworkDriver();
        loaderS.setCorrelationType( CorrelationType.PEARSON_RANK );
        loaderS.setK( 5 );
        loaderS.setSepChar(',');
        loaderS.setSynIdColumn(0);
        loaderS.setSynNameColumn(1);
        loaderS.setSynSepChar(',');
        loaderS.setSynReader(synonymSourceS);
        loaderS.setProfileType("bin");
        loaderS.setThreshold(ProfileToNetworkDriver.THRESHOLD_OFF);		
        loaderS.process(readerS, writerS);

        System.out.println(writerS.toString());
        Map<String, Double> networkMapS = Utils.networkToMap(new StringReader(writerS.toString()), '\t');


        StringReader readerP = new StringReader(dataDensePearson);
        StringReader synonymSourceP = new StringReader(namingFile);
        StringWriter writerP = new StringWriter();

        ProfileToNetworkDriver loaderP = new ProfileToNetworkDriver();
        loaderP.setCorrelationType( CorrelationType.PEARSON );
        loaderP.setK( 5 );
        loaderP.setSepChar(',');
        loaderP.setSynIdColumn(0);
        loaderP.setSynNameColumn(1);
        loaderP.setSynSepChar(',');
        loaderP.setSynReader(synonymSourceP);
        loaderP.setThreshold(ProfileToNetworkDriver.THRESHOLD_OFF);		
        loaderP.process(readerP, writerP);

        System.out.println(writerP.toString());
        Map<String, Double> networkMapP = Utils.networkToMap(new StringReader(writerP.toString()), '\t');


        assertEquals(networkMapP.get("GENE1-GENE0"), networkMapD.get("GENE1-GENE0"), testTolerance);

        assertEquals(networkMapS.get("GENE0-GENE1"), networkMapD.get("GENE0-GENE1"), testTolerance);
        assertEquals(networkMapS.get("GENE1-GENE0"), networkMapD.get("GENE1-GENE0"), testTolerance);

    }

    @Test
    public void testRegressionSpearman_Binary() throws Exception {

        String dataSparse = "header,feature1,feature2,feature3\n" +
        "gene1,1,2,3,4,5\n" +
        "gene0,2,4,6\n";

        String dataDense = "header,feature1,feature2,feature3\n" +
        "gene1,1,1,1,1,1,0\n" +
        "gene0,0,1,0,1,0,1\n";

        String dataDensePearson = "header,feature1,feature2,feature3\n" +
        "gene1,4,4,4,4,4,1\n" +
        "gene0,2,5,2,5,2,5\n";

        StringReader readerD = new StringReader(dataDense);
        StringReader synonymSourceD = new StringReader(namingFile);
        StringWriter writerD = new StringWriter();

        ProfileToNetworkDriver loaderD = new ProfileToNetworkDriver();
        loaderD.setCorrelationType( CorrelationType.SPEARMAN );
        loaderD.setK( 5 );
        loaderD.setSepChar(',');
        loaderD.setSynIdColumn(0);
        loaderD.setSynNameColumn(1);
        loaderD.setSynSepChar(',');
        loaderD.setSynReader(synonymSourceD);
        loaderD.setThreshold(ProfileToNetworkDriver.THRESHOLD_OFF);		
        loaderD.process(readerD, writerD);

        System.out.println(writerD.toString());
        Map<String, Double> networkMapD = Utils.networkToMap(new StringReader(writerD.toString()), '\t');

        StringReader readerS = new StringReader(dataSparse);
        StringReader synonymSourceS = new StringReader(namingFile);
        StringWriter writerS = new StringWriter();

        ProfileToNetworkDriver loaderS = new ProfileToNetworkDriver();
        loaderS.setCorrelationType( CorrelationType.SPEARMAN );
        loaderS.setK( 5 );
        loaderS.setSepChar(',');
        loaderS.setSynIdColumn(0);
        loaderS.setSynNameColumn(1);
        loaderS.setSynSepChar(',');
        loaderS.setSynReader(synonymSourceS);
        loaderS.setProfileType("bin");
        loaderS.setThreshold(ProfileToNetworkDriver.THRESHOLD_OFF);		
        loaderS.process(readerS, writerS);

        System.out.println(writerS.toString());
        Map<String, Double> networkMapS = Utils.networkToMap(new StringReader(writerS.toString()), '\t');

        StringReader readerP = new StringReader(dataDensePearson);
        StringReader synonymSourceP = new StringReader(namingFile);
        StringWriter writerP = new StringWriter();

        ProfileToNetworkDriver loaderP = new ProfileToNetworkDriver();
        loaderP.setCorrelationType( CorrelationType.PEARSON );
        loaderP.setK( 5 );
        loaderP.setSepChar(',');
        loaderP.setSynIdColumn(0);
        loaderP.setSynNameColumn(1);
        loaderP.setSynSepChar(',');
        loaderP.setSynReader(synonymSourceP);
        loaderP.setThreshold(ProfileToNetworkDriver.THRESHOLD_OFF);		
        loaderP.process(readerP, writerP);

        System.out.println(writerP.toString());
        Map<String, Double> networkMapP = Utils.networkToMap(new StringReader(writerP.toString()), '\t');


        assertEquals(networkMapP.get("GENE1-GENE0"), networkMapD.get("GENE1-GENE0"), testTolerance);
        assertEquals(networkMapS.get("GENE0-GENE1"), networkMapD.get("GENE0-GENE1"), testTolerance);
        assertEquals(networkMapS.get("GENE1-GENE0"), networkMapD.get("GENE1-GENE0"), testTolerance);
    }

    // TODO: this testcase seems to be broken (no gene records found, something with the naming file?).
    // fix, and also update to include missing values, and explicit NaN's
    @Test
    public void testRegressionPearsonReal() throws Exception {

        String data = "header	f1	f2	f3	f4	f5	f6	f7	f8	f9	f10	f11	F12	f13	f14	f15	f16	f17	f18	f19	f20	f21	f22	f23	f24\n" +
        "YJRWDELTA20	10.900	11.500	10.600	9.700	10.600	12.800	15.900	12.600	11.300	9.200	8.000	7.100	6.700	5.300	11.200	12.300	10.600	7.100	16.300	15.900	6.800	11.500	16.500	9.400\n" +
        "SPE1	116.500	77.000	61.100	119.000	101.300	89.600	212.900	192.000	136.000	93.200	92.000	128.200	78.700	45.700	70.600	134.500	116.700	70.600	134.500	103.100	126.300	95.900	133.100	107.800\n" +
        "CEN10	0.100	0.100		0.100	0.100		4.400	0.200		0.100	0.200		0.100		2.800			2.700	3.600	3.000	0.100	3.400	2.900	0.100\n";

        StringReader reader = new StringReader(data);
        StringReader synonymSource = new StringReader(namingFile);
        StringWriter writer = new StringWriter();

        ProfileToNetworkDriver loader = new ProfileToNetworkDriver();
        loader.setCorrelationType( CorrelationType.PEARSON );
        loader.setK( 5 );
        loader.setSepChar('\t');
        loader.setSynIdColumn(0);
        loader.setSynNameColumn(1);
        loader.setSynSepChar(',');
        loader.setSynReader(synonymSource);
        loader.setThreshold(ProfileToNetworkDriver.THRESHOLD_OFF);		
        loader.process(reader, writer);

        System.out.println("gaga");
        System.out.println(writer.toString());
    }

    @Test
    public void testRepeatedGene() throws Exception {		
        String data = "header,feature1,feature2,feature3\n" +
        "gene1,0.5,0.5,0.5\n" +
        "gene2,0.2,0.1,0.3\n" +
        "gene3,0.8,0.9,0.8\n" +
        "gene4,0.1,0.5,0.4\n" +
        "gene5,0.8,0.2,0.3\n" +
        "gene4,0.1,0.1,0.4\n";

        StringReader reader = new StringReader(data);
        StringWriter writer = new StringWriter();
        StringReader synonymSource = new StringReader(namingFile);

        ProfileToNetworkDriver loader = new ProfileToNetworkDriver();
        loader.setK(3);
        loader.setCorrelationType( CorrelationType.PEARSON );
        loader.setSepChar(',');
        loader.setSynIdColumn(0);
        loader.setSynNameColumn(1);
        loader.setSynSepChar(',');
        loader.setSynReader(synonymSource);
        loader.setThreshold(ProfileToNetworkDriver.THRESHOLD_OFF);
        loader.process(reader, writer);

        String data1 = "header,feature1,feature2,feature3\n" +
        "gene1,0.5,0.5,0.5\n" +
        "gene2,0.2,0.1,0.3\n" +
        "gene3,0.8,0.9,0.8\n" +
        "gene4,0.1,0.5,0.4\n" +
        "gene5,0.8,0.2,0.3\n" +
        "gene6,0.1,0.1,0.4\n";

        StringReader reader1 = new StringReader(data1);
        StringWriter writer1 = new StringWriter();
        StringReader synonymSource1 = new StringReader(namingFile);

        ProfileToNetworkDriver loader1 = new ProfileToNetworkDriver();
        loader1.setK(5);
        loader1.setCorrelationType( CorrelationType.PEARSON );
        loader1.setSepChar(',');
        loader1.setSynIdColumn(0);
        loader1.setSynNameColumn(1);
        loader1.setSynSepChar(',');
        loader1.setSynReader(synonymSource1);
        loader1.setThreshold(ProfileToNetworkDriver.THRESHOLD_OFF);
        loader1.process(reader1, writer1);		

        System.out.println(writer.toString());
        Map<String, Double> networkMap = Utils.networkToMap(new StringReader(writer.toString()), '\t');
        Map<String, Double> networkMap1 = Utils.networkToMap(new StringReader(writer1.toString()), '\t');
        assertEquals((networkMap1.get("GENE4-GENE2") + networkMap1.get("GENE6-GENE2"))/2,
                networkMap.get("GENE4-GENE2"), testTolerance);
        assertEquals(0.155542d, networkMap.get("GENE5-GENE2"), testTolerance);
        assertEquals((networkMap1.get("GENE4-GENE3") + networkMap1.get("GENE6-GENE3"))/2,
                networkMap.get("GENE4-GENE3"), testTolerance);
        assertEquals(-0.628618d, networkMap.get("GENE5-GENE3"), testTolerance);
        assertEquals((networkMap1.get("GENE2-GENE4") + networkMap1.get("GENE2-GENE6"))/2,
                networkMap.get("GENE2-GENE4"), testTolerance);
        assertEquals((networkMap1.get("GENE3-GENE4") + networkMap1.get("GENE3-GENE6"))/2,
                networkMap.get("GENE3-GENE4"), testTolerance);
        assertEquals(0.155542d, networkMap.get("GENE2-GENE5"), testTolerance);
        assertEquals(-0.628618d, networkMap.get("GENE3-GENE5"), testTolerance);
    }


    @Test
    public void testRepeatedGeneSynonymWithNames() throws Exception {	
        // the exact same data record is repeated but with a different, 
        // synonym. averaging should not affect the result
        String data = "header,feature1,feature2,feature3\n" +
        "GENE1,0.5,0.5,0.5\n" +
        "GENE2,0.2,0.1,0.3\n" +
        "GENE3,0.8,0.9,0.8\n" +
        "GENE4,0.1,0.3,0.4\n" +
        "GENE5,0.8,0.2,0.3\n" +
        "GENE4_syn1,0.1,0.3,0.4\n";

        StringReader reader = new StringReader(data);
        StringWriter writer = new StringWriter();

        StringReader synonymSource = new StringReader(namingFile);

        ProfileToNetworkDriver loader = new ProfileToNetworkDriver();

        loader.setK(3);
        loader.setCorrelationType( CorrelationType.PEARSON );
        loader.setSepChar(',');
        loader.setSynIdColumn(0);
        loader.setSynNameColumn(1);
        loader.setSynSepChar(',');
        loader.setSynReader(synonymSource);
        loader.setThreshold(ProfileToNetworkDriver.THRESHOLD_OFF);
        loader.process(reader, writer);

        System.out.println(writer.toString());
        Map<String, Double> networkMap = Utils.networkToMap(new StringReader(writer.toString()), '\t');
        assertEquals(0.327326d, networkMap.get("GENE4-GENE2"), testTolerance);
        assertEquals(0.155542d, networkMap.get("GENE5-GENE2"), testTolerance);
        assertEquals(0.188982d, networkMap.get("GENE4-GENE3"), testTolerance);
        assertEquals(-0.628618d, networkMap.get("GENE5-GENE3"), testTolerance);
        assertEquals(0.327326d, networkMap.get("GENE2-GENE4"), testTolerance);
        assertEquals(0.188982d, networkMap.get("GENE3-GENE4"), testTolerance);
        assertEquals(0.155542d, networkMap.get("GENE2-GENE5"), testTolerance);
        assertEquals(-0.628618d, networkMap.get("GENE3-GENE5"), testTolerance);
    }

    @Test
    public void testRepeatedGeneSynonymWithNamesDifferingValues() throws Exception {		
        String data = "header,feature1,feature2,feature3\n" +
        "GENE1,0.5,0.5,0.5\n" +
        "GENE2,0.2,0.1,0.3\n" +
        "GENE3,0.8,0.9,0.8\n" +
        "GENE4,0.1,0.5,0.4\n" +
        "GENE5,0.8,0.2,0.3\n" +
        "GENE4_syn1,0.1,0.1,0.4\n";

        StringReader reader = new StringReader(data);
        StringWriter writer = new StringWriter();

        StringReader synonymSource = new StringReader(namingFile);

        ProfileToNetworkDriver loader = new ProfileToNetworkDriver();
        loader.setK(4);
        loader.setCorrelationType( CorrelationType.PEARSON );
        loader.setSepChar(',');
        loader.setSynIdColumn(0);
        loader.setSynNameColumn(1);
        loader.setSynSepChar(',');
        loader.setSynReader(synonymSource);
        loader.setThreshold(ProfileToNetworkDriver.THRESHOLD_OFF);
        loader.process(reader, writer);

        System.out.println(writer.toString());
        Map<String, Double> networkMap = Utils.networkToMap(new StringReader(writer.toString()), '\t');
        assertEquals(0.312916d, networkMap.get("GENE4-GENE2"), testTolerance);
        assertEquals(0.155542d, networkMap.get("GENE5-GENE2"), testTolerance);
        assertEquals(9.66876e-02, networkMap.get("GENE4-GENE3"), testTolerance);
        assertEquals(-0.628618d, networkMap.get("GENE5-GENE3"), testTolerance);
        assertEquals(0.312916d, networkMap.get("GENE2-GENE4"), testTolerance);
        assertEquals(9.66876e-02, networkMap.get("GENE3-GENE4"), testTolerance);
        assertEquals(0.155542d, networkMap.get("GENE2-GENE5"), testTolerance);
        assertEquals(-0.628618d, networkMap.get("GENE3-GENE5"), testTolerance);
        assertEquals(-6.77741e-01, networkMap.get("GENE4-GENE5"), testTolerance);
        assertEquals(-6.77741e-01, networkMap.get("GENE5-GENE4"), testTolerance);
        assertEquals(-8.66025e-01, networkMap.get("GENE2-GENE3"), testTolerance);
        assertEquals(-8.66025e-01, networkMap.get("GENE3-GENE2"), testTolerance);
    }
    @Test
    public void testBinaryNetworkPearsonLogNoNorm() throws Exception {

        String data =
            "header\n" +
            "GENE1,GENE2,1\n" +
            "GENE3,GENE4,1\n" +
            "GENE3,GENE5,1\n" +
            "GENE4,GENE5,1\n";

        StringReader reader = new StringReader(data);
        StringReader synonymSource = new StringReader(namingFile);
        StringWriter writer = new StringWriter();

        ProfileToNetworkDriver loader = new ProfileToNetworkDriver();
        loader.setCorrelationType( CorrelationType.PEARSON_BIN_LOG_NO_NORM );
        loader.setK( 5 );
        loader.setSepChar(',');
        loader.setProfileType(ProfileToNetworkDriver.NETWORK);
        loader.setSynIdColumn(0);
        loader.setSynNameColumn(1);
        loader.setSynSepChar(',');
        loader.setSynReader(synonymSource);
        loader.setThreshold(ProfileToNetworkDriver.THRESHOLD_OFF);
        loader.process(reader, writer);

        System.out.println("Binary Network - Pearson");
        System.out.println(writer);
        /*
		Map<String, Double> networkMap = Utils.networkToMap(new StringReader(writer.toString()), '\t');
		assertEquals(4.143497580133637e-01, networkMap.get("GENE3-GENE1"), testTolerance);
		assertEquals(-8.739482955705147e-01, networkMap.get("GENE4-GENE1"), testTolerance);
		assertEquals(-3.146578445431611e-01, networkMap.get("GENE5-GENE1"), testTolerance);
		assertEquals(-7.235865804365513e-01, networkMap.get("GENE3-GENE2"), testTolerance);
		assertEquals(2.477645851518018e-02, networkMap.get("GENE4-GENE2"), testTolerance);
		assertEquals(4.143497580133637e-01, networkMap.get("GENE5-GENE3"), testTolerance);
		assertEquals(-8.739482955705151e-01, networkMap.get("GENE5-GENE4"), testTolerance);

		// symmetry
		assertEquals(networkMap.get("GENE1-GENE3"), networkMap.get("GENE3-GENE1"), testTolerance);
		assertEquals(networkMap.get("GENE1-GENE4"), networkMap.get("GENE4-GENE1"), testTolerance);
		assertEquals(networkMap.get("GENE1-GENE5"), networkMap.get("GENE5-GENE1"), testTolerance);
		assertEquals(networkMap.get("GENE2-GENE3"), networkMap.get("GENE3-GENE2"), testTolerance);
		assertEquals(networkMap.get("GENE2-GENE4"), networkMap.get("GENE4-GENE2"), testTolerance);
		assertEquals(networkMap.get("GENE3-GENE5"), networkMap.get("GENE5-GENE3"), testTolerance);
		assertEquals(networkMap.get("GENE4-GENE5"), networkMap.get("GENE5-GENE4"), testTolerance);
         */
    }

    /*
     * thresholding applied to log-freq on sparse binary data
     * reduces the # of interactions reported
     */
    @Test
    public void testSparseBinaryRegressionThresholded() throws Exception {
        String data = 
            "header\n" +
            "GENE1,1,5\n" +
            "GENE2,3,4\n" +
            "GENE3,2,4\n" +
            "GENE4,0,3,5\n" +
            "GENE5,0,1,2\n" +
            "GENE6,6\n" +
            "GENE7,7\n" +
            "GENE8,8\n" +
            "GENE9,9\n" +
            "GENE10,10\n";

        StringReader reader = new StringReader(data);
        StringReader synonymSource = new StringReader(namingFile);
        StringWriter writer = new StringWriter();

        ProfileToNetworkDriver loader = new ProfileToNetworkDriver();
        loader.setCorrelationType( CorrelationType.PEARSON_BIN_LOG_NO_NORM );
        loader.setK( 5 );
        loader.setSepChar(',');
        loader.setProfileType(ProfileToNetworkDriver.BINARY);
        loader.setSynIdColumn(0);
        loader.setSynNameColumn(1);
        loader.setSynSepChar(',');
        loader.setSynReader(synonymSource);
        loader.setThreshold(ProfileToNetworkDriver.THRESHOLD_AUTO);
        loader.setKeepAllTies(true);
        loader.setLimitTies(false);
        loader.process(reader, writer);

        System.out.println("Sparse Binary");
        System.out.println(writer);

        Map<String, Double> networkMap = Utils.networkToMap(new StringReader(writer.toString()), '\t');
        assertEquals(12, networkMap.size()); // without threshold, get 34 interactions for this data
        assertNull(networkMap.get("GENE1-GENE3")); // no features in common, shouldn't appear. if you turn threshold off it does!
    }

    @Test
    public void testLevelControl() throws Exception {
        StringBuilder synonyms = new StringBuilder();
        int totalUniqueNodes = 100;

        for (int i = 0; i < totalUniqueNodes; i++) {
            synonyms.append(String.format("gene%1$d,gene%1$d", i));
        }

        ProfileToNetworkDriver loader = new ProfileToNetworkDriver();
        int k = 50;
        loader.setK(k);
        loader.setKeepAllTies(true);
        loader.setLimitTies(true);
        loader.setSynIdColumn(0);
        loader.setSynNameColumn(1);
        loader.setSynSepChar(',');
        loader.setSynReader(new StringReader(synonyms.toString()));

        KHeap heap = new KHeap(k);
        KHeap[] heaps = new KHeap[1];
        heaps[0] = heap;

        for (int i = 0; i < totalUniqueNodes; i++) {
            heap.offer(i, i);
        }

        loader.levelControl(heaps);
        assertEquals(k, heap.size());
    }
}
