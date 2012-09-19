package st.chimera.teddy;

import java.awt.geom.*;
import java.util.*;

import javax.vecmath.*;

import st.chimera.graphics.ch3d.*;

public class Teddy {
	public static final int STAGE_0_START = 0;
	public static final int STAGE_1_DIVIDED = 1;
	public static final int STAGE_2_RESTRICTED = 2;
	public static final int STAGE_3_REMOVED = 3;
	public static final int STAGE_4_CLASSIFYED = 4;
	public static final int STAGE_5_MIDPOINT = 5;
	public static final int STAGE_6_CHORDALAXIS = 6;
	public static final int STAGE_7_FAN = 7;
	public static final int STAGE_8_RESTDEVIDE = 8;

	Axis chordalAxisRoot;
	Loop exteriorLoop;
	Loop loopsHeader;
	int stage;
	ArrayList vertices;

	public Teddy() {
		stage = STAGE_0_START;
		vertices = new ArrayList();
		loopsHeader = new Loop();
		exteriorLoop = null;
		chordalAxisRoot = null;
	}

	/**
	 * 頂点の追加。ドロネー三角分割を構成する。
	 * 
	 * @param x 追加する点のＸ座標
	 * @param y 追加する点のＹ座標
	 */
	public void addPoint(float x, float y) {
		// すでに制約済みなら点を追加できない
		if (stage >= STAGE_2_RESTRICTED)
			return;

		Vertex vertex = new Vertex(x, y);
		vertices.add(vertex);

		if (exteriorLoop == null) {
			exteriorLoop = new Loop();
			exteriorLoop.halfEdge = new HalfEdge();
			exteriorLoop.halfEdge.vertex = vertex;
			exteriorLoop.halfEdge.loop = exteriorLoop;
			vertex.halfEdge = exteriorLoop.halfEdge;

		} else if (exteriorLoop.halfEdge.next == exteriorLoop.halfEdge) {
			// 一つしか点がなければ線を構成する
			exteriorLoop.halfEdge.pair.vertex = vertex;
			exteriorLoop.halfEdge.pair.loop = exteriorLoop;
			vertex.halfEdge = exteriorLoop.halfEdge.pair;
			exteriorLoop.halfEdge.link(exteriorLoop.halfEdge.pair);

		} else {
			stage = STAGE_1_DIVIDED;

			// すでにドロネー分割されている場合
			HalfEdge cur = exteriorLoop.halfEdge;
			do {
				if (cur.left(vertex.pos)) {
					// 分割の外側に点が追加された場合
					divideDelaunayByExteriorVertex(cur, vertex);
					return;
				}
				cur = cur.next;
			} while (cur != exteriorLoop.halfEdge);

			// 分割の内側に点が追加された場合
			// どの三角形の内部に点があるか調べる
			for (Loop loop = loopsHeader.next; loop != loopsHeader; loop = loop.next) {
				if (loop.inLoop(vertex)) {
					divideDelaunayByInteriorVertex(loop, vertex);
					return;
				}
			}
		}
	}

	public void calcVertexHeight() {
		if (stage != STAGE_8_RESTDEVIDE)
			return;

		Axis axis = chordalAxisRoot;

		do {
			Vertex vertex = axis.vertex;
			if (axis.vertex.height == 0.0f) {
				int count = 0;
				HalfEdge edge = vertex.halfEdge;
				do {
					if (exteriorVertex(edge.pair.vertex)) {
						vertex.height += edge.vertex.pos.distance(edge.pair.vertex.pos);
						++count;
					}
					edge = edge.pair.next;
				} while (edge != vertex.halfEdge);
				vertex.height /= count;
			}
			axis = axis.next;
		} while (axis != chordalAxisRoot);
/*
		axis = chordalAxisRoot;
		do {
			Vertex vertex = axis.vertex;
			HalfEdge edge = vertex.halfEdge;
			do {
				if (exteriorVertex(edge.pair.vertex)) {
					divideEdgeByPoints(edge, 4);
				}
				edge = edge.pair.next;
			} while (edge != vertex.halfEdge);
			axis = axis.next;
		} while (axis != chordalAxisRoot);
		

		axis = chordalAxisRoot;
		do {
			Vertex vertex = axis.vertex;
			HalfEdge edge = vertex.halfEdge;
			do {
				if (edge.pair.vertex == axis.next.vertex) {
					if (!edge.loop.postdivided) {
						divideFanTriangle(edge.loop, edge.vertex);
						edge.loop.postdivided = true;
					}
					if (!edge.pair.loop.postdivided) {
						divideFanTriangle(edge.pair.loop, edge.pair.vertex);
						edge.pair.loop.postdivided = true;
					}
				}
				edge = edge.pair.next;
			} while (edge != vertex.halfEdge);
			axis = axis.next;
		} while (axis != chordalAxisRoot);
*/
		nextStage();
	}

