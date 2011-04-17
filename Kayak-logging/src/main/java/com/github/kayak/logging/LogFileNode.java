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

import com.github.kayak.core.LogFile;
import com.github.kayak.logging.input.LogInputTopComponent;
import java.awt.event.ActionEvent;
import java.lang.reflect.InvocationTargetException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.HelpCtx;
import org.openide.util.actions.SystemAction;


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

        this.setIconBaseWithExtension("org/freedesktop/tango/16x16/apps/accessories-text-editor.png");
    }

    @Override
    public SystemAction[] getActions() {
        return new SystemAction[] { openAction };
    }

    @Override
    public SystemAction getDefaultAction() {
        return openAction;
    }

    @Override
    protected Sheet createSheet() {
        Sheet s = super.createSheet();
        Sheet.Set set = s.createPropertiesSet();

        Property platform = new PropertySupport.ReadOnly<String>("Platform", String.class, "Platform", "Platform that was specified in the log file") {

            @Override
            public String getValue() throws IllegalAccessException, InvocationTargetException {
                return logFile.getPlatform();
            }

        };

        Property fileName = new PropertySupport.ReadOnly<String>("File name", String.class, "File name", "Full file name of the log file") {

            @Override
            public String getValue() throws IllegalAccessException, InvocationTargetException {
                return logFile.getFileName();
            }

        };

        Property compressed = new PropertySupport.ReadOnly<Boolean>("Is compressed", Boolean.class, "Is compressed", "Indicates if the file was gzipped") {

            @Override
            public Boolean getValue() throws IllegalAccessException, InvocationTargetException {
                return logFile.getCompressed();
            }

        };

        Property description = new PropertySupport.ReadOnly<String>("Description", String.class, "Description", "Description that was defined in the log file") {

            @Override
            public String getValue() throws IllegalAccessException, InvocationTargetException {
                return logFile.getDescription();
            }

        };

        Property length = new PropertySupport.ReadOnly<String>("Length", String.class, "Length", "Length of the file in milliseconds") {

            @Override
            public String getValue() throws IllegalAccessException, InvocationTargetException {
                return String.valueOf(logFile.getLength() / 1000) + "." + String.valueOf(logFile.getLength() % 1000);
            }

        };
        
        set.put(platform);
        set.put(fileName);
        set.put(compressed);
        set.put(description);
        set.put(length);

        s.put(set);

        return s;
    }

    SystemAction openAction = new SystemAction() {

        @Override
        public void actionPerformed(ActionEvent e) {
            LogInputTopComponent tc = new LogInputTopComponent();
            tc.setLogFile(logFile);
            tc.open();
            tc.requestActive();

        }

        @Override
        public String getName() {
            return "open";
        }

        @Override
        public HelpCtx getHelpCtx() {
            return HelpCtx.DEFAULT_HELP;
        }
    };

}
