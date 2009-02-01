package org.extdoc;

import java.util.Set;

/**
 * User: Andrey Zubkov
 * Date: 25.01.2009
 * Time: 17:35:04
 */
public class StandardCommentPreprocessor implements CommentPreprocessor{

    private Set<Character> assumeWhitespace = null;
    private enum State {SPACE, DESCRIPTION}

    public Set<Character> getAssumeWhitespace() {
        return assumeWhitespace;
    }

    public void setAssumeWhitespace(Set<Character> assumeWhitespace) {
        this.assumeWhitespace = assumeWhitespace;
    }

    private Boolean isWhitespace(Character ch){
        return Character.isWhitespace(ch) || isSpecial(ch);
    }

    private Boolean isSpecial(Character ch){
        return getAssumeWhitespace().contains(ch);
    }

    public void process(Context context){
        State state = State.SPACE;
        StringBuilder buffer = new StringBuilder();
        StringBuilder spaceBuffer = new StringBuilder();
        // special means specified in assumeWhitespace
        boolean foundSpecialCharacter = false;
        StringBuilder comment = context.getCommentBuffer();
        int len = comment.length();
        for (int i=0;i<len;i++){
            char ch = comment.charAt(i);
            switch (state){
                case SPACE:
                    if (isWhitespace(ch)){
                        if (isSpecial(ch)){
                            foundSpecialCharacter = true;
                        }
                        spaceBuffer.append(ch);
                        break;
                    }
                    if (!foundSpecialCharacter){
                        buffer.append(spaceBuffer);
                    }
                    spaceBuffer.setLength(0);
                    state = State.DESCRIPTION;
                    /* fall through */
                case DESCRIPTION:
                    if (ch == '\n'){
                        foundSpecialCharacter = false;
                        state = State.SPACE;
                    }
                    buffer.append(ch);
                    break;
            }
        }
        comment.setLength(0);
        comment.append(buffer);
    }

}
