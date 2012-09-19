package st.chimera.teddy;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.vecmath.*;

import st.chimera.graphics.*;

public class TeddyExample extends JComponent implements MouseInputListener {

	private Teddy delaunay = new Teddy();
	private Panel3DView panel3d = new Panel3DView();

	private boolean drawing;

	public static void main(String[] args) {
		JFrame frame = new JFrame();
		JFrame frame3d = new JFrame();
		TeddyExample panel = new TeddyExample();

		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(panel, BorderLayout.CENTER);
		frame.getContentPane().add(panel.constructControlPanel(), BorderLayout.SOUTH);
		frame3d.getContentPane().setLayout(new BorderLayout());
		frame3d.getContentPane().add(panel.getView(), BorderLayout.CENTER);
		// frame.getContentPane().add(panel.getView(), BorderLayout.EAST);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setResizable(false);
		frame.setVisible(true);

		frame3d.pack();
		frame3d.setResizable(false);
		frame3d.setVisible(true);
	}

	public TeddyExample() {
		this.setPreferredSize(new Dimension(600, 600));
		panel3d.setPreferredSize(new Dimension(600, 600));
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
	}

	public JComponent getView() {
		return panel3d;
	}

	public JPanel constructControlPanel() {
		JPanel panel = new JPanel();
		JButton btnReset = new JButton("初期化");
		JButton btnRestrict = new JButton("制約");
		JButton btnRemove = new JButton("削除");
		JButton btnClassify = new JButton("分類");
		JButton btnMidPoint = new JButton("中点分割");
		JButton btnChordal = new JButton("芯線");
		JButton btnFan = new JButton("扇分割");
		JButton btnRest = new JButton("残分割");
		JButton btnAll = new JButton("全て");
		JButton btn3D = new JButton("3D化");

		btnReset.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				delaunay.reset();
				repaint();
			}
		});

		btnRestrict.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				delaunay.restrictDlaunay();
				repaint();
			}
		});

		btnRemove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				delaunay.removeExteriorTriangles();
				repaint();
			}
		});

		btnClassify.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				delaunay.classifyTriangles();
				repaint();
			}
		});

		btnMidPoint.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				delaunay.createMidPoint();
				repaint();
			}
		});

		btnChordal.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				delaunay.constructChordalAxis();
				repaint();
			}
		});

		btnFan.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				delaunay.constructFanTriangle();
				repaint();
			}
		});

		btnRest.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				delaunay.divideRestLoops();
				delaunay.calcVertexHeight();
				repaint();
			}
		});

		btnAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				delaunay.restrictDlaunay();
				delaunay.removeExteriorTriangles();
				delaunay.classifyTriangles();
				delaunay.createMidPoint();
				delaunay.constructChordalAxis();
				delaunay.constructFanTriangle();
				delaunay.divideRestLoops();
				delaunay.calcVertexHeight();
				repaint();
			}
		});

		btn3D.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				panel3d.setVertices(delaunay.get3DVertices());
				panel3d.update();
				panel3d.repaint();
			}
		});

		panel.setLayout(new FlowLayout());

		// panel.add(btnReset);
		panel.add(btnRestrict);
		panel.add(btnRemove);
		panel.add(btnClassify);
		panel.add(btnMidPoint);
		panel.add(btnChordal);
		panel.add(btnFan);
		panel.add(btnRest);
		panel.add(btnAll);
		panel.add(btn3D);

		return panel;
	}

	public void paint(Graphics g) {
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, getWidth(), getHeight());

		if (drawing) {
			Graphics2D gx = (Graphics2D)g;
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
		} else {
			if (delaunay != null) {

				Loop lines = delaunay.getLines();
				Loop[] triangles = delaunay.getTraingles();

				Graphics2D gx = (Graphics2D)g;

				// アンチエイリアシング有効
				gx.setRenderingHint(
					RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);

				for (int i = 0; i < triangles.length; ++i) {
					switch (triangles[i].kind) {
						case Loop.TERMINAL :
							gx.setPaint(Loop.TERMINAL_COLOR);
							break;
						case Loop.SLEEVE :
							gx.setPaint(Loop.SLEEVE_COLOR);
							break;
						case Loop.JUNCTION :
							gx.setPaint(Loop.JUNCTION_COLOR);
							break;
						case Loop.FAN :
							gx.setPaint(Loop.FAN_COLOR);
							break;
						default :
							gx.setPaint(Color.WHITE);
							break;
					}
					Polygon poly = triangles[i].toPolygon(5.0f);
					gx.fill(poly);

					gx.setStroke(new ArrowStroke(0.5f));
					gx.setPaint(Color.GREEN);
					gx.draw(poly);
				}

				gx.setStroke(new BasicStroke(1.0f));
				gx.setPaint(Color.BLUE);
				if (lines != null)
					gx.draw(lines.toPolygon(5.0f));

				Line2D[] axises = delaunay.getAxis();
				gx.setStroke(new ArrowStroke(1.0f));
				for (int i = 0; i < axises.length; ++i)
					gx.draw(axises[i]);

				Point2f[] points = delaunay.getVertices();
				gx.setPaint(Color.RED);

				for (int i = 0; i < points.length; ++i) {
					gx.fill(
						new Ellipse2D.Float(points[i].x - 2.0f, points[i].y - 2.0f, 5.0f, 5.0f));
				}

				if (delaunay.getStage() < Teddy.STAGE_3_REMOVED) {
					gx.setStroke(new BasicStroke(2.0f));
					for (int i = 0; i < points.length; ++i)
						gx.draw(
							new Line2D.Float(
								points[i].x,
								points[i].y,
								points[(i + 1) % points.length].x,
								points[(i + 1) % points.length].y));
				}

			}
		}
	}

	ArrayList list = new ArrayList();

	public void mouseClicked(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
		drawing = true;
		list.clear();
		list.add(new Point(e.getX(), e.getY()));
	}

	public void mouseReleased(MouseEvent e) {
		drawing = false;

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

		delaunay.reset();

		Spline spline = new Spline(x, y, p.length + 1, true);
		LinkedList list = new LinkedList(Arrays.asList(spline.gets(30)));
		ListIterator itr = list.listIterator();
/*
		Point p1, p2, p3;
		p1 = (Point)itr.next(); 
		p2 = (Point)itr.next(); 
		while (itr.hasNext()) {
			p3 = (Point)itr.next();
			
			Vector2f vec1 = new Vector2f(p1.x - p2.x, p1.y - p2.y);
			Vector2f vec2 = new Vector2f(p3.x - p2.x, p3.y - p2.y);
			vec1.normalize();
			vec2.normalize();
			float cos = vec1.dot(vec2);
			System.out.println(cos); 
			if (cos < -0.96f) {
				itr.previous();
				itr.previous();
				itr.remove();
				itr.next();
				p2 = p3;				
			} else {
				p1 = p2;
				p2 = p3;
			}
		}
*/		
		itr = list.listIterator();
		while (itr.hasNext()) {
			Point point = (Point)itr.next();
			delaunay.addPoint((float)point.getX(), (float)point.getY());
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
