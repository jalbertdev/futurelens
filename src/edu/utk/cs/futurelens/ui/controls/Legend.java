/******************************************************************************

                        FutureLens 

Copyright 2008 G. Shutt, A. Puretskiy, M.W. Berry 
Licensed under the Apache License, Version 2.0 (the "License"); you may not 
use this file except in compliance with the License. You may obtain a copy 
of the License at

http://www.apache.org/licenses/LICENSE-2.0 

Unless required by applicable
law or agreed to in writing, software distributed under the License is
distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied. See the License for the specific
language governing permissions and limitations under the License.

            Interested parties may
            send electronic mail to berry@eecs.utk.edu for
            more information.  Written requests for software
            distribution or use may be sent to:

             Michael W. Berry
             Department of Electrical Engineering and Computer Science
             203 Claxton Complex
             1122 Volunteer Boulevard
             University of Tennessee
             Knoxville, TN 37996-3450

******************************************************************************/

package edu.utk.cs.futurelens.ui.controls;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEffect;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Pattern;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.*;

import edu.utk.cs.futurelens.ui.events.LegendDropEvent;
import edu.utk.cs.futurelens.ui.events.LegendDropListener;

public class Legend extends Composite 
{
	// adjustable stuff
	private Color mainColor;

	// non adjustable settings
	
