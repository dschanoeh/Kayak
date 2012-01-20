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

import com.github.kayak.core.Bus;
import com.github.kayak.core.description.BusDescription;
import com.github.kayak.core.description.Document;
import com.github.kayak.ui.projects.Project;
import com.github.kayak.ui.projects.ProjectManager;
import java.awt.event.ActionEvent;
import java.lang.reflect.InvocationTargetException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Jan-Niklas Meier <dschanoeh@googlemail.com>
 */
public class DocumentNode extends AbstractNode {

    private Document document;

    public DocumentNode(Document document) {
        super(Children.create(new DocumentChildrenFactory(document), true), Lookups.fixed(document));
        this.document = document;
        setName(document.getName());
        setIconBaseWithExtension("org/tango-project/tango-icon-theme/16x16/mimetypes/text-x-generic.png");
    }

    @Override
    protected Sheet createSheet() {
        Sheet s = super.createSheet();
        Sheet.Set set = Sheet.createPropertiesSet();

        Property name = new PropertySupport.ReadOnly<String>("Name", String.class, "Name", "Name of the document") {

            @Override
            public String getValue() throws IllegalAccessException, InvocationTargetException {
                return document.getName();
            }
        };

        Property author = new PropertySupport.ReadOnly<String>("Author", String.class, "Author", "Author of the document") {

            @Override
            public String getValue() throws IllegalAccessException, InvocationTargetException {
                return document.getAuthor();
            }
        };

        Property company = new PropertySupport.ReadOnly<String>("Company", String.class, "Company", "Company that created the document") {

            @Override
            public String getValue() throws IllegalAccessException, InvocationTargetException {
                return document.getCompany();
            }
        };

        Property date = new PropertySupport.ReadOnly<String>("Date", String.class, "Date", "Date of creation") {

            @Override
            public String getValue() throws IllegalAccessException, InvocationTargetException {
                return document.getDate();
            }
        };

        Property version = new PropertySupport.ReadOnly<String>("Version", String.class, "Version", "Version of the document") {

            @Override
            public String getValue() throws IllegalAccessException, InvocationTargetException {
                return document.getVersion();
            }
        };

        set.put(name);
        set.put(author);
        set.put(company);
        set.put(date);
        set.put(version);

        s.put(set);

        return s;
    }

    @Override
    public Action[] getActions(boolean context) {
        return new Action[] { new CreateProjectAction(this) };
    }

}
