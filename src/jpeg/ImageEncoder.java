package jpeg;

import java.awt.Image;
import java.io.BufferedOutputStream;
import java.io.IOException;



/*	
 * 	input: Image Object
 *  Image - [sizeTrimer] - rgbImage
 *  rgbImage - [ YuvConverter] - YUVimage
 *  YuvImage + SampleRatio - [Sampler] - YuvImage'
 *  YUVImage'. three Y Cr Cb - [ MCUConverter] - MCU
 *  MCU - [ BlockConverter] - 3 block Array;
 *  each Block - [DCT Transfer] - [Quantizer] -  dctBlock;
 *  dctBlock - [ AC DC encoder] - [VLIencoder + Huffman encoder] -jpg.file;
 *  output: an jpgFile; put in the outputStream;
 * 
 * 
 * */
public class ImageEncoder {
		
		Image rgbImage; 
	    YuvImage yuvImage;
	    Sampler sampler;
	    Quantizer quantizer;
	    SizeTrimer trimmer; 
	    MCU[] allMCU;
	    //  EntropyCoderI entropyCoder; 	    
	    int samplingRatio; 
	    int qualityFactor;
	    
	    private static final int DefaultSampleRatio = Sampler.YUV_444; 
	    private static final int DefaultQualityFactor = 100;
	    
	    public ImageEncoder(Image rgbImg) { 
	        this(rgbImg, DefaultSampleRatio, DefaultQualityFactor); 
	    } 
	    public ImageEncoder(Image rgb, int sr, int qf){
	    	if ( Sampler.checkValidSamplingRatio(sr) ) {
		    	this.rgbImage = rgb;
		    	this.samplingRatio = sr;
		    	this.qualityFactor = qf;
		    	trimmer =  new SizeTrimer();
		    	sampler = new Sampler();
		    	quantizer = new Quantizer(qf); 
		         
	    	} else {
	    		 throw new IllegalArgumentException("Invalid subsampling Ratio, please input 0/ 1/ 2"); 
	    	}
	    }
	    protected void imageCompress(BufferedOutputStream bos) throws IOException { 
	        Image resizedImage = trimmer.resizeImage(rgbImage, samplingRatio);
	        // showImage(scaledImg); 
	        YuvImage yuv = YuvImage.rgbToYuv(resizedImage); 
	        
	        yuv = sampler.sampling(yuv, samplingRatio);
	        
	        ImageGrid imageGrid = new ImageGrid();
		      MCU [] mcu =  imageGrid.imageGridder(yuv);  
		      System.out.println(" ----------------------    check MCU ARRAY - --------------------------");
		  //    checkMcu(mcu);
		      System.out.println("mcu length" + mcu.length);
		      
		      for (MCU m : mcu) {
		    	 Block[] Y  = m.getYBlockArray();
		    	 Block[] Cr = m.getCrBlockArray();
		    	 Block[] Cb = m.getCbBlockArray();
		    	 for (Block b: Y) {
		    		 DCT dctBlock = DCT.FDCT(b);
		    		 print(dctBlock.getData());
		    	 }
		      }
	        
	      
	        
	        /*
	        for (int i=0;i < minCodedUnits.length;i++) { 
	            // System.out.print("\nProcessing MCU: " + i); 
	            RegionI[] regions = minCodedUnits[i].getRegions(); 
	            for (int j = 0;j < regions.length;j++) { 
	                // System.out.print("\n  Region: " + getRegionType(regions[j])); 
	                BlockI[] blocks = regions[j].getBlocks(); 
	                for (int k=0;k < blocks.length;k++) { 
	                    // System.out.print("\n    Block " + k + ": --------------"); 
	                    // showBlock(blocks[k],"Component block (" + regions[j].getType() + "):"); 
	                    DCTBlockI dctBlock = dct.forward(blocks[k]); 
	                    // showDCTBlock(dctBlock); 
	                    BlockI quantBlock = quantizer.quantizeBlock(dctBlock,regions[j].getType()); 
	                    // showBlock(quantBlock,"Quantized block:"); 
	                    entropyCoder.huffmanEncode(quantBlock,regions[j].getType(),bos); 
	                } 
	            } 
	        } 
	    //    entropyCoder.flushBuffer(bos); */
	    
	    } 
	    
	    public void compressToOutStream(BufferedOutputStream bos) throws IOException { 
	      //  bos.write(SOI); 
	      //  writeHeaderSegments(bos); 
	    	imageCompress(bos); 
	      //  bos.write(EOI); 
	      //  bos.flush(); 
	    } 
	    
	    
	    public static void print(int[][] b) {
			   for(int[] i : b) {
				   for (int j : i) {
					   System.out.print(j + " ");
				   }
				   System.out.println();
			   }
		   }
	    public static void print(double[][] b) {
			   for(double[] i : b) {
				   for (double j : i) {
					   System.out.print(j + " ");
				   }
				   System.out.println();
			   }
		   }
		      

}
