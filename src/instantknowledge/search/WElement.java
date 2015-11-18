/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package instantknowledge.search;

/**
 *
 * @author Aale
 */
class WElement {

    String path;
    String field;

    public WElement(String path, String field) {
        this.path = path;
        this.field = field;
    }

    public WElement() {
    }

    public String getField() {
        return field;
    }

    public String getPath() {
        return path;
    }

    public void setField(String field) {
        this.field = field;
    }

    public void setPath(String path) {
        this.path = path;
    }

}
