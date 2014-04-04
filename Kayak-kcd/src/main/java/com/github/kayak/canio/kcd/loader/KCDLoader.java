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

import com.github.kayak.canio.kcd.BasicLabelType;
import com.github.kayak.canio.kcd.Bus;
import com.github.kayak.canio.kcd.Consumer;
import com.github.kayak.canio.kcd.Label;
import com.github.kayak.canio.kcd.LabelGroup;
import com.github.kayak.canio.kcd.LabelSet;
import com.github.kayak.canio.kcd.Message;
import com.github.kayak.canio.kcd.Multiplex;
import com.github.kayak.canio.kcd.MuxGroup;
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
import com.github.kayak.core.description.MultiplexDescription;
import com.github.kayak.core.description.SignalDescription;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.ByteOrder;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.openide.util.lookup.ServiceProvider;
import org.xml.sax.SAXException;

/**
 *
 * @author Jan-Niklas Meier < dschanoeh@googlemail.com >
 */
@ServiceProvider(service=DescriptionLoader.class)
public class KCDLoader implements DescriptionLoader {

    private static final Logger logger = Logger.getLogger(KCDLoader.class.getCanonicalName());
    Schema schema;
    JAXBContext context;

    public KCDLoader() {
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        InputStream resourceAsStream = KCDLoader.class.getResourceAsStream("Definition.xsd");
        Source s = new StreamSource(resourceAsStream);
        try {
        schema = schemaFactory.newSchema(s);
        } catch(SAXException ex) {
            logger.log(Level.SEVERE, "Could not load schema: ", ex);
        }
        try {
            context = JAXBContext.newInstance(new Class[]{com.github.kayak.canio.kcd.NetworkDefinition.class});
        } catch(JAXBException ex) {
            logger.log(Level.SEVERE, "Could not create JAXB context: ", ex);
        }
    }

