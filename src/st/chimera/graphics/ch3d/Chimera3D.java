package st.chimera.graphics.ch3d;

import java.awt.*;
import java.util.*;

/////////////////////////////////////////////////////////////////////////////
//***************************************************************************
// クラス名   : Chimera3D
// 効用       : ３Ｄ描画クラス
// Version    : 0.6 (2002/11/17)
//***************************************************************************

public class Chimera3D {

    protected final int zSortSize = 1024;

    protected int width;
    protected int height;
    protected Component parent;

    protected int clearColor;

    // 描画用バックバッファ
    protected Graphics2D backBuffer;

    // ライティング属性
    protected int lightDiffuse;
    protected int lightSpecular;
    protected int lightAmbient;
    protected Chimera3DVector lightDirection = new Chimera3DVector();

    // 物質属性
    protected int materialDiffuse;
    protected int materialSpecular;
    protected int materialAmbient;
    protected double materialPower;

    // 各種行列
    protected Chimera3DMatrix matrixWorld      = new Chimera3DMatrix();
    protected Chimera3DMatrix matrixCamera     = new Chimera3DMatrix();
    protected Chimera3DMatrix matrixProjection = new Chimera3DMatrix();
    protected Chimera3DMatrix matrixViewPort   = new Chimera3DMatrix();

    // Ｚソート用バッファ
    protected Vector zSort[] = new Vector[zSortSize];



