/*
 * 作成日: 2003/08/26
 *
 * この生成されたコメントの挿入されるテンプレートを変更するため
 * ウィンドウ > 設定 > Java > コード生成 > コードとコメント
 */
package st.chimera.teddy;

import java.awt.*;
import java.awt.geom.*;
import java.util.*;

/**
 * @author chimera
 *
 * この生成されたコメントの挿入されるテンプレートを変更するため
 * ウィンドウ > 設定 > Java > コード生成 > コードとコメント
 */
public class QuadCurve {

	private int n;
	private double[] x, y;
	GeneralPath path;

	public QuadCurve(double[] x, double[] y, int n, boolean cycle) {
		this.n = n;
		this.x = new double[n];
		this.y = new double[n];
		this.path = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
		System.arraycopy(x, 0, this.x, 0, n);
		System.arraycopy(y, 0, this.y, 0, n);

		makePath();
	}

	private void makePath() {

		int i = 0;
		double x1, y1, x2, y2;
		double mx, my;

		x1 = x[n - 1];
		y1 = y[n - 1];
		x2 = x[i];
		y2 = y[i];
		mx = (x1 + x2) / 2.0;
		my = (y1 + y2) / 2.0;

		path.reset();
		path.moveTo((float)mx, (float)my);
		for (; i < n; ++i) {
			double px1 = (mx + x2) / 2.0;
			double py1 = (my + y2) / 2.0;

			x1 = x2;
			y1 = y2;
			x2 = x[i];
			y2 = y[i];
			mx = (x1 + x2) / 2.0;
			my = (y1 + y2) / 2.0;

			double px2 = (mx + x1) / 2.0;
			double py2 = (my + y1) / 2.0;

			path.curveTo((float)px1, (float)py1, (float)px2, (float)py2, (float)mx, (float)my);

		}

		path.closePath();
	}

	public Point[] gets(double dist) {
		ArrayList list = new ArrayList();
		PathIterator itr = path.getPathIterator(null, dist);

		double[] point = new double[6];

		while (!itr.isDone()) {
			int type = itr.currentSegment(point);
			if (type == PathIterator.SEG_MOVETO || type == PathIterator.SEG_LINETO) {
				list.add(new Point((int)point[0], (int)point[1]));
			}
			itr.next();
		}
		
		Point[] ret = new Point[list.size()];
		list.toArray(ret);
		return ret;
	}
}