	public void classifyTriangles() {
		if (stage != STAGE_3_REMOVED)
			return;

		for (Loop loop = loopsHeader.next; loop != loopsHeader; loop = loop.next) {
			loop.kind = 0;
			HalfEdge edge = loop.halfEdge;
			do {
				if (edge.pair.loop != exteriorLoop)
					++loop.kind;
				edge = edge.next;
			} while (edge != loop.halfEdge);
		}

		nextStage();
	}

	public void constructChordalAxis() {
		if (stage != STAGE_5_MIDPOINT)
			return;

		Loop loop = loopsHeader.next;
		while (loop.kind != Loop.TERMINAL)
			loop = loop.next;

		HalfEdge edge = loop.halfEdge;
		do {
			if (edge.pair.loop != exteriorLoop && edge.prev.pair.loop != exteriorLoop) {
				chordalAxisRoot = new Axis(loop);
				chordalAxisRoot.link(chordalAxisRoot.pair);
				chordalAxisRoot.vertex = edge.prev.prev.vertex;
				chordalAxisRoot.pair.vertex = edge.vertex;

				Axis axis = constructChordalAxis(edge.pair.next);

				chordalAxisRoot.next = axis;
				axis.prev = chordalAxisRoot;
				chordalAxisRoot.pair.prev = axis.pair;
				axis.pair.next = chordalAxisRoot.pair;

				break;
			}
			edge = edge.next;
		} while (edge != loop.halfEdge);

		nextStage();
	}

	private Axis constructChordalAxis(HalfEdge edge) {
		Loop loop = edge.loop;
		Axis ret = new Axis(loop);
		switch (loop.kind) {
			case Loop.TERMINAL :
				ret.vertex = edge.vertex;
				ret.pair.vertex = edge.prev.prev.vertex;
				ret.link(ret.pair);
				return ret;

			case Loop.SLEEVE :
				Axis axis;
				if (edge.next.pair.loop == exteriorLoop) {
					ret.vertex = edge.vertex;
					ret.pair.vertex = edge.prev.prev.vertex;
					axis = constructChordalAxis(edge.next.next.pair);
				} else {
					ret.vertex = edge.vertex;
					ret.pair.vertex = edge.next.next.vertex;
					axis = constructChordalAxis(edge.next.pair);
				}
				ret.next = axis;
				axis.prev = ret;
				ret.pair.prev = axis.pair;
				axis.pair.next = ret.pair;
				return ret;

			case Loop.JUNCTION :
				Vertex mid =
					new Vertex(
						(edge.prev.prev.vertex.pos.x
							+ edge.vertex.pos.x
							+ edge.next.next.vertex.pos.x)
							/ 3.0f,
						(edge.prev.prev.vertex.pos.y
							+ edge.vertex.pos.y
							+ edge.next.next.vertex.pos.y)
							/ 3.0f);
				vertices.add(mid);

				HalfEdge[] edges = new HalfEdge[6];
				edges[0] = edge.prev;
				edges[1] = edges[0].next;
				edges[2] = edges[1].next;
				edges[3] = edges[2].next;
				edges[4] = edges[3].next;
				edges[5] = edges[4].next;

				for (int i = 0; i < 6; ++i)
					edges[i].unlink();

				HalfEdge[] newedges = new HalfEdge[3];
				for (int i = 0; i < 3; ++i)
					newedges[i] = new HalfEdge();

				// 新しくループは二つ生成される
				Loop[] newloops = new Loop[3];
				newloops[0] = loop;
				newloops[1] = new Loop();
				newloops[2] = new Loop();

				loopsHeader.link(newloops[1]);
				loopsHeader.link(newloops[2]);

				for (int i = 0; i < 3; ++i) {
					HalfEdge ne1 = newedges[i];
					HalfEdge ne21 = edges[i * 2];
					HalfEdge ne22 = edges[i * 2 + 1];
					HalfEdge ne3 = newedges[(i + 1) % 3].pair;

					ne1.vertex = mid;
					ne3.vertex = edges[(i + 1) % 3 * 2].vertex;

					ne1.link(ne21);
					ne21.link(ne22);
					ne22.link(ne3);

					newloops[i].halfEdge = ne1;
					ne1.loop = newloops[i];
					ne21.loop = newloops[i];
					ne22.loop = newloops[i];
					ne3.loop = newloops[i];
					newloops[i].kind = Loop.JUNCTION;
				}

				mid.halfEdge = newedges[0];

				ret.vertex = edge.vertex;
				ret.pair.vertex = mid;

				Axis new1 = new Axis(newloops[1]);
				Axis new2 = new Axis(newloops[2]);

				new1.vertex = mid;
				new1.pair.vertex = edges[3].vertex;

				new2.vertex = mid;
				new2.pair.vertex = edges[5].vertex;

				Axis axis1 = constructChordalAxis(edges[2].pair);
				Axis axis2 = constructChordalAxis(edges[4].pair);

				axis1.prev = new1;
				new1.next = axis1;
				axis1.pair.next = new1.pair;
				new1.pair.prev = axis1.pair;

				axis2.prev = new2;
				new2.next = axis2;
				axis2.pair.next = new2.pair;
				new2.pair.prev = axis2.pair;

				ret.next = new1;
				new1.prev = ret;
				new1.pair.next = new2;
				new2.prev = new1.pair;
				new2.pair.next = ret.pair;
				ret.pair.prev = new2.pair;

				return ret;
		}
		return null;
	}

