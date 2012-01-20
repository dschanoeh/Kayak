/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.kayak.ui.options;

import org.openide.util.NbPreferences;

/**
 *
 * @author dschanoeh
 */
public class Options {
    public static String getDescriptionsFolder() {
        String homeFolder = System.getProperty("user.home");
        return NbPreferences.forModule(Options.class).get("Bus description directory", homeFolder + "/kayak/descriptions/");
    }

    public static boolean getShowStartPage() {
        return NbPreferences.forModule(Options.class).getBoolean("Show start page", true);
    }

    public static void setShowStartPage(boolean b) {
        NbPreferences.forModule(Options.class).putBoolean("Show start page", b);
    }
}
