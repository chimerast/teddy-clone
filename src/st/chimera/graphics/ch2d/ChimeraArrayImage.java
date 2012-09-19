package st.chimera.graphics.ch2d;

import java.awt.*;
import java.awt.image.*;

public class ChimeraArrayImage {

	public final static int DRAW_STD   = 0;
	public final static int DRAW_ADD   = 1;
	public final static int DRAW_SUB   = 2;
	public final static int DRAW_TRANS = 3;

	protected int Width;
	protected int Height;
	protected int Data[];

	public ChimeraArrayImage(int ImageWidth, int ImageHeight) {
		Width  = ImageWidth;
		Height = ImageHeight;
		Data   = new int[Width * Height];
	}

	public ChimeraArrayImage(Image Img, ImageObserver Observer) {
		PixelGrabber pg;

		Width  = Img.getWidth(Observer);
		Height = Img.getHeight(Observer);
		Data   = new int[Width * Height];
		pg     = new PixelGrabber(Img, 0, 0, Width, Height, Data, 0, Width);
		try {
			pg.grabPixels();
		} catch(InterruptedException E) {
		}
	}

	public MemoryImageSource ReturnImage() {
		return new MemoryImageSource(Width, Height, Data, 0, Width);
	}

	public void PutColor(int X, int Y, int Color) {
	    Data[Y*Width + X] = Color;
	}

	public void PutColorAA(int X, int Y, int Color) {


	    Data[Y*Width + X] = Color;
	}

	public void Draw(int X, int Y, ChimeraArrayImage Src, int DrawMode) {
		int X1, X2, Y1, Y2;
		int PtrDest, PtrSrc;

		if(X + Src.Width <= 0 || X >= Width
			|| Y + Src.Height <= 0 || Y >= Height) {
			return;
		}

		X1 = (0 > -X) ? 0 : -X;
		Y1 = (0 > -Y) ? 0 : -Y;
		X2 = (Src.Width  < Width  - X) ? Src.Width  : Width  - X;
		Y2 = (Src.Height < Height - Y) ? Src.Height : Height - Y;

		X  = (0 > X) ? 0 : X;
		Y  = (0 > Y) ? 0 : Y;

		PtrDest = Y  * Width     + X;
		PtrSrc  = Y1 * Src.Width + X1;

		switch(DrawMode) {
			case DRAW_STD:
				ChimeraBitBltEffect.blt(Data, Src.Data, PtrDest, PtrSrc,
					Width, Src.Width, X2-X1, Y2-Y1);
				break;
			case DRAW_ADD:
				ChimeraBitBltEffect.bltAdd(Data, Src.Data, PtrDest, PtrSrc,
					Width, Src.Width, X2-X1, Y2-Y1);
				break;
			case DRAW_SUB:
				ChimeraBitBltEffect.bltSub(Data, Src.Data, PtrDest, PtrSrc,
					Width, Src.Width, X2-X1, Y2-Y1);
				break;
			case DRAW_TRANS:
				ChimeraBitBltEffect.bltTrans(Data, Src.Data, PtrDest, PtrSrc,
					Width, Src.Width, X2-X1, Y2-Y1);
				break;
		}
	}

	public void DrawAlpha(int X, int Y, ChimeraArrayImage Src, int DrawMode, int Alpha) {
		int X1, X2, Y1, Y2;
		int PtrDest, PtrSrc;

		if(X + Src.Width <= 0 || X >= Width
			|| Y + Src.Height <= 0 || Y >= Height) {
			return;
		}

		X1 = (0 > -X) ? 0 : -X;
		Y1 = (0 > -Y) ? 0 : -Y;
		X2 = (Src.Width  < Width  - X) ? Src.Width  : Width  - X;
		Y2 = (Src.Height < Height - Y) ? Src.Height : Height - Y;

		X  = (0 > X) ? 0 : X;
		Y  = (0 > Y) ? 0 : Y;

		PtrDest = Y  * Width     + X;
		PtrSrc  = Y1 * Src.Width + X1;

		switch(DrawMode) {
			case DRAW_ADD:
				ChimeraBitBltEffect.bltAddAlpha(Data, Src.Data, PtrDest, PtrSrc,
					Width, Src.Width, X2-X1, Y2-Y1, Alpha);
				break;
			case DRAW_SUB:
				ChimeraBitBltEffect.bltSubAlpha(Data, Src.Data, PtrDest, PtrSrc,
					Width, Src.Width, X2-X1, Y2-Y1, Alpha);
				break;
			case DRAW_TRANS:
				ChimeraBitBltEffect.bltTransAlpha(Data, Src.Data, PtrDest, PtrSrc,
					Width, Src.Width, X2-X1, Y2-Y1, Alpha);
				break;
		}
	}

	public void DrawRotate(int X, int Y, ChimeraArrayImage Src, int Rad) {
		ChimeraBitBltEffect.bltRotate(Data, Src.Data, Width, Src.Width,
			Src.Width, Src.Height, X, Y, Width, Height, Rad);
	}

	public void DrawRotateAA(int X, int Y, ChimeraArrayImage Src, int Rad) {
		ChimeraBitBltEffect.bltRotateAA(Data, Src.Data, Width, Src.Width,
			Src.Width, Src.Height, X, Y, Width, Height, Rad);
	}

	public void DrawRotateAAAdd(int X, int Y, ChimeraArrayImage Src, int Rad) {
		ChimeraBitBltEffect.bltRotateAAAdd(Data, Src.Data, Width, Src.Width,
			Src.Width, Src.Height, X, Y, Width, Height, Rad);
	}

	public void AddColor(int Color) {
		int PtrData, PixelDest;
		int RDest, BDest, GDest;
		int RSrc, BSrc, GSrc;

		PtrData = 0;
		RSrc = (Color >> 16) & 0xFF;
		GSrc = (Color >> 8 ) & 0xFF;
		BSrc = (Color >> 0 ) & 0xFF;
		for(int y = 0; y < Height; ++y) {
			for(int x = 0; x < Width; ++x) {
				PixelDest = Data[PtrData + x];
				RDest = Math.min(0xFF, RSrc + ((PixelDest >> 16) & 0xFF));
				GDest = Math.min(0xFF, GSrc + ((PixelDest >> 8 ) & 0xFF));
				BDest = Math.min(0xFF, BSrc + ((PixelDest      ) & 0xFF));
				Data[PtrData + x] = 0xFF000000 | (RDest<<16) | (GDest<<8) | BDest;
			}
			PtrData += Width;
		}
	}

	public void InvColor() {
		for(int i = 0; i < Width * Height; ++i) {
			Data[i] = Data[i] ^ 0x00FFFFFF;
		}
	}
}
