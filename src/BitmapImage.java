import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

// provided by professor
public class BitmapImage
{
	private int width, height;
	private byte pixelData[];
	private BufferedImage img;
    /** loads the specified file if it is PNG, JPG, or bmp */
    public BitmapImage(String filename) throws IOException { loadFrom(new File(filename)); }
    public BitmapImage(File f) throws IOException { loadFrom(f); }
    public BitmapImage(BitmapImage other, int minX, int maxX, int minY, int maxY) {
    	this.width=-1; this.height=-1;
    	// This function takes a slice of the other bitmapImage
    	this.setSize(maxX-minX+1, maxY-minY+1);
    	for (int i=minX; i<=maxX; i++)
    		for (int j=minY; j<=maxY; j++)
    			this.setPixelRGB(i-minX, j-minY, other.getPixelAsColor(i, j).getRed(), other.getPixelAsColor(i, j).getGreen(), other.getPixelAsColor(i, j).getBlue());
    }
    public void display() {
    	JFrame f = new JFrame("Graphics display");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Container cp = f.getContentPane();
        cp.add(new JLabel(new ImageIcon(img)));
        f.setSize(400,400);
        f.setLocation(200,200);
        f.setVisible(true);
    }
    /** copies the specified image */
    public BitmapImage(Image img) { loadFrom(img); }
    public BitmapImage(int width,int height) { setSize(width,height);  }
    /** loads files of format PNG, JPG, and BMP */
    public void loadFrom(File f) throws IOException {
        if (f.getName().endsWith(".bmp")) loadFromBitmap(f);
        else { // use ImageIO which loads jpeg, png and other formats
           BufferedImage img2 = ImageIO.read(f);
           setSize(img2.getWidth(),img2.getHeight());
           Graphics g = this.img.getGraphics();
             g.drawImage(img2,0, 0, null);
         }
    }
    /** Loads an image from the file designated by the filename from formats PNG, JPG, and bmp */
    public static BufferedImage getImageForFile(String fn) throws IOException {
        return new BitmapImage(fn).getImage(); }
    /** Saves using default bit depth of 24 bits per pixel */
    public static void saveImageAsBitmap(Image img,File f) throws IOException {
        new BitmapImage(img).saveAsBitmap(f); }

