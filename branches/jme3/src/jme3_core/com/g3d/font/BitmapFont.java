package com.g3d.font;

import com.g3d.material.Material;

/**
 *
 * @author dhdd
 *
 *         Represents a font within jME that is generated with the AngelCode Bitmap Font Generator
 */
public class BitmapFont {

    public enum Align {
        Left, Center, Right
    }

    private BitmapCharacterSet charSet;
    private Material[] pages;

    public BitmapFont() {
    }

    public void setCharSet(BitmapCharacterSet charSet) {
        this.charSet = charSet;
    }

    public void setPages(Material[] pages) {
        this.pages = pages;
    }

    public Material getPage(int index) {
        return pages[index];
    }

    public BitmapCharacterSet getCharSet() {
        return charSet;
    }
    
    /**
     * Gets the line height of a StringBlock.
     * @param sb
     * @return
     */
    public float getLineHeight(StringBlock sb) {
        return charSet.getLineHeight() * (sb.getSize() / charSet.getRenderedSize());
    }

    private Kerning findKerningNode(int newLineLastChar, int nextChar) {
        BitmapCharacter c = charSet.getCharacter(newLineLastChar);
        for (Kerning k : c.getKerningList()){
            if (k.getSecond() == nextChar){
                return k;
            }
        }

        return null;
    }

    public float updateText(StringBlock block, QuadList target, boolean rightToLeft) {

        String text = block.getText();
        float x = 0;
        float y = 0;
        float lineWidth = 0f;
        float sizeScale = (float) block.getSize() / charSet.getRenderedSize();
        char lastChar = 0;
        int lineNumber = 1;
        int wordNumber = 1;
        int quadIndex = -1;
        float wordWidth = 0f;
        boolean firstCharOfLine = true;
        boolean useKerning = block.isKerning();
        target.setActualSize(text.length());

        float incrScale = rightToLeft ? -1f : 1f;

        for (int i = 0; i < text.length(); i++){
            char theChar = text.charAt(i);
            BitmapCharacter c = charSet.getCharacter((int) theChar);
            if (c == null){
//                logger.warning("Character '" + text.charAt(i) + "' is not in alphabet, skipping it.");
            }else if (theChar == '\n' || theChar == '\r' || theChar == '\t'){
                // dont print these characters
                continue;
            }else{
                float xOffset = c.getXOffset() * sizeScale;
                float yOffset = c.getYOffset() * sizeScale;
                float xAdvance = c.getXAdvance() * sizeScale;
                float width = c.getWidth() * sizeScale;
                float height = c.getHeight() * sizeScale;

                // Adjust for kerning
                float kernAmount = 0f;
                if (!firstCharOfLine && useKerning){
                    Kerning kern = findKerningNode(lastChar, theChar);
                    if (kern != null){
                        kernAmount = kern.getAmount() * sizeScale;
                        x += kernAmount * incrScale;
                        lineWidth += kernAmount;
                        wordWidth += kernAmount;
                    }
                }
                firstCharOfLine = false;

                // Create the quad
                quadIndex++;
                FontQuad q = target.getQuad(quadIndex);

                // Determine quad position
                float quadPosX = x + (xOffset * incrScale);
                if (rightToLeft){
                    quadPosX -= width;
                }

                float quadPosY = y - yOffset;
                q.setPosition(quadPosX, quadPosY);
                q.setSize(width, height);

                float u0 = (float) c.getX() / charSet.getWidth();
                float v0 = (float) c.getY() / charSet.getHeight();
                float w = (float) c.getWidth() / charSet.getWidth();
                float h = (float) c.getHeight() / charSet.getHeight();
                q.setUV(u0, v0, w, h);

                q.setColor(block.getColor());
                q.setLineNumber(lineNumber);

                if (theChar == ' '){
                    // since this is a space,
                    // increment wordnumber and reset wordwidth
                    wordNumber++;
                    wordWidth = 0f;
                }

                // set data
                q.setWordNumber(wordNumber);
                q.setWordWidth(wordWidth);
                q.setBitmapChar(c);
                q.setSizeScale(sizeScale);
                q.setCharacter(text.charAt(i));
                q.setTotalWidth(kernAmount + xAdvance);

                x += xAdvance * incrScale;
                wordWidth += xAdvance;
                lineWidth += xAdvance;

                lastChar = theChar;
            }
        }

        Align alignment = block.getAlignment();
        // Justify the last (now complete) line
        if (alignment == Align.Center){
            for (int k = 0; k < target.getQuantity(); k++){
                FontQuad q = target.getQuad(k);
                if (q.getLineNumber() == lineNumber){
                    q.setX(q.getX() - lineWidth / 2f);
                }
            }
        }
        if (alignment == Align.Right){
            for (int k = 0; k < target.getQuantity(); k++){
                FontQuad q = target.getQuad(k);
                if (q.getLineNumber() == lineNumber){
                    q.setX(q.getX() - lineWidth);
                }
            }
        }
        if (rightToLeft){
            // move all characters so that the current X = 0
            for (int k = 0; k < target.getQuantity(); k++){
                FontQuad q = target.getQuad(k);
                if (q.getLineNumber() == lineNumber){
                    q.setX(q.getX() + lineWidth);
                }
            }
        }

        return lineWidth;
    }