	public void constructFanTriangle() {
		if (stage != STAGE_6_CHORDALAXIS)
			return;

		List terminalList = new LinkedList();
		Axis end = chordalAxisRoot;
		while (end.loop.kind == Loop.TERMINAL)
			end = end.prev;

		Axis cur = end;
		do {
			if (cur.loop.kind == Loop.TERMINAL) {
				terminalList.add(cur.next);
				cur = cur.next.next;
			} else {
				cur = cur.next;
			}

		} while (cur != end);

		Iterator itr = terminalList.iterator();
		while (itr.hasNext()) {
			constructFanTriangle((Axis)itr.next(), null);
		}

		nextStage();
	}

	private void constructFanTriangle(Axis axis, HalfEdge edge) {
		Vertex vertex = axis.pair.vertex;
		float rsqr = axis.vertex.halfEdge.pair.vertex.pos.distanceSquared(vertex.pos);

		if (axis.loop.kind == Loop.TERMINAL && edge == null) {
			HalfEdge next;
			if (axis.vertex.halfEdge.loop == exteriorLoop) {
				next = axis.vertex.halfEdge.pair.next.next.pair;
			} else {
				next = axis.vertex.halfEdge.next.pair;
			}

			chordalAxisRoot = axis.next;

			if (testInCircle(axis.pair, next.pair.next, vertex.pos, rsqr)) {
				constructFanTriangle(axis.next, next);
			} else {
				divideFanTriangle(next.pair.loop, axis.next.vertex);
			}
		} else if (axis.loop.kind == Loop.SLEEVE) {
			HalfEdge next;
			if (edge.next.pair.loop == exteriorLoop) {
				next = edge.next.next.pair;
			} else {
				next = edge.next.pair;
			}

			chordalAxisRoot = axis.next;
			removeDiagonalLine(axis.vertex);

			if (testInCircle(axis.pair, next.pair.next, vertex.pos, rsqr)) {
				constructFanTriangle(axis.next, next);
			} else {
				divideFanTriangle(next.pair.loop, axis.next.vertex);
			}

		} else if (axis.loop.kind == Loop.JUNCTION) {
			HalfEdge next = edge.next.pair;
			removeDiagonalLine(axis.vertex);
			divideFanTriangle(next.pair.loop, axis.next.vertex);

			chordalAxisRoot = axis.next.next;
		}

		axis.pair.unlink();
		axis.unlink();
	}

	public void createMidPoint() {
		if (stage != STAGE_4_CLASSIFYED)
			return;

		for (Loop loop = loopsHeader.next; loop != loopsHeader; loop = loop.next) {
			HalfEdge edge = loop.halfEdge;
			do {
				if (edge.pair.loop != exteriorLoop)
					divideEdgeByMidPoint(edge);
				edge = edge.next;
			} while (edge != loop.halfEdge);
		}

		nextStage();
	}

