package org.w3c.dom.html2;

/**
 * Group options together in logical subdivisions. See the OPTGROUP element 
 * definition in HTML 4.01.
 * <p>See also the <a href='http://www.w3.org/TR/2003/REC-DOM-Level-2-HTML-20030109'>Document Object Model (DOM) Level 2 HTML Specification</a>.
 */
public interface HTMLOptGroupElement extends HTMLElement {
    /**
     * The control is unavailable in this context. See the disabled attribute 
     * definition in HTML 4.01.
     */
    public boolean getDisabled();
    /**
     * The control is unavailable in this context. See the disabled attribute 
     * definition in HTML 4.01.
     */
    public void setDisabled(boolean disabled);

    /**
     * Assigns a label to this option group. See the label attribute definition
     *  in HTML 4.01.
     */
    public String getLabel();
    /**
     * Assigns a label to this option group. See the label attribute definition
     *  in HTML 4.01.
     */
    public void setLabel(String label);

}
