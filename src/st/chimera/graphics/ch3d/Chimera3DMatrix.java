package st.chimera.graphics.ch3d;

/////////////////////////////////////////////////////////////////////////////
//***************************************************************************
// クラス名   : Chimera3DMatrix
// 効用       : １次変換用行列
// Version    : 0.6 (2002/11/17)
//***************************************************************************
public class Chimera3DMatrix {
    public double m[][] = new double[4][4];

    public Chimera3DMatrix() {
    }

    public Chimera3DMatrix(Chimera3DMatrix rhs) {
        assign(rhs);
    }

    // クローン =========================================================
    public Object clone() {
        return new Chimera3DMatrix(this);
    }

    // ゼロ初期化 =======================================================
    public Chimera3DMatrix zero() {
        for(int i = 0; i < 4; ++i) {
            for(int j = 0; j < 4; ++j) {
                m[i][j] = 0.0;
            }
        }
        return this;
    }

    // 基本行列化 =======================================================
    public Chimera3DMatrix initialize() {
        zero();
        m[0][0] = m[1][1] = m[2][2] = m[3][3] = 1.0;
        return this;
    }

    // コピー ===========================================================
    public void assign(Chimera3DMatrix rhs) {
        for(int i = 0; i < 4; ++i) {
            for(int j = 0; j < 4; ++j) {
                this.m[i][j] = rhs.m[i][j];
            }
        }
    }

    // 積算 =============================================================
    public Chimera3DMatrix mod(Chimera3DMatrix rhs) {
        Chimera3DMatrix ret = new Chimera3DMatrix();
        for(int i = 0; i < 4; ++i) {
            for(int j = 0; j < 4; ++j) {
                ret.m[i][j] = 0.0;
                for(int k = 0; k < 4; ++k) {
                    ret.m[i][j] += this.m[i][k] * rhs.m[k][j];
                }
            }
        }
        return ret;
    }

    // Ｘ軸回転 =========================================================
    public Chimera3DMatrix rotX(double rad) {
        double sin = Math.sin(rad);
        double cos = Math.cos(rad);
        Chimera3DMatrix tmp = new Chimera3DMatrix();
        tmp.initialize();
        tmp.m[1][1] =  cos; tmp.m[1][2] =  sin;
        tmp.m[2][1] = -sin; tmp.m[2][2] =  cos;
        assign(mod(tmp));
        return this;
    }

    // Ｙ軸回転 =========================================================
    public Chimera3DMatrix rotY(double rad) {
        double sin = Math.sin(rad);
        double cos = Math.cos(rad);
        Chimera3DMatrix tmp = new Chimera3DMatrix();
        tmp.initialize();
        tmp.m[0][0] =  cos; tmp.m[0][2] = -sin;
        tmp.m[2][0] =  sin; tmp.m[2][2] =  cos;
        assign(mod(tmp));
        return this;
    }

    // Ｚ軸回転 =========================================================
    public Chimera3DMatrix rotZ(double rad) {
        double sin = Math.sin(rad);
        double cos = Math.cos(rad);
        Chimera3DMatrix tmp = new Chimera3DMatrix();
        tmp.initialize();
        tmp.m[0][0] =  cos; tmp.m[0][1] =  sin;
        tmp.m[1][0] = -sin; tmp.m[1][1] =  cos;
        assign(mod(tmp));
        return this;
    }

    // 移動 =============================================================
    public Chimera3DMatrix move(double dx, double dy, double dz) {
        Chimera3DMatrix tmp = new Chimera3DMatrix();
        tmp.initialize();
        tmp.m[3][0] = dx; tmp.m[3][1] = dy; tmp.m[3][2] = dz;
        assign(mod(tmp));
        return this;
    }

    // 拡大縮小 =========================================================
    public Chimera3DMatrix scale(double sx, double sy, double sz) {
        Chimera3DMatrix tmp = new Chimera3DMatrix();
        tmp.initialize();
        tmp.m[0][0] = sx; tmp.m[1][1] = sy; tmp.m[2][2] = sz;
        assign(mod(tmp));
        return this;
    }

    // 拡大縮小 =========================================================
    public Chimera3DMatrix scale(double s) {
        return scale(s, s, s);
    }

