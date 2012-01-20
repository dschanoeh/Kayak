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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jan-Niklas Meier < dschanoeh@googlemail.com >
 */
public class ExtendedMessageTest {

    static MessageDescription message;
    static MessageDescription extendedMessage;

    public ExtendedMessageTest() {

    }

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        message = new MessageDescription(42, false);
        extendedMessage = new MessageDescription(42, true);

        SignalDescription signal = message.createSignalDescription();
        signal.setType(SignalDescription.Type.UNSIGNED);
        signal.setLength(2);
        signal.setOffset(0);
        signal.setIntercept(0);
        signal.setSlope(1);

        SignalDescription extendedSignal = extendedMessage.createSignalDescription();
        extendedSignal.setType(SignalDescription.Type.UNSIGNED);
        extendedSignal.setLength(2);
        extendedSignal.setOffset(0);
        extendedSignal.setIntercept(0);
        extendedSignal.setSlope(1);
    }

    @Test
    public void test1() {
        try {
            Frame standardFrame = new Frame(42, false, new byte[] {0x11, 0x22});

            Message m = message.decodeFrame(standardFrame);
            assertNotNull(m);
            assertEquals(m.getSignals().size(),1);

            Message m2 = extendedMessage.decodeFrame(standardFrame);
            assertNull(m2);

        } catch (DescriptionException ex) {
            Logger.getLogger(ExtendedMessageTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Test
    public void test2() {
        try {
            Frame extendedFrame = new Frame(42, true, new byte[] {0x11, 0x22});

            Message m = message.decodeFrame(extendedFrame);
            assertNull(m);

            Message m2 = extendedMessage.decodeFrame(extendedFrame);
            assertNotNull(m2);
            assertEquals(m2.getSignals().size(),1);

        } catch (DescriptionException ex) {
            Logger.getLogger(ExtendedMessageTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
