package com.github.kayak.core.controller;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import com.github.kayak.canio.kcd.*;

import de.vwag.kayak.canData.DBCParser;

public class Application {

	static NetworkDefinition netdef = null;
	static Document doc = null;
	
	public void start(){
	
	DBCParser parser = new DBCParser();
	
	
	//parser.parse("etc/AU716_DCAN_3.12.11_02.dbc");
	parser.parse("etc/test.dbc");
	
	try{
		JAXBContext con = JAXBContext.newInstance(
				new Class[]{com.github.kayak.canio.kcd.NetworkDefinition.class});
		Unmarshaller u = con.createUnmarshaller();
		Object o = u.unmarshal(new File( "etc/can_definition_sample.xml" ));
		
		if ( o.getClass() == NetworkDefinition.class ){
			
			System.out.println("ok...file has root node NetworkDefinition\n");
			netdef = (NetworkDefinition) o;
			System.out.println("ok...Version of document is" + netdef.getVersion());
			doc = netdef.getDocument();
			System.out.println("ok...Content of document is" + doc.getContent());
			for (Node n: netdef.getNode()){
				System.out.println(n.getName());
			    
			}
			for (Bus b: netdef.getBus()){
				System.out.println("####" + b.getName());
				for(Message m: b.getMessage()){
					System.out.println(m.getName());
				
				}
			}
		}
		
		} catch(JAXBException e) { 
			e.printStackTrace();
		}
        
	}
}
