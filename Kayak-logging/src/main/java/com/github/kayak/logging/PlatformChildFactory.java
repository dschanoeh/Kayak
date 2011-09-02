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
import java.util.HashSet;
import java.util.List;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Node;

/**
 *
 * @author Jan-Niklas Meier <dschanoeh@googlemail.com>
 */
public class PlatformChildFactory extends ChildFactory<LogFile> {
    
    private String platform;
    private LogFileManager manager = LogFileManager.getGlobalLogFileManager();
    
    private LogFileManagementChangeListener listener = new LogFileManagementChangeListener() {

        @Override
        public void logFilesForPlatformChanged(String p) {
            if(platform.equals(p))
                refresh(true);
        }

        @Override
        public void platformsChanged() {
            
        }
        
        @Override
        public void favouritesChanged() {
            
        }
    };

    public PlatformChildFactory(String platform) {
        this.platform = platform;
        LogFileManager.getGlobalLogFileManager().addListener(listener);
    }

    @Override
    protected boolean createKeys(List<LogFile> toPopulate) {
        HashSet<LogFile> logFiles = manager.getFilesForPlatform(platform);
        toPopulate.addAll(logFiles);

        return true;
    }

    @Override
    protected Node[] createNodesForKey(LogFile key) {
        LogFileNode node = new LogFileNode(key);
        return new Node[] {node};
    }
}
