package com.github.kayak.ui.send;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

/**
 *  The ButtonColumn class provides a renderer and an editor that looks like a
 *  JButton. The renderer and editor will then be used for a specified column
 *  in the table. The TableModel will contain the String to be displayed on
 *  the button.
 *
 *  The button can be invoked by a mouse click or by pressing the space bar
 *  when the cell has focus. Optionaly a mnemonic can be set to invoke the
 *  button. When the button is invoked the provided Action is invoked. The
 *  source of the Action will be the table. The action command will contain
 *  the model row number of the button that was clicked.
 *
 */
public class CheckBoxColumn extends AbstractCellEditor
        implements TableCellRenderer, TableCellEditor, ActionListener, MouseListener {

    private JTable table;
    private Action action;
    private int mnemonic;
    private Border originalBorder;
    private Border focusBorder;
    private JCheckBox renderCheckBox;
    private JCheckBox editCheckBox;
    private boolean isButtonColumnEditor;

    /**
     *  Create the ButtonColumn to be used as a renderer and editor. The
     *  renderer and editor will automatically be installed on the TableColumn
     *  of the specified column.
     *
     *  @param table the table containing the button renderer/editor
     *  @param action the Action to be invoked when the button is invoked
     *  @param column the column to which the button renderer/editor is added
     */
    public CheckBoxColumn(JTable table, Action action, int column) {
        this.table = table;
        this.action = action;

        renderCheckBox = new JCheckBox();
        editCheckBox = new JCheckBox();
        
        editCheckBox.setFocusPainted(false);
        editCheckBox.addActionListener(this);
        originalBorder = editCheckBox.getBorder();
        setFocusBorder(new LineBorder(Color.BLUE));

        TableColumnModel columnModel = table.getColumnModel();
        columnModel.getColumn(column).setCellRenderer(this);
        columnModel.getColumn(column).setCellEditor(this);
        table.addMouseListener(this);
    }

    /**
     *  Get foreground color of the button when the cell has focus
     *
     *  @return the foreground color
     */
    public Border getFocusBorder() {
        return focusBorder;
    }

    /**
     *  The foreground color of the button when the cell has focus
     *
     *  @param focusBorder the foreground color
     */
    public void setFocusBorder(Border focusBorder) {
        this.focusBorder = focusBorder;
        editCheckBox.setBorder(focusBorder);
    }

    public int getMnemonic() {
        return mnemonic;
    }

    /**
     *  The mnemonic to activate the button when the cell has focus
     *
     *  @param mnemonic the mnemonic
     */
    public void setMnemonic(int mnemonic) {
        this.mnemonic = mnemonic;
        renderCheckBox.setMnemonic(mnemonic);
        editCheckBox.setMnemonic(mnemonic);
    }

    @Override
    public Component getTableCellEditorComponent(
            JTable table, Object value, boolean isSelected, int row, int column) {
        editCheckBox.setSelected((Boolean) value);
        return editCheckBox;
    }

    @Override
    public Object getCellEditorValue() {
        return "";
    }

//
//  Implement TableCellRenderer interface
//
    @Override
    public Component getTableCellRendererComponent(
            JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (isSelected) {
            renderCheckBox.setForeground(table.getSelectionForeground());
            renderCheckBox.setBackground(table.getSelectionBackground());
        } else {
            renderCheckBox.setForeground(table.getForeground());
            renderCheckBox.setBackground(UIManager.getColor("Button.background"));
        }

        if (hasFocus) {
            renderCheckBox.setBorder(focusBorder);
        } else {
            renderCheckBox.setBorder(originalBorder);
        }

        renderCheckBox.setSelected((Boolean) value);
        //renderCheckBox.setText((value == null) ? "" : value.toString());
        return renderCheckBox;
    }

//
//  Implement ActionListener interface
//
	/*
     *	The button has been pressed. Stop editing and invoke the custom Action
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        int row = table.convertRowIndexToModel(table.getEditingRow());
        fireEditingStopped();

        //  Invoke the Action

        ActionEvent event = new ActionEvent(
                table,
                ActionEvent.ACTION_PERFORMED,
                "" + row);
        action.actionPerformed(event);
    }

//
//  Implement MouseListener interface
//
	/*
     *  When the mouse is pressed the editor is invoked. If you then then drag
     *  the mouse to another cell before releasing it, the editor is still
     *  active. Make sure editing is stopped when the mouse is released.
     */
    @Override
    public void mousePressed(MouseEvent e) {
        if (table.isEditing()
                && table.getCellEditor() == this) {
            isButtonColumnEditor = true;
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (isButtonColumnEditor
                && table.isEditing()) {
            table.getCellEditor().stopCellEditing();
        }

        isButtonColumnEditor = false;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }
}
