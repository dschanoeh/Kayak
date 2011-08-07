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
import java.util.HashMap;

/**
 *
 * @author Jan-Niklas Meier < dschanoeh@googlemail.com >
 */
public class BusDescription {

    private String name;
    private int baudrate;
    private HashMap<Integer,MessageDescription> messages;
    private Document document;

    public int getBaudrate() {
        return baudrate;
    }

    public void setBaudrate(int baudrate) {
        this.baudrate = baudrate;
    }

    public HashMap<Integer,MessageDescription> getMessages() {
        return messages;
    }

    public void setMessages(HashMap<Integer,MessageDescription> messages) {
        this.messages = messages;
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

    public Message decodeFrame(Frame frame) {
        MessageDescription message = messages.get(frame.getIdentifier());

        if(messages != null) {
            return message.decodeFrame(frame);
        } else {
            return null;
        }
    }

    public Document getDocument() {
        return document;
    }

    public MessageDescription createMessage(int id) {
        if(!messages.containsKey(id)) {
            MessageDescription m = new MessageDescription(this, id);
            messages.put(id, m);
            return m;
        } else {
            return null;
        }
    }

}
