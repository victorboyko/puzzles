package com.puzzles;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.imageio.ImageIO;

public class MakeCubePuzzle {
	
	private static final String[] COLORS = {"blue", "red", "pink", "yellow"};
	private static final String SEP = System.getProperty("line.separator");

	
	private static String toString(boolean[][] figure) {
		StringBuilder res = new StringBuilder();
		for(int i = 0; i < figure.length; i++) {
			for(int j = 0; j < figure[i].length; j++) {
				res.append(figure[i][j] ? "▓" : "░");
			}
			res.append(SEP);
		}
		return res.toString();
	}
	
	private static String emptyCubeStr; 
	static {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < 5; i++) {
			sb.append("     ").append(SEP);
		}
		emptyCubeStr = sb.toString();
	}
	
	private static List<Set<boolean[][]>> readInput() throws IOException {
		List<Set<boolean[][]>> inputSets = new LinkedList<Set<boolean[][]>>();
		File file = new File("src/com/puzzles/assets/cube_puzzle_input.jpg");
		System.out.println(String.format("reading %s", file.getAbsoluteFile()));
		BufferedImage image = ImageIO.read(file);
		for(int i = 0; i < 2; i++) {
			for(int j = 0; j < 2; j++) {
				Set<boolean[][]> set = new HashSet<boolean[][]>();
				inputSets.add(set);
				for(int i1 = 0; i1 < 2; i1++) {
					for(int j1 = 0; j1 < 3; j1++) {
						boolean[][] figure =  new boolean[5][5];
						set.add(figure);
						for(int i2 = 0; i2 < 5; i2++) {
							for(int j2 = 0; j2 < 5; j2++) {
								int y = i * 206 + i1 * 96 + i2 * 16 + 4;
								int x = j * 301 + j1 * 96 + j2 * 16 + 4;
//								System.out.println(String.format(" (%s-%s-%s, %s-%s-%s) <- (%s, %s)", 
//										j2, j1, j, i2, i1, i, x, y));
								Color c = new Color( image.getRGB(x, y));
								boolean white = c.getRed() > 180 && c.getBlue() > 180 && c.getGreen() > 180;
								figure[i2][j2] = !white;
//								image.setRGB(x, y, Color.MAGENTA.getRGB());
							}
						}
					}
				}
			}
		}
//		ImageIO.write(((RenderedImage)image), "png", new File("src/com/puzzles/assets/out.png"));
		return inputSets;
	}
	
	private static String makeColumns(String src, int rowNum) {
		StringBuilder[] rows = new StringBuilder[rowNum];
		for(int i = 0; i < rows.length;i++) {
			rows[i] = new StringBuilder();
		}
		int n = 0;
		while(src.length() > 0) {
			int sepInd = src.indexOf(SEP);
			String s = src.substring(0, sepInd);
			src = src.substring(sepInd+1);
			rows[n++ % rowNum].append(s).append("   ");
		}
		StringBuilder res = new StringBuilder();
		for(StringBuilder s : rows) {
			res.append(s.toString()).append(SEP);
		}
		return res.toString();
	}
	
	static enum Dir {
		TOP(0), LEFT(1), RIGHT(2), BOTTOM(3);
		
	    private int val;
	    private Dir(int val){
	        this.val = val;
	    }
	    
	    public static Dir valueOf(int num) {
	        for (Dir d: Dir.values()) {
	            if (d.val == num) return d;
	        }
	        return null;
	    }
	};
	
	private static class DirPair {
		Dir d1, d2;
		boolean inverse;
		public DirPair(Dir d1, Dir d2, boolean inverse) {
			this(d1, d2);
			this.inverse = inverse;
		}
		public DirPair(Dir d1, Dir d2) {
			this.d1 = d1;
			this.d2 = d2;
		}
		
		@Override
		public int hashCode() {
			return 41 + d1.hashCode() << 4 + d2.hashCode() + (this.inverse ? 0 : 65536);
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj == this) return true;
			if (!(obj instanceof DirPair)) return false;
			DirPair dp = (DirPair)obj;
			return dp.d1.equals(this.d1) && dp.d2.equals(this.d2) && dp.inverse == this.inverse;
		}
	}
	
	
	
	private static class Piece {
		private Map<DirPair, Set<Piece>> links = new HashMap<DirPair, Set<Piece>>();
		{
			for(Dir d1 : Dir.values()) {
				for(Dir d2 : Dir.values()) {
					links.put(new DirPair(d1, d2), new HashSet<Piece>());
					links.put(new DirPair(d1, d2, true), new HashSet<Piece>());
				}
			}
		}
		boolean[][] figure;
		
		public Piece(boolean[][] figure) {
			this.figure = figure;
		}
		
		public Piece clone() {
			Piece piece = new Piece(this.figure);
			for(DirPair dp : this.links.keySet()) {
				piece.links.put(dp, new HashSet<Piece>(this.links.get(dp)));
			}
			
			return piece;
		}
		
		@Override
		public String toString() {
			return MakeCubePuzzle.toString(figure);
		}
	}
	

	final static List<int[]> allCombs = new LinkedList<int[]>();
	static {
		Set<Integer> allDigs = new HashSet<Integer>();
		for(int i = 0; i < 6; i++) {
			int[] ar = new int[6];
			allDigs.add(ar[0] = i);
			allCombs.add(ar);
		}
		
		for(int i = 1; i < 6; i++) {
			for(int[] ar : new HashSet<int[]>(allCombs)) {
				Set<Integer> digs = new HashSet<Integer>();
				for(int j = 0; j < i; j++) {
					digs.add(ar[j]);
				}
				Set<Integer> restDigs = new HashSet<Integer>(allDigs);
				restDigs.removeAll(digs);
				List<Integer> restDList = new ArrayList<Integer>(restDigs);
				ar[i] = restDList.get(0);
				for(int k = 1; k < restDList.size(); k++) {
					int[] ar2 = new int[6];
					System.arraycopy(ar, 0, ar2, 0, i);
					ar2[i] = restDList.get(k);
					allCombs.add(ar2);
				}
				
			}
		}
//		if (allCombs.size() != 720 /* 6! */) {
//			System.err.println("Error - wrong amount of combinations generated");
//		}
	}

	//░░░░░
	//░░2░░    
	//░103░
	//░░4░░
	//░░5░░
	//░░░░░
	final static Map<Integer, Integer> cubeLinks = new HashMap<Integer, Integer>();
	static {
		cubeLinks.put(0*4 + Dir.LEFT.val, 	1*4 + Dir.RIGHT.val);
		cubeLinks.put(0*4 + Dir.TOP.val, 	2*4 + Dir.BOTTOM.val);
		cubeLinks.put(0*4 + Dir.RIGHT.val, 	3*4 + Dir.LEFT.val);
		cubeLinks.put(0*4 + Dir.BOTTOM.val, 4*4 + Dir.TOP.val);
		
		cubeLinks.put(4*4+Dir.LEFT.val, 	1*4 + Dir.BOTTOM.val	+ 24);
		cubeLinks.put(4*4+Dir.RIGHT.val, 	3*4 + Dir.BOTTOM.val);
		cubeLinks.put(4*4+Dir.BOTTOM.val, 	5*4 + Dir.TOP.val);
	
		cubeLinks.put(1*4+Dir.TOP.val, 		2*4 + Dir.LEFT.val);
		cubeLinks.put(3*4+Dir.TOP.val, 		2*4 + Dir.RIGHT.val		+ 24);
		
		cubeLinks.put(5*4+Dir.TOP.val, 		4*4 + Dir.BOTTOM.val);
		cubeLinks.put(5*4+Dir.BOTTOM.val, 	2*4 + Dir.TOP.val);
		cubeLinks.put(5*4+Dir.LEFT.val, 	1*4 + Dir.LEFT.val		+ 24);
		cubeLinks.put(5*4+Dir.RIGHT.val, 	3*4 + Dir.RIGHT.val		+ 24);
		
	}
	static class CornerCtx {
		Dir d11,d12,d21,d22,d31,d32;
		int i1,i2,i3;
		public CornerCtx(int i1, int i2, int i3, Dir d11, Dir d12, Dir d21, Dir d22, Dir d31, Dir d32) {
			this.i1 = i1; this.i2 = i2; this.i3 = i3;
			this.d11 = d11; this.d12 = d12; this.d21 = d21; this.d22 = d22; this.d31 = d31; this.d32 = d32;
		}
	}
	
	final static Set<CornerCtx> corners = new HashSet<CornerCtx>(); 
	static {
		corners.add(new CornerCtx(0, 2, 1, Dir.TOP, Dir.LEFT, Dir.BOTTOM, Dir.LEFT, Dir.TOP, Dir.RIGHT));
		corners.add(new CornerCtx(0, 2, 3, Dir.TOP, Dir.RIGHT, Dir.BOTTOM, Dir.RIGHT, Dir.TOP, Dir.LEFT));
		corners.add(new CornerCtx(0, 4, 1, Dir.BOTTOM, Dir.LEFT, Dir.TOP, Dir.LEFT, Dir.BOTTOM, Dir.RIGHT));
		corners.add(new CornerCtx(0, 4, 3, Dir.BOTTOM, Dir.RIGHT, Dir.TOP, Dir.RIGHT, Dir.BOTTOM, Dir.LEFT));
		corners.add(new CornerCtx(5, 2, 1, Dir.BOTTOM, Dir.LEFT, Dir.TOP, Dir.LEFT, Dir.TOP, Dir.LEFT));
		corners.add(new CornerCtx(5, 2, 3, Dir.BOTTOM, Dir.RIGHT, Dir.TOP, Dir.RIGHT, Dir.TOP, Dir.RIGHT));
		corners.add(new CornerCtx(5, 4, 1, Dir.TOP, Dir.LEFT, Dir.BOTTOM, Dir.LEFT, Dir.BOTTOM, Dir.LEFT));
		corners.add(new CornerCtx(5, 4, 3, Dir.TOP, Dir.RIGHT, Dir.BOTTOM, Dir.RIGHT, Dir.BOTTOM, Dir.RIGHT));
	}
	
	private static boolean fitsCorner(Piece p1, Piece p2, Piece p3, CornerCtx cRem) {
		class LocalCtx {
			Dir d1, d2;
			Piece p;
			public LocalCtx(Piece p, Dir d1, Dir d2) {
				this.p = p;
				this.d1 = d1;
				this.d2 = d2;
			}
		}
		int cnt = 0;
		for(LocalCtx cLoc : new LocalCtx[] {
				new LocalCtx(p1, cRem.d11, cRem.d12),
				new LocalCtx(p2, cRem.d21, cRem.d22),
				new LocalCtx(p3, cRem.d31, cRem.d32)} ) {
			switch (cLoc.d1) {
				case TOP:
					switch (cLoc.d2) {
						case LEFT:
							cnt += (cLoc.p.figure[0][0]) ? 1 : 0;
							break;
						case RIGHT:
							cnt += (cLoc.p.figure[0][cLoc.p.figure[0].length-1]) ? 1 : 0;
							break;
					}
					break;
				case BOTTOM:
					switch (cLoc.d2) {
						case LEFT:
							cnt += (cLoc.p.figure[cLoc.p.figure.length-1][0]) ? 1 : 0;						
							break;
						case RIGHT:
							cnt += (cLoc.p.figure[cLoc.p.figure.length-1][cLoc.p.figure[0].length-1]) ? 1 : 0;						
							break;
					}
					break;
			}
		}
		return cnt == 1;
		
	}
	
	private static boolean[][] flip(boolean[][] in) {
		boolean[][] res = new boolean[in.length][in[0].length];
		for(int i = 0; i < in.length; i++) {
			for(int j = 0; j < in.length; j++) {
				res[in.length-1-i][j]=in[i][j];
			}
		}
		return res;
	}
	
	private static boolean[][] rotateLeft(boolean[][] in, int cnt) {
		if (cnt == 0) return in;
		boolean[][] res = new boolean[in.length][in[0].length];
		for(int i = 0; i < in.length; i++) {
			for(int j = 0; j < in.length; j++) {
				res[in.length-1-j][i]=in[i][j];
			}
		}
		return rotateLeft(res, cnt-1);
	}
	
	private static String makeCube(Set<boolean[][]> figures) {
		List<boolean[][]> fList = new ArrayList<boolean[][]>(figures);
		
		final List<Piece> pcsVar = new ArrayList<Piece>();
		
		int[] goodCmb = null;
		for(int var = 0; goodCmb == null && var < (4096 /* 4^6 */ * 64 /*  2^6 two sides */ ); var++) {
			pcsVar.clear();
			int varLocal = var;
			for(int i = 0; i < fList.size(); i++) {
				boolean[][] figure = fList.get(i);
				boolean[][] newFigure = rotateLeft(figure, varLocal % 4);
				varLocal /= 4;
				newFigure = (varLocal % 2 == 0) ? newFigure : flip(newFigure);
				varLocal /= 2;
				pcsVar.add(new Piece(newFigure));
			}
			
			for(int i = 0; i < pcsVar.size(); i++) {
				Piece p1 = pcsVar.get(i);
				for(int j = i + 1; j < pcsVar.size(); j++) {
					Piece p2 = pcsVar.get(j);
					for(Dir d1 : Dir.values()) {
						for(Dir d2 : Dir.values()) {
							if (fits(p1, p2, d1, d2)) {
								p1.links.get(new DirPair(d1, d2)).add(p2);
								p2.links.get(new DirPair(d2, d1)).add(p1);
							}
							if (fits(p1, p2, d1, d2, true)) {
								p1.links.get(new DirPair(d1, d2, true)).add(p2);
								p2.links.get(new DirPair(d2, d1, true)).add(p1);
							}
						}
					}
				}
			}
			goodCmb = findCubeCombination(figures, pcsVar);
		}
		if (goodCmb == null) throw new IllegalStateException("No solution found, contradicts to the task requirement contract");
		
		StringBuilder res = new StringBuilder();
		
		for(Integer[] iarr : new Integer[][] {
				{null, 	2, 	null},
				{1, 	0, 	3},
				{null, 	4, 	null},
				{null,	5, 	null}}) {
			StringBuilder sb = new StringBuilder();
			for(Integer i : iarr) {
				sb.append(i == null ? emptyCubeStr : toString(pcsVar.get(goodCmb[i]).figure));
			}
			res.append(makeColumns(sb.toString(), 5)).append(SEP);
		}
		
		return res.toString();
	}
	
	private static int[] findCubeCombination(Set<boolean[][]> figures, List<Piece> pcsBase) {
		
		int[] goodCmb = null;
		for(int[] comb : allCombs) {
			boolean fits = true;
			for(Entry<Integer, Integer> e : cubeLinks.entrySet()) {
				int p1ind = (e.getKey() / 4) % 6;
				Piece p1 = pcsBase.get(comb[p1ind]);
				Dir d1 = Dir.valueOf(e.getKey() % 4);
				int p2ind = (e.getValue() / 4) % 6;
				Piece p2 = pcsBase.get(comb[p2ind]);
				Dir d2 = Dir.valueOf(e.getValue() % 4);
				boolean inverse = e.getValue() / 24 > 0;
				if (!p1.links.get(new DirPair(d1, d2, inverse)).contains(p2)) {
					fits = false;
					break;
				}
//				System.out.print(""); //for debug bp
			}
			if (!fits) continue;
			for(CornerCtx c : corners) {
				Piece p1 = pcsBase.get(comb[c.i1]);
				Piece p2 = pcsBase.get(comb[c.i2]);
				Piece p3 = pcsBase.get(comb[c.i3]);
				if (!fitsCorner(p1, p2, p3, c)) {
					fits = false;
					break;
				}
//				System.out.print(""); //for debug bp
			}
			if (!fits) continue;
			goodCmb = comb;
			break;
		}
		return goodCmb;
		

	}
	
	private static boolean fits(Piece a, Piece b, Dir da, Dir db) {
		return fits(a, b, da, db, false);
	}
	
	private static boolean fits(Piece a, Piece b, Dir da, Dir db, boolean inverse) {

		boolean[][] arrs = new boolean[2][5];

		class Context {
			Dir d; Piece p; int ind; 
			public Context(Dir d, Piece p, int ind) {
				this.d = d; this.p = p; this.ind = ind;
			}
		}
		
		for(Context ctx : new Context[] {new Context(da, a, 0), new Context(db, b, 1) }) {
		
			switch (ctx.d) {
				case TOP:
					arrs[ctx.ind] = ctx.p.figure[0];
					break;
				case BOTTOM:
					arrs[ctx.ind] = ctx.p.figure[4];
					break;
				case LEFT:
					for(int i = 0; i < 5; i++) {
						arrs[ctx.ind][i] = ctx.p.figure[i][0];
					}
					break;		
				case RIGHT:
					for(int i = 0; i < 5; i++) {
						arrs[ctx.ind][i] = ctx.p.figure[i][4];
					}
					break;	
			}
		}
		if (inverse) {
			for(int i = 0; i < arrs[1].length / 2; i++) {
				boolean buf = arrs[1][i];
				arrs[1][i] = arrs[1][arrs[1].length-1-i];
				arrs[1][arrs[1].length-1-i] =  buf;
			}
			
		}
		boolean res = true;
		for(int i = 1; i < 4; i++) {
			res &= arrs[0][i] ^ arrs[1][i];
		}
		return res;
	}
	
	
	public static void solution() throws IOException {

		List<Set<boolean[][]>> inputSets = readInput();
		
		int i;
		i = 0;
		for(Set<boolean[][]> set : inputSets) {
			System.out.println(String.format("%s figures provided:", COLORS[i++]));
			StringBuilder sBuf = new StringBuilder();
			for(boolean[][] figure : set) {
				sBuf.append(toString(figure)).append(SEP);
			}
			System.out.println(makeColumns(sBuf.toString(), 6));
			//break;
		}
		
		i = 0;
		for(Set<boolean[][]> set : inputSets) {
			System.out.println(String.format("Solution for %s figures:", COLORS[i]));
			long startTime = System.currentTimeMillis();
			System.out.println(makeCube(set));
			long endTime = System.currentTimeMillis();
			System.out.println(String.format("it took %s ms to find sulution for %s figures", endTime - startTime, COLORS[i++]));
			//break;
		}
	}
	
	public static void main(String[] args) throws IOException {
		solution();
	}
	
}
