import java.io.*;
// Provided by professor
// found at http://useruploadedfiles.programmersheaven.com/54752/fileIO/
/** Little Endian Data Output Stream is used mainly for compatability with software written in other languages such as c++. */
public class LEDataOutputStream
{
	private OutputStream os;

    public LEDataOutputStream(OutputStream os) { this.os = os; }
    public void write(int i) throws IOException { os.write(i); }
    /** fills n bytes with the value specified by byteVal */
    public void fillNBytes(int n, int byteVal) throws IOException  {
        if (n <= 0) return;
        for (int i = 0; i < n; i++)
            os.write(byteVal); }
    public void writeInt(int i) throws IOException {
        os.write(i);
        os.write(i>>8);
        os.write(i >> 16);
        os.write(i >> 24); }

    public void writeByte(int v) throws IOException { os.write(v); }

    public void writeShort(int v) throws IOException {
        os.write(v);
        os.write(v >> 8); }
    public void writeStringBytes(String s) throws IOException { os.write(s.getBytes()); }
    public void writeLong(long v) throws IOException {
        for (int i = 0; i < 8; i++)
            os.write((int)(v >> (i*8))); }
}