    @Override
    public Document parseFile(File file) {
        NetworkDefinition netdef = null;

        try {
            context = JAXBContext.newInstance(new Class[]{com.github.kayak.canio.kcd.NetworkDefinition.class});
            Unmarshaller umarshall = context.createUnmarshaller();
            umarshall.setSchema(schema);

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

        } catch(UnmarshalException e) {
            logger.log(Level.WARNING, "Found invalid file: " + file.getAbsolutePath() + "!", e);
            return null;
        } catch (Exception e) {
            logger.log(Level.WARNING, "Could not load kcd file " + file.getAbsolutePath() + "!", e);
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


        for(Node n : netdef.getNode()) {
            com.github.kayak.core.description.Node node = doc.createNode(n.getId(), n.getName());
        }

        for(Bus b : netdef.getBus()) {
            BusDescription description = doc.createBusDescription();
            description.setName(b.getName());
            description.setBaudrate(b.getBaudrate());

            /* Messages for each bus */
            for(Message m :  b.getMessage()) {
                MessageDescription messageDescription;

                if(m.getFormat().equals("extended"))
                    messageDescription = new MessageDescription(Integer.parseInt(m.getId().substring(2),16), true);
                else
                    messageDescription = new MessageDescription(Integer.parseInt(m.getId().substring(2),16), false);

                messageDescription.setInterval(m.getInterval());
                messageDescription.setName(m.getName());

                /* set producer */
                Producer producer = m.getProducer();
                if(producer != null) {
                    List<NodeRef> ref = producer.getNodeRef();
                    if(ref.size() > 1) {

                    } else if (ref.size() == 1) {
                        String id = ref.get(0).getId();
                        com.github.kayak.core.description.Node n = doc.getNodeWithID(id);
                        if(n != null)
                            messageDescription.setProducer(n);
                    }
                }

                for(Multiplex multiplex : m.getMultiplex()) {
                    MultiplexDescription multiplexDescription = messageDescription.createMultiplexDescription();

                    /* Set multiplex values */
                    if(multiplex.getEndianess().equals("big")) {
                        multiplexDescription.setByteOrder(ByteOrder.BIG_ENDIAN);
                    } else {
                        multiplexDescription.setByteOrder(ByteOrder.LITTLE_ENDIAN);
                    }
                    multiplexDescription.setLength(multiplex.getLength());
                    multiplexDescription.setOffset(multiplex.getOffset());
                    multiplexDescription.setName(multiplex.getName());
                    
                    if(multiplex.getValue() != null) {
                        String typeString = multiplex.getValue().getType();
                        if (typeString.equals("signed")) {
                            multiplexDescription.setType(SignalDescription.Type.SIGNED);
                        } else if (typeString.equals("double")) {
                            multiplexDescription.setType(SignalDescription.Type.DOUBLE);
                        } else if (typeString.equals("float")) {
                            multiplexDescription.setType(SignalDescription.Type.SINGLE);
                        } else {
                            multiplexDescription.setType(SignalDescription.Type.UNSIGNED);
                        }
                    }

                    /* Transform MuxGroups to Signal lists */
                    for(MuxGroup group : multiplex.getMuxGroup()) {
                        long value = (long) group.getCount();

                        for(Signal s : group.getSignal()) {
                            SignalDescription signalDescription = multiplexDescription.createMultiplexedSignal(value);
                            signalToSignalDescription(s, signalDescription);

                            /* set consumers */
                            Consumer c = s.getConsumer();
                            if(c != null && c.getNodeRef() != null) {
                                List<NodeRef> signalRef = c.getNodeRef();
                                HashSet<com.github.kayak.core.description.Node> consumers = new HashSet<com.github.kayak.core.description.Node>();

                                for(NodeRef nr : signalRef) {
                                    com.github.kayak.core.description.Node n = doc.getNodeWithID(nr.getId());
                                    if(n != null)
                                        consumers.add(n);
                                }
                            }
                        }
                    }
                }

                for(Signal s : m.getSignal()) {
                    SignalDescription signalDescription = messageDescription.createSignalDescription();
                    signalToSignalDescription(s, signalDescription);


                    /* set consumers */
                    Consumer c = s.getConsumer();
                    if(c != null && c.getNodeRef() != null) {
                        List<NodeRef> signalRef = c.getNodeRef();
                        HashSet<com.github.kayak.core.description.Node> consumers = new HashSet<com.github.kayak.core.description.Node>();

                        for(NodeRef nr : signalRef) {
                            com.github.kayak.core.description.Node n = doc.getNodeWithID(nr.getId());
                            if(n != null)
                                consumers.add(n);
                        }
                    }

                }
                description.addMessageDescription(messageDescription);
            }
        }

        return doc;
    }

    private com.github.kayak.core.description.SignalDescription signalToSignalDescription(Signal s, SignalDescription signalDescription) {
        if (s.getEndianess().equals("big")) {
            signalDescription.setByteOrder(ByteOrder.BIG_ENDIAN);
        } else {
            signalDescription.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        }

        Value value = s.getValue();
        if (value != null) {
            Double intercept = value.getIntercept();
            if (intercept != null) {
                signalDescription.setIntercept(intercept);
            } else {
                signalDescription.setIntercept(0);
            }

            Double slope = value.getSlope();
            if (slope != null) {
                signalDescription.setSlope(slope);
            } else {
                signalDescription.setSlope(1);
            }

            String typeString = value.getType();
            if (typeString.equals("signed")) {
                signalDescription.setType(SignalDescription.Type.SIGNED);
            } else if (typeString.equals("double")) {
                signalDescription.setType(SignalDescription.Type.DOUBLE);
            } else if (typeString.equals("float")) {
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

        LabelSet ls = s.getLabelSet();
        if(ls != null) {
            List<BasicLabelType> labels = ls.getLabelOrLabelGroup();
            if(labels != null) {
                for(BasicLabelType basicLabel : labels) {
                    if(basicLabel instanceof Label) {
                        Label l = (Label) basicLabel;
                        com.github.kayak.core.description.Label label = new com.github.kayak.core.description.Label(l.getValue().longValue(), l.getName());
                        signalDescription.addLabel(label);
                    } else if(basicLabel instanceof LabelGroup) {
                        LabelGroup l = (LabelGroup) basicLabel;
                        com.github.kayak.core.description.Label label = new com.github.kayak.core.description.Label(l.getFrom().longValue(), l.getTo().longValue(), l.getName());
                        signalDescription.addLabel(label);
                    }
                }
            }
        }

        return signalDescription;
    }

    @Override
    public String[] getSupportedExtensions() {
        return new String[] { "kcd", "kcd.gz" };
    }

}
