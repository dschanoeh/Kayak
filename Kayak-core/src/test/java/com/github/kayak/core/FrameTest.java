/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.kayak.core;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author dsi9mjn
 */
public class FrameTest {

    public FrameTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testFromLogFileNotation() {
        System.out.println("fromLogFileNotation");
        String line = "(1244101432.788973)  can1 040#4B0B000000000000";
        String busName = "";
        Frame.FrameBusNamePair result = Frame.fromLogFileNotation(line);

        assertEquals(1244101432788973L, result.getFrame().getTimestamp());
        assertEquals(0x40, result.getFrame().getIdentifier());
        assertEquals("can1", result.getBusName());
    }

    @Test
    public void testFromLogFileNotation2() {
        System.out.println("fromLogFileNotation2");
        String line = "(1244101432.830624) vcan2 7D3#6C00082E36560100";
        String busName = "";
        Frame.FrameBusNamePair result = Frame.fromLogFileNotation(line);

        assertEquals(1244101432830624L, result.getFrame().getTimestamp());
        assertEquals(0x7d3, result.getFrame().getIdentifier());
        assertEquals("vcan2", result.getBusName());
    }

    @Test
    public void testIsExtended() {
        System.out.println("isExtendedIdentifier");
        Frame instance = new Frame(0x456743, true,  new byte[] {});
        boolean result = instance.isExtended();
        assertEquals(true, result);
    }

    @Test
    public void testIsExtended2() {
        System.out.println("isExtendedIdentifier2");
        String line = "(1244101432.830624) vcan2 7D3#6C00082E36560100";
        Frame instance = Frame.fromLogFileNotation(line).getFrame();
        boolean result = instance.isExtended();
        assertEquals(false, result);
    }

    public void testIsExtended3() {
        System.out.println("isExtendedIdentifier3");
        String line = "(1244101432.830624) vcan2 000007D3#6C00082E36560100";
        Frame instance = Frame.fromLogFileNotation(line).getFrame();
        boolean result = instance.isExtended();
        assertEquals(true, result);
    }

    @Test
    public void patternTest() {
        System.out.println("patternTest");
        String line = "(1244101432.830624) vcan2 7D3#6C00082E36560100";
        boolean result = Frame.LogFileNotationPattern.matcher(line).matches();
        assertEquals(true, result);
    }
}
