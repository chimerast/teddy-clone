package st.chimera.teddy;

import java.awt.*;

import javax.vecmath.*;

class Loop {
	public static final int TERMINAL = 1;
	public static final int SLEEVE = 2;
	public static final int JUNCTION = 3;
	public static final int FAN = 4;

	public static final Color TERMINAL_COLOR = new Color(0xffffffcc);
	public static final Color SLEEVE_COLOR = new Color(0xffccccff);
	public static final Color JUNCTION_COLOR = new Color(0xffffcccc);
	public static final Color FAN_COLOR = new Color(0xffffccff);

	HalfEdge halfEdge;
	Loop next;
	Loop prev;
	int kind;
	boolean divided;
	boolean postdivided;

	public Loop() {
		this.next = this;
		this.prev = this;
		kind = 0;
		divided = false;
		postdivided = false;
	}

	void link(Loop next) {
		this.next.prev = next;
		next.next = this.next;
		this.next = next;
		next.prev = this;
	}

	void unlink() {
		this.next.prev = this.prev;
		this.prev.next = this.next;
		this.next = this;
		this.prev = this;
	}

	boolean inLoop(Vertex v) {
		HalfEdge cur = halfEdge;
		do {
			if (!cur.left(v.pos))
				return false;
			cur = cur.next;
		} while (cur != halfEdge);

		return true;
	}

	Polygon toPolygon(float ratio) {
		Polygon ret = new Polygon();

		HalfEdge edge = this.halfEdge;
		do {
			Vector2f v1 = new Vector2f(edge.prev.vertex.pos);
			Vector2f v2 = new Vector2f(edge.next.vertex.pos);
			v1.sub(edge.vertex.pos);
			v2.sub(edge.vertex.pos);
			v1.normalize();
			v2.normalize();

			float sign = 1.0f;			
			if (v1.x * v2.y - v2.x * v1.y < 0.0f) 
				sign = -1.0f;
			v1.add(v2);
			
			ret.addPoint(
				(int) (edge.vertex.pos.x + sign * ratio * v1.x),
				(int) (edge.vertex.pos.y + sign * ratio * v1.y));
			edge = edge.next;
		} while (edge != this.halfEdge);

		return ret;
	}
}