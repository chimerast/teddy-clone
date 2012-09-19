package st.chimera.graphics.ch3d;

/////////////////////////////////////////////////////////////////////////////
//***************************************************************************
// クラス名   : Chimera3DTLVertex
// 効用       : 変換・ライティング済み頂点
// Version    : 0.6 (2002/11/17)
//***************************************************************************
public class Chimera3DTLVertex {
    public Chimera3DVector pos = new Chimera3DVector();
    public double w;
    public int diffuse;
    public int specular;

    public void transformFinal() {
        w = 1.0 / w;
        pos.x *= w;
        pos.y *= w;
        pos.z *= w;
    }
}
