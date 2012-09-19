package st.chimera.graphics.ch3d;

/////////////////////////////////////////////////////////////////////////////
//***************************************************************************
// クラス名   : Chimera3DTriangle
// 効用       : 三角形
// Version    : 0.6 (2002/11/17)
//***************************************************************************
public class Chimera3DTriangle {
    public Chimera3DTLVertex vtx1, vtx2, vtx3;
    public int color;

    // 隠面消去 =========================================================
    public boolean isCulling() {
        double check =
            vtx3.pos.x * ((vtx1.pos.z*vtx2.pos.y) - (vtx1.pos.y*vtx2.pos.z))
            + vtx3.pos.y * ((vtx1.pos.x*vtx2.pos.z) - (vtx1.pos.z*vtx2.pos.x))
            + vtx3.pos.z * ((vtx1.pos.y*vtx2.pos.x) - (vtx1.pos.x*vtx2.pos.y));
        return check > 0.0f;
    }
}