	/**
	 * ドロネー三角分割の条件を満たすように、再帰的に
	 * 対角線を入れ替える。
	 * 
	 * @param edge 入れかえるか調査する対角線
	 */
	private void delaunayCheck(HalfEdge edge) {

		// 外周部の場合
		if (edge.pair.loop == null)
			return;

		// 制約されている場合
		if (edge.edge.restricted == true)
			return;

		// edgeを対角線として持つ四角形の取り出し
		Vertex[] vertex = new Vertex[4];
		vertex[0] = edge.next.vertex;
		vertex[1] = edge.prev.vertex;
		vertex[2] = edge.pair.next.vertex;
		vertex[3] = edge.pair.prev.vertex;

		// 凸四角形出ない場合は入れ替えない
		for (int i = 0; i < 4; ++i) {
			float x1 = vertex[(i + 1) % 4].pos.x - vertex[i + 0].pos.x;
			float y1 = vertex[(i + 1) % 4].pos.y - vertex[i + 0].pos.y;
			float x2 = vertex[(i + 2) % 4].pos.x - vertex[i + 0].pos.x;
			float y2 = vertex[(i + 2) % 4].pos.y - vertex[i + 0].pos.y;
			if (!(x1 * y2 - x2 * y1 < 0.0f))
				return;
		}

		// 新しい分割と古い分割においての三角形の最小の内角を探す
		float oldDeg = -1, newDeg = -1;
		oldDeg = Math.max(oldDeg, Vertex.cos(vertex, 0, 1, 2));
		oldDeg = Math.max(oldDeg, Vertex.cos(vertex, 2, 0, 1));
		oldDeg = Math.max(oldDeg, Vertex.cos(vertex, 1, 2, 0));
		oldDeg = Math.max(oldDeg, Vertex.cos(vertex, 2, 3, 0));
		oldDeg = Math.max(oldDeg, Vertex.cos(vertex, 0, 2, 3));
		oldDeg = Math.max(oldDeg, Vertex.cos(vertex, 3, 0, 2));

		newDeg = Math.max(newDeg, Vertex.cos(vertex, 1, 2, 3));
		newDeg = Math.max(newDeg, Vertex.cos(vertex, 3, 1, 2));
		newDeg = Math.max(newDeg, Vertex.cos(vertex, 2, 3, 1));
		newDeg = Math.max(newDeg, Vertex.cos(vertex, 3, 0, 1));
		newDeg = Math.max(newDeg, Vertex.cos(vertex, 1, 3, 0));
		newDeg = Math.max(newDeg, Vertex.cos(vertex, 0, 1, 3));

		if (newDeg < oldDeg) {
			// 新しい分割の最小の内角が古いものより大きくなる場合
			// 対角線を入れ換える
			flipDiagonalLine(edge);

			// 入れ換えた対角線以外の辺について再帰的に処理する
			delaunayCheck(edge.next);
			delaunayCheck(edge.prev);
			delaunayCheck(edge.pair.next);
			delaunayCheck(edge.pair.prev);
		}
	}

	/**
	 * 最外の頂点が構成する凸多角形の外側に点が配置された場合の分割。
	 * 
	 * @param edge 点が置かれることにより内側に入る点 
	 * @param vertex 置かれる頂点
	 */
	private void divideDelaunayByExteriorVertex(HalfEdge edge, Vertex vertex) {
		HalfEdge end;

		// 最初の辺を探す
		while (edge.prev.left(vertex.pos))
			edge = edge.prev;

		// 終わりの辺を探す
		end = edge;
		while (end.next.left(vertex.pos))
			end = end.next;
		end = end.next;

		// 新しく作られる外側の辺
		HalfEdge exteriorEdge1, exteriorEdge2;
		// 新しく作られる内側の辺
		HalfEdge interiorEdge1, interiorEdge2, interiorEdge3;

		exteriorEdge1 = new HalfEdge();
		exteriorEdge1.vertex = edge.vertex;

		// 外側の辺は内側の辺と対になっている
		interiorEdge3 = exteriorEdge1;

		while (edge != end) {
			interiorEdge1 = interiorEdge3.pair;
			interiorEdge1.vertex = vertex;

			interiorEdge3 = new HalfEdge();
			interiorEdge3.vertex = edge.next.vertex;

			interiorEdge2 = edge;
			edge = edge.next;
			interiorEdge2.unlink();

			interiorEdge1.link(interiorEdge2);
			interiorEdge2.link(interiorEdge3);

			// 内側辺による新しいループの生成
			Loop loop = new Loop();
			loop.halfEdge = interiorEdge1;
			interiorEdge1.loop = loop;
			interiorEdge2.loop = loop;
			interiorEdge3.loop = loop;
			loopsHeader.link(loop);

			// 再帰的ドロネー三角分割
			// ### ここでやっていいのか不安な部分 だめなら外に出す
			delaunayCheck(interiorEdge2);
		}
		exteriorEdge2 = interiorEdge3.pair;
		exteriorEdge2.vertex = vertex;

		vertex.halfEdge = exteriorEdge2;

		exteriorEdge1.loop = exteriorLoop;
		exteriorEdge2.loop = exteriorLoop;

		// 外側辺の形成
		end.prev.link(exteriorEdge1);
		end.prev.link(exteriorEdge2);

		// ルートが必ず外周に来るようにする
		exteriorLoop.halfEdge = exteriorEdge1;
	}

