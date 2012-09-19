package st.chimera.graphics.ch2d;

class ChimeraBitBltEffect {

	public static int colorBlend(int Dest, int Src, int Alpha) {
		int RDest, GDest, BDest, RSrc, GSrc, BSrc;
		int RAlpha, GAlpha, BAlpha;

		RAlpha = (Alpha >> 16) & 0xFF;
		GAlpha = (Alpha >> 8 ) & 0xFF;
		BAlpha = (Alpha >> 0 ) & 0xFF;

		RDest = Dest & 0x00FF0000; RSrc  = Src  & 0x00FF0000;
		RDest = (RSrc - RDest) * RAlpha + (RDest << 8);
		GDest = Dest & 0x0000FF00; GSrc  = Src  & 0x0000FF00;
		GDest = (GSrc - GDest) * GAlpha + (GDest << 8);
		BDest = Dest & 0x000000FF; BSrc  = Src  & 0x000000FF;
		BDest = (BSrc - BDest) * BAlpha + (BDest << 8);

		return 0xFF000000 | ((RDest >> 8) & 0x00FF0000)
			| ((GDest >> 8) & 0x0000FF00) | ((BDest >> 8) & 0x000000FF);
	}

	public static int colorAdd(int Dest, int Src, int Alpha) {
		int RDest, GDest, BDest, RSrc, GSrc, BSrc;
		int RAlpha, GAlpha, BAlpha;

		RAlpha = (Alpha >> 16) & 0xFF;
		GAlpha = (Alpha >> 8 ) & 0xFF;
		BAlpha = (Alpha >> 0 ) & 0xFF;

		RDest = Math.min(0xFF, ((Dest >> 16)&0xFF) + ((((Src >> 16)&0xFF)*RAlpha) >> 8));
		GDest = Math.min(0xFF, ((Dest >> 8 )&0xFF) + ((((Src >> 8 )&0xFF)*GAlpha) >> 8));
		BDest = Math.min(0xFF, ((Dest >> 0 )&0xFF) + ((((Src      )&0xFF)*BAlpha) >> 8));

		return 0xFF000000 | (RDest << 16) | (GDest << 8) | BDest;
	}

	public static int colorSub(int Dest, int Src, int Alpha) {
		int RDest, GDest, BDest, RSrc, GSrc, BSrc;
		int RAlpha, GAlpha, BAlpha;

		RAlpha = (Alpha >> 16) & 0xFF;
		GAlpha = (Alpha >> 8 ) & 0xFF;
		BAlpha = (Alpha >> 0 ) & 0xFF;

		RDest = Math.min(0x00, ((Dest >> 16)&0xFF) - ((((Src >> 16)&0xFF)*RAlpha) >> 8));
		GDest = Math.min(0x00, ((Dest >> 8 )&0xFF) - ((((Src >> 8 )&0xFF)*GAlpha) >> 8));
		BDest = Math.min(0x00, ((Dest >> 0 )&0xFF) - ((((Src      )&0xFF)*BAlpha) >> 8));

		return 0xFF000000 | (RDest << 16) | (GDest << 8) | BDest;
	}

	public static int colorAnitAlias(int C11, int C12, int C21, int C22, int AX, int AY) {
		return colorBlend(colorBlend(C11, C12, AX), colorBlend(C21, C22, AX), AY);
	}

	public static void blt(int Dest[], int Src[], int PtrDest, int PtrSrc,
		int PitchDest, int PitchSrc, int Width, int Height) {

		for(int y = 0; y < Height; ++y) {
			System.arraycopy(Src, PtrSrc, Dest, PtrDest, Width);
			PtrDest += PitchDest;
			PtrSrc	+= PitchSrc;
		}
	}

