/**
 * 	This file is part of Kayak.
 *
 *	Kayak is free software: you can redistribute it and/or modify
 *	it under the terms of the GNU Lesser General Public License as published by
 *	the Free Software Foundation, either version 3 of the License, or
 *	(at your option) any later version.
 *
 *	Kayak is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU General Public License for more details.
 *
 *	You should have received a copy of the GNU Lesser General Public License
 *	along with Kayak.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.github.kayak.ui.gaugeview;

import java.awt.Graphics;
import java.util.Properties;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jan-Niklas Meier <dschanoeh@googlemail.com>
 */
public class GaugeTopComponentTest {

    public GaugeTopComponentTest() {
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
    public void scaleTest() {
        double d = GaugeTopComponent.scaleElementsForRange(800f);

        assertEquals(100f, d, 0.1f);
    }

    @Test
    public void scaleTest2() {
        double d = GaugeTopComponent.scaleElementsForRange(1f);

        assertEquals(0.1f, d, 0.01f);
    }

    @Test
    public void scaleTest3() {
        double d = GaugeTopComponent.scaleElementsForRange(1900f);

        assertEquals(100f, d, 0.1f);
    }

    @Test
    public void scaleTest4() {
        double d = GaugeTopComponent.scaleElementsForRange(5000f);

        assertEquals(1000f, d, 1f);
    }
}
