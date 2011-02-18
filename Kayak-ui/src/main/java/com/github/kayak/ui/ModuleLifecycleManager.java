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

package com.github.kayak.ui;

import com.github.kayak.ui.connections.ConnectionManager;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.ModuleInstall;
import org.openide.util.Exceptions;

/**
 *
 * @author Jan-Niklas Meier <dschanoeh@googlemail.com>
 */
public class ModuleLifecycleManager extends ModuleInstall {

    @Override
    public void restored() {
        readConnections();
    }

    @Override
    public void close() {
        super.close();

        writeConnections();
    }

    private void writeConnections() {
        FileObject root = FileUtil.getConfigRoot();

        FileObject connectionStorage = root.getFileObject("Connections.xml");
        if (connectionStorage == null) {
            try {
                connectionStorage = root.createData("Connections.xml");
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        FileLock lock;
        try {
            lock = connectionStorage.lock();

            try {
                OutputStream stream = connectionStorage.getOutputStream(lock);
                ConnectionManager.getGlobalConnectionManager().writeToFile(stream);
                stream.close();
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            } finally {
                lock.releaseLock();
            }
        } catch(IOException ex) {
        }
    }

    private void readConnections() {
        FileObject root = FileUtil.getConfigRoot();

        FileObject connectionStorage = root.getFileObject("Connections.xml");
        if(connectionStorage==null)
            return;

        FileLock lock;
        try {
            lock = connectionStorage.lock();

            try {
                InputStream stream = connectionStorage.getInputStream();
                ConnectionManager.getGlobalConnectionManager().loadFromFile(stream);
                stream.close();
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            } finally {
                lock.releaseLock();
            }
        } catch(IOException ex) {
        }
    }
}
