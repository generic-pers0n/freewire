/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011-2021, FrostWire(R). All rights reserved.
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

import com.frostwire.util.OSUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

class ShutdownWindow extends JDialog {
    /**
     *
     */
    private static final long serialVersionUID = 446845150731872693L;

    ShutdownWindow() {
        super(GUIMediator.getAppFrame());

        setResizable(false);
        setTitle(I18n.tr("Shutting down FrostWire..."));
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // Add the label
        JLabel label = new JLabel(I18n.tr("Please wait while FrostWire shuts down..."));
        add(label);

        // Add the progress bar
        JProgressBar bar = new LimeJProgressBar();
        bar.setIndeterminate(true);
        bar.setStringPainted(false);
        add(bar);

        // Layout the final dialog
        pack();
    }

    public static void main(String[] args) {
        ShutdownWindow window = new ShutdownWindow();
        window.setVisible(true);
    }
}