    // カメラ行列設定 ===================================================
    public void setCameraMatrix(Chimera3DVector from, Chimera3DVector at,
                                Chimera3DVector worldUp) throws Exception {
        Chimera3DVector view = at.sub(from);
        double len = view.calcMagnitude();
        if(len < 1.0e-6) throw new Exception();
        view = view.mod(1.0 / len);
        double dotProduct = worldUp.calcDotProduct(view);
        Chimera3DVector up = worldUp.sub(view.mod(dotProduct));
        if(1.0e-6 > (len = up.calcMagnitude())) {
            up = (new Chimera3DVector(0.0, 1.0, 0.0)).sub(view.mod(view.y));
            if(1.0e-6 > (len = up.calcMagnitude())) {
                up = (new Chimera3DVector(0.0, 0.0, 1.0)).sub(view.mod(view.z));
                if(1.0e-6 > (len = up.calcMagnitude()))
                    throw new Exception();
            }
        }
        up = up.mod(1.0 / len);
        Chimera3DVector right = up.calcCrossProduct(view);
        initialize();
        m[0][0] = right.x; m[0][1] = up.x; m[0][2] = view.x;
        m[1][0] = right.y; m[1][1] = up.y; m[1][2] = view.y;
        m[2][0] = right.z; m[2][1] = up.z; m[2][2] = view.z;
        m[3][0] = -from.calcDotProduct(right);
        m[3][1] = -from.calcDotProduct(up);
        m[3][2] = -from.calcDotProduct(view);
    }

    // 投影行列 =========================================================
    public void setProjectionMatrix(double FOV, double aspect,
                                    double nearPlane, double farPlane) throws Exception {
        if(Math.abs(farPlane - nearPlane) < 0.01) throw new Exception();
        if(Math.abs(Math.sin(FOV/2)) < 0.01) throw new Exception();
        zero();
        m[0][0] = aspect * (Math.cos(FOV/2) / Math.sin(FOV/2));
        m[1][1] = 1.0    * (Math.cos(FOV/2) / Math.sin(FOV/2));
        m[2][2] = farPlane / (farPlane - nearPlane);
        m[2][3] = 1.0;
        m[3][2] = -m[2][2]*nearPlane;
    }

    // ビューポート行列設定 =============================================
    public void setViewPortMatrix(double x, double y, double width,
                                  double height, double minZ, double maxZ) {
        initialize();
        m[0][0] = width   / 2.0;
        m[1][1] = -height / 2.0;
        m[2][2] = maxZ - minZ;
        m[3][0] = x + width  / 2.0;
        m[3][1] = y + height / 2.0;
        m[3][2] = minZ;
    }

    // 頂点座標変換 =====================================================
    public void transformVectorPosition(Chimera3DVector dest, Chimera3DVector src) {
        double w = src.x * m[0][3] + src.y * m[1][3]
            + src.z * m[2][3] + m[3][3];
        w = 1.0 / w;
        dest.x = (src.x * m[0][0] + src.y * m[1][0]
                  + src.z * m[2][0] + m[3][0]) * w;
        dest.y = (src.x * m[0][1] + src.y * m[1][1]
                  + src.z * m[2][1] + m[3][1]) * w;
        dest.z = (src.x * m[0][2] + src.y * m[1][2]
                  + src.z * m[2][2] + m[3][2]) * w;
    }

    // 頂点座標変換 =====================================================
    public void transformVertexPosition(Chimera3DTLVertex dest, Chimera3DVertex src) {
        dest.pos.x = src.pos.x * m[0][0] + src.pos.y * m[1][0]
            + src.pos.z * m[2][0] + m[3][0];
        dest.pos.y = src.pos.x * m[0][1] + src.pos.y * m[1][1]
            + src.pos.z * m[2][1] + m[3][1];
        dest.pos.z = src.pos.x * m[0][2] + src.pos.y * m[1][2]
            + src.pos.z * m[2][2] + m[3][2];
        dest.w     = src.pos.x * m[0][3] + src.pos.y * m[1][3]
            + src.pos.z * m[2][3] + m[3][3];
    }

    // 頂点座標変換 =====================================================
    public void transformVertexPosition(Chimera3DTLVertex dest, Chimera3DLVertex src) {
        dest.pos.x = src.pos.x * m[0][0] + src.pos.y * m[1][0]
            + src.pos.z * m[2][0] + m[3][0];
        dest.pos.y = src.pos.x * m[0][1] + src.pos.y * m[1][1]
            + src.pos.z * m[2][1] + m[3][1];
        dest.pos.z = src.pos.x * m[0][2] + src.pos.y * m[1][2]
            + src.pos.z * m[2][2] + m[3][2];
        dest.w     = src.pos.x * m[0][3] + src.pos.y * m[1][3]
            + src.pos.z * m[2][3] + m[3][3];
    }

    // 法線ベクトル変換 =================================================
    public void transformVertexNormal(Chimera3DVector dest, Chimera3DVertex src) {
        dest.x = src.nor.x * m[0][0] + src.nor.y * m[1][0]
            + src.nor.z * m[2][0];
        dest.y = src.nor.x * m[0][1] + src.nor.y * m[1][1]
            + src.nor.z * m[2][1];
        dest.z = src.nor.x * m[0][2] + src.nor.y * m[1][2]
            + src.nor.z * m[2][2];
        dest.normalize();
    }
}