    /** Saves bitmap using the specified bit depth of 16,24, or 32. 
     * You can't save to 1,4, or 8. */
    public static void saveImageAsBitmap(Image img, File f,int bitDepth) throws IOException {
        new BitmapImage(img).saveAsBitmap(f,bitDepth); }
    /** Returns an Image representing what was loaded */
    public BufferedImage getImage() { return img; }
    /** Loads any uncompressed bitmap with bit depths 1,4,8,16,24, or 32.
     * @throws IOException if there is any problem loading the file */
    public void loadFromBitmap(File f) throws IOException {
    	// load this image from a file
        InputStream in = new FileInputStream(f);
        // use LEDataInputStream to load the data
        LEDataInputStream din = new LEDataInputStream(in);
        String buf = din.readString(2);
        if (!buf.equals("BM")) throw new IOException("Invalid Windows Bitmap File");
        int fileSize = din.readInt(); // read total file size
        din.readString(4);
        int headerByteCount = din.readInt(); // read file header size or pixel offset
        din.readString(4);// ignore
        int width = din.readInt(); // read width
        int height = din.readInt(); // read height
        din.readString(2); // ignore bytes
        int bitDepth = din.readShort();
        if (bitDepth != 1 && bitDepth != 4 && bitDepth != 8 && bitDepth != 16 && bitDepth != 24 && bitDepth != 32) {
             throw new IOException("Unsupported bit depth: "+bitDepth); }
        int compressionType = din.readInt();
        if (compressionType != 0) {
             throw new IOException("Unsupported compression: "+compressionType); }
        int bytesOfPixelData = din.readInt();
        if (fileSize != bytesOfPixelData + headerByteCount) {
             throw new IOException("Inconsistent header information"); }
        din.readString(54-38);
        int colours[]=null;

        if (bitDepth <= 8) { // if image uses a colour palette 
        	colours = new int[1 << bitDepth];
        	for (int i = 0; i < colours.length; i++) {
        		colours[i] = din.readInt();

        		// swap red and blue bytes so colour[i] can be used directly to calls on setPixel
        		int temp = colours[i];
        		temp = ((temp & 0xff) << 16) | (temp & 0xff00) | ((temp & 0xff0000) >> 16);
        		colours[i] = temp; }
        	din.readString(headerByteCount-colours.length*4-54); }
         else din.readString(headerByteCount-54);

         setSize(width, height);

         if (bitDepth<=8)
             readPixelDataForIndexed(bitDepth, colours, bytesOfPixelData, din);
         else
             readPixelDataForNonIndexed(bitDepth,din);
         // now read in the pixel data 
         in.close();
    }
    /** for images of bit depths 1, 4, 8 */
    private void readPixelDataForIndexed(int bitDepth, int colours[],int bytesOfPixelData, 
         LEDataInputStream din) throws IOException {
        int padding = ((int)Math.ceil(width * bitDepth / 8.0)) % 4;
        if (padding != 0) padding = 4 - padding;        
        for (int y = height - 1; y >= 0; y--) {
        	for (int i = 0; ; i++) {
        		int b = din.read();
        		if (b < 0) break;
        		if (bitDepth == 1) { // decompess 8 pixels for every byte
        			int i2 = i * 8;
        			for (int z = 0; z < 8; z++) {
        				int x = i2+z;
        				setPixel(x, y, colours[(b >> (7-z)) & 1]); }
        			if ((i2+8) >= width) break; }
        		else if (bitDepth == 4) { // decompress 2 pixels for each byte
        			int i2 = i * 2;
        			for (int z = 0; z < 2; z++) {
        				int x = i2 + z;
        				if (x < 0) {
        					x += width * (1 + Math.abs(x / width)); }
        				setPixel(x, y, colours[(b >> ((1 - z) * 4)) & 15]); }
        			if (i2 +2>= width) break; }
        		else if (bitDepth == 8) {                        
        			setPixel(i,y,colours[b]);
        			if (i + 1 == width) break; } }
        	if (y == 0) break;
        	if (padding != 0) din.readString(padding); // ignore padding bytes
        }
    }
    /** This is for bit depths of 16, 24, and 32 */
    private void readPixelDataForNonIndexed(int bitDepth, LEDataInputStream din) throws IOException {
        int red = 0, green = 0, blue = 0;
        int padding = (width*bitDepth/8)%4;
         if (padding!=0) padding = 4-padding;

        for (int y = height - 1; y >= 0; y--)
        {
            for (int x = 0; x < width; x++)
            {
                if (bitDepth == 32)
                {
                    blue = din.read();
                    green = din.read();
                    red = din.read();
                    din.read();
                    setPixelRGB(x, y, red, green, blue);
                }
                else if (bitDepth == 24)
                {
                    blue = din.read();
                    green = din.read();
                    red = din.read();
                    setPixelRGB(x, y, red, green, blue);
                }
                else if (bitDepth == 16)
                {
                    int encodedColour = din.readShort();
                    encodedColour =
                      ((encodedColour & 0x1f) << 19) // blue
                      | ((encodedColour & 0x3e0) << 6) // green
                      | ((encodedColour & 0x7c00) >> 7) // red
                     ;
                    setPixel(x, y, encodedColour);
                }
            }
            if (padding != 0) // ignore padding bytes
                din.readString(padding);
        }
    }
    /** Saves a bitmap defaulting with 24 bit colour depth*/
    public void saveAsBitmap(File f) throws IOException { saveAsBitmap(f, 24); }    
    /** supports 16,24, and 32 bit colour depths */
    public void saveAsBitmap(File f,int bitDepth) throws IOException {
        if (bitDepth != 32 && (bitDepth != 24) && (bitDepth != 16)) {
            System.err.println("Unsupported bit depth: "+bitDepth
                +" assumed to be 24 for saving bitmap.");
            bitDepth = 24; }
        OutputStream out = new FileOutputStream(f);
        LEDataOutputStream dout = new LEDataOutputStream(out);
        dout.writeStringBytes("BM"); // write "BM"
        int headerSize = 54; // would be different if there was a palette
        /* scanlines must have a multiple of 4 bytes so a few extra bytes may be padded onto each row */
        /*int padding = (width * height * bitDepth / 8) % 4;*/
        int padding=(width*bitDepth/8)%4;
        if (padding != 0) padding = 4 - padding;
        int pixeldatasize = (width*bitDepth/8+padding) * height;
        int filesize = headerSize + pixeldatasize;

        dout.writeInt(filesize);
        dout.writeInt(0);
        dout.writeInt(headerSize);
        dout.writeInt(40);
        dout.writeInt(width);
        dout.writeInt(height);
        dout.writeShort(1); // number of colour planes must always be 1
        dout.writeShort(bitDepth);
        dout.writeInt(0);// compression mode (0 for uncompressed)
        dout.writeInt(pixeldatasize);
        dout.fillNBytes(headerSize-38,0);

        // just put a bunch of bytes with value 0 to fill the space up to the beginning of pixel data
        for (int y=height-1;y>=0;y--) {
        	for (int x = 0; x < width; x++) {
               int offset = (x + y * width) * 4;
               if (bitDepth == 24) {
            	   dout.write(pixelData[offset+1]); // blue
            	   dout.write(pixelData[offset + 2]);// green
            	   dout.write(pixelData[offset + 3]);// red  
               } else if (bitDepth == 32) {
            	   dout.write(pixelData[offset + 1]);// blue
            	   dout.write(pixelData[offset + 2]);// green
            	   dout.write(pixelData[offset + 3]);// red  
            	   dout.write(0); // alpha
               } else if (bitDepth==16) { // 16 bit 
            	   int c = ((pixelData[offset + 1] & 0xf8)>>3)//blue 
            	   | ((pixelData[offset + 2] &0xf8)<<2) // green
            	   | ((pixelData[offset + 3]&0xf8)<<7); // red
            	   dout.write(c&0xff);
            	   dout.write(c >> 8);
               }
        	}
//        	if (y==0) break;
        	if (padding != 0) // add extra bytes to ensure each scanline is a multiple of 4 bytes
        		dout.fillNBytes(padding, 0);
        }
        out.close();
    }
    public int getHeight() { return height; }
    public int getWidth() { return width; }
    public int getHeight(ImageObserver observer) { return height; }
    public int getWidth(ImageObserver observer) { return width; }
    public void flush() { img.flush(); width = 0; height = 0; pixelData = null; }
    public Object getProperty(String name, ImageObserver observer) {
        return img.getProperty(name, observer); }
    public Image getScaledInstance(int width, int height, int hints) {
        return img.getScaledInstance(width, height, hints); }
    public ImageProducer getSource() {
        return img.getSource(); }
    public Graphics getGraphics() {
        return img.getGraphics(); }
    public void setSize(int width, int height) {
        if (width==this.width && height==this.height) return;
        this.width = width;
        this.height = height;
        this.img = new BufferedImage(width,height,BufferedImage.TYPE_4BYTE_ABGR);
        pixelData = (byte[])((DataBufferByte)this.img.getRaster().getDataBuffer()).getData();
    }
    public void loadFrom(Image img) {
        setSize(img.getWidth(null), img.getHeight(null));
        Graphics g = this.img.getGraphics();
        // use a buffered image to grab all the information
        g.drawImage(img, 0, 0, null);
    }
    public void setPixel(int x, int y, int colour) {
        if (x < 0 | x >= width | y < 0 | y >= height) return;
        colour = colour & 0xffffff;
        int offset = (x+y*width)*4;
          pixelData[offset] = (byte)255; // alpha
          pixelData[offset+1] = (byte)(colour >>16); // blue
          pixelData[offset+2] = (byte)(colour >>8); // green
          pixelData[offset+3] = (byte)(colour); // red
    }
    /** returns an int in format 0xAABBGGRR */
    public int getColourForRGB(int red, int green, int blue) {
        if (red < 0) red = 0; else if (red > 255) red = 255;
        if (green < 0) green = 0; else if (green > 255) green = 255;
        if (blue < 0) blue = 0; else if (blue > 255) blue = 255;
        return (blue << 16) | (green << 8) | red;
    }
    public void setPixelRGB(int x, int y, int red, int green, int blue) {
        setPixel(x, y, getColourForRGB(red,green,blue)); }
    public void setPixel(int x, int y, Color colour) {
        setPixelRGB(x,y,colour.getRed(),colour.getGreen(),colour.getBlue()); }
    private static int unsignedByteToInt(int b) {
        if (b >= 0) return b; else return 256 + b; }
    public Color getPixelAsColor(int x, int y) {
        int offset = (x + y * width) * 4;
        return new Color(unsignedByteToInt(pixelData[offset + 3]), 
            unsignedByteToInt(pixelData[offset + 2]), unsignedByteToInt(pixelData[offset + 1]));
    }    
    public int getPixel(int x, int y) { 
        int offset = (x+y*width)*4;
        int pixel = 
                 (unsignedByteToInt(pixelData[offset+1])) | // blue
                 (unsignedByteToInt(pixelData[offset+2])<<8) | // green
                 (unsignedByteToInt(pixelData[offset + 3]) << 16); // red
        return pixel;
    }
}