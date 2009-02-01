package org.extdoc;

/**
 * User: Andrey Zubkov
 * Date: 25.01.2009
 * Time: 16:38:55
 */
public class Source {
    
    private String src = null;
    private String match = "*.js";
    private Boolean skipHidden = null;

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public String getMatch() {
        return match;
    }

    public void setMatch(String match) {
        this.match = match;
    }

    public Boolean getSkipHidden() {
        return skipHidden;
    }

    public void setSkipHidden(Boolean skipHidden) {
        this.skipHidden = skipHidden;
    }
}
