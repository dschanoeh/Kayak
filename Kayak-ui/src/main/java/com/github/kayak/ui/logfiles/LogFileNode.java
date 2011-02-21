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
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.Exceptions;


/**
 *
 * @author Jan-Niklas Meier <dschanoeh@googlemail.com>
 */
public class LogFileNode extends AbstractNode {
    
    private LogFile logFile;

    public LogFileNode(LogFile logFile) {
        super(Children.create(new BusNodeFactory(logFile), true));
        this.logFile = logFile;
        this.setDisplayName(logFile.getDescription());
        this.setShortDescription(logFile.getFileName());

        this.setIconBaseWithExtension("com/github/kayak/ui/logfiles/accessories-text-editor.png");
    }

    @Override
    protected Sheet createSheet() {
        Sheet s = super.createSheet();
        Sheet.Set set = s.createPropertiesSet();

        set.setName("Log file");

        try {
            Property platform = new PropertySupport.Reflection<String>(logFile.getPlatform(), String.class, "Platform", null);
            platform.setName("Platform");
            set.put(platform);
        } catch (NoSuchMethodException ex) {
            Exceptions.printStackTrace(ex);
        }

        
        s.put(set);

        return s;
    }

}
