package st.chimera.graphics.ch3d;

/////////////////////////////////////////////////////////////////////////////
//***************************************************************************
// クラス名   : Chimera3DVector
// 効用       : ３次元ベクトル
// Version    : 0.6 (2002/11/17)
//***************************************************************************
public class Chimera3DVector {
    public double x, y, z;

    public Chimera3DVector() {
    }

    public Chimera3DVector(double x, double y, double z) {
        this.x = x; this.y = y; this.z = z;
    }

    public Object clone() {
        return new Chimera3DVector(x, y, z);
    }

    public Chimera3DVector add(Chimera3DVector rhs) {
        return new Chimera3DVector(x+rhs.x, y+rhs.y, z+rhs.z);
    }

    public Chimera3DVector sub(Chimera3DVector rhs) {
        return new Chimera3DVector(x-rhs.x, y-rhs.y, z-rhs.z);
    }

    public Chimera3DVector mod(double rhs) {
        return new Chimera3DVector(x*rhs, y*rhs, z*rhs);
    }

    public Chimera3DVector mod(Chimera3DVector rhs) {
        return new Chimera3DVector(x*rhs.x, y*rhs.y, z*rhs.z);
    }

    public Chimera3DVector normalize() {
        double size = 1.0 / calcMagnitude();
        x *= size; y *= size; z *= size;
        return this;
    }

    public double calcMagnitude() {
        return Math.sqrt(x*x + y*y + z*z);
    }

    public double calcDotProduct(Chimera3DVector rhs) {
        return x*rhs.x + y*rhs.y + z*rhs.z;
    }

    public Chimera3DVector calcCrossProduct(Chimera3DVector rhs) {
        return new Chimera3DVector(y*rhs.z - z*rhs.y,
                                   z*rhs.x - x*rhs.z, x*rhs.y - y*rhs.x);
    }
}
