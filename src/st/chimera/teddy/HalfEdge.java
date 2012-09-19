package st.chimera.teddy;

import javax.vecmath.*;

class HalfEdge {
	HalfEdge pair;
	HalfEdge next;
	HalfEdge prev;
	Edge edge;
	Loop loop;
	Vertex vertex;

	public HalfEdge() {
		this.pair = new HalfEdge(this);
		this.next = this;
		this.prev = this;
		this.edge = new Edge();
		this.pair.edge = edge;
	}

	private HalfEdge(HalfEdge pair) {
		this.pair = pair;
		this.next = this;
		this.prev = this;
	}

	void link(HalfEdge next) {
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

	boolean left(Point2f pos) {
		float x1 = next.vertex.pos.x - vertex.pos.x;
		float y1 = next.vertex.pos.y - vertex.pos.y;
		float x2 = pos.x - vertex.pos.x;
		float y2 = pos.y - vertex.pos.y;
		return x1 * y2 - x2 * y1 < 0.0f;
	}
}