	/**
	 * すでに分割されている三角形の中に頂点が置かれる場合。
	 * 
	 * @param loop 点を内包する三角形
	 * @param vertex 置かれる頂点
	 */
	private void divideDelaunayByInteriorVertex(Loop loop, Vertex vertex) {
		HalfEdge[] edges = new HalfEdge[3];
		edges[0] = loop.halfEdge.prev;
		edges[1] = loop.halfEdge;
		edges[2] = loop.halfEdge.next;

		for (int i = 0; i < 3; ++i)
			edges[i].unlink();

		HalfEdge[] newedges = new HalfEdge[3];
		for (int i = 0; i < 3; ++i)
			newedges[i] = new HalfEdge();

		// 新しくループは二つ生成される
		Loop[] newloops = new Loop[3];
		newloops[0] = loop;
		newloops[1] = new Loop();
		newloops[2] = new Loop();

		loopsHeader.link(newloops[1]);
		loopsHeader.link(newloops[2]);

		for (int i = 0; i < 3; ++i) {
			HalfEdge ne1 = newedges[i];
			HalfEdge ne2 = edges[i];
			HalfEdge ne3 = newedges[(i + 1) % 3].pair;

			ne1.vertex = vertex;
			ne3.vertex = edges[(i + 1) % 3].vertex;

			ne1.link(ne2);
			ne2.link(ne3);

			newloops[i].halfEdge = ne1;
			ne1.loop = newloops[i];
			ne2.loop = newloops[i];
			ne3.loop = newloops[i];
		}

		vertex.halfEdge = newedges[0];

		for (int i = 0; i < 3; ++i)
			delaunayCheck(edges[i]);
	}

	private void divideEdgeByMidPoint(HalfEdge edge) {
		if (edge.edge.divided)
			return;

		Vertex vertex =
			new Vertex(
				edge.vertex.pos.x * 0.5f + edge.pair.vertex.pos.x * 0.5f,
				edge.vertex.pos.y * 0.5f + edge.pair.vertex.pos.y * 0.5f);
		vertices.add(vertex);

		HalfEdge newedge = new HalfEdge();

		edge.link(newedge);
		newedge.vertex = vertex;
		newedge.loop = edge.loop;

		edge.pair.prev.link(newedge.pair);
		newedge.pair.vertex = edge.pair.vertex;
		newedge.pair.loop = edge.pair.loop;
		edge.pair.vertex = vertex;

		newedge.vertex.halfEdge = newedge;
		newedge.pair.vertex.halfEdge = newedge.pair;

		edge.edge.divided = true;
		newedge.edge.divided = true;
	}

	private void divideEdgeByPoints(HalfEdge edge, int n) {
		if (edge.edge.postdivided)
			return;

		HalfEdge cur = edge;
		Vertex[] vertex = new Vertex[n];
		for (int i = 0; i < n ; ++i) {
			float t = (i + 1) / (float)(n + 1);
			vertex[i] = 
				new Vertex(
					edge.vertex.pos.x * t + edge.pair.vertex.pos.x * (1.0f - t),
					edge.vertex.pos.y * t + edge.pair.vertex.pos.y * (1.0f - t));
			if (edge.vertex.height == 0.0) {
				vertex[i].height = (float)Math.sqrt(1.0 - t * t) * edge.pair.vertex.height;
			} else {
				vertex[i].height = (float)Math.sqrt(1.0 - t * t) * edge.vertex.height;		
			}
			vertices.add(vertex[i]);

			HalfEdge newedge = new HalfEdge();

			cur.link(newedge);
			newedge.vertex = vertex[i];
			newedge.loop = cur.loop;

			cur.pair.prev.link(newedge.pair);
			newedge.pair.vertex = cur.pair.vertex;
			newedge.pair.loop = cur.pair.loop;
			cur.pair.vertex = vertex[i];

			newedge.vertex.halfEdge = newedge;
			newedge.pair.vertex.halfEdge = newedge.pair;
		
			cur.edge.postdivided = true;
			newedge.edge.postdivided = true; 
			
			cur = newedge;
		}
	}

