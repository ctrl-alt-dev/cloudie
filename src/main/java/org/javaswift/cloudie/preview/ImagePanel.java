/*
 *  Copyright 2012-2013 E.Hooijmeijer
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.javaswift.cloudie.preview;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import org.javaswift.cloudie.preview.PreviewPanel.PreviewComponent;


/**
 * ImagePanel, renders a given image on a panel.
 * @author E.Hooijmeijer
 */
public class ImagePanel extends JPanel implements PreviewComponent {

    private BufferedImage image;

    public ImagePanel() {
        super();
        setMinimumSize(new Dimension(128, 128));
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public void displayPreview(String contentType, ByteArrayInputStream in) {
        try {
            setImage(ImageIO.read(in));
        } catch (IOException e) {
            clearImage();
            e.printStackTrace();
        }
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public boolean supports(String type) {
        return "image/jpeg".equals(type) || "image/png".equals(type) || "image/gif".equals(type);
    }

    /**
     * @param image the image to set
     */
    public void setImage(BufferedImage image) {
        this.image = image;
        repaint();
    }

    public void clearImage() {
        this.image = null;
        repaint();
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    protected void paintComponent(Graphics g) {
        if (image != null) {
            g.drawImage(image, 0, 0, this.getWidth(), this.getHeight(), this);
        } else {
            g.setColor(Color.LIGHT_GRAY);
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }

}
