/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
*	Tiny Look and Feel                                                         *
*                                                                              *
*  (C) Copyright 2003 - 2007 Hans Bickel                                       *
*                                                                              *
*   For licensing information and credits, please refer to the                 *
*   comment in file de.muntjak.tinylookandfeel.TinyLookAndFeel                 *
*                                                                              *
* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

package de.muntjak.tinylookandfeel;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JSlider;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicSliderUI;
import javax.swing.plaf.metal.MetalSliderUI;

import de.muntjak.tinylookandfeel.controlpanel.ColorRoutines;
import de.muntjak.tinylookandfeel.controlpanel.SBChooser;


/**
 * TinySliderUI
 * 
 * @version 1.0
 * @author Hans Bickel
 */
public class TinySliderUI extends MetalSliderUI {
	
	/* the only instance of the stroke for the focus */
	private static BasicStroke focusStroke =
		new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
		1.0f, new float[] { 1.0f, 1.0f }, 0.0f);

	protected boolean isRollover = false, wasRollover = false;
	protected boolean isDragging = false;

	protected TrackListener trackListener;

	protected TrackListener createTrackListener(JSlider slider) {
		return new MyTrackListener();
	}

	protected Dimension getThumbSize() {
		if (slider.getOrientation() == JSlider.VERTICAL) {
			return Theme.sliderVertSize[Theme.derivedStyle[Theme.style]];
		} else {
			return Theme.sliderHorzSize[Theme.derivedStyle[Theme.style]];
		}
	}
	
	/**
     * Returns the shorter dimension of the track.
     */
    protected int getTrackWidth() {
        return 4;
    }

	
	public void paintThumb(Graphics g) {
		Color c = null;
		
		if(!slider.isEnabled()) {
			c = Theme.sliderThumbDisabledColor[Theme.style].getColor();
		}
		else if(isDragging) {
			c = Theme.sliderThumbPressedColor[Theme.style].getColor();
		}
		else if(isRollover && Theme.sliderRolloverEnabled[Theme.style]) {
			if(Theme.derivedStyle[Theme.style] == Theme.YQ_STYLE) {
				c = Theme.sliderThumbColor[Theme.style].getColor();
			}
			else {
				c = Theme.sliderThumbRolloverColor[Theme.style].getColor();
			}
		}
		else {
			c = Theme.sliderThumbColor[Theme.style].getColor();
		}
		
		g.setColor(c);
		
		switch(Theme.derivedStyle[Theme.style]) {
			case Theme.TINY_STYLE:
				drawTinyThumb(g, c);
				break;
			case Theme.W99_STYLE:
				drawWinThumb(g, c);
				break;
			case Theme.YQ_STYLE:
				drawXpThumb(g);
				break;
		}
	}
	
	private void drawTinyThumb(Graphics g, Color c) {
		int x1 = thumbRect.x;
		int y1 = thumbRect.y;
		int x2 = x1 + thumbRect.width - 1;
		int y2 = y1 + thumbRect.height - 1;
		
		g.fillRect(x1 + 1, y1 + 1,  thumbRect.width - 2, thumbRect.height - 2);
	}
	
	private void drawWinThumb(Graphics g, Color c) {
		int x1 = thumbRect.x;
		int y1 = thumbRect.y;
		int x2 = x1 + thumbRect.width - 1;
		int y2 = y1 + thumbRect.height - 1;
		
		if(slider.getOrientation() == JSlider.HORIZONTAL) {
			g.fillRect(x1 + 1, y1 + 1,  thumbRect.width - 2, 15);
			g.drawLine(x1 + 2, y1 + 16, x1 + 7, y1 + 16);
			g.drawLine(x1 + 3, y1 + 17, x1 + 6, y1 + 17);
			g.drawLine(x1 + 4, y1 + 18, x1 + 5, y1 + 18);

			if(isDragging && slider.isEnabled()) {
				g.setColor(Theme.sliderThumbColor[Theme.style].getColor());
				g.drawLine(x1 + 1, y1 + 1, x2 - 2, y1 + 1);
				g.drawLine(x1 + 1, y1 + 2, x1 + 1, y1 + 16);
			}
			
			if(!slider.isEnabled()) {
				g.setColor(Theme.sliderLightDisabledColor[Theme.style].getColor());
			}
			else {
				g.setColor(Theme.sliderLightColor[Theme.style].getColor());
			}
			g.drawLine(x1, y1, x1, y1 + 15);
			g.drawLine(x1 + 1, y1, x2 - 1, y1);
			g.drawLine(x1 + 1, y1 + 16, x1 + 1, y1 + 16);
			g.drawLine(x1 + 2, y1 + 17, x1 + 2, y1 + 17);
			g.drawLine(x1 + 3, y1 + 18, x1 + 3, y1 + 18);
			g.drawLine(x1 + 4, y1 + 19, x1 + 4, y1 + 19);
			
			if(!slider.isEnabled()) {
				g.setColor(Theme.sliderDarkDisabledColor[Theme.style].getColor());
			}
			else {
				g.setColor(Theme.sliderDarkColor[Theme.style].getColor());
			}
			g.drawLine(x2 - 1, y1 + 1, x2 - 1, y1 + 15);
			g.drawLine(x1 + 8, y1 + 16, x1 + 8, y1 + 16);
			g.drawLine(x1 + 7, y1 + 17, x1 + 7, y1 + 17);
			g.drawLine(x1 + 6, y1 + 18, x1 + 6, y1 + 18);
			g.drawLine(x1 + 5, y1 + 19, x1 + 5, y1 + 19);
			
			if(!slider.isEnabled()) {
				g.setColor(Theme.sliderBorderDisabledColor[Theme.style].getColor());
			}
			else {
				g.setColor(Theme.sliderBorderColor[Theme.style].getColor());
			}
			g.drawLine(x2, y1, x2, y1 + 15);
			g.drawLine(x1 + 9, y1 + 16, x1 + 9, y1 + 16);
			g.drawLine(x1 + 8, y1 + 17, x1 + 8, y1 + 17);
			g.drawLine(x1 + 7, y1 + 18, x1 + 7, y1 + 18);
			g.drawLine(x1 + 6, y1 + 19, x1 + 6, y1 + 19);
			g.drawLine(x1 + 5, y1 + 20, x1 + 5, y1 + 20);
		}
		else {
			g.fillRect(x1 + 2, y1 + 1,  thumbRect.width - 2, thumbRect.height - 2);
			
			if(isDragging && slider.isEnabled()) {
				g.setColor(Theme.sliderThumbColor[Theme.style].getColor());
				g.drawLine(x1 + 2, y1 + 1, x2 - 2, y1 + 1);
				g.drawLine(x1 + 2, y1 + 2, x1 + 2, y2 - 2);
			}
			
			if(!slider.isEnabled()) {
				g.setColor(Theme.sliderLightDisabledColor[Theme.style].getColor());
			}
			else {
				g.setColor(Theme.sliderLightColor[Theme.style].getColor());
			}
			g.drawLine(x1 + 1, y1, x1 + 1, y2 - 1);
			g.drawLine(x1 + 1, y1, x2 - 1, y1);

			if(!slider.isEnabled()) {
				g.setColor(Theme.sliderDarkDisabledColor[Theme.style].getColor());
			}
			else {
				g.setColor(Theme.sliderDarkColor[Theme.style].getColor());
			}
			g.drawLine(x1 + 2, y1 + 9, x2 - 1, y1 + 9);
			g.drawLine(x2 - 1, y1 + 1, x2 - 1, y2 - 2);
			
			if(!slider.isEnabled()) {
				g.setColor(Theme.sliderBorderDisabledColor[Theme.style].getColor());
			}
			else {
				g.setColor(Theme.sliderBorderColor[Theme.style].getColor());
			}
			g.drawLine(x1 + 1, y2, x2, y2);
			g.drawLine(x2, y1, x2, y2);
		}
	}
	
	private void drawXpThumb(Graphics g) {
		int x1 = thumbRect.x;
		int y1 = thumbRect.y;
		int x2 = x1 + thumbRect.width - 1;
		int y2 = y1 + thumbRect.height - 1;
		
		if(slider.getPaintTicks()) {
			// draw arrow-like thumb
			if(slider.getOrientation() == JSlider.HORIZONTAL) {
				g.fillRect(x1 + 1, y1 + 4, thumbRect.width - 4, thumbRect.height - 8);
				g.drawLine(x1 + 5, y2 - 3, x1 + 5, y2 - 3);

				Color c = g.getColor();
				g.setColor(ColorRoutines.darken(c, 10));
				g.drawLine(x2 - 2, y1 + 4, x2 - 2, y2 - 6);
				g.setColor(ColorRoutines.darken(c, 20));
				g.drawLine(x2 - 1, y1 + 4, x2 - 1, y2 - 7);
				
				Color bc = null;
				if(!slider.isEnabled()) {
					bc = Theme.sliderBorderDisabledColor[Theme.style].getColor();
				}
				else {
					bc = Theme.sliderBorderColor[Theme.style].getColor();
				}
				g.setColor(bc);
				g.drawLine(x1 + 1, y1, x2 - 1, y1);
				g.drawLine(x1, y1 + 1, x1, y2 - 5);
				g.drawLine(x1 + 1, y2 - 4, x1 + 1, y2 - 4);
				g.drawLine(x1 + 2, y2 - 3, x1 + 2, y2 - 3);
				g.drawLine(x1 + 3, y2 - 2, x1 + 3, y2 - 2);
				g.drawLine(x1 + 4, y2 - 1, x1 + 4, y2 - 1);
	
				g.setColor(Theme.sliderDarkColor[Theme.style].getColor());
				g.drawLine(x2, y1 + 1, x2, y2 - 5);
				g.drawLine(x2 - 1, y2 - 4, x2 - 1, y2 - 4);
				g.drawLine(x2 - 2, y2 - 3, x2 - 2, y2 - 3);
				g.drawLine(x2 - 3, y2 - 2, x2 - 3, y2 - 2);
				g.drawLine(x2 - 4, y2 - 1, x2 - 4, y2 - 1);
				g.drawLine(x2 - 5, y2, x2 - 5, y2);

				Color col = null;
				if(!slider.isEnabled()) {
					col = Theme.sliderBorderDisabledColor[Theme.style].getColor();
					g.setColor(col);
				}
				else if(!isDragging && isRollover && Theme.sliderRolloverEnabled[Theme.style]) {
					col = Theme.sliderThumbRolloverColor[Theme.style].getColor();
				}
				else {
					col = Theme.sliderLightColor[Theme.style].getColor();
				}
				
				Color c2 = SBChooser.getAdjustedColor(col, 67, 39);
				if(slider.isEnabled()) g.setColor(c2);
				g.drawLine(x1 + 1, y1 + 1, x2 - 1, y1 + 1);
				g.drawLine(x1 + 1, y2 - 6, x1 + 1, y2 - 6);
				g.drawLine(x1 + 2, y2 - 5, x1 + 2, y2 - 5);
				g.drawLine(x1 + 3, y2 - 4, x1 + 3, y2 - 4);

				if(slider.isEnabled()) g.setColor(ColorRoutines.getAverage(bc, c2));
				g.drawLine(x1 + 1, y2 - 5, x1 + 1, y2 - 5);
				g.drawLine(x1 + 2, y2 - 4, x1 + 2, y2 - 4);
				g.drawLine(x1 + 3, y2 - 3, x1 + 3, y2 - 3);

				Color c3 = ColorRoutines.getAverage(col, c2);
				if(slider.isEnabled()) g.setColor(c3);
				g.drawLine(x1 + 1, y1 + 2, x2 - 1, y1 + 2);
				g.drawLine(x1 + 4, y2 - 3, x1 + 4, y2 - 3);
				g.drawLine(x1 + 5, y2 - 2, x1 + 5, y2 - 2);
				g.drawLine(x1 + 6, y2 - 3, x1 + 6, y2 - 3);

				if(slider.isEnabled()) g.setColor(ColorRoutines.getAverage(bc, c3));
				g.drawLine(x1 + 4, y2 - 2, x1 + 4, y2 - 2);
				g.drawLine(x1 + 5, y2 - 1, x1 + 5, y2 - 1);
				g.drawLine(x1 + 6, y2 - 2, x1 + 6, y2 - 2);
					
				if(slider.isEnabled()) g.setColor(col);
				g.drawLine(x1 + 1, y1 + 3, x2 - 1, y1 + 3);
				g.drawLine(x1 + 9, y2 - 6, x1 + 9, y2 - 6);
				g.drawLine(x1 + 8, y2 - 5, x1 + 8, y2 - 5);
				g.drawLine(x1 + 7, y2 - 4, x1 + 7, y2 - 4);

				if(slider.isEnabled()) g.setColor(ColorRoutines.getAverage(bc, col));
				g.drawLine(x1 + 9, y2 - 5, x1 + 9, y2 - 5);
				g.drawLine(x1 + 8, y2 - 4, x1 + 8, y2 - 4);
				g.drawLine(x1 + 7, y2 - 3, x1 + 7, y2 - 3);
			}
			else {	// VERTICAL
				g.fillRect(x1 + 4, y1 + 1, thumbRect.width - 8, thumbRect.height - 4);
				g.drawLine(x2 - 3, y1 + 5, x2 - 3, y1 + 5);

				Color c = g.getColor();
				g.setColor(ColorRoutines.darken(c, 10));
				g.drawLine(x1 + 4, y2 - 2, x2 - 6, y2 - 2);
				g.setColor(ColorRoutines.darken(c, 20));
				g.drawLine(x1 + 4, y2 - 1, x2 - 7, y2 - 1);
				
				Color bc = null;
				if(!slider.isEnabled()) {
					bc = Theme.sliderBorderDisabledColor[Theme.style].getColor();
				}
				else {
					bc = Theme.sliderBorderColor[Theme.style].getColor();
				}
				g.setColor(bc);
				g.drawLine(x1, y1 + 1, x1, y2 - 1);
				g.drawLine(x1 + 1, y1, x2 - 5, y1);
				g.drawLine(x2 - 4, y1 + 1, x2 - 4, y1 + 1);
				g.drawLine(x2 - 3, y1 + 2, x2 - 3, y1 + 2);
				g.drawLine(x2 - 2, y1 + 3, x2 - 2, y1 + 3);
				g.drawLine(x2 - 1, y1 + 4, x2 - 1, y1 + 4);
	
				g.setColor(Theme.sliderDarkColor[Theme.style].getColor());
				g.drawLine(x1 + 1, y2, x2 - 5, y2);
				g.drawLine(x2 - 4, y2 - 1, x2 - 4, y2 - 1);
				g.drawLine(x2 - 3, y2 - 2, x2 - 3, y2 - 2);
				g.drawLine(x2 - 2, y2 - 3, x2 - 2, y2 - 3);
				g.drawLine(x2 - 1, y2 - 4, x2 - 1, y2 - 4);
				g.drawLine(x2, y2 - 5, x2, y2 - 5);

				Color col = null;
				if(!slider.isEnabled()) {
					col = Theme.sliderBorderDisabledColor[Theme.style].getColor();
					g.setColor(col);
				}
				else if(!isDragging && isRollover && Theme.sliderRolloverEnabled[Theme.style]) {
					col = Theme.sliderThumbRolloverColor[Theme.style].getColor();
				}
				else {
					col = Theme.sliderLightColor[Theme.style].getColor();
				}
				
				Color c2 = SBChooser.getAdjustedColor(col, 67, 39);
				if(slider.isEnabled()) g.setColor(c2);
				g.drawLine(x1 + 1, y1 + 1, x1 + 1, y2 - 1);
				g.drawLine(x2 - 6, y1 + 1, x2 - 6, y1 + 1);
				g.drawLine(x2 - 5, y1 + 2, x2 - 5, y1 + 2);
				g.drawLine(x2 - 4, y1 + 3, x2 - 4, y1 + 3);

				if(slider.isEnabled()) g.setColor(ColorRoutines.getAverage(bc, c2));
				g.drawLine(x2 - 5, y1 + 1, x2 - 5, y1 + 1);
				g.drawLine(x2 - 4, y1 + 2, x2 - 4, y1 + 2);
				g.drawLine(x2 - 3, y1 + 3, x2 - 3, y1 + 3);

				Color c3 = ColorRoutines.getAverage(col, c2);
				if(slider.isEnabled()) g.setColor(c3);
				g.drawLine(x1 + 2, y1 + 1, x1 + 2, y2 - 1);
				g.drawLine(x2 - 3, y1 + 4, x2 - 3, y1 + 4);
				g.drawLine(x2 - 2, y1 + 5, x2 - 2, y1 + 5);
				g.drawLine(x2 - 3, y1 + 6, x2 - 3, y1 + 6);

				if(slider.isEnabled()) g.setColor(ColorRoutines.getAverage(bc, c3));
				g.drawLine(x2 - 2, y1 + 4, x2 - 2, y1 + 4);
				g.drawLine(x2 - 1, y1 + 5, x2 - 1, y1 + 5);
				g.drawLine(x2 - 2, y1 + 6, x2 - 2, y1 + 6);
					
				if(slider.isEnabled()) g.setColor(col);
				g.drawLine(x1 + 3, y1 + 1, x1 + 3, y2 - 1);
				g.drawLine(x2 - 6, y1 + 9, x2 - 6, y1 + 9);
				g.drawLine(x2 - 5, y1 + 8, x2 - 5, y1 + 8);
				g.drawLine(x2 - 4, y1 + 7, x2 - 4, y1 + 7);

				if(slider.isEnabled()) g.setColor(ColorRoutines.getAverage(bc, col));
				g.drawLine(x2 - 5, y1 + 9, x2 - 5, y1 + 9);
				g.drawLine(x2 - 4, y1 + 8, x2 - 4, y1 + 8);
				g.drawLine(x2 - 3, y1 + 7, x2 - 3, y1 + 7);
			}
		}
		else {	// no ticks painted
			// draw rectangular thumb
			if(slider.getOrientation() == JSlider.HORIZONTAL) {
				g.fillRect(x1 + 1, y1 + 1, thumbRect.width - 4, thumbRect.height - 4);
				
				Color c = g.getColor();
				g.setColor(ColorRoutines.darken(c, 10));
				g.drawLine(x2 - 2, y1 + 3, x2 - 2, y2 - 3);
				g.setColor(ColorRoutines.darken(c, 20));
				g.drawLine(x2 - 1, y1 + 3, x2 - 1, y2 - 3);
				
					
				if(!slider.isEnabled()) {
					g.setColor(Theme.sliderBorderDisabledColor[Theme.style].getColor());
				}
				else {
					g.setColor(Theme.sliderBorderColor[Theme.style].getColor());
				}
				g.drawLine(x1, y1 + 1, x1, y2 - 1);
				g.drawLine(x1 + 1, y1, x2 - 1, y1);
				
				if(!slider.isEnabled()) {
					g.setColor(Theme.sliderDarkDisabledColor[Theme.style].getColor());
				}
				else {
					g.setColor(Theme.sliderDarkColor[Theme.style].getColor());
				}
				g.drawLine(x1 + 1, y2, x2 - 1, y2);
				g.drawLine(x2, y1 + 1, x2, y2 - 1);
				
				Color col = null;
				if(!slider.isEnabled()) {
					col = Theme.sliderBorderDisabledColor[Theme.style].getColor();
					g.setColor(col);
				}
				else if(!isDragging && isRollover && Theme.sliderRolloverEnabled[Theme.style]) {
					col = Theme.sliderThumbRolloverColor[Theme.style].getColor();
				}
				else {
					col = Theme.sliderLightColor[Theme.style].getColor();
				}
				
				Color c2 = SBChooser.getAdjustedColor(col, 67, 39);
				if(slider.isEnabled()) g.setColor(c2);
				g.drawLine(x1 + 1, y1 + 1, x2 - 1, y1 + 1);
				
				if(slider.isEnabled()) g.setColor(ColorRoutines.getAverage(col, c2));
				g.drawLine(x1 + 1, y1 + 2, x2 - 1, y1 + 2);
				g.drawLine(x1 + 1, y2 - 2, x2 - 1, y2 - 2);
					
				if(slider.isEnabled()) g.setColor(col);
				g.drawLine(x1 + 1, y2 - 1, x2 - 1, y2 - 1);
			}
			else {	// VERTICAL
				g.fillRect(x1 + 1, y1 + 1, thumbRect.width - 4, thumbRect.height - 4);
				
				Color c = g.getColor();
				g.setColor(ColorRoutines.darken(c, 10));
				g.drawLine(x1 + 3, y2 - 2, x2 - 3, y2 - 2);
				g.setColor(ColorRoutines.darken(c, 20));
				g.drawLine(x1 + 3, y2 - 1, x2 - 3, y2 - 1);
				
					
				if(!slider.isEnabled()) {
					g.setColor(Theme.sliderBorderDisabledColor[Theme.style].getColor());
				}
				else {
					g.setColor(Theme.sliderBorderColor[Theme.style].getColor());
				}
				g.drawLine(x1 + 1, y1, x2 - 1, y1);
				g.drawLine(x1, y1 + 1, x1, y2 - 1);
				
				if(!slider.isEnabled()) {
					g.setColor(Theme.sliderDarkDisabledColor[Theme.style].getColor());
				}
				else {
					g.setColor(Theme.sliderDarkColor[Theme.style].getColor());
				}
				g.drawLine(x2, y1 + 1, x2, y2 - 1);
				g.drawLine(x1 + 1, y2, x2 - 1, y2);
				
				Color col = null;
				if(!slider.isEnabled()) {
					col = Theme.sliderBorderDisabledColor[Theme.style].getColor();
					g.setColor(col);
				}
				else if(!isDragging && isRollover && Theme.sliderRolloverEnabled[Theme.style]) {
					col = Theme.sliderThumbRolloverColor[Theme.style].getColor();
				}
				else {
					col = Theme.sliderLightColor[Theme.style].getColor();
				}
				
				Color c2 = SBChooser.getAdjustedColor(col, 67, 39);
				if(slider.isEnabled()) g.setColor(c2);
				g.drawLine(x1 + 1, y1 + 1, x1 + 1, y2 - 1);
				
				if(slider.isEnabled()) g.setColor(ColorRoutines.getAverage(col, c2));
				g.drawLine(x1 + 2, y1 + 1, x1 + 2, y2 - 1);
				g.drawLine(x2 - 2, y1 + 1, x2 - 2, y2 - 1);
					
				if(slider.isEnabled()) g.setColor(col);
				g.drawLine(x2 - 1, y1 + 1, x2 - 1, y2 - 1);
			}
		}
	}
	
	public static ComponentUI createUI(JComponent c) {
		return new TinySliderUI();
	}
	
	public void installUI(JComponent c) {
		super.installUI(c);
		
		c.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
	}

	protected int getThumbOverhang() {
		if (slider.getOrientation() == JSlider.VERTICAL) {
			return (int) (getThumbSize().getWidth() - getTrackWidth()) / 2;
		} else {
			return (int) (getThumbSize().getHeight() - getTrackWidth()) / 2;
		}
	}

	/**
	 * This TrackListener extends the BasicSliderUI.TrackListener such that
	 * rollover and dragging state can be tracked.
	 */
	class MyTrackListener extends BasicSliderUI.TrackListener {
		public void mouseReleased(MouseEvent e) {
			super.mouseReleased(e);
			isDragging = false;
			slider.repaint();
		}

		public void mousePressed(MouseEvent e) {
			super.mousePressed(e);
			if (thumbRect.contains(e.getX(), e.getY())) {
				isDragging = true;
			}
			slider.repaint();
		}
		public void mouseEntered(MouseEvent e) {
			isRollover = false;
			wasRollover = false;
			if (thumbRect.contains(e.getX(), e.getY())) {
				isRollover = true;
			}
		}
		public void mouseExited(MouseEvent e) {
			isRollover = false;
			if (isRollover != wasRollover) {
				slider.repaint();
				wasRollover = isRollover;
			}
		}
		public void mouseDragged(MouseEvent e) {
			if (thumbRect.contains(e.getX(), e.getY())) {
				isRollover = true;
			}
			super.mouseDragged(e);
		}
		public void mouseMoved(MouseEvent e) {
			if (thumbRect.contains(e.getX(), e.getY())) {
				isRollover = true;
				if (isRollover != wasRollover) {
					slider.repaint();
					wasRollover = isRollover;
				}
			} else {
				isRollover = false;
				if (isRollover != wasRollover) {
					slider.repaint();
					wasRollover = isRollover;
				}
			}
		}
	}

	public void paintTrack(Graphics g) {
//		g.setColor(Color.GREEN);
//		g.fillRect(0, 0, slider.getWidth(), slider.getHeight());
		
		switch(Theme.derivedStyle[Theme.style]) {
			case Theme.TINY_STYLE:
				drawTinyTrack(g);
				break;
			case Theme.W99_STYLE:
				drawWinTrack(g);
				break;
			case Theme.YQ_STYLE:
				drawXpTrack(g);
				break;
		}
	}
	
	public void paintFocus(Graphics g)  {
		if(!Theme.sliderFocusEnabled[Theme.style]) return;
		if(!(g instanceof Graphics2D)) return;
		
		Graphics2D g2d = (Graphics2D)g;
		Stroke memStroke = g2d.getStroke();
		
		g2d.setStroke(focusStroke);
		g2d.setColor(Theme.sliderFocusColor[Theme.style].getColor());
		g2d.drawRect(0, 0, slider.getWidth() - 1, slider.getHeight() - 1);
		
		g2d.setStroke(memStroke);
    }
	
	private void drawTinyTrack(Graphics g) {
		int x1 = trackRect.x;
		int x2 = x1 + trackRect.width;
		int y1 = trackRect.y;
		int y2 = y1 + trackRect.height;
		
		if (slider.getOrientation() == JSlider.HORIZONTAL) {
			int y = y1 + (trackRect.height - 7) / 2;
			
			g.setColor(new Color(170, 170, 170));
			g.drawLine(x1, y + 1, x1, y + 4);
			g.drawLine(x2 - 1, y + 1, x2 - 1, y + 1);
			g.fillRect(x1 + 1, y, trackRect.width - 2, 5);
			
			g.setColor(new Color(34, 34, 34));
			g.drawLine(x1 + 1, y + 2, x1 + 1, y + 4);
			g.drawLine(x2 - 1, y + 2, x2 - 1, y + 4);
			g.drawLine(x1 + 2, y + 1, x2 - 2, y + 1);
			g.drawLine(x1 + 2, y + 5, x2 - 2, y + 5);
			
			g.setColor(Color.WHITE);
			g.drawLine(x1 + 1, y + 5, x1 + 1, y + 5);
			g.drawLine(x1 + 2, y + 6, x2 - 1, y + 6);
			g.drawLine(x2 - 1, y + 5, x2 - 1, y + 5);
			g.drawLine(x2, y + 2, x2, y + 4);
		}
		else {
			int x = x1 + (trackRect.width - 7) / 2 + 1;
			g.setColor(new Color(170, 170, 170));
			g.drawLine(x + 1, y1 + 1, x + 1, y1 + 1);
			g.drawLine(x + 1, y2, x + 4, y2);
			g.fillRect(x, y1 + 2, 5, trackRect.height - 2);
			
			g.setColor(new Color(34, 34, 34));
			g.drawLine(x + 2, y1 + 1, x + 4, y1 + 1);
			g.drawLine(x + 2, y2 - 1, x + 4, y2 - 1);
			g.drawLine(x + 1, y1 + 2, x + 1, y2 - 2);
			g.drawLine(x + 5, y1 + 2, x + 5, y2 - 2);
			
			g.setColor(Color.WHITE);
			g.drawLine(x + 2, y1, x + 5, y1);
			g.drawLine(x + 5, y1 + 1, x + 5, y1 + 1);
			g.drawLine(x + 6, y1 + 1, x + 6, y2 - 2);
			g.drawLine(x + 5, y2 - 1, x + 5, y2 - 1);
		}
	}
	
	private void drawWinTrack(Graphics g) {
		int x1 = trackRect.x;
		int x2 = x1 + trackRect.width;
		int y1 = trackRect.y;
		int y2 = y1 + trackRect.height;
		
		if (slider.getOrientation() == JSlider.HORIZONTAL) {
			int y = y1 + (trackRect.height - 4) / 2;
			
			g.setColor(Theme.sliderTrackColor[Theme.style].getColor());
			g.drawLine(x1 + 1, y + 2, x2 - 1, y + 2);
			g.drawLine(x2 - 1, y + 1, x2 - 1, y + 1);
			
			g.setColor(Theme.sliderTrackDarkColor[Theme.style].getColor());
			g.drawLine(x1, y, x1, y + 3);
			g.drawLine(x1 + 1, y, x2, y);
			
			g.setColor(Theme.sliderTrackBorderColor[Theme.style].getColor());
			g.drawLine(x1 + 1, y + 1, x2 - 2, y + 1);
			
			g.setColor(Theme.sliderTrackLightColor[Theme.style].getColor());
			g.drawLine(x2, y + 1, x2, y + 2);
			g.drawLine(x1 + 1, y + 3, x2, y + 3);
		}
		else {
			int x = x1 + (trackRect.width - 4) / 2 + 1;
			
			g.setColor(Theme.sliderTrackColor[Theme.style].getColor());
			g.drawLine(x + 2, y1 + 1, x + 2, y2 - 1);
			g.drawLine(x + 1, y2 - 1, x + 1, y2 - 1);
			
			g.setColor(Theme.sliderTrackDarkColor[Theme.style].getColor());
			g.drawLine(x, y1, x + 3, y1);
			g.drawLine(x, y1 + 1, x, y2);
			
			g.setColor(Theme.sliderTrackBorderColor[Theme.style].getColor());
			g.drawLine(x + 1, y1 + 1, x + 1, y2 - 2);
			
			g.setColor(Theme.sliderTrackLightColor[Theme.style].getColor());
			g.drawLine(x + 3, y1 + 1, x + 3, y2);
			g.drawLine(x + 1, y2, x + 2, y2);
		}
	}
	
	private void drawXpTrack(Graphics g) {
		int x1 = trackRect.x;
		int x2 = x1 + trackRect.width;
		int y1 = trackRect.y;
		int y2 = y1 + trackRect.height;

		// Draw the track
		if (slider.getOrientation() == JSlider.HORIZONTAL) {
			int y = y1 + (trackRect.height - 4) / 2;
			
			g.setColor(Theme.sliderTrackColor[Theme.style].getColor());
			g.drawLine(x1 + 1, y + 2, x2 - 2, y + 2);
			
			g.setColor(Theme.sliderTrackDarkColor[Theme.style].getColor());
			g.drawLine(x1 + 1, y + 1, x2 - 2, y + 1);
			g.drawLine(x2 - 1, y + 1, x2 - 1, y + 2);
			g.drawLine(x1, y, x1, y);
			g.drawLine(x1, y + 3, x1, y + 3);
			
			g.setColor(Theme.sliderTrackLightColor[Theme.style].getColor());
			g.drawLine(x1 + 1, y + 3, x2 - 1, y + 3);
			g.drawLine(x2, y + 1, x2, y + 3);
			
			g.setColor(Theme.sliderTrackBorderColor[Theme.style].getColor());
			g.drawLine(x1 + 1, y, x2, y);
			g.setColor(ColorRoutines.lighten(
				Theme.sliderTrackBorderColor[Theme.style].getColor(), 20));
			g.drawLine(x1, y + 1, x1, y + 2);
		}
		else {
			int x = x1 + (trackRect.width - 4) / 2;
			
			g.setColor(Theme.sliderTrackBorderColor[Theme.style].getColor());
			g.drawLine(x, y1 + 2, x, y2 - 2);
			g.setColor(ColorRoutines.lighten(
				Theme.sliderTrackBorderColor[Theme.style].getColor(), 20));
			g.drawLine(x, y1 + 1, x + 2, y1 + 1);
			g.drawLine(x, y2 - 1, x + 2, y2 - 1);
			
			g.setColor(Theme.sliderTrackDarkColor[Theme.style].getColor());
			g.drawLine(x, y1, x + 3, y1);
			g.drawLine(x + 1, y1 + 2, x + 1, y2 - 2);
			g.drawLine(x, y2, x + 3, y2);
			
			g.setColor(Theme.sliderTrackLightColor[Theme.style].getColor());
			g.drawLine(x + 3, y1 + 1, x + 3, y2 - 1);
			
			g.setColor(Theme.sliderTrackColor[Theme.style].getColor());
			g.drawLine(x + 2, y1 + 2, x + 2, y2 - 2);
		}
	}
	
    protected void paintMinorTickForHorizSlider( Graphics g, Rectangle tickBounds, int x ) {
    	g.setColor(slider.isEnabled() ? Theme.sliderTickColor[Theme.style].getColor() : Theme.sliderTickDisabledColor[Theme.style].getColor());
        g.drawLine( x, 0, x, tickBounds.height / 2 - 1 );
    }

    protected void paintMajorTickForHorizSlider( Graphics g, Rectangle tickBounds, int x ) {
    	g.setColor(slider.isEnabled() ? Theme.sliderTickColor[Theme.style].getColor() : Theme.sliderTickDisabledColor[Theme.style].getColor());
        g.drawLine( x, 0, x, tickBounds.height - 2 );
    }

    protected void paintMinorTickForVertSlider( Graphics g, Rectangle tickBounds, int y ) {
    	g.setColor(slider.isEnabled() ? Theme.sliderTickColor[Theme.style].getColor() : Theme.sliderTickDisabledColor[Theme.style].getColor());
        g.drawLine( 0, y, tickBounds.width / 2 - 1, y );
    }

    protected void paintMajorTickForVertSlider( Graphics g, Rectangle tickBounds, int y ) {
    	g.setColor(slider.isEnabled() ? Theme.sliderTickColor[Theme.style].getColor() : Theme.sliderTickDisabledColor[Theme.style].getColor());
        g.drawLine( 0, y,  tickBounds.width - 2, y );
    }
}
