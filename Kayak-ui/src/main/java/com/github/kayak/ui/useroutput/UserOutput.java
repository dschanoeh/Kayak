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
package com.github.kayak.ui.useroutput;

import com.github.kayak.core.EventFrame;
import com.github.kayak.core.description.Signal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;

/**
 *
 * @author Jan-Niklas Meier <dschanoeh@googlemail.com>
 */
public class UserOutput {

    private static InputOutput io;
    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    private static void createOutput() {
        if(io == null) {
            io = IOProvider.getDefault().getIO("Information", true);
        }

        io.select();
    }

    public static void printSignalErrorValue(Signal s) {

    }

    public static void print(EventFrame ef) {
        createOutput();
        synchronized(io) {
            OutputWriter out = io.getOut();
            Date date = new Date();
            out.write("[");
            out.write(dateFormat.format(date));
            out.write("] EVENT ");
            out.write(ef.getMessage());
            out.write("\n");
            out.close();
        }
    }

    public static void printInfo(String info) {
        createOutput();
        synchronized(io) {
            OutputWriter out = io.getOut();
            Date date = new Date();
            out.write("[");
            out.write(dateFormat.format(date));
            out.write("] INFO ");
            out.write(info);
            out.write("\n");
            out.close();
        }
    }

    public static void printWarning(String info) {
        createOutput();
        synchronized(io) {
            OutputWriter out = io.getOut();
            Date date = new Date();
            out.write("[");
            out.write(dateFormat.format(date));
            out.write("] WARNING ");
            out.write(info);
            out.write("\n");
            out.close();
        }
    }

}