	public static void bltRotate(int Dest[], int Src[], int PitchDest, int PitchSrc,
		int Width, int Height, int X, int Y, int ScreenWidth, int ScreenHeight,
		int Rad) {

		int TPtrDest;
		int TSin, TCos;
		int CX, CY, SX, SY, SW, SH, FX, FY;

		TCos = (int)Math.round(Math.cos(Rad / 65536.0 * 2 * Math.PI) * 65536.0);
		TSin = (int)Math.round(Math.sin(Rad / 65536.0 * 2 * Math.PI) * 65536.0);

		// 転送先での転送範囲
		CX = (Math.abs(TCos * Width / 2) + Math.abs(TSin * Height / 2)) >> 16;
		CY = (Math.abs(TSin * Width / 2) + Math.abs(TCos * Height / 2)) >> 16;

		SX = ((Width  / 2) << 16) - CX * TCos + CY * TSin;
		SY = ((Height / 2) << 16) - CX * TSin - CY * TCos;

		FX = X - CX;
		FY = Y - CY;

		// クリッピング処理
		if(FX < 0) { SX -= TCos * FX; SY -= TSin * FX; FX = 0; }
		if(FY < 0) { SX += TSin * FY; SY -= TCos * FY; FY = 0; }

		TPtrDest = FX + PitchDest * FY;

		// クリッピング処理
		SW = Math.min(X + CX, ScreenWidth ) - FX;
		SH = Math.min(Y + CY, ScreenHeight) - FY;

		if(SW <= 0 || SH <= 0) return;

		// 転送
		for(int DY = 0; DY < SH; ++DY) {
			int DSX = SX, DSY = SY;
			for(int DX = 0; DX < SW; ++DX) {
				int PX = DSX, PY = DSY;

				PX >>= 16; PY >>= 16;
				if(!(PX < 0 || PY < 0 || PX >= Width || PY >= Height))
					Dest[TPtrDest + DX] = Src[PY * PitchSrc + PX];

				DSX += TCos;
				DSY += TSin;
			}
			SX -= TSin;
			SY += TCos;
			TPtrDest += PitchDest;
		}
	}

	public static void bltRotateAA(int Dest[], int Src[], int PitchDest, int PitchSrc,
		int Width, int Height, int X, int Y, int ScreenWidth, int ScreenHeight,
		int Rad) {

		int TPtrDest;
		int TSin, TCos;
		int CX, CY, SX, SY, SW, SH, FX, FY;
		int DSX, DSY, PX, PY, DPX, DPY;

		TCos = (int)Math.round(Math.cos(Rad / 65536.0 * 2 * Math.PI) * 65536.0);
		TSin = (int)Math.round(Math.sin(Rad / 65536.0 * 2 * Math.PI) * 65536.0);

		// 転送先での転送範囲
		CX = (Math.abs(TCos * Width / 2) + Math.abs(TSin * Height / 2)) >> 16;
		CY = (Math.abs(TSin * Width / 2) + Math.abs(TCos * Height / 2)) >> 16;

		SX = ((Width  / 2) << 16) - CX * TCos + CY * TSin;
		SY = ((Height / 2) << 16) - CX * TSin - CY * TCos;

		FX = X - CX;
		FY = Y - CY;

		// クリッピング処理
		if(FX < 0) { SX -= TCos * FX; SY -= TSin * FX; FX = 0; }
		if(FY < 0) { SX += TSin * FY; SY -= TCos * FY; FY = 0; }

		TPtrDest = FX + PitchDest * FY;

		// クリッピング処理
		SW = Math.min(X + CX, ScreenWidth ) - FX;
		SH = Math.min(Y + CY, ScreenHeight) - FY;

		if(SW <= 0 || SH <= 0) return;

		// 転送
		for(int DY = 0; DY < SH; ++DY) {
			DSX = SX;
			DSY = SY;
			for(int DX = 0; DX < SW; ++DX) {
				PX = DSX >> 16;
				PY = DSY >> 16;
				DPX = (DSX & 0x0000FFFF) >> 8;
				DPY = (DSY & 0x0000FFFF) >> 8;

				if(!(PX < 0 || PY < 0 || PX+1 >= Width || PY+1 >= Height))
				{
					Dest[TPtrDest + DX] = colorAnitAlias(
						Src[PY     * PitchSrc + PX  ],
						Src[PY     * PitchSrc + PX+1],
						Src[(PY+1) * PitchSrc + PX  ],
						Src[(PY+1) * PitchSrc + PX+1],
						(DPX << 16) | (DPX << 8) | (DPX << 0),
						(DPY << 16) | (DPY << 8) | (DPY << 0));
				}

				DSX += TCos;
				DSY += TSin;
			}
			SX -= TSin;
			SY += TCos;
			TPtrDest += PitchDest;
		}
	}

