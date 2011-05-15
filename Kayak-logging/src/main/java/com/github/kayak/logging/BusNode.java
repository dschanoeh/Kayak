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

package com.github.kayak.logging;

import java.awt.datatransfer.Transferable;
import java.io.IOException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;

/**
 *
 * @author Jan-Niklas Meier <dschanoeh@googlemail.com>
 */
public class BusNode extends AbstractNode {

    private LogFileBusTupel tupel;

    public BusNode(LogFileBusTupel tupel) {
        super(Children.LEAF);

        this.tupel = tupel;

        this.setDisplayName(tupel.getBusName() + " @ " + tupel.getLogFile().getFileName());

    }

    @Override
    public Transferable drag() throws IOException {
        return tupel;
    }
}
