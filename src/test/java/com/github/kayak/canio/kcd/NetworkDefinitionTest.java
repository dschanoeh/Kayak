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
package com.github.kayak.canio.kcd;

import static org.junit.Assert.*;

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

	private static String sample = "src/test/resources/can_definition_sample.xml";
	private static NetworkDefinition netdef = null;
	private static Document doc = null;
	private static Object object = null;
	private static Marshaller marshall = null;
	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

	}
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		
		try{
			JAXBContext context = JAXBContext.newInstance(
					new Class[]{ com.github.kayak.canio.kcd.NetworkDefinition.class });
			Unmarshaller umarshall = context.createUnmarshaller();
			object = umarshall.unmarshal(new File( sample ));

			assertTrue("Expected unmarshalled object is of type NetworkDefinition(" 
					+ object.getClass() 
					+ "." 
					+ NetworkDefinition.class 
					+ ")", (object.getClass() == NetworkDefinition.class));
			
			if ( object.getClass() == NetworkDefinition.class ){
				netdef = (NetworkDefinition) object;
			}
		
			marshall = context.createMarshaller();
			
		} catch(JAXBException e) { 
			e.printStackTrace();
		}
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link com.github.kayak.canio.kcd.NetworkDefinition#getDocument()}.
	 */
	@Test
	public void testGetDocument() {

			
			doc = netdef.getDocument();
			assertNotNull(doc);
			System.out.println("ok...Content of document is" + doc.getContent());
		

		
	}

	/**
	 * Test method for {@link com.github.kayak.canio.kcd.NetworkDefinition#setDocument(com.github.kayak.canio.kcd.Document)}.
	 */
	@Test
	public void testSetDocument() {
		netdef.setDocument(doc);
   	}

	/**
	 * Test method for {@link com.github.kayak.canio.kcd.NetworkDefinition#getNode()}.
	 */
	@Test
	public void testGetNode() {
		for (Node n: netdef.getNode()){
			System.out.println(n.getName());
		    
		}
	}

	/**
	 * Test method for {@link com.github.kayak.canio.kcd.NetworkDefinition#getBus()}.
	 */
	@Test
	public void testGetBus() {
		List<String> list = new ArrayList<String>();
	    list.add(new String("Motor"));
	    list.add(new String("Instrumentation"));
	    list.add(new String("Comfort"));
	   
		
		for (Bus b: netdef.getBus()){
			assertTrue(list.contains(b.getName()));
				
			for(Message m: b.getMessage()){
				System.out.println(m.getName());
				
			
			}
		}
	}

	/**
	 * Test method for {@link com.github.kayak.canio.kcd.NetworkDefinition#getVersion()}.
	 */
	@Test
	public void testGetVersion() {
		assertEquals("Version uniqueness","0.1",netdef.getVersion());
	}

	/**
	 * Test method for {@link com.github.kayak.canio.kcd.NetworkDefinition#setVersion(java.lang.String)}.
	 */
	@Test
	public void testSetVersion() {
		netdef.setVersion("7e57");
		try {
			marshall.marshal(object, System.out);
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

