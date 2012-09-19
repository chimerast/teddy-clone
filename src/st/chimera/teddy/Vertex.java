package st.chimera.teddy;

import javax.vecmath.*;

class Vertex {
	Point2f pos;
	HalfEdge halfEdge;
	float height = 0.0f;

	public Vertex(float x, float y) {
		pos = new Point2f(x, y);
	}

	public static float cos(Vertex[] v, int a, int b, int c) {
		Vector2f vec1 = new Vector2f(v[a].pos.x - v[b].pos.x, v[a].pos.y - v[b].pos.y);
		Vector2f vec2 = new Vector2f(v[c].pos.x - v[b].pos.x, v[c].pos.y - v[b].pos.y);
		vec1.normalize();
		vec2.normalize();
		return vec1.dot(vec2);
	}
}