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
package com.github.kayak.logging.snapshots;

import java.util.ArrayList;
import javax.swing.AbstractListModel;

/**
 *
 * @author Jan-Niklas Meier <dschanoeh@googlemail.com>
 */
public class SnapshotModel extends AbstractListModel {

    private final ArrayList<SnapshotBuffer> savedBuffers = new ArrayList<SnapshotBuffer>();

    @Override
    public int getSize() {
        synchronized(savedBuffers) {
        return savedBuffers.size();
        }
    }

    @Override
    public Object getElementAt(int index) {
        synchronized(savedBuffers) {
            return savedBuffers.get(index);
        }
    }

    public void addSnapshot(SnapshotBuffer s) {
        synchronized(savedBuffers) {
            savedBuffers.add(s);
            int i = savedBuffers.indexOf(s);
            fireIntervalAdded(this, i, i);
        }
    }

    public void removeSnapshot(SnapshotBuffer s) {
        synchronized(savedBuffers) {
            int i = savedBuffers.indexOf(s);
            savedBuffers.remove(s);
            fireIntervalRemoved(this, i, i);
        }
    }
}