	// number of color blocks - this could be adjustable in theory
	private final int numBlocks = 4;
	private final int defaultWidth = 150;
	private final int marginWidth = 2;
	private final int marginHeight = 4;
	private final int horizSpace = 2;
	private final int vertSpace = 2;
	private final int colorBlockHeight = 20;
	private final int rectArc = 8;
	private final Color borderColor = getDisplay().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW);
	
	// width in characters to reserve for the min/max
	private final int minCharacterWidth = 3;
	private final int maxCharacterWidth = 3;
	
	private final int highlightBorderWidth = 4;
	private final Color highlightColor = new Color(getDisplay(), 87, 120, 158);
	private final Color highlightBorderColor = new Color(getDisplay(), 87, 120, 158);
	private final Color closeButtonColor = new Color(getDisplay(), 120, 120, 120);
	private final int closeButtonOffset = 3;
	
	private final int closeButtonDiameter = 15;
	
	// text to display
	private String text = null; // contains a concatenated list of terms
	private ArrayList<String> terms;
	
	// maximum
	private Integer maxCount = 0;
	private Integer minCount = 0;
	
	// all the colors used
	private Color colors[];
	
	// used to calculate the size of text required
	private GC textGc;
	private int textHeight;
	private int maxTextWidth = 0;
	
	private int closeButtonX = 0;
	private int closeButtonY = 0;
	
	// cache
	private Image imgCache = null;
	private Image imgClose = null;
	private Image imgCloseBg = null;
	private boolean cacheDirty = false;
	
	// used for dragging and dropping
	private static boolean dragSucceeded;
	private boolean isHighlighted = false;
	
	private ArrayList<LegendDropListener> dropListeners;
	
	// drag effect
	private class LegendDragEffect extends DragSourceEffect
	{
		private Display display;
		private Image image;
		
		public LegendDragEffect(Control control) 
		{
			super(control);
			display = getDisplay();
		}
		
		public void dragStart(DragSourceEvent event)
		{
			// copy the image
			Rectangle imgSize = imgCache.getBounds();
			GC gc;
			
			imgSize.width -= highlightBorderWidth * 2 - 1;
			imgSize.height -= highlightBorderWidth * 2 - 1;
			
			image = new Image (display, imgSize.width, imgSize.height);
			
			gc = new GC(image);
		
			gc.drawImage(imgCache, highlightBorderWidth, highlightBorderWidth, imgSize.width,
					imgSize.height, 0, 0, imgSize.width, imgSize.height);
			
			gc.dispose();
			
			event.image = image;
		}
		
		public void dragFinished(DragSourceEvent event)
		{
			image.dispose();
		}
	}
	
	// private drag/drop transfer
	private static class LegendTransfer extends ByteArrayTransfer
	{
		private static final String TYPENAME = "legendtransfer";
		private static final int TYPEID = registerType(TYPENAME);
		private static final LegendTransfer instance = new LegendTransfer();

		private LegendTransfer()
		{
		}
		
		public static LegendTransfer getInstance()
		{
			return(instance);
		}

		public void javaToNative(Object object, TransferData transferData)
		{
			if(! validate(object) || ! isSupportedType(transferData))
				DND.error(DND.ERROR_INVALID_DATA);
			
			String data = (String)object;
			super.javaToNative(data.getBytes(), transferData);	
		}
		
		public Object nativeToJava(TransferData transferData)
		{
			if(! isSupportedType(transferData))
				return(null);
			
			byte[] buffer = (byte[]) super.nativeToJava(transferData);
			
			if(buffer == null)
				return(null);
			
			String data = new String(buffer);
			
			return(data);
		}
		
		@Override
		protected int[] getTypeIds() 
		{
			return(new int[] {TYPEID});
		}

		@Override
		protected String[] getTypeNames() 
		{
			return(new String[] {TYPENAME});
		}
		
		protected boolean validate(Object object)
		{
			if(object == null || !(object instanceof String))
				return(false);
			
			return(true);
		}
	}
	
	public Legend(Composite parent, int style) 
	{
		super(parent, style);
	
		// create the list for all the colors
		colors = new Color[numBlocks];
		
		mainColor = null;
		
		textGc = new GC(this.getDisplay());
		
		terms = new ArrayList<String>();
		dropListeners = new ArrayList<LegendDropListener>();
		
		// add listeners
		addControlListener( new ControlAdapter() {
			public void controlResized(ControlEvent e) {
				Legend.this.onResize(e);
			}
		});
		
		addPaintListener( new PaintListener() {
			public void paintControl(PaintEvent e) {
				Legend.this.onCanvasPaint(e);
			}
		});
		
		addDisposeListener( new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				Legend.this.onDispose();
			}
		});
		
		addMouseTrackListener(new MouseTrackListener() {
			public void mouseEnter(MouseEvent e) {
				Legend.this.onMouseOver(e);
			}

			public void mouseExit(MouseEvent e) {
				Legend.this.onMouseOut(e);
			}

			public void mouseHover(MouseEvent e) {
								
			}
		});
		
		addMouseListener(new MouseListener() {
			public void mouseDoubleClick(MouseEvent e) {
				
			}

			public void mouseDown(MouseEvent e) {
				
			}

			public void mouseUp(MouseEvent e) {
				onMouseUp(e);
			}
		});
		
		// set up drag and drop
		// set up the drag source
		//int ops = DND.DROP_MOVE;
		//uncomment the line below and comment the one above to add copying
		int ops = DND.DROP_MOVE | DND.DROP_COPY;
		DragSource source = new DragSource(this, ops);
		source.setTransfer(new Transfer[] {LegendTransfer.getInstance()});
		
		source.addDragListener(new DragSourceListener() {
			public void dragStart(DragSourceEvent event) {
				Legend.this.onDragStart(event);
			}
			
			public void dragSetData(DragSourceEvent event) {
				Legend.this.onDragSetData(event);
			}
			
			public void dragFinished(DragSourceEvent event) {
				Legend.this.onDragFinished(event);
			}
		});
		
		// set the effect
		source.setDragSourceEffect(new LegendDragEffect(this));
		
		// set up the drop target
		DropTarget target = new DropTarget(this, ops);
		target.setTransfer(new Transfer[] {LegendTransfer.getInstance()});
		
		target.addDropListener(new DropTargetListener() {
			public void dragEnter(DropTargetEvent event) {
				Legend.this.onDragEnter(event);
			}

			public void dragLeave(DropTargetEvent event) {
				Legend.this.onDragLeave(event);
			}

			public void dragOperationChanged(DropTargetEvent event) {
				
			}

			public void dragOver(DropTargetEvent event) {
				
			}

			public void drop(DropTargetEvent event) {
				Legend.this.onDrop(event);
			}

			public void dropAccept(DropTargetEvent event) {
				
			}
			
		});
	}

	// drag event handlers
	private void onDragStart(DragSourceEvent event)
	{
		// for some reason on osx a drag will succeed if a control has been dragged over
		//  a receptive control but is not dropped on that control.  there is no way to tell
		//  if the drop was *actually* successful
		dragSucceeded = false;
		
		this.setVisible(false);
	}
	
	private void onDragSetData(DragSourceEvent event)
	{
		if(LegendTransfer.getInstance().isSupportedType(event.dataType))
			event.data = this.text;
	}
	
	private void onDragFinished(DragSourceEvent event)
	{
		if(dragSucceeded && event.detail == DND.DROP_MOVE && event.doit)
		{
			Composite parent = this.getParent();
			
			if(! this.isDisposed())
				this.dispose();
			
			parent.layout(true);
			parent.setSize(parent.computeSize(parent.getSize().x, SWT.DEFAULT));
		}
		else
		{
			this.setVisible(true);
		}
	}
	
	// drop event handlers
	private void onDragLeave(DropTargetEvent event)
	{
		isHighlighted = false;
		repaint();
	}
	
	private void onDragEnter(DropTargetEvent event)
	{
		isHighlighted = true;
		repaint();
	}
	
	private void onDrop(DropTargetEvent event)
	{
		String[] newTerms;
		
		// for osx
		dragSucceeded = true;
		
		// split the strings into terms
		newTerms = ((String)event.data).split(",\\s+");
		/*String term = "";
		for(String temp : newTerms)
			term += temp;
		addTerm(term);
		System.out.println(term);*/
		if(event.detail == DND.DROP_COPY)
		{
			//System.out.println("copy, DND: " +event.detail);
			event.detail = DND.DROP_MOVE;
			for(String term : newTerms)
				addPhrase(term);
		}
		else
		{
			//System.out.println("not a copy, DND: "+event.detail);
			for(String term : newTerms)
				addTerm(term);
		}
		
		
		// might need a larger drawing area..
		this.getParent().layout(true);
		
		isHighlighted = false;
		
		// alert all the listeners
		// create the event
		LegendDropEvent e = new LegendDropEvent(this, newTerms, this);
		
		// alert all the listeners
		for(LegendDropListener l : dropListeners)
			l.legendDropped(e);
	}
	
	public void addPhrase(String term)
	{
		if(text == null)
			text = term;
		else{
			text += ", " + term;
			//terms.clear();
			//terms.remove(0);
			//removeTerm(text);
			//text += " " + term;
		}
		
		terms.add(term);

		String temp = terms.toString();
		temp = removeChar(temp, '[');
		temp = removeChar(temp,']');
		temp = temp.replaceAll(",", "");
		
		//uncomment these lines for chained phrases:
		terms.clear();
		terms.add(temp);
	
		//System.out.println(temp);

		// redraw the thing
		repaint();
		
	}

	public void addDropListener(LegendDropListener listener)
	{
		dropListeners.add(listener);
	}
	
	public String getText()
	{
		return(text);
	}
	
	public void setColor(Color color)
	{
		float[] hsb;
		
		this.mainColor = color;
		
		// set the last color to the darkest
		colors[numBlocks - 1] = color;
		
		// get the initial saturation
		hsb = color.getRGB().getHSB();
		
		// go through and create the other colors
		for(int i = numBlocks - 2; i >= 0; i--)
		{
			// adjust the saturation
			hsb[1] -= (0.8 / (float)numBlocks);
			
			if(hsb[1] < 0)
				hsb[1] = 0;
			
			// create a new color
			colors[i] = new Color(this.getDisplay(), new RGB(hsb[0], hsb[1], hsb[2]));
		}
	}
	
	public Color getColor(int index)
	{
		if(index >= colors.length)
			throw new IllegalArgumentException("index is out bounds");
		
		// return the original color when passed -1
		if(index < 0)
			index = colors.length - 1;
		
		return(colors[index]);
	}
	
	public ArrayList<String> getTerms()
	{
		return(this.terms);
	}
	
	public void setMax(int max)
	{
		this.maxCount = max;
	}
	
	public void setMin(int min)
	{
		this.minCount = min;
	}
	
	public int getNumBlocks()
	{
		return(this.numBlocks);
	}
	
	public void addTerm(String term)
	{
		if(text == null)
			text = term;
		else{
			text += ", " + term;
			//terms.clear();
			//terms.remove(0);
			//removeTerm(text);
			//text += " " + term;
		}
		
		terms.add(term);

		String temp = terms.toString();
		temp = removeChar(temp, '[');
		temp = removeChar(temp,']');
		temp = temp.replaceAll(",", "");
		
		//uncomment these lines for chained phrases:
		//terms.clear();
		//terms.add(temp);
	
		//System.out.println(term);

		// redraw the thing
		repaint();
	}
	
	public String removeChar(String s, char c) {
		String r = "";
		  for (int i = 0; i < s.length(); i ++)
		  {
		     if (s.charAt(i) != c) r += s.charAt(i);
		  }
		   return r;
		}
	
	public boolean contains(String term)
	{
		return(terms.contains(term));
	}
	
	public void removeTerm(String term)
	{
		if(contains(term))
		{
			terms.remove(term);
			text.replaceFirst(term + "(, )*", "");
			
			// repaintin' time
			this.getParent().layout(true);
			repaint();
		}
	}
	
	public Point computeSize(int wHint, int hHint, boolean changed)
	{
		int width, height;
		
		if(wHint != SWT.DEFAULT)
			width = wHint;
		else
			width = defaultWidth;
		
		// figure out the text height
		textHeight = wrap(width).size() * (textGc.stringExtent(text).y + vertSpace);
		
		if(hHint != SWT.DEFAULT)
			height = hHint;
		else
			height = highlightBorderWidth + marginHeight + colorBlockHeight + vertSpace + textHeight + marginHeight + highlightBorderWidth; 
		
		return(new Point(width, height));
	}
	
	private void onCanvasPaint(PaintEvent e)
	{
		GC gc = e.gc;
		
		if(cacheDirty)
		{
			Rectangle canvasSize = this.getClientArea();
			
			int blockWidth;
			int totalBlockWidth;
			int x, y;
			int minTextWidth, maxTextWidth;
			
			// make sure the color has been set
			if(mainColor == null)
				setColor(this.getDisplay().getSystemColor(SWT.COLOR_BLUE));
			
			// need to repaint
			if(imgCache == null)
				imgCache = new Image(getDisplay(), canvasSize.width, canvasSize.height);
			
			// resize the cache if necessary
			if(imgCache.getBounds().height != canvasSize.height ||
					imgCache.getBounds().width != canvasSize.width)
			{
				imgCache.dispose();
				imgCache = new Image(getDisplay(), canvasSize.width, canvasSize.height);
			}
			
			GC canvas = new GC(imgCache);
			
			// figure out how wide to make the min/max
			minTextWidth = canvas.stringExtent("#").x * minCharacterWidth;
			maxTextWidth = canvas.stringExtent("#").x * maxCharacterWidth;
			
			// blank everything
			canvas.setBackground(this.getBackground());
			canvas.fillRectangle(canvasSize);
			
			// draw the highlight rectangle
			if(isHighlighted && highlightBorderWidth > 0)
			{				
				// draw the gradient
				for(int i = highlightBorderWidth - 1; i >= 0; i--)
				{
					// draw the border
					if(i == highlightBorderWidth - 1)
					{
						canvas.setBackground(highlightBorderColor);
						canvas.setAlpha(255);
					}
					else
					{
						canvas.setBackground(highlightColor);
						canvas.setAlpha(255 / highlightBorderWidth);
					}
					
					canvas.fillRoundRectangle(i, i,	canvasSize.width - i * 2, canvasSize.height - i * 2, 
							rectArc * 2 - rectArc / highlightBorderWidth * i, rectArc * 2 - rectArc / highlightBorderWidth * i);
				}
			
				// restore the alpha
				canvas.setAlpha(255);
			}
			
			// move the drawing position
			x = highlightBorderWidth;
			y = highlightBorderWidth;
			
			// draw the enclosing shape
			// create the gradient fill
			Pattern p1 = new Pattern(this.getDisplay(), 0, 0, 0, canvasSize.height, 
					getDisplay().getSystemColor(SWT.COLOR_WHITE), getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
			
			canvas.setBackgroundPattern(p1);
			canvas.fillRoundRectangle(x, y,	canvasSize.width - highlightBorderWidth * 2, canvasSize.height - highlightBorderWidth * 2, rectArc, rectArc);
			
			// draw the border
			if(! isHighlighted)
			{
				Color oldFg = canvas.getForeground();
				canvas.setForeground(borderColor);
				canvas.drawRoundRectangle(x, y, canvasSize.width - highlightBorderWidth * 2, canvasSize.height - highlightBorderWidth * 2, rectArc, rectArc);
				canvas.setForeground(oldFg);
			}
			
			// move into the enclosing rectangle
			x += marginWidth;
			y += marginHeight;
			
			// draw the minimum (or minus)
			canvas.setFont(this.getFont());
			
			if(minCount > 0)
				// center the text
				canvas.drawText(minCount.toString(), x + (minTextWidth - canvas.stringExtent(minCount.toString()).x) / 2, y, true);
			else
				canvas.drawText("-", x + (minTextWidth - canvas.stringExtent("-").x) / 2, y, true);
			
			// move to the right
			x += horizSpace + minTextWidth;
			
			// figure out how wide to make the color block
			totalBlockWidth = canvasSize.width - x - maxTextWidth - horizSpace - highlightBorderWidth;
			blockWidth = totalBlockWidth / numBlocks;
			
			// draw the boxes of color
			for(int i = 0; i < numBlocks; i++)
			{
				canvas.setBackground(colors[i]);
				
				// draw the last tiny bit if not a multiple of the width
				if((i >= numBlocks - 1) && (totalBlockWidth % blockWidth != 0))
					blockWidth += totalBlockWidth % blockWidth;
				
				canvas.fillRectangle(x, y, blockWidth, colorBlockHeight);
				
				x += blockWidth;
			}
			
			// draw the maximum text
			if(maxCount > 0)
				canvas.drawText(maxCount.toString(), x + (minTextWidth - canvas.stringExtent(maxCount.toString()).x) / 2, y, true);
			else
				canvas.drawText("+", x + (minTextWidth - canvas.stringExtent("+").x) / 2, y, true);
			
			// move down
			y += colorBlockHeight + vertSpace;
			
			// draw the terms
			ArrayList<String> lines = wrap(canvasSize.width);
			int lineHeight = canvas.stringExtent(text).y;
			
			for(String l : lines)
			{	
				// center
				x = (canvasSize.width - canvas.stringExtent(l).x) / 2;
				
				canvas.drawText(l, x, y, true);
				
				// move down
				y += vertSpace + lineHeight;
			}
			
			// copy the area behind the close button
			if(imgCloseBg == null)
				imgCloseBg = new Image(getDisplay(), closeButtonDiameter, closeButtonDiameter);
			
			GC gcCloseBg = new GC(imgCloseBg);
			gcCloseBg.drawImage(imgCache, closeButtonX,closeButtonY,
					closeButtonDiameter, closeButtonDiameter, 0, 0, closeButtonDiameter, closeButtonDiameter);
			gcCloseBg.dispose();
			
			p1.dispose();
			canvas.dispose();

			// cache is clean now
			cacheDirty = false;
		}
		
		// copy
		gc.drawImage(imgCache, e.x, e.y, e.width, e.height,
				e.x, e.y, e.width, e.height);
		
		// draw the close button
		if(imgClose != null)
			gc.drawImage(imgClose, closeButtonX, closeButtonY);
	}
	
	private void drawCloseButton(int opacity)
	{
		if(imgCloseBg == null)
			return;
		
		// make a copy of the background
		imgClose = new Image(getDisplay(), imgCloseBg, SWT.IMAGE_COPY);
		
		if(opacity > 0)
		{
			// draw all over it
			GC gcClose = new GC(imgClose);
			
			gcClose.setLineJoin(SWT.JOIN_ROUND);
			gcClose.setLineWidth(2);
			gcClose.setAlpha(opacity);
			gcClose.setBackground(closeButtonColor);
			gcClose.fillOval(0, 0, closeButtonDiameter, closeButtonDiameter);
			gcClose.setAlpha(235);
			gcClose.setForeground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
			gcClose.drawLine(4, 4, 11, 11);
			gcClose.drawLine(11, 4, 4, 11);
		}
		
		// copy the image to the widget
		this.redraw(closeButtonX, closeButtonY,	closeButtonDiameter, closeButtonDiameter, false);
	}
	
	private void onMouseOver(MouseEvent e)
	{
		drawCloseButton(200);
	}
	
	private void onMouseOut(MouseEvent e)
	{
		drawCloseButton(0);
	}
	
	private void onMouseUp(MouseEvent e)
	{
		if(e.x >= closeButtonX && e.x < closeButtonX + closeButtonDiameter &&
				e.y >= closeButtonY && e.y < closeButtonY + closeButtonDiameter)
		{
			// peace
			Composite parent = this.getParent();
			
			this.dispose();
			parent.layout(true);
			parent.setSize(parent.computeSize(parent.getSize().x, SWT.DEFAULT));
		}
	}
	
	private ArrayList<String> wrap(int width)
	{
		ArrayList<String> lineList = new ArrayList<String>();
		String line = new String();
		int length = 0, wordLineCount = 0;
		
		// figure out how many lines of text to take up
		for(String term : terms)
		{
			// add the comma between terms
			term = term + ",";
			
			// split the term into words
			for(String word : term.split("\\s+"))
			{
				// eat up words until the string length is too long
				length += textGc.stringExtent(word + " ").x;
				
				// check if this exceeded the bounds
				if(length >= width - highlightBorderWidth * 2)
				{
					// check if this is a single word
					if(wordLineCount == 0)
					{
						// it is...set the line
						if(maxTextWidth > 0 && word.length() >= maxTextWidth)
							line = word.substring(0, maxTextWidth - 3) + "..., ";
						else
							line = word;
						
						// commit the line
						lineList.add(line);
						
						// clear the line
						line = "";
						wordLineCount = 0;
						length = 0;
					}
					else
					{
						// there is at least one word on the line
						lineList.add(line);
						
						// add the next word onto the line
						if(maxTextWidth > 0 && word.length() >= maxTextWidth)
							line = word.substring(0, maxTextWidth - 3) + "..., ";
						else
							line = new String(word + " ");
						
						wordLineCount = 1;
						length = textGc.stringExtent(line).x;
					}
				}
				else
				{
					// it hasn't...throw it on the list
					line += word + " ";
					
					wordLineCount++;
				}
			}
		}
		
		// add the last line
		if(line.length() > 0)
		{
			// remove the trailing comma
			line = line.substring(0, line.length() - 2);
			
			lineList.add(line);
		}
		
		return(lineList);
	}
	
	private void onDispose()
	{
		// for some reason this causes a crash...
		//for(Color c : this.colors)
		//	c.dispose();
		
		if(imgCache != null)
			imgCache.dispose();
		
		if(imgClose != null)
			imgClose.dispose();
	}
	
	private void onResize(ControlEvent e)
	{
		Rectangle parentExtents = this.getClientArea();
		String text = "";
		
		// figure out how wide (in characters) the maximum text can be
		while(textGc.stringExtent(text).x < parentExtents.width)
			text += "#";
		
		maxTextWidth = text.length() - 1;
		
		// set the x position of the close button
		closeButtonX = parentExtents.width - closeButtonDiameter - closeButtonOffset - highlightBorderWidth;
		closeButtonY = closeButtonOffset + highlightBorderWidth;
		
		invalidateCache();
	}
	
	private void repaint()
	{
		invalidateCache();
		redraw(0, 0, getSize().x, getSize().y, true);
	}
	
	private void invalidateCache()
	{
		cacheDirty = true;
	}
}
