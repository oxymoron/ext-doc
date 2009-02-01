package org.extdoc;

import java.util.List;
import java.util.ArrayList;
import java.io.File;

/**
 * User: Andrey Zubkov
 * Date: 25.01.2009
 * Time: 16:05:25
 */
public class Context {
    
    private List<File> sourceFiles = null;
    private File currentFile = null;
    private Integer positionInFile = 0;
    private StringBuilder commentBuffer = null;
    private StringBuilder codeBuffer = null;

    private List<Tag> currentTags = null;

    public List<File> getSourceFiles() {
        if (sourceFiles==null) sourceFiles = new ArrayList<File>();
        return sourceFiles;
    }

    public void addSourceFile(File sourceFile) {
        getSourceFiles().add(sourceFile);
    }

    public File getCurrentFile() {
        return currentFile;
    }

    public void setCurrentFile(File currentFile) {
        this.currentFile = currentFile;
    }

    public Integer getPositionInFile() {
        return positionInFile;
    }

    public void resetPositionInFile() {
        this.positionInFile = 0;
    }
    
    public void incrementPositionInFile() {
        this.positionInFile++;
    }

    public StringBuilder getCommentBuffer() {
        if (commentBuffer == null) commentBuffer = new StringBuilder();
        return commentBuffer;
    }

    public void resetCommentBuffer() {
        commentBuffer.setLength(0);
    }

    public void appendToCommentBuffer(Character ch) {
        commentBuffer.append(ch);
    }

    public void setCommentBuffer(StringBuilder commentBuffer) {
        this.commentBuffer = commentBuffer;
    }

    public StringBuilder getCodeBuffer() {
        if (codeBuffer == null) codeBuffer = new StringBuilder();
        return codeBuffer;
    }

    public void resetCodeBuffer() {
        codeBuffer.setLength(0);
    }

    public void appendToCodeBuffer(Character ch) {
        codeBuffer.append(ch);
    }

    public void resetBuffers(){
        resetCodeBuffer();
        resetCommentBuffer();
    }

    public List<Tag> getCurrentTags() {
        return currentTags;
    }

    public void setCurrentTags(List<Tag> currentTags) {
        this.currentTags = currentTags;
    }
}
