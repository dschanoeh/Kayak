/**
 *  This file is part of Kayak.
 *
 *  Kayak is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as
 *  published by the Free Software Foundation, either version 3 of
 *  the License, or (at your option) any later version.
 *
 *  Kayak is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 *  GNU General Public License for more details.
 *  
 *	You should have received a copy of the GNU Lesser General Public
 *  License along with Kayak. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.github.kayak.canio.kcd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author julietkilo
 * 
 */
public class NetworkDefinitionTest {

	private static String sample = "src/test/resources/can_definition_sample.kcd";
	private static NetworkDefinition netdef = null;
	private static Document doc = null;
	private static Object object = null;
	private static Marshaller marshall = null;
	private static File file = null;

	/**
	 * @throws java.lang.Exception
	 *             Methods with the annotation 'BeforeClass' are executed once
	 *             before the first of the series of tests. External resources
	 *             that are used by all tests should be initialized here.
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		file = new File(sample);
		assertNotNull(file);
	}

	/**
	 * @throws java.lang.Exception
	 *             Methods with the annotation 'Before' are executed before
	 *             every test. The test object should be brought to the initial
	 *             state all tests assume it to be in.
	 */
	@Before
	public void setUp() throws Exception {
		JAXBContext context = null;

		try {
			context = JAXBContext
					.newInstance(new Class[] { com.github.kayak.canio.kcd.NetworkDefinition.class });
			assertNotNull("Failed to create context", context);
			Unmarshaller umarshall = context.createUnmarshaller();
			assertNotNull("Failed to create unmarshaller", umarshall);
			object = umarshall.unmarshal(file);
			assertNotNull("Failed to unmarshall file", object);
			marshall = context.createMarshaller();
			assertNotNull("Failed to create marshaller", marshall);

		} catch (JAXBException e) {
			e.printStackTrace();
		}

		assertTrue("Expected unmarshalled object is of type NetworkDefinition("
				+ object.getClass() + "." + NetworkDefinition.class + ")",
				(object.getClass() == NetworkDefinition.class));

		if (object.getClass() == NetworkDefinition.class) {
			netdef = (NetworkDefinition) object;
		}
	}

	/**
	 * @throws java.lang.Exception
	 *             Methods with the annotation 'After' are executed after every
	 *             test.
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for
	 * {@link com.github.kayak.canio.kcd.NetworkDefinition#getDocument()}.
	 */
	@Test
	public void testGetDocument() {
		doc = netdef.getDocument();
		assertNotNull(doc);
		assertTrue("Unexpected content within 'Document' element", doc
				.getContent().contains("A car designed by Homer J. Simpson"));
	}

	/**
	 * Test method for
	 * {@link com.github.kayak.canio.kcd.NetworkDefinition#setDocument(com.github.kayak.canio.kcd.Document)}
	 * .
	 */
	@Test
	public void testSetDocument() {
		String author = "Lyle Lanley";
		String company = "North Haverbrook Railway Ltd.";
		String content = "The Springfiled Monorail is a short-lived transport system.";
		String date = "1993-01-14";
		String name = "Springfield Monorail";
		String version = "2.0";
		doc = netdef.getDocument();
		doc.setAuthor(author);
		doc.setCompany(company);
		doc.setContent(content);
		doc.setDate(date);
		doc.setName(name);
		doc.setVersion(version);
		netdef.setDocument(doc);

		assertEquals("Unexpected document author", author, doc.getAuthor());
		assertEquals("Unexpected document company", company, doc.getCompany());
		assertEquals("Unexpected document content", content, doc.getContent());
		assertEquals("Unexpected document date", date, doc.getDate());
		assertEquals("Unexpected document name", name, doc.getName());
		assertEquals("Unexpected document version", version, doc.getVersion());
	}

	/**
	 * Test method for
	 * {@link com.github.kayak.canio.kcd.NetworkDefinition#getNode()}.
	 */
	@Test
	public void testGetNode() {
		List<String> list = new ArrayList<String>();
		list.add(new String("Motor ACME"));
		list.add(new String("Navigation"));
		list.add(new String("Fuel"));
		list.add(new String("Climate"));
		list.add(new String("Brake ACME"));
		list.add(new String("Steering"));
		list.add(new String("Crypto"));
		list.add(new String("CruiseControl"));
		list.add(new String("Clock"));
		list.add(new String("Gearbox"));
		list.add(new String("Motor alternative supplier"));
		list.add(new String("BodyComputer"));
		list.add(new String("Radio"));
		list.add(new String("DoorLocking"));
		list.add(new String("Seat"));
		list.add(new String("BodyControl"));
		list.add(new String("Brake alternative supplier"));
		list.add(new String("ParkDistance"));

		for (Node n : netdef.getNode()) {
			assertTrue("Unexpected node", list.contains(n.getName()));
		}
	}

	/**
	 * Test method for
	 * {@link com.github.kayak.canio.kcd.NetworkDefinition#getBus()}.
	 */
	@Test
	public void testGetBus() {
		List<String> busList = new ArrayList<String>();
		busList.add(new String("Motor"));
		busList.add(new String("Instrumentation"));
		busList.add(new String("Comfort"));

		List<String> msgList = new ArrayList<String>();

		msgList.add(new String("Airbag"));
		msgList.add(new String("ABS"));
		msgList.add(new String("CruiseControlStatus"));
		msgList.add(new String("Emission"));
		msgList.add(new String("SteeringInfo"));
		msgList.add(new String("AntiTheft"));
		msgList.add(new String("Gear"));
		msgList.add(new String("Headlights"));
		msgList.add(new String("Wiper"));
		msgList.add(new String("BCC"));
		msgList.add(new String("TankController"));
		msgList.add(new String("ParksensorFront"));
		msgList.add(new String("ParksensorBack"));
		msgList.add(new String("Temperature"));
		msgList.add(new String("AirCondition"));
		msgList.add(new String("FrontLeftDoor"));
		msgList.add(new String("FrontRightDoor"));
		msgList.add(new String("BackLeftDoor"));
		msgList.add(new String("BackRightDoor"));
		msgList.add(new String("TrunkLid"));
		msgList.add(new String("DriverSeat"));
		msgList.add(new String("CodriverSeat"));
		msgList.add(new String("Radio"));
		msgList.add(new String("Navigation"));
		msgList.add(new String("DateTime"));

		for (Bus b : netdef.getBus()) {
			assertTrue("Unexpected bus", busList.contains(b.getName()));

			for (Message m : b.getMessage()) {
				assertTrue("Unexpected message", msgList.contains(m.getName()));
			}
		}
	}
}


