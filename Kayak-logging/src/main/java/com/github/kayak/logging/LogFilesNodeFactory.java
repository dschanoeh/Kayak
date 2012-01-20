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
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 *
 * @author Jan-Niklas Meier <dschanoeh@googlemail.com>
 */
public class LogFilesNodeFactory extends ChildFactory<LogFilesNodeFactory.Folders> {

    public static enum Folders { DIRECTORY, FAVOURTIES };

    @Override
    protected boolean createKeys(List<Folders> toPopulate) {
        toPopulate.add(Folders.DIRECTORY);
        toPopulate.add(Folders.FAVOURTIES);
        return true;
    }

    @Override
    protected Node[] createNodesForKey(Folders key) {
        if(key == Folders.DIRECTORY) {
            AbstractNode node = new AbstractNode(Children.create(new LogDirectoryFactory(), true));
            node.setDisplayName("Log directory");
            node.setIconBaseWithExtension("org/tango-project/tango-icon-theme/16x16/places/folder.png");
            return new Node[] {node};
        } else if(key == Folders.FAVOURTIES) {
            AbstractNode node = new AbstractNode(Children.create(new LogFavouritesFactory(), true));
            node.setDisplayName("Favourites");
            node.setIconBaseWithExtension("org/tango-project/tango-icon-theme/16x16/actions/bookmark-new.png");
            return new Node[] {node};
        }

        return null;
    }




}
