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

package com.github.kayak.canio.kcd.loader;

import com.github.kayak.canio.kcd.Bus;
import com.github.kayak.canio.kcd.Consumer;
import com.github.kayak.canio.kcd.Message;
import com.github.kayak.canio.kcd.NetworkDefinition;
import com.github.kayak.canio.kcd.Node;
import com.github.kayak.canio.kcd.NodeRef;
import com.github.kayak.canio.kcd.Producer;
import com.github.kayak.canio.kcd.Signal;
import com.github.kayak.canio.kcd.Value;
import com.github.kayak.core.description.BusDescription;
import com.github.kayak.core.description.DescriptionLoader;
import com.github.kayak.core.description.Document;
import com.github.kayak.core.description.MessageDescription;
import com.github.kayak.core.description.SignalDescription;
import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteOrder;
import java.util.HashSet;
import java.util.List;
import java.util.zip.GZIPInputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jan-Niklas Meier < dschanoeh@googlemail.com >
 */
@ServiceProvider(service=DescriptionLoader.class)
public class KCDLoader implements DescriptionLoader {

    @Override
    public Document parseFile(File file) {

        JAXBContext context = null;
        NetworkDefinition netdef = null;

        try {
            context = JAXBContext.newInstance(new Class[]{com.github.kayak.canio.kcd.NetworkDefinition.class});
            Unmarshaller umarshall = context.createUnmarshaller();
            Object object;

            if(file.getName().endsWith(".kcd.gz")) {
                GZIPInputStream zipstream = new GZIPInputStream(new FileInputStream(file));
                object = umarshall.unmarshal(zipstream);
            } else if(file.getName().endsWith(".kcd")) {
                object = umarshall.unmarshal(file);
            } else {
                return null;
            }

            if (object.getClass() == NetworkDefinition.class) {
                netdef = (NetworkDefinition) object;
            }

        } catch (Exception e) {
            return null;
        }

        Document doc = new Document();

        com.github.kayak.canio.kcd.Document documentInfo = netdef.getDocument();
        doc.setVersion(documentInfo.getVersion());
        doc.setAuthor(documentInfo.getAuthor());
        doc.setCompany(documentInfo.getCompany());
        doc.setDate(documentInfo.getDate());
        doc.setName(documentInfo.getName());
        doc.setFileName(file.getAbsolutePath());

        HashSet<com.github.kayak.core.description.Node> nodes = doc.getNodes();
        for(Node n : netdef.getNode()) {
            nodes.add(new com.github.kayak.core.description.Node(n.getId(), n.getName()));
        }

        for(Bus b : netdef.getBus()) {
            BusDescription description = doc.createBusDescription();
            description.setName(b.getName());
            description.setBaudrate(b.getBaudrate());

            for(Message m :  b.getMessage()) {
                MessageDescription messageDescription = description.createMessage(Integer.parseInt(m.getId().substring(2),16));
                messageDescription.setInterval(m.getInterval());
                messageDescription.setName(m.getName());
                
                /* set producer */
                Producer producer = m.getProducer();
                if(producer != null) {
                    List<NodeRef> ref = m.getProducer().getNodeRef();
                    if(ref.size() > 1) {

                    } else if (ref.size() == 1) {
                        String id = ref.get(0).getId();
                        for(com.github.kayak.core.description.Node n : nodes) {
                            if(n.getId().equals(id)) {
                                messageDescription.setProducer(n);
                                break;
                            }
                        }
                    }
                }

                for(Signal s : m.getSignal()) {
                    SignalDescription signalDescription = messageDescription.createSignal();
                    if(s.getEndianess().equals("motorola")) {
                        signalDescription.setByteOrder(ByteOrder.BIG_ENDIAN);
                    } else {
                        signalDescription.setByteOrder(ByteOrder.LITTLE_ENDIAN);
                    }
                    
                    /* set consumers */
                    Consumer c = s.getConsumer();
                    if(c != null && c.getNodeRef() != null) {
                        List<NodeRef> signalRef = c.getNodeRef();
                        HashSet<com.github.kayak.core.description.Node> consumers = new HashSet<com.github.kayak.core.description.Node>();

                        for(NodeRef nr : signalRef) {
                            for(com.github.kayak.core.description.Node n : nodes) {
                                if(n.getId().equals(nr.getId())) {
                                    consumers.add(n);
                                    break;
                                }
                            }
                        }
                        signalDescription.setConsumers(consumers);
                    }

                    Value value = s.getValue();
                    if(value != null) {
                        Double intercept = value.getIntercept();
                        if(intercept != null)
                            signalDescription.setIntercept(intercept);
                        else
                            signalDescription.setIntercept(0);
                        
                        Double slope = value.getSlope();
                        if(slope != null)
                            signalDescription.setSlope(slope);
                        else 
                            signalDescription.setSlope(1);
                        
                        String typeString = value.getType();
                        if(typeString.equals("signed")) {
                            signalDescription.setType(SignalDescription.Type.SIGNED);
                        } else if(typeString.equals("double")) {
                            signalDescription.setType(SignalDescription.Type.DOUBLE);
                        } else if(typeString.equals("float")) {
                            signalDescription.setType(SignalDescription.Type.SINGLE);
                        } else {
                            signalDescription.setType(SignalDescription.Type.UNSIGNED);
                        }
                        
                        signalDescription.setUnit(value.getUnit());
                    }

                    signalDescription.setLength(s.getLength());
                    signalDescription.setName(s.getName());
                    signalDescription.setNotes(s.getNotes());
                    signalDescription.setOffset(s.getOffset()); 
                    
                    /* TODO add labels */

                    messageDescription.getSignals().add(signalDescription);
                }

                description.getMessages().put(messageDescription.getId(), messageDescription);
            }

            doc.getBusses().add(description);
        }

        return doc;
    }

    @Override
    public String[] getSupportedExtensions() {
        return new String[] { "kcd", "kcd.gz" };
    }

}
