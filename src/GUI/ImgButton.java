
/*
 * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle or the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */ 



/*
 * Used by BoxLayoutDemo.java.
*/

package GUI;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

class ImgButton extends JComponent implements MouseListener {
	private static final long serialVersionUID = 1L;
	private final Dimension preferredSize;
    private String Name;
	private boolean activated=false;

	public ImgButton(float alignmentX, int shortSideSize, String Name, boolean activated) {
    	this.activated = activated;
        this.Name = Name;
        preferredSize = new Dimension(shortSideSize*2, shortSideSize);
    }

    public void paint(Graphics g) {
        int width = getWidth();
        int height = getHeight();
        //TODO: change placeholder because of missing copyright
        Image img_gray = Toolkit.getDefaultToolkit().getImage("GrayButton.jpg");
        Image img_yellow = Toolkit.getDefaultToolkit().getImage("YellowButton.jpg");

        if(this.activated == true){
        	g.drawImage(img_yellow, 0, 0, width, height, null);
        	g.setColor(Color.black);
        } else {
        	g.drawImage(img_gray, 0, 0, width, height, null);
        	g.setColor(new Color(100,100,100));
        }
        g.setFont(new Font("Serif",Font.BOLD,17));
        g.drawString(this.Name, 40, height/2+5);
        
    }
    
    public Dimension getPreferredSize() {
        return preferredSize;
    }

    public Dimension getMinimumSize() {
        return preferredSize;
    }

    public Dimension getMaximumSize() {
        return super.getMaximumSize();
    }
    
    public boolean getActivated() {
		return activated;
	}

	public void setActivated(boolean activated) {
		this.activated = activated;
	}
	
    public String getName() {
		return Name;
	}
    
	public void setName(String Name) {
		this.Name = Name;
	}

	public void mousePressed(MouseEvent e) { }
    public void mouseReleased(MouseEvent e) { }
    public void mouseEntered(MouseEvent e) { }
    public void mouseExited(MouseEvent e) { }
    public void mouseClicked(MouseEvent e) { }
    
}
