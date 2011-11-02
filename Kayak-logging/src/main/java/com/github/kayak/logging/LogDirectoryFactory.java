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

import java.util.List;
import java.util.TreeSet;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 *
 * @author Jan-Niklas Meier <dschanoeh@googlemail.com>
 */
public class LogDirectoryFactory extends ChildFactory<String> {

    private LogFileManagementChangeListener listener = new LogFileManagementChangeListener() {

        @Override
        public void logFilesForPlatformChanged(String platform) {

        }

        @Override
        public void platformsChanged() {
            refresh(true);
        }

        @Override
        public void favouritesChanged() {

        }
    };

    public LogDirectoryFactory() {
        LogFileManager.getGlobalLogFileManager().addListener(listener);
    }

    @Override
    protected boolean createKeys(List<String> toPopulate) {
        TreeSet<String> platforms = LogFileManager.getGlobalLogFileManager().getPlatforms();
        toPopulate.addAll(platforms);
        return true;
    }

    @Override
    protected Node[] createNodesForKey(String key) {
        AbstractNode node = new AbstractNode(Children.create(new PlatformChildFactory(key), false));
        node.setIconBaseWithExtension("org/tango-project/tango-icon-theme/16x16/places/folder.png");
        node.setDisplayName(key);

        return new Node[] {node};
    }
}
