/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.kayak.ui.messageview;

import com.github.kayak.core.Bus;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import org.openide.awt.ActionRegistration;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionID;
import org.openide.util.NbBundle.Messages;

@ActionID(category = "BusViews",
id = "com.github.kayak.ui.messageview.OpenMessageViewAction")
@ActionRegistration(displayName = "#CTL_OpenMessageViewAction", iconBase="org/freedesktop/tango/16x16/mimetypes/text-x-generic.png")
@ActionReferences({
    @ActionReference(path = "Menu/Bus views", position = -90)
})
@Messages("CTL_OpenMessageViewAction=Open message view")
public final class OpenMessageViewAction implements ActionListener {

    private final Bus context;

    public OpenMessageViewAction(Bus context) {
	this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        MessageViewTopComponent tc = new MessageViewTopComponent();
        tc.setBus(context);
        tc.open();
        tc.requestActive();
    }
}