	public static void bltRotateAAAdd(int Dest[], int Src[], int PitchDest, int PitchSrc,
		int Width, int Height, int X, int Y, int ScreenWidth, int ScreenHeight,
		int Rad) {

		int TPtrDest;
		int TSin, TCos;
		int CX, CY, SX, SY, SW, SH, FX, FY;
		int DSX, DSY, PX, PY, DPX, DPY;
		int Mask1 = 0x00FEFEFE;
		int Mask2 = 0x01010100;
		int Temp1, Temp2;

		int PixelDest, PixelSrc, PixelAlpha;

		TCos = (int)Math.round(Math.cos(Rad / 65536.0 * 2 * Math.PI) * 65536.0);
		TSin = (int)Math.round(Math.sin(Rad / 65536.0 * 2 * Math.PI) * 65536.0);

		// 転送先での転送範囲
		CX = (Math.abs(TCos * Width / 2) + Math.abs(TSin * Height / 2)) >> 16;
		CY = (Math.abs(TSin * Width / 2) + Math.abs(TCos * Height / 2)) >> 16;

		SX = ((Width  / 2) << 16) - CX * TCos + CY * TSin;
		SY = ((Height / 2) << 16) - CX * TSin - CY * TCos;

		FX = X - CX;
		FY = Y - CY;

		// クリッピング処理
		if(FX < 0) { SX -= TCos * FX; SY -= TSin * FX; FX = 0; }
		if(FY < 0) { SX += TSin * FY; SY -= TCos * FY; FY = 0; }

		TPtrDest = FX + PitchDest * FY;

		// クリッピング処理
		SW = Math.min(X + CX, ScreenWidth ) - FX;
		SH = Math.min(Y + CY, ScreenHeight) - FY;

		if(SW <= 0 || SH <= 0) return;

		// 転送
		for(int DY = 0; DY < SH; ++DY) {
			DSX = SX;
			DSY = SY;
			for(int DX = 0; DX < SW; ++DX) {
				PX = DSX >> 16;
				PY = DSY >> 16;
				DPX = (DSX & 0x0000FFFF) >> 8;
				DPY = (DSY & 0x0000FFFF) >> 8;

				if(!(PX < 0 || PY < 0 || PX+1 >= Width || PY+1 >= Height))
				{
					PixelDest = colorAnitAlias(
						Src[PY     * PitchSrc + PX  ],
						Src[PY     * PitchSrc + PX+1],
						Src[(PY+1) * PitchSrc + PX  ],
						Src[(PY+1) * PitchSrc + PX+1],
						(DPX << 16) | (DPX << 8) | (DPX << 0),
						(DPY << 16) | (DPY << 8) | (DPY << 0));


					Temp1 = (Dest[TPtrDest + DX] & Mask1) + (PixelDest & Mask1);
					Temp2 = Temp1 & Mask2;
					Temp2 -= Temp2 >> 8;
					Dest[TPtrDest + DX] = Temp1 | Temp2 | 0xFF000000;
				}

				DSX += TCos;
				DSY += TSin;
			}
			SX -= TSin;
			SY += TCos;
			TPtrDest += PitchDest;
		}
	}