    // コンストラクタ ===================================================
    public Chimera3D(int width, int height, Graphics2D backBuffer) throws Exception {
        this.width      = width;
        this.height     = height;
        this.backBuffer = backBuffer;

        for(int i = 0; i < zSortSize; ++i) {
            zSort[i] = new Vector();
        }

        // 変換行列はすべて初期化
        matrixWorld.initialize();
        matrixCamera.setCameraMatrix(
            new Chimera3DVector(0.0, 0.0, -6.0),
            new Chimera3DVector(0.0, 0.0, 0.0),
            new Chimera3DVector(0.0, 1.0, 0.0)
            );
        matrixProjection.setProjectionMatrix(Math.atan(0.5)*2,
                                             (double)height / width, 1.0, 50.0);
        matrixViewPort.setViewPortMatrix(0, 0, width, height, 0.0, 1.0);
		/*
		// アンチエイリアシング有効
		backBuffer.setRenderingHint(
			RenderingHints.KEY_ANTIALIASING,
			RenderingHints.VALUE_ANTIALIAS_ON);
		*/

		// アルファブレンド速度重視
		backBuffer.setRenderingHint(
			RenderingHints.KEY_ALPHA_INTERPOLATION,
			RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
			
		backBuffer.setStroke(new BasicStroke(0.1f));

    }


    // 画面更新 =========================================================
    public void update() {
        for(int i = zSortSize - 1; i >= 0; --i) {
            for(int j = 0; j < zSort[i].size(); ++j) {
                Chimera3DTriangle tri = (Chimera3DTriangle)zSort[i].elementAt(j);
                int x[] = new int[3];
                int y[] = new int[3];
                x[0] = Math.round((float)tri.vtx1.pos.x);
                y[0] = Math.round((float)tri.vtx1.pos.y);
                x[1] = Math.round((float)tri.vtx2.pos.x);
                y[1] = Math.round((float)tri.vtx2.pos.y);
                x[2] = Math.round((float)tri.vtx3.pos.x);
                y[2] = Math.round((float)tri.vtx3.pos.y);
                backBuffer.setColor(new Color(tri.color));
                backBuffer.fillPolygon(x, y, 3);
            }
        }
    }


    // 画面消去 =========================================================
    public void clear() {
        for(int i = 0; i < zSortSize; ++i) {
            zSort[i].removeAllElements();
        }
        backBuffer.setColor(new Color(clearColor));
        backBuffer.fillRect(0, 0, width, height);
    }


    // 消去色設定 =======================================================
    public void setClearColor(int clearColor) {
        this.clearColor = clearColor;
    }


    // ワールド座標変換行列設定 =========================================
    public void setWorldMatrix(Chimera3DMatrix mat) {
        matrixWorld = mat;
    }

    // カメラ視点行列設定 ===============================================
    public void setCameraMatrix(Chimera3DMatrix mat) {
        matrixCamera = mat;
    }

    // 投影行列設定 =====================================================
    public void setProjectionMatrix(Chimera3DMatrix mat) {
        matrixProjection = mat;
    }

    // 材質設定 =========================================================
    public void setMaterial(int diffuse, int specular, int ambient, double power) {
        materialDiffuse  = diffuse;
        materialSpecular = specular;
        materialAmbient  = ambient;
        materialPower    = power;
    }


    // ライティング設定 =================================================
    public void setLighting(int diffuse, int specular, int ambient,
                            Chimera3DVector direction) {
        lightDiffuse   = diffuse;
        lightSpecular  = specular;
        lightAmbient   = ambient;
        lightDirection = direction;
    }


    // 座標変換 =========================================================
    protected void stageTransform(Chimera3DTLVertex dest[], Chimera3DVertex src[]) {
        Chimera3DMatrix mat = matrixWorld.mod(matrixCamera).mod(matrixProjection).mod(matrixViewPort);
        for(int i = 0; i < src.length; ++i) {
            mat.transformVertexPosition(dest[i], src[i]);
        }
    }


    // 座標変換 =========================================================
    protected void stageTransform(Chimera3DTLVertex dest[], Chimera3DLVertex src[]) {
        Chimera3DMatrix mat = matrixWorld.mod(matrixCamera).mod(matrixProjection).mod(matrixViewPort);
        for(int i = 0; i < src.length; ++i) {
            mat.transformVertexPosition(dest[i], src[i]);
        }
    }


    // 隠面消去用座標変換 ===============================================
    protected void stageCulling(Chimera3DTLVertex dest[], Chimera3DVertex src[]) {
        Chimera3DMatrix mat = matrixWorld.mod(matrixCamera);
        for(int i = 0; i < src.length; ++i) {
            mat.transformVertexPosition(dest[i], src[i]);
        }
    }


    // 隠面消去用座標変換 ===============================================
    protected void stageCulling(Chimera3DTLVertex dest[], Chimera3DLVertex src[]) {
        Chimera3DMatrix mat = matrixWorld.mod(matrixCamera);
        for(int i = 0; i < src.length; ++i) {
            mat.transformVertexPosition(dest[i], src[i]);
        }
    }


    // ライティング適用 =================================================
    protected void stageLighting(Chimera3DTLVertex dest[], Chimera3DVertex src[]) {
        Chimera3DVector direction = new Chimera3DVector();
        int diffuseR, diffuseG, diffuseB;
        int ambientR, ambientG, ambientB;
        int specularR, specularG, specularB;
        double power;

        diffuseR  = ((materialDiffuse  >> 16) & 0xff) * ((lightDiffuse  >> 16) & 0xff) >> 8;
        diffuseG  = ((materialDiffuse  >>  8) & 0xff) * ((lightDiffuse  >>  8) & 0xff) >> 8;
        diffuseB  = ((materialDiffuse  >>  0) & 0xff) * ((lightDiffuse  >>  0) & 0xff) >> 8;
        ambientR  = ((materialAmbient  >> 16) & 0xff) * ((lightAmbient  >> 16) & 0xff) >> 8;
        ambientG  = ((materialAmbient  >>  8) & 0xff) * ((lightAmbient  >>  8) & 0xff) >> 8;
        ambientB  = ((materialAmbient  >>  0) & 0xff) * ((lightAmbient  >>  0) & 0xff) >> 8;
        specularR = ((materialSpecular >> 16) & 0xff) * ((lightSpecular >> 16) & 0xff) >> 8;
        specularG = ((materialSpecular >>  8) & 0xff) * ((lightSpecular >>  8) & 0xff) >> 8;
        specularB = ((materialSpecular >>  0) & 0xff) * ((lightSpecular >>  0) & 0xff) >> 8;
        power     = materialPower;

        direction.x = -lightDirection.x;
        direction.y = -lightDirection.y;
        direction.z = -lightDirection.z;
        direction.normalize();

        double dotProduct;
        Chimera3DVector normal = new Chimera3DVector();

        for(int i = 0; i < src.length; ++i) {
            matrixWorld.transformVertexNormal(normal, src[i]);
            dotProduct = Math.max(normal.calcDotProduct(direction), 0.0);

            dest[i].diffuse = 0xff000000
                | (Math.min(Math.round((float)(diffuseR * dotProduct + ambientR)), 0xff) << 16)
                | (Math.min(Math.round((float)(diffuseG * dotProduct + ambientG)), 0xff) <<  8)
                | (Math.min(Math.round((float)(diffuseB * dotProduct + ambientB)), 0xff) <<  0);

            if(power == 0.0) {
                dotProduct = 0.0;
                dest[i].specular = 0xff000000;
            } else {
                dotProduct = Math.pow(dotProduct, power);
                dest[i].specular = 0xff000000
                    | (Math.round((float)(specularR * dotProduct)) << 16)
                    | (Math.round((float)(specularG * dotProduct)) <<  8)
                    | (Math.round((float)(specularB * dotProduct)) <<  0);
            }

        }
    }


    // 最終処理 =========================================================
    protected void stageFinalize(Chimera3DTLVertex dest[]) {
        for(int i = 0; i < dest.length; ++i) {
            dest[i].transformFinal();
        }
    }


    // 頂点描画 =========================================================
    public void drawPoint(Chimera3DLVertex vertex[]) {
        Chimera3DMatrix mat = matrixWorld.mod(matrixCamera).mod(matrixProjection).mod(matrixViewPort);
        Chimera3DVector dest = new Chimera3DVector();
        for(int i = 0; i < vertex.length; ++i) {
            mat.transformVectorPosition(dest, vertex[i].pos);
            if (dest.z >= 0.0 && dest.z < 1.0) {
                if (dest.y >= 0.0 && dest.y < height) {
                    if (dest.x >= 0.0 && dest.x < width) {
                        backBuffer.setColor(new Color(vertex[i].color));
                        backBuffer.fillRect((int)dest.x, (int)dest.y, 2, 2);
                    }
                }
            }
        }
    }


    // 線描画 ===========================================================
    public void drawLine(Chimera3DLVertex vertex[]) {
        Chimera3DMatrix mat = matrixWorld.mod(matrixCamera).mod(matrixProjection).mod(matrixViewPort);
        Chimera3DVector dest = new Chimera3DVector();
        mat.transformVectorPosition(dest, vertex[0].pos);

        for(int i = 0; i < vertex.length-1; ++i) {
            Chimera3DVector to = new Chimera3DVector();
            mat.transformVectorPosition(to, vertex[i+1].pos);
            if (dest.z >= 0.0 && dest.z < 1.0 || to.z >= 0.0 && to.z < 1.0) {
                if (dest.y >= 0.0 && dest.y < height || to.y >= 0.0 && to.y < height) {
                    if (dest.x >= 0.0 && dest.x < width || to.x >= 0.0 && to.x < width) {
                        backBuffer.setColor(new Color(vertex[i].color));
                        backBuffer.drawLine((int)dest.x, (int)dest.y, (int)to.x, (int)to.y);
                    }
                }
            }
            dest = to;
        }
    } 


    // 線描画 ===========================================================
    public void drawLine(Chimera3DVector vector[], int color) {
        Chimera3DMatrix mat = matrixWorld.mod(matrixCamera).mod(matrixProjection).mod(matrixViewPort);
        Chimera3DVector dest = new Chimera3DVector();
        mat.transformVectorPosition(dest, vector[0]);

        backBuffer.setColor(new Color(color));
        for(int i = 0; i < vector.length-1; ++i) {
            Chimera3DVector to = new Chimera3DVector();
            mat.transformVectorPosition(to, vector[i+1]);
            if (dest.z >= 0.0 && dest.z < 1.0 || to.z >= 0.0 && to.z < 1.0) {
                if (dest.y >= 0.0 && dest.y < height || to.y >= 0.0 && to.y < height) {
                    if (dest.x >= 0.0 && dest.x < width || to.x >= 0.0 && to.x < width) {
                        backBuffer.drawLine((int)dest.x, (int)dest.y, (int)to.x, (int)to.y);
                    }
                }
            }
            dest = to;
        }
    } 
           

    // インデックス付きポリゴン描画 =====================================
    public void drawIndexedPrimitive(Chimera3DVertex vertex[], short index[]) {
        Chimera3DTLVertex[] dest = new Chimera3DTLVertex[vertex.length];
        Chimera3DTLVertex[] cull = new Chimera3DTLVertex[vertex.length];
        for(int i = 0; i < vertex.length; ++i) {
            dest[i] = new Chimera3DTLVertex();
            cull[i] = new Chimera3DTLVertex();
        }

        stageCulling(cull, vertex);
        stageTransform(dest, vertex);
        stageLighting(dest, vertex);
        stageFinalize(dest);

        for(int i = 0; i < index.length; i+=3) {
            Chimera3DTriangle tri = new Chimera3DTriangle();

            // 隠面消去
            tri.vtx1 = cull[index[i+0]];
            tri.vtx2 = cull[index[i+1]];
            tri.vtx3 = cull[index[i+2]];
            if(tri.isCulling()) continue;

            // 描画ポリゴン
            tri.vtx1 = dest[index[i+0]];
            tri.vtx2 = dest[index[i+1]];
            tri.vtx3 = dest[index[i+2]];
            tri.color = 0xff000000 |
                (((
                      Math.min(0xff, ((tri.vtx1.diffuse  >> 16) & 0xff) +
                               ((tri.vtx1.specular >> 16) & 0xff)) +
                      Math.min(0xff, ((tri.vtx2.diffuse  >> 16) & 0xff) +
                               ((tri.vtx2.specular >> 16) & 0xff)) +
                      Math.min(0xff, ((tri.vtx3.diffuse  >> 16) & 0xff) +
                               ((tri.vtx3.specular >> 16) & 0xff))
                      ) / 3) << 16) |
                (((
                      Math.min(0xff, ((tri.vtx1.diffuse  >>  8) & 0xff) +
                               ((tri.vtx1.specular >>  8) & 0xff)) +
                      Math.min(0xff, ((tri.vtx2.diffuse  >>  8) & 0xff) +
                               ((tri.vtx2.specular >>  8) & 0xff)) +
                      Math.min(0xff, ((tri.vtx3.diffuse  >>  8) & 0xff) +
                               ((tri.vtx3.specular >>  8) & 0xff))
                      ) / 3) <<  8) |
                (((
                      Math.min(0xff, ((tri.vtx1.diffuse  >>  0) & 0xff) +
                               ((tri.vtx1.specular >>  0) & 0xff)) +
                      Math.min(0xff, ((tri.vtx2.diffuse  >>  0) & 0xff) +
                               ((tri.vtx2.specular >>  0) & 0xff)) +
                      Math.min(0xff, ((tri.vtx3.diffuse  >>  0) & 0xff) +
                               ((tri.vtx3.specular >>  0) & 0xff))
                      ) / 3) <<  0);

            double z = (tri.vtx1.pos.z + tri.vtx2.pos.z + tri.vtx3.pos.z) / 3.0;

            if(z >= 0.0 && z < 1.0) {
                zSort[Math.round((float)(z*zSortSize))].addElement(tri);
            }
        }
    }


    // インデックス付きポリゴン描画 =====================================
    public void drawIndexedPrimitive(Chimera3DLVertex vertex[], short index[]) {
        Chimera3DTLVertex[] dest = new Chimera3DTLVertex[vertex.length];
        Chimera3DTLVertex[] cull = new Chimera3DTLVertex[vertex.length];
        for(int i = 0; i < vertex.length; ++i) {
            dest[i] = new Chimera3DTLVertex();
            cull[i] = new Chimera3DTLVertex();
        }

        stageCulling(cull, vertex);
        stageTransform(dest, vertex);
        for(int i = 0; i < vertex.length; ++i) {
            dest[i].diffuse  = vertex[i].color;
            dest[i].specular = 0x000000;
        }
        stageFinalize(dest);

        for(int i = 0; i < index.length; i+=3) {
            Chimera3DTriangle tri = new Chimera3DTriangle();

            // 隠面消去
            tri.vtx1 = cull[index[i+0]];
            tri.vtx2 = cull[index[i+1]];
            tri.vtx3 = cull[index[i+2]];
            if(tri.isCulling()) continue;

            // 描画ポリゴン
            tri.vtx1 = dest[index[i+0]];
            tri.vtx2 = dest[index[i+1]];
            tri.vtx3 = dest[index[i+2]];
            tri.color = 0xff000000 |
                (((
                      ((tri.vtx1.diffuse  >> 16) & 0xff) +
                      ((tri.vtx2.diffuse  >> 16) & 0xff) +
                      ((tri.vtx3.diffuse  >> 16) & 0xff)
                      ) / 3) << 16) |
                (((
                      ((tri.vtx1.diffuse  >>  8) & 0xff) +
                      ((tri.vtx2.diffuse  >>  8) & 0xff) +
                      ((tri.vtx3.diffuse  >>  8) & 0xff)
                      ) / 3) <<  8) |
                (((
                      ((tri.vtx1.diffuse  >>  0) & 0xff) +
                      ((tri.vtx2.diffuse  >>  0) & 0xff) +
                      ((tri.vtx3.diffuse  >>  0) & 0xff)
                      ) / 3) <<  0);

            double z = (tri.vtx1.pos.z + tri.vtx2.pos.z + tri.vtx3.pos.z) / 3.0;

            if(z >= 0.0 && z < 1.0) {
                zSort[Math.round((float)(z*zSortSize))].addElement(tri);
            }
        }

    }
}
