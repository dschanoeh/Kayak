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

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import com.github.kayak.core.LogFile;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;

/**
 *
 * @author Jan-Niklas Meier <dschanoeh@googlemail.com>
 */
@ActionRegistration(displayName="Compress Log file...", iconBase="org/tango-project/tango-icon-theme/16x16/mimetypes/package-x-generic.png", iconInMenu=true, surviveFocusChange=true)
@ActionID(category="Log files", id="com.github.kayak.logging.CompressLogFileAction")
@ActionReferences(value = {
    @ActionReference(path="Menu/Log files", position=20)})
public class CompressLogFileAction extends AbstractAction {

    private LogFile lf;
    private static final Logger logger = Logger.getLogger(CompressLogFileAction.class.getCanonicalName());

    public CompressLogFileAction(LogFile context) {
            putValue (NAME, "Compress");
            lf = context;
        }

        @Override
        public void actionPerformed(ActionEvent e) {

            if (lf != null && !lf.getCompressed()) {
                File f = lf.getFile();

                try {

                    File newFile = new File(f.getAbsolutePath() + ".gz");
                    GZIPOutputStream out = new GZIPOutputStream(new FileOutputStream(newFile));
                    FileInputStream in = new FileInputStream(f);

                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    in.close();

                    out.finish();
                    out.close();
                    f.delete();
                    LogFileManager.getGlobalLogFileManager().removeLogFile(lf);
                    LogFileManager.getGlobalLogFileManager().addLogFile(new LogFile(newFile));
                } catch (IOException ex) {
                    logger.log(Level.WARNING, "Could not compress log file", ex);
                }
            }
        }
}
