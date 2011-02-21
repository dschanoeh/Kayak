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

package com.github.kayak.ui.logfiles;

import com.github.kayak.core.LogFile;
import java.util.List;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Node;

/**
 *
 * @author Jan-Niklas Meier <dschanoeh@googlemail.com>
 */
public class BusNodeFactory extends ChildFactory<String> {

    private LogFile logFile;

    public BusNodeFactory(LogFile logFile) {
        this.logFile = logFile;
    }

    @Override
    protected boolean createKeys(List<String> toPopulate) {
        toPopulate.add("can0");
        toPopulate.add("can1");
        toPopulate.add("can1");

        return true;
    }

    @Override
    protected Node[] createNodesForKey(String key) {
        return new Node[] { new BusNode(new LogFileBusTupel(logFile, key)) };
    }
}
