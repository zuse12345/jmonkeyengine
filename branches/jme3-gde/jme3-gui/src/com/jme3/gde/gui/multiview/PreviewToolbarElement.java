/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jme3.gde.gui.multiview;

import com.jme3.gde.gui.NiftyGuiDataObject;
import org.netbeans.modules.xml.multiview.ToolBarMultiViewElement;
import org.netbeans.modules.xml.multiview.ui.SectionView;
import org.netbeans.modules.xml.multiview.ui.ToolBarDesignEditor;
import org.openide.nodes.Node;

/**
 *
 * @author normenhansen
 */
public class PreviewToolbarElement extends ToolBarMultiViewElement {

    private NiftyGuiDataObject dObj;
    private SectionView view;
    private ToolBarDesignEditor comp;
    private PreviewFactory factory;

    public PreviewToolbarElement(NiftyGuiDataObject dObj) {

        super(dObj);
        this.dObj = dObj;
        comp = new ToolBarDesignEditor();
        factory = new PreviewFactory(comp, dObj);
        setVisualEditor(comp);

    }

    public SectionView getSectionView() {

        return view;

    }

    public void componentShowing() {

        super.componentShowing();
        view = new TocView(dObj);
        comp.setContentView(view);
        view.open();

    }

    private class TocView extends SectionView {

        TocView(NiftyGuiDataObject dObj) {
            super(factory);
            String toc = dObj.getName();
            Node itemNode = new ItemNode(toc);
            setRoot(itemNode);
        }

    }

    private class ItemNode extends org.openide.nodes.AbstractNode {

        ItemNode(String toc) {
            super(org.openide.nodes.Children.LEAF);
            setDisplayName(dObj.getPrimaryFile().getNameExt());
        }

    }

}
