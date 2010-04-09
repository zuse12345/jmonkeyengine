/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jme3.gde.gui.multiview;

import com.jme3.gde.gui.NiftyGuiDataObject;
import org.netbeans.modules.xml.multiview.ui.InnerPanelFactory;
import org.netbeans.modules.xml.multiview.ui.SectionInnerPanel;
import org.netbeans.modules.xml.multiview.ui.SectionView;
import org.netbeans.modules.xml.multiview.ui.ToolBarDesignEditor;

/**
 *
 * @author normenhansen
 */
public class PreviewFactory implements InnerPanelFactory {

    private NiftyGuiDataObject dObj;
    private ToolBarDesignEditor editor;

    PreviewFactory(ToolBarDesignEditor editor, NiftyGuiDataObject dObj) {
        this.dObj=dObj;
        this.editor=editor;
    }

    public SectionInnerPanel createInnerPanel(Object key) {
       return new PreviewPanel((SectionView)editor.getContentView());
    }

}
