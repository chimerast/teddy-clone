package st.chimera.teddy;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

public class SplineExample extends JComponent implements MouseInputListener {
	
	ArrayList list = new ArrayList();

	public static void main(String[] args) {
		JFrame frame = new JFrame();
		SplineExample panel = new SplineExample();
		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(panel, BorderLayout.CENTER);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setResizable(false);
		frame.setVisible(true);
	}

	public SplineExample() {
		this.setPreferredSize(new Dimension(400, 400));
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
	}

	public void paint(Graphics g) {
		Graphics2D gx = (Graphics2D)g;

		gx.setColor(Color.WHITE);
		gx.fillRect(0, 0, getWidth(), getHeight());
		
		gx.setColor(Color.BLACK);
		Iterator itr = list.iterator();
		if (itr.hasNext()) {
			Point prev = (Point)itr.next();
			while (itr.hasNext()) {
				Point point = (Point)itr.next();
				gx.draw(new Line2D.Double(prev, point));
				prev = point;
			}
		}
	}

	public void mouseClicked(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
		list.add(new Point(e.getX(), e.getY()));
	}

	public void mouseReleased(MouseEvent e) {
		Point[] p = new Point[list.size()];
		double[] x = new double[list.size() + 1];
		double[] y = new double[list.size() + 1];
		list.toArray(p);
		for (int i = 0; i < p.length; ++i) {
			x[i] = (int)p[i].x;
			y[i] = (int)p[i].y;
		}
		x[p.length] = (int)p[0].x;
		y[p.length] = (int)p[0].y;
		
		list.clear();
		Spline spline = new Spline(x, y, p.length + 1, true);
		for (int i = 0; i < 40; ++i) {
			list.add(spline.get(i / 40.0));
		}
		repaint();
	}

	public void mouseDragged(MouseEvent e) {
		list.add(new Point(e.getX(), e.getY()));
		repaint();
	}

	public void mouseMoved(MouseEvent e) {
	}

}