	private void divideFanTriangle(Loop loop, Vertex start) {
		HalfEdge edge = loop.halfEdge;

		while (edge.vertex != start)
			edge = edge.prev;

		HalfEdge inner1 = edge;
		HalfEdge cur = edge.next;
		edge = edge.prev;

		inner1.unlink();

		while (cur.next != edge) {
			HalfEdge inner2 = cur;
			HalfEdge inner3 = new HalfEdge();

			inner1.vertex = start;
			inner3.vertex = cur.next.vertex;

			cur = cur.next;

			inner2.unlink();

			inner1.link(inner2);
			inner2.link(inner3);

			Loop newloop = new Loop();
			newloop.halfEdge = inner1;
			newloop.kind = Loop.FAN;
			inner1.loop = newloop;
			inner2.loop = newloop;
			inner3.loop = newloop;
			loopsHeader.link(newloop);

			inner1 = inner3.pair;
		}

		edge.link(inner1);
		inner1.vertex = start;
		inner1.loop = edge.loop;
		inner1.loop.kind = Loop.FAN;
		inner1.loop.halfEdge = inner1;
	}

	public void divideRestLoops() {
		if (stage != STAGE_7_FAN)
			return;

		Axis axis = chordalAxisRoot;
		do {
			if (!axis.loop.divided) {
				divideFanTriangle(axis.loop, axis.vertex);
				axis.loop.divided = true;
			}
			axis = axis.next;
		} while (axis != chordalAxisRoot);

		nextStage();
	}


	boolean exteriorVertex(Vertex vertex) {
		HalfEdge edge = vertex.halfEdge;
		do {
			if (edge.loop == exteriorLoop || edge.pair.loop == exteriorLoop ) 
				return true;
			edge = edge.pair.next;
		} while (edge != vertex.halfEdge);
		
		return false;
	}

	/**
	 * ２つの三角形からなる四角形の対角線を入れ換える。
	 * 
	 * @param edge 入れ換える対角線
	 * @return 入れ換えられた対角線
	 */
	private HalfEdge flipDiagonalLine(HalfEdge edge) {
		HalfEdge[] edges = new HalfEdge[4];

		edges[0] = edge.next;
		edges[1] = edge.prev;
		edges[2] = edge.pair.next;
		edges[3] = edge.pair.prev;

		for (int i = 0; i < edges.length; ++i) {
			edges[i].unlink();
			edges[i].vertex.halfEdge = edges[i];
		}

		edge.loop.halfEdge = edge;
		edge.pair.loop.halfEdge = edge.pair;

		edge.vertex = edges[3].vertex;
		edges[1].link(edges[2]);
		edges[2].link(edge);
		edges[1].loop = edge.loop;
		edges[2].loop = edge.loop;

		edge.pair.vertex = edges[1].vertex;
		edges[3].link(edges[0]);
		edges[0].link(edge.pair);
		edges[3].loop = edge.pair.loop;
		edges[0].loop = edge.pair.loop;

		return edge;
	}

