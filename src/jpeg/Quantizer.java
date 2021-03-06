package jpeg;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;


public class Quantizer {
	 
	 static final int DefalutQualityFactor = 100;
	 
	 static final int[] Quatntize_Y = { 16,  11,  10,  16,  24,  40,  51,  61, 
         12,  12,  14,  19,  26,  58,  60,  55, 
         14,  13,  16,  24,  40,  57,  69,  56, 
         14,  17,  22,  29,  51,  87,  80,  62, 
         18,  22,  37,  56,  68, 109, 103,  77, 
         24,  35,  55,  64,  81, 104, 113,  92, 
         49,  64,  78,  87, 103, 121, 120, 101, 
         72,  92,  95,  98, 112, 100, 103,  99}; 


	static final int[] Quatntize_C = { 17, 18, 24, 47, 99, 99, 99, 99, 
	           18, 21, 26, 66, 99, 99, 99, 99, 
	           24, 26, 56, 99, 99, 99, 99, 99, 
	           47, 66, 99, 99, 99, 99, 99, 99, 
	           99, 99, 99, 99, 99, 99, 99, 99, 
	           99, 99, 99, 99, 99, 99, 99, 99, 
	           99, 99, 99, 99, 99, 99, 99, 99, 
	           99, 99, 99, 99, 99, 99, 99, 99}; 


	
	private int qualityFactor = Quantizer.DefalutQualityFactor;
	private int scaledQF;
	
	public Quantizer(int qualityFactor) { 
		this.qualityFactor = qualityFactor; 
		this.scaledQF = scaledQF();
	}
	
	private int scaledQF() { 
		if (qualityFactor < 50) {		
			return 5000/qualityFactor;
		}  		
		return (200 - 2 * qualityFactor); 
	} 
	
	public int[] getQuantizeY() { 		 
		int [] scaledQF_Y = new int [Quatntize_Y.length];		 
		 for (int i = 0; i < Quatntize_Y.length; i++) {
			 scaledQF_Y[i] = clamp((int) ((Quatntize_Y [i] * scaledQF+ 50) / 100), 1, 255); 
		 }		 
		 return scaledQF_Y;
	} 
		
		
	public int[] getQuantizeC() { 		
		 int [] scaledQF_C = new int [Quatntize_C.length];		 
		 for (int i = 0; i < Quatntize_C.length; i++) {
			 scaledQF_C[i] = clamp((int) ((Quatntize_C [i] * scaledQF+ 50) / 100), 1, 255); 
		 }	 
		 return scaledQF_C;
	} 
	
	private static int clamp(int val, int min, int max) { 
		 return Math.min(max, Math.max(min, val)); 
	} 
	
		
	public Block quantizeDCTBlock(DCT dct, int component) { 
		int[] Q = null;
		if (component == YuvImage.Y_COMP) {
			Q = getQuantizeY();
		} else {
			Q = getQuantizeC();	
		}
		double[][] dctData = dct.getData(); 
		
		int[][] QBlock = new int[Block.SIZE][Block.SIZE]; 
		  
		for (int i = 0; i < Block.SIZE; i++) { 
			  for (int j = 0; j < Block.SIZE; j++) { 
				   int index = i * Block.SIZE + j; 
				   QBlock[i][j] = (int) (dctData[i][j] / Q[index]); 
			  } 
		}   
		return new Block(QBlock, component); 
	}
	  public static void main(String[] args) {
			
			File f = null;
		    //read image
		    try{
		      f = new File("/Users/apple/Desktop/hihi.png"); //image file path
		      Image image = ImageIO.read(f);
		      BufferedImage buffered = (BufferedImage) image;
		    //  System.out.println("Reading complete.");
		      System.out.println("old IMAGE H" + image.getHeight(null));
		      System.out.println("old IMAGE W" + image.getWidth(null));
		      SizeTrimer st = new SizeTrimer();
		      
		      int SamplingRatio = 0;
		      
		      image = st.resizeImage(image, SamplingRatio);
		      System.out.println("new IMAGE H" + image.getHeight(null));
		      System.out.println("new IMAGE W" + image.getWidth(null));
		      YuvImage yuv = YuvImage.rgbToYuv(image);
		      Sampler sp = new Sampler();
		      yuv = sp.sampling(yuv, SamplingRatio);	
		      ImageGrid imageGrid = new ImageGrid();
		      MCU [] mcu =  imageGrid.imageGridder(yuv);  
		      System.out.println(" ----------------------    check MCU ARRAY - --------------------------");
		  //    checkMcu(mcu);	
		      Quantizer q = new Quantizer(80);
		      
		      for (MCU m : mcu) {		    
		    	 Block[] Y  = m.getYBlockArray();
		    	 Block[] Cr = m.getCrBlockArray();
		    	 Block[] Cb = m.getCbBlockArray();
		    	
		    	 for (Block b: Y) {
		    		 DCT dctYBlock = DCT.FDCT(b);
		    		 Block block = q.quantizeDCTBlock(dctYBlock, 0);
		    		 System.out.println(" ---------------YYYYYYY----------------------------");
		    		 print(block.getData());
		    		  
		    	 }	
		    	 for (Block b: Cb) {
		    		 DCT dctCbBlock = DCT.FDCT(b);
		    		 Block block = q.quantizeDCTBlock(dctCbBlock, 1);
		    		 System.out.println(" ----------------------CB---------------------");
		    		 print(block.getData());
		    	 }
		    	 for (Block b: Cr) {
		    		 DCT dctCrBlock = DCT.FDCT(b);
		    		 Block block = q.quantizeDCTBlock(dctCrBlock, 2);
		    		 System.out.println(" -----------------------CR--------------------");
		    		 print(block.getData());
		    	 }
		    	 
		      }
		    }
		    catch(IOException e){
		      System.out.println("Error: "+e);
		    }
		}
			
	  
	    public static void print(int[][] b) {
			   for(int[] i : b) {
				   for (int j : i) {
					   System.out.print(j + " ");
				   }
				   System.out.println();
			   }
		   }
}
