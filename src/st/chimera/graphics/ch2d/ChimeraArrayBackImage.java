package st.chimera.graphics.ch2d;

import java.awt.*;
import java.awt.image.*;

public class ChimeraArrayBackImage extends ChimeraArrayImage {

    public ChimeraArrayBackImage() {
		super(256, 256);
    }

    public ChimeraArrayBackImage(Image Img, ImageObserver Observer) {
		super(Img, Observer);
    }

	public void FeedBack(int Param) {
		int ModColor, ModColor1, ModColor2;
		int PixelDest, Alpha;
		int RD, GD, BD, RS, GS, BS;

		int Temp[] = new int[256*256];

		for(int i = 0; i < 256*256; ++i)
			Temp[i] = Data[i];

		ModColor1  = Color.HSBtoRGB(((float)(Param&4095)/4096.0f), 0.8f, 0.8f);
		ModColor2  = Color.HSBtoRGB(((float)((Param+1300)&4095)/4096.0f), 0.8f, 0.8f);

		for(int y = 1; y < 256; ++y) {
			for(int x = 1; x < 256; ++x) {
				PixelDest = Temp[(y-1)*256 + x-1];
				Alpha = (x + y) / 2;

				RD = ModColor1 & 0xFF0000; RS = ModColor2 & 0xFF0000;
				RD = (RS - RD) * Alpha + (RD << 8);
				GD = ModColor1 & 0x00FF00; GS = ModColor2 & 0x00FF00;
				GD = (GS - GD) * Alpha + (GD << 8);
				BD = ModColor1 & 0x0000FF; BS = ModColor2 & 0x0000FF;
				BD = (BS - BD) * Alpha + (BD << 8);
				RD >>>= 24; GD >>>= 16; BD >>>= 8;

				Data[y*Width + x] =
					  ((((PixelDest & 0xFF0000) * RD) >> 8) & 0xFF0000)
					| ((((PixelDest & 0x00FF00) * GD) >> 8) & 0x00FF00)
					| ((((PixelDest & 0x0000FF) * BD) >> 8) & 0x0000FF)
					| 0xFF000000;
			}
		}
	}

	public void FeedBackAA(int Param) {
		int SX, SY;
		int PX, PY, DPX, DPY;
		int Alpha, RD, GD, BD, RS, GS, BS;
		int PixelDest, PixelSrc, PixelAlpha;
		int ModColor, ModColor1, ModColor2;

		int Temp[] = new int[256*256];

		for(int i = 0; i < 256*256; ++i)
			Temp[i] = Data[i];

		ModColor1  = Color.HSBtoRGB(((float)(Param&4095)/4096.0f), 0.2f, 1.0f);
		ModColor2  = Color.HSBtoRGB(((float)((Param+1300)&4095)/4096.0f), 0.2f, 1.0f);

		for(int y = 0; y < Height; ++y) {
			for(int x = 0; x < Width; ++x) {
				SX = ((x - 128) << 16) * 127 / 128 + (128 << 16);
				SY = ((y - 128) << 16) * 127 / 128 + (128 << 16);

				PX  = SX >> 16;
				PY  = SY >> 16;
				DPX = (SX & 0x0000FFFF) >> 8;
				DPY = (SY & 0x0000FFFF) >> 8;

				if(PX < 0 || PY < 0 || PX >= 255 || PY >= 255) continue;

				PixelSrc = Temp[PY    *256 + PX  ];
				PixelAlpha = ((0x100-DPX) * (0x100-DPY)) >> 8;
				PixelDest =
					  ((((PixelSrc & 0x0000FF) * PixelAlpha) >> 8) & 0x0000FF)
					| ((((PixelSrc & 0x00FF00) * PixelAlpha) >> 8) & 0x00FF00)
					| ((((PixelSrc & 0xFF0000) * PixelAlpha) >> 8) & 0xFF0000);

				PixelSrc = Temp[PY    *256 + PX+1];
				PixelAlpha = ((DPX      ) * (0x100-DPY)) >> 8;
				PixelDest +=
					  ((((PixelSrc & 0x0000FF) * PixelAlpha) >> 8) & 0x0000FF)
					| ((((PixelSrc & 0x00FF00) * PixelAlpha) >> 8) & 0x00FF00)
					| ((((PixelSrc & 0xFF0000) * PixelAlpha) >> 8) & 0xFF0000);

				PixelSrc = Temp[(PY+1)*256 + PX  ];
				PixelAlpha = ((0x100-DPX) * (DPY      )) >> 8;
				PixelDest +=
					  ((((PixelSrc & 0x0000FF) * PixelAlpha) >> 8) & 0x0000FF)
					| ((((PixelSrc & 0x00FF00) * PixelAlpha) >> 8) & 0x00FF00)
					| ((((PixelSrc & 0xFF0000) * PixelAlpha) >> 8) & 0xFF0000);

				PixelSrc = Temp[(PY+1)*256 + PX+1];
				PixelAlpha = ((DPX      ) * (DPY      )) >> 8;
				PixelDest +=
					  ((((PixelSrc & 0x0000FF) * PixelAlpha) >> 8) & 0x0000FF)
					| ((((PixelSrc & 0x00FF00) * PixelAlpha) >> 8) & 0x00FF00)
					| ((((PixelSrc & 0xFF0000) * PixelAlpha) >> 8) & 0xFF0000);

				Alpha = (x + y) / 2;

				RD = ModColor1 & 0xFF0000; RS = ModColor2 & 0xFF0000;
				RD = (RS - RD) * Alpha + (RD << 8);
				GD = ModColor1 & 0x00FF00; GS = ModColor2 & 0x00FF00;
				GD = (GS - GD) * Alpha + (GD << 8);
				BD = ModColor1 & 0x0000FF; BS = ModColor2 & 0x0000FF;
				BD = (BS - BD) * Alpha + (BD << 8);
				RD >>>= 24; GD >>>= 16; BD >>>= 8;

				Data[y*Width + x] =
					  ((((PixelDest & 0xFF0000) * RD) >> 8) & 0xFF0000)
					| ((((PixelDest & 0x00FF00) * GD) >> 8) & 0x00FF00)
					| ((((PixelDest & 0x0000FF) * BD) >> 8) & 0x0000FF)
					| 0xFF000000;
			}
		}
	}
}