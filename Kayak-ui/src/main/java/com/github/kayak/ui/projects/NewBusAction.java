/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.kayak.ui.projects;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public final class NewBusAction implements ActionListener {

    private final NewBusCookie context;

    public NewBusAction(NewBusCookie context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        context.addNewBus();
    }
}
