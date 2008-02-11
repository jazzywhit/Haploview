/*
* %W% %E%
*
* Copyright 1997, 1998 Sun Microsystems, Inc. All Rights Reserved.
*
* Redistribution and use in source and binary forms, with or
* without modification, are permitted provided that the following
* conditions are met:
*
* - Redistributions of source code must retain the above copyright
*   notice, this list of conditions and the following disclaimer.
*
* - Redistribution in binary form must reproduce the above
*   copyright notice, this list of conditions and the following
*   disclaimer in the documentation and/or other materials
*   provided with the distribution.
*
* Neither the name of Sun Microsystems, Inc. or the names of
* contributors may be used to endorse or promote products derived
* from this software without specific prior written permission.
*
* This software is provided "AS IS," without a warranty of any
* kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND
* WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY
* EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY
* DAMAGES OR LIABILITIES SUFFERED BY LICENSEE AS A RESULT OF OR
* RELATING TO USE, MODIFICATION OR DISTRIBUTION OF THIS SOFTWARE OR
* ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE
* FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT,
* SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
* CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF
* THE USE OF OR INABILITY TO USE THIS SOFTWARE, EVEN IF SUN HAS
* BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
*
* You acknowledge that this software is not designed, licensed or
* intended for use in the design, construction, operation or
* maintenance of any nuclear facility.
*/

package edu.mit.wi.haploview.TreeTable;

import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.table.*;

import java.awt.Component;
import java.awt.Graphics;

/**
 * This example shows how to create a simple JTreeTable component, 
 * by using a JTree as a renderer (and editor) for the cells in a 
 * particular column in the JTable.  
 *
 * @version %I% %G%
 *
 * @author Philip Milne
 * @author Scott Violet
 */

public class JTreeTable extends JTable {
    static final long serialVersionUID = 2438273584963759245L;
    protected TreeTableCellRenderer tree;

    public JTreeTable(TreeTableModel treeTableModel) {
        super();

        // Create the tree. It will be used as a renderer and editor.
        tree = new TreeTableCellRenderer(treeTableModel);

        // Install a tableModel representing the visible rows in the tree.
        super.setModel(new TreeTableModelAdapter(treeTableModel, tree));

        // Force the JTable and JTree to share their row selection models.
        tree.setSelectionModel(new DefaultTreeSelectionModel() {
	        static final long serialVersionUID = -7983407332773933546L;
            // Extend the implementation of the constructor, as if:
            /* public this() */ {
                setSelectionModel(listSelectionModel);
            }
        });
        // Make the tree and table row heights the same.
        tree.setRowHeight(getRowHeight());

        // Install the tree renderer and editor.
        setDefaultRenderer(TreeTableModel.class, tree);
        setDefaultEditor(TreeTableModel.class, new TreeTableCellEditor());

    }

    public JTree getTree() {
        return tree;
    }

    /* Workaround for BasicTableUI anomaly. Make sure the UI never tries to
    * paint the editor. The UI currently uses different techniques to
    * paint the renderers and editors and overriding setBounds() below
    * is not the right thing to do for an editor. Returning -1 for the
    * editing row in this case, ensures the editor is never painted.
    */
    public int getEditingRow() {
        return (getColumnClass(editingColumn) == TreeTableModel.class) ? -1 : editingRow;  
    }

    //
    // The renderer used to display the tree nodes, a JTree.  
    //

    public class TreeTableCellRenderer extends JTree implements TableCellRenderer {
        static final long serialVersionUID = -2538618387281265863L;

        protected int visibleRow;

        public TreeTableCellRenderer(TreeModel model) {
            super(model);
        }

        public void setBounds(int x, int y, int w, int h) {
            super.setBounds(x, 0, w, JTreeTable.this.getHeight());
        }

        public void paint(Graphics g) {
            g.translate(0, -visibleRow * getRowHeight());
            super.paint(g);
        }

        public Component getTableCellRendererComponent(JTable table,
                                                       Object value,
                                                       boolean isSelected,
                                                       boolean hasFocus,
                                                       int row, int column) {
            if(isSelected)
                setBackground(table.getSelectionBackground());
            else
                setBackground(table.getBackground());

            visibleRow = row;
            return this;
        }
    }

    //
    // The editor used to interact with tree nodes, a JTree.  
    //

    public class TreeTableCellEditor extends AbstractCellEditor implements TableCellEditor {
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int r, int c) {
            return tree;
        }
    }

}

