import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
//Provided by professor
//found at http://useruploadedfiles.programmersheaven.com/54752/fileIO/
/** Little Endian Data Input Stream is used mainly for compatability with software written in other languages such as c++. */
public class LEDataInputStream
{
    /** powersOfTwo[x+1023] = 2 to the power of x for -1024 < x < 1025 */
	private static final double[] powersOfTwo = getPowersOfTwo();
	private InputStream is;

	public LEDataInputStream(InputStream is) { this.is = is; }
    private int unsignedByte(byte b) {
        if (b >= 0) return b;
        else return 256+b; }
    public int read() throws IOException { return is.read(); }
    public int readShort() throws IOException {
    	int result = is.read();
        if (result < 0)// end of file reached
            throw new EOFException();
        int r2 = is.read();
        if (r2 < 0)// end of file reached
            throw new EOFException();
        return result | (r2*256); }

    public String readString(int len) throws IOException {
       if (len < 0) return "";
       byte[]resultBytes = new byte[len];
       if (is.read(resultBytes) < 0) 
    	   throw new EOFException();
       else return new String(resultBytes); }

    public int readInt() throws IOException {
    	int result;
    	byte[] data = new byte[4];
    	result = is.read(data);
    	if (result < 0) // end of file reached
    		throw new EOFException();
    	result = (unsignedByte(data[2]) << 16) | (unsignedByte(data[1]) << 8) | unsignedByte(data[0]);
    	result|=(unsignedByte(data[3]) << 24);
    	return result; }
    
    /** This method has been timed and executes an average of only 90% to 120% 
     * the time of its big endian counterpart */
    public double readDouble()throws IOException {        
    	byte[] data = new byte[8];
    	double result;
    	double fraction;
    	int e; 	      
    	is.read(data); // read the bytes of the double

        // long to avoid problems with missinterpretation of the sign bit from an int
    	fraction = ((unsignedByte(data[6]) & 0x0f) << 8) | unsignedByte(data[5]);
    	fraction *= 0x10000;
    	fraction += (unsignedByte(data[4])<<8);
    	fraction *= 0x1000000;
    	fraction += (long)((unsignedByte(data[3]) << 24) | (unsignedByte(data[2]) << 16) | 
    			(unsignedByte(data[1]) << 8) | unsignedByte(data[0]));
    	fraction = fraction / powersOfTwo[52+1023];
    	if (fraction > 1) {
    		System.out.println("problem: fraction should be less than 1: "+fraction);
    		fraction = 1; }
    	e = ((unsignedByte(data[7])&0x007f) << 4) | (((unsignedByte(data[6])&0x00f0)>> 4));
    	e = e - 1023;
    	result = (1 + fraction) * powersOfTwo[e+1023];
    	if (unsignedByte(data[7]) > 0x79)
    		result = -result;
    	// convert the bytes into the result
    	return result; }    

    private static double[] getPowersOfTwo() { 
    	double[]result = new double[2048];
    	for (int i = 0; i < result.length; i++) {
    		result[i] = Math.pow(2, i-1023); }
    	return result; }
}