	public static void bltAdd(int Dest[], int Src[], int PtrDest, int PtrSrc,
		int PitchDest, int PitchSrc, int Width, int Height) {

		int Mask1 = 0x00FEFEFE;
		int Mask2 = 0x01010100;
		int Temp1, Temp2;

		for(int y = 0; y < Height; ++y) {
			for(int x = 0; x < Width; ++x) {
				Temp1 = (Dest[PtrDest + x] & Mask1) + (Src[PtrSrc + x] & Mask1);
				Temp2 = Temp1 & Mask2;
				Temp2 -= Temp2 >> 8;
				Dest[PtrDest + x] = Temp1 | Temp2 | 0xFF000000;
			}
			PtrDest += PitchDest;
			PtrSrc	+= PitchSrc;
		}
	}

	public static void bltSub(int Dest[], int Src[], int PtrDest, int PtrSrc,
		int PitchDest, int PitchSrc, int Width, int Height) {

		int Mask1 = 0x00FEFEFE;
		int Mask2 = 0x01010100;
		int Temp1, Temp2;

		for(int y = 0; y < Height; ++y) {
			for(int x = 0; x < Width; ++x) {
				Temp1 = Dest[PtrDest + x];
				Temp1 = ((~Temp1) & Mask1) + (Src[PtrSrc + x] & Mask1);
				Temp2 = Temp1 & Mask2;
				Temp2 -= Temp2 >> 8;
				Dest[PtrDest + x] = (~(Temp1 | Temp2)) | 0xFF000000;
			}
			PtrDest += PitchDest;
			PtrSrc	+= PitchSrc;
		}
	}

	public static void bltTrans(int Dest[], int Src[], int PtrDest, int PtrSrc,
		int PitchDest, int PitchSrc, int Width, int Height) {
		int Mask =	0x007F7F7F;

		for(int y = 0; y < Height; ++y) {
			for(int x = 0; x < Width; ++x) {
				Dest[PtrDest + x] = (((Dest[PtrDest + x] >> 1) & Mask)
					+ ((Src[PtrSrc + x] >> 1) & Mask)) | 0xFF000000;
			}
			PtrDest += PitchDest;
			PtrSrc	+= PitchSrc;
		}
	}

	public static void bltAddAlpha(int Dest[], int Src[], int PtrDest, int PtrSrc,
		int PitchDest, int PitchSrc, int Width, int Height, int Alpha) {
		for(int y = 0; y < Height; ++y) {
			for(int x = 0; x < Width; ++x) {
				if(Src[PtrSrc + x] != 0) {
					Dest[PtrDest + x] = colorAdd(Dest[PtrDest + x], Src[PtrSrc + x], Alpha);
				}
			}
			PtrDest += PitchDest;
			PtrSrc	+= PitchSrc;
		}
	}

	public static void bltSubAlpha(int Dest[], int Src[], int PtrDest, int PtrSrc,
		int PitchDest, int PitchSrc, int Width, int Height, int Alpha) {
		int PixelSrc;
		int RDest, GDest, BDest;
		int RSrc, GSrc, BSrc;
		int RAlpha, GAlpha, BAlpha;

		RAlpha = (Alpha >> 16) & 0xFF;
		GAlpha = (Alpha >> 8 ) & 0xFF;
		BAlpha = (Alpha >> 0 ) & 0xFF;
		for(int y = 0; y < Height; ++y) {
			for(int x = 0; x < Width; ++x) {
				if(Src[PtrSrc + x] != 0) {
					Dest[PtrDest + x] = colorSub(Dest[PtrDest + x], Src[PtrSrc + x], Alpha);
				}
			}
			PtrDest += PitchDest;
			PtrSrc	+= PitchSrc;
		}
	}

	public static void bltTransAlpha(int Dest[], int Src[], int PtrDest, int PtrSrc,
		int PitchDest, int PitchSrc, int Width, int Height, int Alpha) {

		for(int y = 0; y < Height; ++y) {
			for(int x = 0; x < Width; ++x) {
				if(Src[PtrSrc + x] != 0) {
					Dest[PtrDest + x] = colorBlend(Dest[PtrDest + x], Src[PtrSrc + x], Alpha);
				}
			}
			PtrDest += PitchDest;
			PtrSrc	+= PitchSrc;
		}
	}
}