	public Chimera3DVertex[] get3DVertices() {
		ArrayList list = new ArrayList();
		for (Loop loop = loopsHeader.next; loop != loopsHeader; loop = loop.next) {
			Chimera3DVertex[] vertex = new Chimera3DVertex[6];
			for (int i = 0; i < vertex.length; ++i)
				vertex[i] = new Chimera3DVertex();
			HalfEdge edge = loop.halfEdge;

			vertex[0].pos =
				new Chimera3DVector(
					edge.prev.vertex.pos.x - 300,
					edge.prev.vertex.pos.y - 300,
					edge.prev.vertex.height);
			vertex[1].pos =
				new Chimera3DVector(
					edge.vertex.pos.x - 300,
					edge.vertex.pos.y - 300,
					edge.vertex.height);
			vertex[2].pos =
				new Chimera3DVector(
					edge.next.vertex.pos.x - 300,
					edge.next.vertex.pos.y - 300,
					edge.next.vertex.height);
			vertex[3].pos =
				new Chimera3DVector(
					edge.next.vertex.pos.x - 300,
					edge.next.vertex.pos.y - 300,
					-edge.next.vertex.height);
			vertex[4].pos =
				new Chimera3DVector(
					edge.vertex.pos.x - 300,
					edge.vertex.pos.y - 300,
					-edge.vertex.height);
			vertex[5].pos =
				new Chimera3DVector(
					edge.prev.vertex.pos.x - 300,
					edge.prev.vertex.pos.y - 300,
					-edge.prev.vertex.height);

			Chimera3DVector v1 = vertex[0].pos.sub(vertex[1].pos);
			Chimera3DVector v2 = vertex[2].pos.sub(vertex[1].pos);
			Chimera3DVector v3 = vertex[3].pos.sub(vertex[4].pos);
			Chimera3DVector v4 = vertex[5].pos.sub(vertex[4].pos);
			Chimera3DVector nor1 = v1.calcCrossProduct(v2);
			Chimera3DVector nor2 = v3.calcCrossProduct(v4);

			nor1.normalize();
			nor2.normalize();

			vertex[0].nor = nor1;
			vertex[1].nor = nor1;
			vertex[2].nor = nor1;

			vertex[3].nor = nor2;
			vertex[4].nor = nor2;
			vertex[5].nor = nor2;

			list.add(vertex[0]);
			list.add(vertex[1]);
			list.add(vertex[2]);
			list.add(vertex[3]);
			list.add(vertex[4]);
			list.add(vertex[5]);
		}

		Chimera3DVertex[] ret = new Chimera3DVertex[list.size()];
		System.arraycopy(list.toArray(), 0, ret, 0, list.size());

		return ret;
	}

	public Line2D[] getAxis() {
		ArrayList list = new ArrayList();
		Axis axis = chordalAxisRoot;
		if (axis != null) {
			do {
				list.add(
					new Line2D.Float(
						axis.vertex.pos.x,
						axis.vertex.pos.y,
						axis.pair.vertex.pos.x,
						axis.pair.vertex.pos.y));
				axis = axis.next;
			} while (axis != chordalAxisRoot);
		}

		Line2D[] lines = new Line2D[list.size()];
		System.arraycopy(list.toArray(), 0, lines, 0, lines.length);
		return lines;
	}

	public Loop getLines() {
		return exteriorLoop;
	}

	public int getStage() {
		return stage;
	}

	public Loop[] getTraingles() {
		ArrayList list = new ArrayList();
		for (Loop loop = loopsHeader.next; loop != loopsHeader; loop = loop.next) {
			list.add(loop);
		}
		Loop[] ret = new Loop[list.size()];
		System.arraycopy(list.toArray(), 0, ret, 0, list.size());
		return ret;
	}

	public Point2f[] getVertices() {
		int i = 0;
		Point2f[] points = new Point2f[vertices.size()];
		Iterator itr = vertices.iterator();
		while (itr.hasNext()) {
			points[i++] = ((Vertex)itr.next()).pos;
		}
		return points;
	}

	private void nextStage() {
		HalfEdge edge = exteriorLoop.halfEdge;
		do {
			if (edge.loop != exteriorLoop)
				System.err.println("error");
			if (!exteriorVertex(edge.vertex)) 
				System.err.println("error");				
			edge = edge.next;
		} while (edge != exteriorLoop.halfEdge);
		++stage;
	}

	private void removeDiagonalLine(Vertex mid) {
		HalfEdge[] edge = new HalfEdge[4];
		edge[0] = mid.halfEdge;
		edge[1] = mid.halfEdge.pair;
		edge[2] = mid.halfEdge.prev.pair;
		edge[3] = mid.halfEdge.prev;

		edge[0].prev = edge[1];
		edge[1].next = edge[0];
		edge[2].prev = edge[3];
		edge[3].next = edge[2];

		edge[0].next.vertex.halfEdge = edge[0].next;
		edge[0].next.loop.halfEdge = edge[0].next;
		edge[2].next.vertex.halfEdge = edge[2].next;

		edge[2].loop.unlink();

		HalfEdge cur = edge[0];
		do {
			cur.loop = edge[0].next.loop;
			cur = cur.next;
		} while (cur != edge[0]);

		for (int i = 0; i < 4; ++i)
			edge[i].unlink();

		vertices.remove(mid);
	}

