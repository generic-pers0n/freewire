/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011-2023, FrostWire(R). All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.limegroup.gnutella.gui;

import com.limegroup.gnutella.gui.GUIUtils.SizePolicy;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.StringTokenizer;

/**
 * This class uses a <tt>JTextArea</tt> to simulate a <tt>JLabel</tt> that
 * allows multiple-line labels.  It does this by using JLabel's values for
 * border, font, etc.
 */
public class MultiLineLabel extends JTextArea {
    /**
     * The default pixel width for labels when the width is not
     * specified in the constructor.
     */
    private final static int DEFAULT_LABEL_WIDTH = 200;
    /**
     * The actual text, prior to \n being inserted.
     */
    private String _theText;
    /**
     * Resize handler to update the text layout when the parent is resized.
     * <p>
     * Is constructed lazily when the component obtains a parent.
     */
    private ResizeHandler resizeHandler = null;
    /**
     * Whether a resize handler should be installed.
     */
    private boolean resizable = false;

    /**
     * Creates a label that can have multiple lines and that has the
     * default width.
     *
     * @param s the <tt>String</tt> to display in the label
     */
    public MultiLineLabel(String s) {
        this(s, false);
    }

    public MultiLineLabel(String s, boolean resizable) {
        if (s == null) {
            throw new NullPointerException("null string in MultilineLabel");
        }
        this.setOpaque(false);
        setText(s);
        this.resizable = resizable;
    }

    /**
     * Creates a label with new lines inserted after the specified number
     * of pixels have been filled on each line.
     *
     * @param s      the <tt>String</tt> to display in the label
     * @param pixels the pixel limit for each line
     */
    public MultiLineLabel(String s, int pixels) {
        this(s, pixels, false);
    }

    public MultiLineLabel(String s, int pixels, boolean resizable) {
        if (s == null) {
            throw new NullPointerException("null string in MultilineLabel");
        }
        this.setOpaque(false);
        setText(s, pixels);
        this.resizable = resizable;
    }

    /**
     * New constructor that takes an array of strings.  This creates a
     * new <tt>MultiLineLabel</tt> with the string at each index in
     * the array placed on its own line.  The array cannot contain
     * any null strings.
     *
     * @param strs the array of strings that should each be placed on
     *             its own line in the label
     */
    public MultiLineLabel(String[] strs) {
        this.setOpaque(false);
        _theText = createSizedString(strs);
        super.setText(_theText);
    }

    /**
     * Creates a label that can have multiple lines and that sets the
     * number of rows and columns for the JTextArea.
     *
     * @param s      the <tt>String</tt> to display in the label
     * @param pixels the pixel limit for each line.
     * @param rows   the number of rows to include in the label
     * @param cols   the number of columns to include in the label
     */
    public MultiLineLabel(String s, int pixels, int rows, int cols) {
        super(rows, cols);
        if (s == null) {
            throw new NullPointerException("null string in MultilineLabel");
        }
        this.setOpaque(false);
        setText(s, pixels);
    }

    /**
     * Change the text before passing it up to the super setText.
     *
     * @param s      the <tt>String</tt> to display in the label
     * @param pixels the pixel limit for each line
     */
    private void setText(String s, int pixels) {
        _theText = s;
        super.setText(createSizedString(s, pixels));
    }

    /**
     * Gets the current text.
     */
    public String getText() {
        return _theText;
    }

    /**
     * Change the text before passing it up to the super setText.
     *
     * @param s the <tt>String</tt> to display in the label
     */
    public void setText(String s) {
        _theText = s;
        super.setText(createSizedString(s, DEFAULT_LABEL_WIDTH));
    }

    /**
     * Tells the look and feel to reset some values for this
     * component so that it doesn't use JTextArea's default values.
     * <p>
     * DO NOT CALL THIS METHOD YOURSELF!
     */
    public void updateUI() {
        super.updateUI();
        // refactor this method to allow the use of a skin UI, not critical now
        setBackground(new Color(255, 255, 255, 0));
        //setLineWrap(true);
        setWrapStyleWord(true);
        setHighlighter(null);
        setEditable(false);
        LookAndFeel.installBorder(this, "Label.border");
        LookAndFeel.installColorsAndFont(this, "Label.background", "Label.foreground", "Label.font");
        if (resizeHandler != null) {
            resizeHandler.componentResized(null);
        }
        // update restricted size
        SizePolicy policy = (SizePolicy) getClientProperty(SizePolicy.class);
        if (policy != null) {
            GUIUtils.restrictSize(this, policy);
        }
    }

    /**
     * Convert the input string to a string with newlines at the
     * closest word to the number of pixels specified in the 'pixels'
     * parameter.
     *
     * @param message the <tt>String</tt> to display in the label
     * @param pixels  the pixel width on each line before
     *                inserting a new line character
     */
    private String createSizedString(final String message, final int pixels) {
        FontMetrics fm = getFontMetrics(getFont());
        String word;
        //  Find if a single line is longer than the pixel limit.  If so, use
        //  that limit instead of pixels
        StringTokenizer st = new StringTokenizer(message);
        int newWidth = pixels;
        while (st.hasMoreTokens()) {
            word = st.nextToken();
            newWidth = Math.max(newWidth, fm.stringWidth(word));
        }
        //  layout multiple lines
        StringBuilder sb = new StringBuilder();
        StringBuilder cursb = new StringBuilder();
        boolean isNewLine;
        st = new StringTokenizer(message, " \n", true);
        while (st.hasMoreTokens()) {
            word = st.nextToken();
            if (word.equals(" "))
                continue;
            isNewLine = word.equals("\n");
            if (isNewLine || fm.stringWidth(cursb + word) > newWidth) {
                sb.append(cursb);
                sb.append("\n");
                cursb = new StringBuilder();
            }
            if (!isNewLine) {
                cursb.append(word);
                cursb.append(" ");
            }
        }
        sb.append(cursb);
        return sb.toString();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        checkAndAddResizeHandler();
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        Component parent = getParent();
        if (resizeHandler != null && parent != null) {
            parent.removeComponentListener(resizeHandler);
            resizeHandler = null;
        }
    }

    /**
     * Checks conditions and adds resize handler if they are met.
     */
    private void checkAndAddResizeHandler() {
        Component parent = getParent();
        if (parent != null && resizable && resizeHandler == null) {
            resizeHandler = new ResizeHandler();
            parent.addComponentListener(resizeHandler);
        }
    }

    /**
     * Creates a multiline label with one line taken up by each string
     * in the string array argument.
     *
     * @param strs the array of strings to put in the multiline label
     * @return a new string with newlines inserted at the appropriate
     * places
     */
    private String createSizedString(final String[] strs) {
        StringBuilder sb = new StringBuilder();
        for (String str : strs) {
            sb.append(str);
            sb.append("\n");
        }
        return sb.toString();
    }

    private class ResizeHandler extends ComponentAdapter {
        private int lastWidth = -1;

        @Override
        public void componentResized(ComponentEvent e) {
            int width = getSize().width;
            if (width != lastWidth) {
                lastWidth = width;
                setText(getText(), getSize().width);
            }
        }
    }
}
