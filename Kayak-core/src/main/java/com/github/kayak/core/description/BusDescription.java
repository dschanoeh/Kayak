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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Jan-Niklas Meier < dschanoeh@googlemail.com >
 */
public class BusDescription {

    private String name;
    private int baudrate;
    private Map<Integer,MessageDescription> messages;
    private Document document;

    public int getBaudrate() {
        return baudrate;
    }

    public void setBaudrate(int baudrate) {
        this.baudrate = baudrate;
    }

    public Map<Integer,MessageDescription> getMessages() {
        return Collections.unmodifiableMap(messages);
    }

    public void addMessageDescription(MessageDescription d) {
        messages.put(d.getId(), d);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    protected BusDescription(Document d) {
        document = d;
        name = "";
        baudrate = 500000;
        messages = new HashMap<Integer,MessageDescription>();
    }

    public Message decodeFrame(Frame frame) throws DescriptionException {
        MessageDescription message = messages.get(frame.getIdentifier());

        if(message != null) {
            return message.decodeFrame(frame);
        } else {
            return null;
        }
    }

    public Document getDocument() {
        return document;
    }

}
