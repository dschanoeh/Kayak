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
package com.github.kayak.core.controller;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.kayak.backend.*;

public class Application {
	Logger logger = Logger.getLogger("com.github.kayak.core.controller");
	
	public void start(){

		RAWConnection con = new RAWConnection("127.0.0.1", 28600, "vcan0");
		BCMConnection con2 = new BCMConnection("127.0.0.1", 28600, "vcan0");
		con.open();
		con2.open();
		
		Bus b = new Bus();
		b.connectTo(con);
		b.connectTo(con2);
		
		FrameReceiver f = new FrameReceiver() {

			@Override
			public void newFrame(Frame frame) {
				logger.log(Level.INFO, frame.toString());
			}
			
		};
		Subscription s = new Subscription(f, b);
		s.subscribe(649);
		//s.setSubscribeAll(true);
		b.addRAWSubscription(s);
        try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		b.sendFrame(new Frame(649, new byte[] {12,21}));
        con.close();
        con2.close();
	}
}
