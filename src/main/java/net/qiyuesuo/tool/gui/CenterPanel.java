package net.qiyuesuo.tool.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class CenterPanel extends JPanel implements BasePanel{
	
	private static final long serialVersionUID = 1L;

	private Map<Integer,ImgPanel> images = new TreeMap<Integer, ImgPanel>();
	
	private ComContext comContext; //主面板
	
	public CenterPanel(ComContext comContext) {
		super();
		this.comContext = comContext;
		this.comContext.setCenterPanel(this);
		this.setBackground(Color.GREEN); 
		
		new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE, new PdfDropTargetListener(this));
	}
	
	
	public void paintImage(Map<Integer,BufferedImage> imageMap) {
		for(ImgPanel imgPanle :images.values()) {
			this.remove(imgPanle);
		}
		images.clear();
		imageMap.forEach((key,val)->{
			ImgPanel imgPanle = new ImgPanel(key,val);
			images.put(key, imgPanle);
			this.add(imgPanle);
		});
		this.setPreferredSize(new Dimension(this.getWidth(), getRealHeight()));
		this.repaint();
		this.getComContext().getCenterScrollPane().validate();
	}
	
	private int getRealHeight() {
		int height = 0;
		for(ImgPanel imgPanel :images.values()) {
			height += imgPanel.getRealHeight();
		}
		return height==0?CompSize.MAIN_FRAME_WIDTH:height;
	}
	
	@Override
	public ComContext getComContext() {
		return comContext;
	}
	

	static class PdfDropTargetListener implements DropTargetListener{
		
		private CenterPanel centerPanel;
		
		public PdfDropTargetListener(CenterPanel centerPanel) {
			super();
			this.centerPanel = centerPanel;
		}

		@Override
		public void dragEnter(DropTargetDragEvent dtde) {
			
		}


		@Override
		public void dragOver(DropTargetDragEvent dtde) {
			
		}


		@Override
		public void dropActionChanged(DropTargetDragEvent dtde) {
			
		}


		@Override
		public void dragExit(DropTargetEvent dte) {
			
		}


		@Override
		public void drop(DropTargetDropEvent dtde) {
			try {
				if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
					dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
					List<?> list = (List<?>) (dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor));
					
					Iterator<?> iterator = list.iterator();
					while (iterator.hasNext()) {
						File f = (File) iterator.next();
						if(f.getName() != null && f.getName().toLowerCase().endsWith("pdf")) {
							System.out.println(f.getAbsolutePath());
							SearchFormPanel searchFormPanel = centerPanel.getComContext().getSearchFormPanel();
							searchFormPanel.handlePdfFile(f.getAbsolutePath());
							break;
						}else {
							JOptionPane.showMessageDialog(centerPanel, "仅支持pdf文件");
							break;
						}
					}
					dtde.dropComplete(true);
					// this.updateUI();
				} else {
					dtde.rejectDrop();
				}
			} catch (IOException ioe) {
				ioe.printStackTrace();
			} catch (UnsupportedFlavorException ufe) {
				ufe.printStackTrace();
			}
		}
	
	}
	


	

}
