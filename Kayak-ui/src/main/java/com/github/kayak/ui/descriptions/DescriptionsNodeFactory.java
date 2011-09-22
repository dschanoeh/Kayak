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
package com.github.kayak.ui.descriptions;

import com.github.kayak.core.description.Document;
import java.util.List;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Node;

/**
 *
 * @author Jan-Niklas Meier <dschanoeh@googlemail.com>
 */
public class DescriptionsNodeFactory extends ChildFactory<Document> {

    private DescriptionManagementChangeListener listener = new DescriptionManagementChangeListener() {

        @Override
        public void descriptionAdded(Document doc) {
            refresh(false);
        }

        @Override
        public void descriptionRemoved(Document doc) {
            refresh(false);
        }
    };

    public DescriptionsNodeFactory() {
        DescriptionManagement.getGlobalDescriptionManagement().addListener(listener);
    }

    @Override
    protected boolean createKeys(List<Document> list) {
        list.addAll(DescriptionManagement.getGlobalDescriptionManagement().getDescriptions());
        return true;
    }

    @Override
    protected Node[] createNodesForKey(Document key) {
        return new Node[] { new DocumentNode(key) };
    }

}
