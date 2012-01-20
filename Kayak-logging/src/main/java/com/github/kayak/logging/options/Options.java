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
package com.github.kayak.logging.options;

import org.openide.util.NbPreferences;

/**
 *
 * @author Jan-Niklas Meier <dschanoeh@googlemail.com>
 */
public class Options {

    public static String getLogFilesFolder() {
        String homeFolder = System.getProperty("user.home");
        return NbPreferences.forModule(Options.class).get("Log file directory", homeFolder + "/kayak/log/");
    }

    public static int getSnapshotBufferDepth() {
        return Integer.parseInt(NbPreferences.forModule(Options.class).get("Snapshot buffer depth", Integer.toString(5000)));
    }

    public static int getSnapshotBufferFinish() {
        return Integer.parseInt(NbPreferences.forModule(Options.class).get("Snapshot buffer finish", Integer.toString(5000)));
    }

    public static boolean getSnapshotsEnabled() {
        return NbPreferences.forModule(Options.class).getBoolean("Snapshots enabled", true);
    }

}
