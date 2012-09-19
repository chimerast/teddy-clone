/*
 * 作成日: 2003/08/25
 *
 * この生成されたコメントの挿入されるテンプレートを変更するため
 * ウィンドウ > 設定 > Java > コード生成 > コードとコメント
 */
package st.chimera.teddy;

import java.awt.*;

/**
 * @author chimera
 *
 * この生成されたコメントの挿入されるテンプレートを変更するため
 * ウィンドウ > 設定 > Java > コード生成 > コードとコメント
 */
public class Spline {

	private boolean isCycle;
	private int n;
	private double[] p;
	private double[] x, y, a, b;

	public Spline(double[] x, double[] y, int n, boolean cycle) {
		isCycle = cycle;

		this.n = n;
		this.p = new double[n];
		this.x = new double[n];
		this.y = new double[n];
		this.a = new double[n];
		this.b = new double[n];

		System.arraycopy(x, 0, this.x, 0, n);
		System.arraycopy(y, 0, this.y, 0, n);

		makeTable();
	}

	private void makeTable() {
		p[0] = 0;
		for (int i = 1; i < n; i++) {
			double dx = x[i] - x[i - 1];
			double dy = y[i] - y[i - 1];
			p[i] = p[i - 1] + Math.sqrt(dx * dx + dy * dy);
		}

		for (int i = 1; i < n; i++)
			p[i] /= p[n - 1];

		if (isCycle) {
			makeTableCycle(p, x, a);
			makeTableCycle(p, y, b);
		} else {
			makeTableNonCycle(p, x, a);
			makeTableNonCycle(p, y, b);
		}
	}

	private void makeTableNonCycle(double[] P1, double[] P2, double[] P3) {
		double T;
		double[] H, D;

		H = new double[n];
		D = new double[n];

		P3[0] = P3[n - 1] = 0;

		for (int i = 0; i < n - 1; i++) {
			H[i] = P1[i + 1] - P1[i];
			D[i + 1] = (P2[i + 1] - P2[i]) / H[i];
		}

		P3[1] = D[2] - D[1] - H[0] * P3[0];
		D[1] = 2 * (P1[2] - P1[0]);
		for (int i = 1; i < n - 2; i++) {
			T = H[i] / D[i];
			P3[i + 1] = D[i + 2] - D[i + 1] - P3[i] * T;
			D[i + 1] = 2 * (P1[i + 2] - P1[i]) - H[i] * T;
		}

		P3[n - 2] -= H[n - 2] * P3[n - 1];
		for (int i = n - 2; i > 0; i--)
			P3[i] = (P3[i] - H[i] * P3[i + 1]) / D[i];
	}

	private void makeTableCycle(double[] P1, double[] P2, double[] P3) {
		double T;
		double[] H, D, W;

		H = new double[n];
		D = new double[n];
		W = new double[n];

		for (int i = 0; i < n - 1; i++) {
			H[i] = P1[i + 1] - P1[i];
			W[i] = (P2[i + 1] - P2[i]) / H[i];
		}

		W[n - 1] = W[0];

		for (int i = 1; i < n - 1; i++)
			D[i] = 2 * (P1[i + 1] - P1[i - 1]);

		D[n - 1] = 2 * (H[n - 2] + H[0]);

		for (int i = 1; i <= n - 1; i++)
			P3[i] = W[i] - W[i - 1];
		W[1] = H[0];
		W[n - 2] = H[n - 2];
		W[n - 1] = D[n - 1];
		for (int i = 2; i < n - 2; i++)
			W[i] = 0;
		for (int i = 1; i < n - 1; i++) {
			T = H[i] / D[i];
			P3[i + 1] = P3[i + 1] - P3[i] * T;
			D[i + 1] = D[i + 1] - H[i] * T;
			W[i + 1] = W[i + 1] - W[i] * T;
		}

		W[0] = W[n - 1];
		P3[0] = P3[n - 1];

		for (int i = n - 3; i >= 0; i--) {
			T = H[i] / D[i + 1];
			P3[i] = P3[i] - P3[i + 1] * T;
			W[i] = W[i] - W[i + 1] * T;
		}
		T = P3[0] / W[0];
		P3[0] = T;
		P3[n - 1] = T;
		for (int i = 1; i < n - 1; i++)
			P3[i] = (P3[i] - W[i] * T) / D[i];
	}

	public Point get(double t) {
		if (isCycle) {
			return new Point((int)getSplineCycle(t, p, x, a), (int)getSplineCycle(t, p, y, b));
		} else {
			return new Point(
				(int)getSplineNonCycle(t, p, x, a),
				(int)getSplineNonCycle(t, p, y, b));
		}
	}

	public Point[] gets(int n) {
		Point[] ret;
		if (isCycle) {
			ret = new Point[n];
			for (int i = 0; i < ret.length; ++i) {
				ret[i] =
					new Point(
						(int)getSplineCycle(i / (double)n, p, x, a),
						(int)getSplineCycle(i / (double)n, p, y, b));
			}
		} else {
			ret = new Point[n + 1];
			for (int i = 0; i < ret.length; ++i) {
				ret[i] =
					new Point(
						(int)getSplineNonCycle(i / (double)n, p, x, a),
						(int)getSplineNonCycle(i / (double)n, p, y, b));
			}
		}
		return ret;
	}

	private double getSplineNonCycle(double T, double[] P1, double[] P2, double[] P3) {
		int i, j, k;
		double H, D;

		i = 0;
		j = n - 1;

		while (i < j) {
			k = (i + j) / 2;
			if (P1[k] < T)
				i = k + 1;
			else
				j = k;
		}

		if (i > 0)
			i--;

		H = P1[i + 1] - P1[i];
		D = T - P1[i];

		return (
			((P3[i + 1] - P3[i]) * D / H + P3[i] * 3) * D
				+ ((P2[i + 1] - P2[i]) / H - (P3[i] * 2 + P3[i + 1]) * H))
			* D
			+ P2[i];
	}

	private double getSplineCycle(double T, double[] P1, double[] P2, double[] P3) {
		int i, j, k;
		double H, D, Period;

		Period = P1[n - 1] - P1[0];
		while (T > P1[n - 1])
			T -= Period;
		while (T < P1[0])
			T += Period;

		i = 0;
		j = n - 1;

		while (i < j) {
			k = (i + j) / 2;
			if (P1[k] < T)
				i = k + 1;
			else
				j = k;
		}

		if (i > 0)
			i--;

		H = P1[i + 1] - P1[i];
		D = T - P1[i];

		return (
			((P3[i + 1] - P3[i]) * D / H + P3[i] * 3) * D
				+ ((P2[i + 1] - P2[i]) / H - (P3[i] * 2 + P3[i + 1]) * H))
			* D
			+ P2[i];
	}
}
