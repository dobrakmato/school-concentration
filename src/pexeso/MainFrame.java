package pexeso;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;

public class MainFrame extends JFrame {
	private static final long serialVersionUID = 4089028248150053279L;
	public static int cardSize = 128;
	public static Icon baseTexture = new ImageIcon();
	private static Icon okIcon;
	private JPanel contentPane;
	private JLabel label;
	private int clicks;
	private PexesoCard firstRevealed = null;
	private PexesoCard[][] plocha;
	private List<ImageIcon> images = new ArrayList<ImageIcon>();
	private int points;
	private int size;
	private boolean canRevealFirst;
	private boolean canClick = true;

	/**
	 * Launch the application.
	 */
	public static void main(final String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainFrame frame = new MainFrame();
					
					if(args.length == 1) {
						int pocet = Integer.parseInt(args[0]);
						if((pocet % 2) != 0)
							pocet++;
						
						frame.size = pocet;
					} else {
						frame.size = 4;
					}
					
					frame.setVisible(true);
					frame.loadImgs();
					frame.buildHraciuPlochu();
					frame.setBounds(frame.getX(), frame.getY(), 128 + (frame.size * MainFrame.cardSize), 128 + (frame.size * MainFrame.cardSize));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	protected void loadImgs() {
		System.out.println("Loading textures from folder " + System.getProperty("user.dir") + "/img/...");
		MainFrame.baseTexture = new ImageIcon(System.getProperty("user.dir") + "/img/base.png");
		MainFrame.okIcon = new ImageIcon(System.getProperty("user.dir") + "/img/pexeso_okcard.png");
		for(File f : new File(System.getProperty("user.dir") + "/img/").listFiles()) {
			if(!f.getName().contains("base") && !f.getName().contains("pexeso_okcard")) {
				try {
					System.out.println("Loading texture " + f.getName() + " ...");
					this.images.add(new ImageIcon(f.toURI().toURL()));
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void buildHraciuPlochu() {
		int size = this.size;
		
		this.removeCards();
		this.plocha = new PexesoCard[size][size];
		
		for(int x = 0; x < size / 2; x++) {
			for(int y = 0; y < size / 2; y++) {
				this.plocha[x][y] = null;
			}
		}
		
		LinkedList<ImageIcon> willbeused = new LinkedList<ImageIcon>();
		for(int n = 0; n < size * size / 2; n++) {
			ImageIcon ii = this.images.get(n);
			willbeused.add(ii);
			willbeused.add(ii);
		}
		
		Collections.shuffle(willbeused);
		
		for(int x = 0; x < size; x++) {
			for(int y = 0; y < size; y++) {
				System.out.println("Generating card for [" + x + ";" + y + "]");
				this.plocha[x][y] = new PexesoCard(willbeused.pop(), x, y);
				getContentPane().add(this.plocha[x][y]);
			}
		}
	}

	public void removeCards(){
		if(this.plocha != null) {
			for(PexesoCard[] cards : this.plocha) {
				if(cards != null) {
					for(PexesoCard card : cards) {
						if(card != null) {
							this.remove(card);
						}
					}
				}
			}
		}	
	}

	public void a(final PexesoCard card) {
		this.clicks++;
		this.updateLabel();
		
		if(this.points == (int)((this.size * this.size) / 2)) {
			JOptionPane.showMessageDialog(this, "You won!");
		}
		
		if(this.firstRevealed == card) {
			return;
		}
		
		if(!card.getIcon().equals(MainFrame.okIcon)) {				
			if(this.firstRevealed == null) {
				this.canRevealFirst = false;
				this.firstRevealed = card;
			}
			else {
				if(this.firstRevealed.getIMGHash() == card.getIMGHash()) {
					this.firstRevealed.setIcon(MainFrame.okIcon);
					card.setIcon(MainFrame.okIcon);
					this.points++;
					this.canRevealFirst = true;
					this.firstRevealed = null;
				}
				else {
					javax.swing.Timer t = new javax.swing.Timer(700, new ActionListener() {
						
						@Override
						public void actionPerformed(ActionEvent e) {
							MainFrame.this.firstRevealed.conceal();
							card.conceal();
							MainFrame.this.firstRevealed = null;
							MainFrame.this.canRevealFirst = true;
							MainFrame.this.canClick = true;
						}
					});
					
					t.setRepeats(false);
					t.start();
					this.canClick = false;
				}
			}
		}
		this.updateLabel();
	}
	
	public void updateLabel() {
		this.label.setText("Clicks: " + this.clicks + " Points: " + this.points + " Successfullity: " + ((float)this.points / (float)this.clicks) * 100 + "%");
	}
	
	/**
	 * Create the frame.
	 */
	public MainFrame() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 1036, 662);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		this.label = new JLabel("Pexeso game - click to start!");
		label.setBounds(5, 5, 1008, 16);
		contentPane.add(this.label);
	}

	public class PexesoCard extends JLabel
	{
		private static final long serialVersionUID = 7393596754164141112L;
		private ImageIcon texture;
		private boolean revealed;
		private int cx;
		private int cy;
		
		public PexesoCard(ImageIcon img, int cardX, int cardY) {
			this.cx = cardX;
			this.cy = cardY;
			this.texture = img;
			this.conceal();
			this.setBounds(16 + (MainFrame.cardSize * cardX + 16), 32 + (MainFrame.cardSize * cardY + 16), MainFrame.cardSize, MainFrame.cardSize);
			this.setVisible(true);
			this.addMouseListener(new MouseListener() {
				@Override
				public void mouseReleased(MouseEvent e) {
				}
				@Override
				public void mousePressed(MouseEvent e) {
				}
				@Override
				public void mouseExited(MouseEvent e) {
				}
				@Override
				public void mouseEntered(MouseEvent e) {
				}
				@Override
				public void mouseClicked(MouseEvent e) {
					System.out.println("Mouse clicked card at " + PexesoCard.this.cx + ";" + PexesoCard.this.cy);
					if(PexesoCard.this.getIcon() != MainFrame.okIcon && MainFrame.this.canClick == true) {
						PexesoCard.this.mClick(e);
						MainFrame.this.a(PexesoCard.this);
					}
				}
			});
		}

		public int getCY() {
			return this.cy;
		}

		public int getCX() {
			return this.cx;
		}

		public int getIMGHash() {
			return this.texture.hashCode();
		}

		protected void mClick(MouseEvent e) {
			if(this.revealed) {
				this.conceal();
			}
			else {
				this.reveal();
			}
		}

		public void reveal(){
			System.out.println("Revealing card at " + PexesoCard.this.cx + ";" + PexesoCard.this.cy);
			PexesoCard.this.revealed = true;
			PexesoCard.this.setIcon(PexesoCard.this.texture);
		}
		
		public void conceal(){
			System.out.println("Concealing card at " + PexesoCard.this.cx + ";" + PexesoCard.this.cy);
			PexesoCard.this.revealed = false;
			PexesoCard.this.setIcon(MainFrame.baseTexture);
		}
	}
}