    public void updateTextRect(StringBlock b, QuadList target) {

        String text = b.getText();
        float x = b.getTextBox().x;
        float y = b.getTextBox().y;
        float maxWidth = b.getTextBox().width;
        float lastLineWidth = 0f;
        float lineWidth = 0f;
        float sizeScale = b.getSize() / charSet.getRenderedSize();
        char lastChar = 0;
        int lineNumber = 1;
        int wordNumber = 1;
        int quadIndex = -1;
        float wordWidth = 0f;
        boolean firstCharOfLine = true;
        boolean useKerning = b.isKerning();
        Align alignment = b.getAlignment();

        target.setActualSize(text.length());

        for (int i = 0; i < text.length(); i++){
            BitmapCharacter c = charSet.getCharacter((int) text.charAt(i));

            if (c == null){
//        logger.warning("Character '" + text.charAt(i) + "' is not in alphabet, skipping it.");
            }else{
                float xOffset = c.getXOffset() * sizeScale;
                float yOffset = c.getYOffset() * sizeScale;
                float xAdvance = c.getXAdvance() * sizeScale;
                float width = c.getWidth() * sizeScale;
                float height = c.getHeight() * sizeScale;

                // Newline
                if (text.charAt(i) == '\n' || text.charAt(i) == '\r' || (lineWidth + xAdvance >= maxWidth)){
                    x = b.getTextBox().x;
                    y -= charSet.getLineHeight() * sizeScale;
//                    float offset = 0f;
                    if ((lineWidth + xAdvance >= maxWidth) && (wordNumber != 1)){
                        // Next character extends past text box width
                        // We have to move the last word down one line
                        char newLineLastChar = 0;
                        lastLineWidth = lineWidth;
                        lineWidth = 0f;

                        for (int j = 0; j <= quadIndex; j++){
                            FontQuad q = target.getQuad(j);
                            BitmapCharacter localChar = q.getBitmapChar();

                            float localxOffset = localChar.getXOffset() * sizeScale;
                            float localyOffset = localChar.getYOffset() * sizeScale;
                            float localxAdvance = localChar.getXAdvance() * sizeScale;

                            // Move current word to the left side of the text box
                            if ((q.getLineNumber() == lineNumber) && (q.getWordNumber() == wordNumber)){
                                if (alignment == Align.Left && q.getCharacter() == ' '){
                                    continue;
                                }
                                q.setLineNumber(q.getLineNumber() + 1);
                                q.setWordNumber(1);
                                float quadPosX = x + localxOffset;
                                float quadPosY = y - localyOffset;
                                q.setPosition(quadPosX, quadPosY);

                                x += localxAdvance;
                                lastLineWidth -= localxAdvance;
                                lineWidth += localxAdvance;
                                Kerning kern = findKerningNode(newLineLastChar, q.getCharacter());
                                if (kern != null && useKerning){
                                    x += kern.getAmount() * sizeScale;
                                    lineWidth += kern.getAmount() * sizeScale;
                                }
                            }

                            newLineLastChar = q.getCharacter();
                        }

                        // Justify the previous (now complete) line
                        if (alignment == Align.Center){
                            for (int k = 0; k < target.getQuantity(); k++){
                                FontQuad q = target.getQuad(k);

                                if (q.getLineNumber() == lineNumber){
                                    q.setX(q.getX() + b.getTextBox().width / 2f - lastLineWidth / 2f);
                                }
                            }
                        }
                        if (alignment == Align.Right){
                            for (int k = 0; k < target.getQuantity(); k++){
                                FontQuad q = target.getQuad(k);
                                if (q.getLineNumber() == lineNumber){
                                    q.setX(q.getX() + b.getTextBox().width - lastLineWidth);
                                }
                            }
                        }

                    }else{
                        // New line without any "carry-down" word
                        firstCharOfLine = true;
                        lastLineWidth = lineWidth;
                        lineWidth = 0f;
                    }

                    wordNumber = 1;
                    lineNumber++;

                } // End new line check

                // Dont print these
                if (text.charAt(i) == '\n' || text.charAt(i) == '\r' || text.charAt(i) == '\t'){
                    continue;
                }

                // Set starting cursor for alignment
                if (firstCharOfLine){
                    x = b.getTextBox().x;
                }

                // Adjust for kerning
                float kernAmount = 0f;
                if (!firstCharOfLine && useKerning){
                    Kerning kern = findKerningNode(lastChar, (char) text.charAt(i));
                    if (kern != null){
                        kernAmount = kern.getAmount() * sizeScale;
                        x += kernAmount;
                        lineWidth += kernAmount;
                        wordWidth += kernAmount;
                    }
                }
                firstCharOfLine = false;

                // edit the quad
                quadIndex++;
                FontQuad q = target.getQuad(quadIndex);

                float quadPosX = x + (xOffset);
                float quadPosY = y - yOffset;
                q.setPosition(quadPosX, quadPosY);
                q.setSize(width, height);

                float u0 = (float) c.getX() / charSet.getWidth();
                float v0 = (float) c.getY() / charSet.getHeight();
                float w = (float) c.getWidth() / charSet.getWidth();
                float h = (float) c.getHeight() / charSet.getHeight();
                q.setUV(u0, v0, w, h);
                q.setColor(b.getColor());

                q.setLineNumber(lineNumber);
                if (text.charAt(i) == ' '){
                    wordNumber++;
                    wordWidth = 0f;
                }
                q.setWordNumber(wordNumber);
                wordWidth += xAdvance;
                q.setWordWidth(wordWidth);
                q.setBitmapChar(c);
                q.setSizeScale(sizeScale);
                q.setCharacter(text.charAt(i));

                x += xAdvance;
                lineWidth += xAdvance;
                lastChar = text.charAt(i);

            }
        }

        // Justify the last (now complete) line
        if (alignment == Align.Center){
            for (int k = 0; k < target.getQuantity(); k++){
                FontQuad q = target.getQuad(k);
                if (q.getLineNumber() == lineNumber){
                    q.setX(q.getX() + b.getTextBox().width / 2f - lineWidth / 2f);
                }
            }
        }
        if (alignment == Align.Right){
            for (int k = 0; k < target.getQuantity(); k++){
                FontQuad q = target.getQuad(k);
                if (q.getLineNumber() == lineNumber){
                    q.setX(q.getX() + b.getTextBox().width - lineWidth);
                }
            }
        }
    }






}