	/**
	 * 制約された辺の外の分割を取り除く。
	 * 
	 * @param edge 取り除く辺
	 * @return 取り除かれた辺の次の辺
	 */
	private HalfEdge removeExteriorTriangle(HalfEdge edge) {
		// 制約された辺ならばそのまま返る
		if (edge.edge.restricted)
			return edge.next;

		HalfEdge[] interior = new HalfEdge[3];
		interior[0] = edge.pair.prev;
		interior[1] = edge.pair;
		interior[2] = edge.pair.next;

		interior[1].loop.unlink();

		interior[0].unlink();
		interior[2].unlink();
		interior[0].loop = exteriorLoop;
		interior[2].loop = exteriorLoop;

		interior[0].vertex.halfEdge = interior[0];
		interior[2].vertex.halfEdge = interior[2];

		interior[2].vertex.halfEdge = interior[2];
		interior[0].pair.vertex.halfEdge = interior[0].pair;

		edge.prev.link(interior[2]);
		edge.prev.link(interior[0]);
		edge.unlink();

		exteriorLoop.halfEdge = interior[0];

		return interior[2];
	}

	/**
	 * 制約された辺の外の分割を取り除く。
	 */
	public void removeExteriorTriangles() {
		if (stage != STAGE_2_RESTRICTED)
			return;

		HalfEdge end = exteriorLoop.halfEdge;

		while (!end.edge.restricted)
			end = end.next;

		HalfEdge cur = end;
		do {
			cur = removeExteriorTriangle(cur);
		} while (cur != end);

		nextStage();
	}

	/**
	 * 状態の初期化
	 */
	public void reset() {
		stage = STAGE_0_START;
		vertices = new ArrayList();
		loopsHeader = new Loop();
		exteriorLoop = null;
		chordalAxisRoot = null;
	}

	/**
	 * 制約を行う。addPoint()された順に点を置いたときに辺をおけるように
	 * ドロネー分割を行う。
	 */
	public void restrictDlaunay() {
		if (stage != STAGE_1_DIVIDED)
			return;

		List replacedList = new LinkedList();

		int size = vertices.size();
		iterate : for (int i = 0; i < size; ++i) {
			Vertex v1 = (Vertex)vertices.get(i);
			Vertex v2 = (Vertex)vertices.get((i + 1) % size);

			HalfEdge edge = v1.halfEdge;
			flip : while (true) {
				if (edge.pair.vertex == v2) {
					// v1からv2への辺が存在していれば
					// 制約をつけて終わる
					edge.edge.restricted = true;
					continue iterate;
				} else if (edge.left(v2.pos) && edge.prev.left(v2.pos)) {
					// 辺をおくときに障害となる辺があれば、
					// その辺を入れ替える
					replacedList.add(flipDiagonalLine(edge.next));
					edge = v1.halfEdge;
					continue flip;
				}
				edge = edge.pair.next;

				if (edge == v1.halfEdge) {
					break flip;
				}
			}
		}

		// 動かした全ての対角線に対してドロネー分割を
		// 満たすように辺の移動を行う
		Iterator itr = replacedList.iterator();
		while (itr.hasNext())
			delaunayCheck((HalfEdge)itr.next());

		nextStage();
	}

	private boolean testInCircle(Axis axis, HalfEdge edge, Point2f pos, float rsqr) {
		if (axis.loop.kind == Loop.TERMINAL) {
			if (pos.distanceSquared(axis.next.vertex.pos) < rsqr)
				return true;
			else
				return false;
		} else if (axis.loop.kind == Loop.SLEEVE) {
			// Sleeve三角形なら次の三角形の結果を見る
			HalfEdge next;
			Vertex vertex;
			if (edge.next.pair.loop == exteriorLoop) {
				next = edge.next.next.pair;
				vertex = edge.next.next.vertex;
			} else {
				next = edge.next.pair;
				vertex = edge.prev.prev.vertex;
			}

			if (!testInCircle(axis.next, next, pos, rsqr))
				return false;
			if (pos.distanceSquared(vertex.pos) < rsqr)
				return true;
			else
				return false;
		}
		return false;
	}
}
