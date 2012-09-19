package st.chimera.teddy;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import st.chimera.graphics.ch3d.*;

public class Panel3DView extends JComponent {
	Chimera3D ch3d;
	Image buffer;
	
	protected Chimera3DVertex[] vertices;
	
	Point mp;
	Chimera3DMatrix world = new Chimera3DMatrix();

	public Panel3DView() {
		setPreferredSize(new Dimension(600, 600));
		addComponentListener(new ComponentAdapter() {
			public void componentShown(ComponentEvent e) {
				try {
					init();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			
			public void componentResized(ComponentEvent e) {
				try {
					init();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});

		this.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e){
				mp = e.getPoint();	
			}
		});

		this.addMouseMotionListener(new MouseMotionAdapter() {
			public void mouseDragged(MouseEvent e) {
				Point nmp = e.getPoint();
				world.rotX((nmp.getY() - mp.getY()) * -0.02).rotY((nmp.getX() - mp.getX()) * -0.02);
				ch3d.setWorldMatrix(world);
				mp = nmp;
				
				update();				
			}
		});
		world.initialize().scale(1.0 / 100.0);
	}

	public void init() throws Exception {
		buffer = createImage(600, 600);
		ch3d = new Chimera3D(600, 600, (Graphics2D)buffer.getGraphics());
		ch3d.setClearColor(0);
		ch3d.setLighting(0xffffff, 0, 0x3f3f9f,
			new Chimera3DVector(-1.0f, -1.0f, 1.0f));
	}
	
	public void paint(Graphics g) {
		g.drawImage(buffer, 0, 0, null);
	}
	
	public void update() {
		ch3d.clear();		
		int color = Color.HSBtoRGB(0.6f, 0.4f, 1.0f);
		ch3d.setMaterial(color, 0x3f3f3f, 0x9f9f9f, 1.0f);
		
		if (vertices != null) {
			short[] indices = new short[vertices.length];
			for (int i = 0; i < indices.length; ++i)
				indices[i] = (short)i;
			ch3d.drawIndexedPrimitive(vertices, indices); 
		}

		ch3d.update();
		repaint();
	}
	

	public void setVertices(Chimera3DVertex[] vertices) {
		this.vertices = vertices;
	}
}
