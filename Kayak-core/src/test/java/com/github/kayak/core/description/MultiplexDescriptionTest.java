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
package com.github.kayak.core.description;

import com.github.kayak.core.Frame;
import java.nio.ByteOrder;
import java.util.Set;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jan-Niklas Meier < dschanoeh@googlemail.com >
 */
public class MultiplexDescriptionTest {

    MessageDescription messageDescription;
    MultiplexDescription description;
    SignalDescription signal1;

    public MultiplexDescriptionTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        messageDescription = new MessageDescription(0x12, false);

        description = messageDescription.createMultiplexDescription();
        description.setLength(3);
        description.setOffset(8);
        messageDescription.addMultiplex(description);

        signal1 = description.createMultiplexedSignal(0);
        signal1.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        signal1.setOffset(16);
        signal1.setLength(8);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testMultiplex() throws DescriptionException {
        Frame f = new Frame(0x12, false, new byte[] {(byte) 0xff, (byte) 0x00, (byte) 0x13});

        Message message = messageDescription.decodeFrame(f);
        assertNotNull(message);
        Set<Signal> signals = message.getSignals();

        boolean found = false;

        for(Signal s : signals) {
            if(s.getDescription() == signal1) {
                found = true;

                assertEquals(s.getRawValue(), 0x13);
            }
        }

        assertTrue(found);
    }

}
