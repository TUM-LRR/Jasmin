package jasmin.gui;

import javax.swing.text.AbstractDocument.DefaultDocumentEvent;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;
/*
 * NoStyleUndo.java
 *  
 * Created on 17. Oktober 2006, 08:31
 *
 */

/**
 * This class capsulates an DefaultDocumentEvent to mark its UndoEdit as not signifcant for the UndoManager. 
 * @author Kai Orend
 */




public class NoStyleUndo implements UndoableEdit {
    
    DefaultDocumentEvent dde=null;
    
    
    public NoStyleUndo(DefaultDocumentEvent dde) {
        this.dde=dde;
    }
    
    public void undo() throws CannotUndoException {
        dde.undo();
    }
    
    public boolean canUndo() {
        return dde.canUndo();
    }
    
    public void redo() throws CannotRedoException {
        dde.redo();
    }
    
    public boolean canRedo() {
        return dde.canRedo();
    }
    
    public void die() {
        dde.die();
    }
    
    public boolean addEdit(UndoableEdit anEdit) {
        return dde.addEdit(anEdit);
    }
    
    public boolean replaceEdit(UndoableEdit anEdit) {
        return dde.replaceEdit(anEdit);
    }
    
    public boolean isSignificant() {
        return false;
    }
    
    public String getPresentationName() {
        return dde.getPresentationName();
    }
    
    public String getUndoPresentationName() {
        return dde.getUndoPresentationName();
    }
    
    public String getRedoPresentationName() {
        return dde.getRedoPresentationName();
    }
}

