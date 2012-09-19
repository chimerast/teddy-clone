package st.chimera.teddy;

class Axis {
	Axis pair;
	Axis next;
	Axis prev;
	Loop loop;
	Vertex vertex;
	
	public Axis(Loop loop) {
		this.pair = new Axis(this, loop);
		this.next = this;
		this.prev = this;
		this.loop = loop;
	}

	private Axis(Axis pair, Loop loop) {
		this.pair = pair;
		this.next = this;
		this.prev = this;
		this.loop = loop;
	}
	
	void link(Axis next) {
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
}
