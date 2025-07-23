package electrosphere.net.parser.util;

import io.github.studiorailgun.CircularByteBuffer;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.Semaphore;

public class ByteStreamUtils {
    
    static ByteBuffer integerCompactor;
    static Semaphore bufferLock = new Semaphore(1);
    
    static {
        integerCompactor = ByteBuffer.allocate(8);
    }
    
    public static int popIntFromByteQueue(List<Byte> queue){
        int rVal = -1;
        bufferLock.acquireUninterruptibly();
        integerCompactor.clear();
        integerCompactor.position(0);
        integerCompactor.limit(4);
        integerCompactor.put(0,queue.remove(0));
        integerCompactor.put(1,queue.remove(0));
        integerCompactor.put(2,queue.remove(0));
        integerCompactor.put(3,queue.remove(0));
        integerCompactor.position(0);
        integerCompactor.limit(4);
        rVal = integerCompactor.getInt();
        bufferLock.release();
        return rVal;
    }
    
    public static float popFloatFromByteQueue(List<Byte> queue){
        float rVal = -1;
        bufferLock.acquireUninterruptibly();
        integerCompactor.clear();
        integerCompactor.position(0);
        integerCompactor.limit(4);
        integerCompactor.put(0,queue.remove(0));
        integerCompactor.put(1,queue.remove(0));
        integerCompactor.put(2,queue.remove(0));
        integerCompactor.put(3,queue.remove(0));
        integerCompactor.position(0);
        integerCompactor.limit(4);
        rVal = integerCompactor.getFloat();
        bufferLock.release();
        return rVal;
    }
    
    public static long popLongFromByteQueue(List<Byte> queue){
        long rVal = -1;
        bufferLock.acquireUninterruptibly();
        integerCompactor.clear();
        integerCompactor.position(0);
        integerCompactor.limit(8);
        integerCompactor.put(0,queue.remove(0));
        integerCompactor.put(1,queue.remove(0));
        integerCompactor.put(2,queue.remove(0));
        integerCompactor.put(3,queue.remove(0));
        integerCompactor.put(4,queue.remove(0));
        integerCompactor.put(5,queue.remove(0));
        integerCompactor.put(6,queue.remove(0));
        integerCompactor.put(7,queue.remove(0));
        integerCompactor.position(0);
        integerCompactor.limit(8);
        rVal = integerCompactor.getLong();
        bufferLock.release();
        return rVal;
    }
    
    public static String popStringFromByteQueue(List<Byte> queue){
        int length = popIntFromByteQueue(queue);
        byte[] stringBytes = new byte[length];
        for(int i = 0; i < length; i++){
            stringBytes[i] = queue.remove(0);
        }
        String rVal = new String(stringBytes);
        return rVal;
    }

    public static String popStringFromByteBuffer(ByteBuffer buff, int len){
        byte[] dest = new byte[len];
        buff.get(dest, 0, len);
        String rVal = new String(dest);
        return rVal;
    }

    public static byte[] popByteArrayFromByteQueue(List<Byte> queue){
        int length = popIntFromByteQueue(queue);
        byte[] bytes = new byte[length];
        for(int i = 0; i < length; i++){
            bytes[i] = queue.remove(0);
        }
        return bytes;
    }

    public static byte[] popByteArrayFromByteBuffer(ByteBuffer buff, int len){
        byte[] dest = new byte[len];
        buff.get(dest, 0, len);
        return dest;
    }

    public static double popDoubleFromByteQueue(List<Byte> queue){
        double rVal = -1;
        bufferLock.acquireUninterruptibly();
        integerCompactor.clear();
        integerCompactor.position(0);
        integerCompactor.limit(8);
        integerCompactor.put(0,queue.remove(0));
        integerCompactor.put(1,queue.remove(0));
        integerCompactor.put(2,queue.remove(0));
        integerCompactor.put(3,queue.remove(0));
        integerCompactor.put(4,queue.remove(0));
        integerCompactor.put(5,queue.remove(0));
        integerCompactor.put(6,queue.remove(0));
        integerCompactor.put(7,queue.remove(0));
        integerCompactor.position(0);
        integerCompactor.limit(8);
        rVal = integerCompactor.getDouble();
        bufferLock.release();
        return rVal;
    }

    public static int popIntFromByteQueue(CircularByteBuffer byteBuffer){
        int rVal = -1;
        bufferLock.acquireUninterruptibly();
        integerCompactor.clear();
        integerCompactor.position(0);
        integerCompactor.limit(4);
        integerCompactor.put(0,byteBuffer.peek(0));
        integerCompactor.put(1,byteBuffer.peek(1));
        integerCompactor.put(2,byteBuffer.peek(2));
        integerCompactor.put(3,byteBuffer.peek(3));
        byteBuffer.read(4);
        integerCompactor.position(0);
        integerCompactor.limit(4);
        rVal = integerCompactor.getInt();
        bufferLock.release();
        return rVal;
    }
    
    public static float popFloatFromByteQueue(CircularByteBuffer byteBuffer){
        float rVal = -1;
        bufferLock.acquireUninterruptibly();
        integerCompactor.clear();
        integerCompactor.position(0);
        integerCompactor.limit(4);
        integerCompactor.put(0,byteBuffer.read(1));
        integerCompactor.put(1,byteBuffer.read(1));
        integerCompactor.put(2,byteBuffer.read(1));
        integerCompactor.put(3,byteBuffer.read(1));
        integerCompactor.position(0);
        integerCompactor.limit(4);
        rVal = integerCompactor.getFloat();
        bufferLock.release();
        return rVal;
    }
    
    public static long popLongFromByteQueue(CircularByteBuffer byteBuffer){
        long rVal = -1;
        bufferLock.acquireUninterruptibly();
        integerCompactor.clear();
        integerCompactor.position(0);
        integerCompactor.limit(8);
        integerCompactor.put(0,byteBuffer.read(1));
        integerCompactor.put(1,byteBuffer.read(1));
        integerCompactor.put(2,byteBuffer.read(1));
        integerCompactor.put(3,byteBuffer.read(1));
        integerCompactor.put(4,byteBuffer.read(1));
        integerCompactor.put(5,byteBuffer.read(1));
        integerCompactor.put(6,byteBuffer.read(1));
        integerCompactor.put(7,byteBuffer.read(1));
        integerCompactor.position(0);
        integerCompactor.limit(8);
        rVal = integerCompactor.getLong();
        bufferLock.release();
        return rVal;
    }

    public static short popShortFromByteQueue(CircularByteBuffer byteBuffer){
        short rVal = -1;
        bufferLock.acquireUninterruptibly();
        integerCompactor.clear();
        integerCompactor.position(0);
        integerCompactor.limit(2);
        integerCompactor.put(0,byteBuffer.peek(0));
        integerCompactor.put(1,byteBuffer.peek(1));
        byteBuffer.read(2);
        integerCompactor.position(0);
        integerCompactor.limit(2);
        rVal = integerCompactor.getShort();
        bufferLock.release();
        return rVal;
    }
    
    public static String popStringFromByteQueue(CircularByteBuffer byteBuffer){
        int length = popIntFromByteQueue(byteBuffer);
        byte[] stringBytes = byteBuffer.read(length);
        String rVal = new String(stringBytes);
        return rVal;
    }

    public static byte[] popByteArrayFromByteQueue(CircularByteBuffer byteBuffer){
        int length = popIntFromByteQueue(byteBuffer);
        byte[] bytes = byteBuffer.read(length);
        return bytes;
    }

    public static double popDoubleFromByteQueue(CircularByteBuffer byteBuffer){
        double rVal = -1;
        bufferLock.acquireUninterruptibly();
        integerCompactor.clear();
        integerCompactor.position(0);
        integerCompactor.limit(8);
        integerCompactor.put(0,byteBuffer.read(1));
        integerCompactor.put(1,byteBuffer.read(1));
        integerCompactor.put(2,byteBuffer.read(1));
        integerCompactor.put(3,byteBuffer.read(1));
        integerCompactor.put(4,byteBuffer.read(1));
        integerCompactor.put(5,byteBuffer.read(1));
        integerCompactor.put(6,byteBuffer.read(1));
        integerCompactor.put(7,byteBuffer.read(1));
        integerCompactor.position(0);
        integerCompactor.limit(8);
        rVal = integerCompactor.getDouble();
        bufferLock.release();
        return rVal;
    }
    
    public static byte[] serializeIntToBytes(int i){
        byte[] rVal = new byte[4];
        bufferLock.acquireUninterruptibly();
        integerCompactor.clear();
        integerCompactor.position(0);
        integerCompactor.limit(4);
        integerCompactor.putInt(i);
        integerCompactor.position(0);
        integerCompactor.limit(4);
        rVal[0] = integerCompactor.get(0);
        rVal[1] = integerCompactor.get(1);
        rVal[2] = integerCompactor.get(2);
        rVal[3] = integerCompactor.get(3);
        bufferLock.release();
        return rVal;
    }
    
    public static byte[] serializeFloatToBytes(float i){
        byte[] rVal = new byte[4];
        bufferLock.acquireUninterruptibly();
        integerCompactor.clear();
        integerCompactor.position(0);
        integerCompactor.limit(4);
        integerCompactor.putFloat(i);
        integerCompactor.position(0);
        integerCompactor.limit(4);
        rVal[0] = integerCompactor.get(0);
        rVal[1] = integerCompactor.get(1);
        rVal[2] = integerCompactor.get(2);
        rVal[3] = integerCompactor.get(3);
        bufferLock.release();
        return rVal;
    }
    
    public static byte[] serializeLongToBytes(long i){
        byte[] rVal = new byte[8];
        bufferLock.acquireUninterruptibly();
        integerCompactor.clear();
        integerCompactor.position(0);
        integerCompactor.limit(8);
        integerCompactor.putLong(i);
        integerCompactor.position(0);
        integerCompactor.limit(8);
        rVal[0] = integerCompactor.get(0);
        rVal[1] = integerCompactor.get(1);
        rVal[2] = integerCompactor.get(2);
        rVal[3] = integerCompactor.get(3);
        rVal[4] = integerCompactor.get(4);
        rVal[5] = integerCompactor.get(5);
        rVal[6] = integerCompactor.get(6);
        rVal[7] = integerCompactor.get(7);
        bufferLock.release();
        return rVal;
    }
    
    public static byte[] serializeStringToBytes(String s){
        int length = s.length();
        byte[] rVal = new byte[length + 4]; //the 4 is the header int for the string size
        byte[] serializedInteger = serializeIntToBytes(length);
        rVal[0] = serializedInteger[0];
        rVal[1] = serializedInteger[1];
        rVal[2] = serializedInteger[2];
        rVal[3] = serializedInteger[3];
        byte[] stringBytes = s.getBytes();
        for(int i = 0; i < length; i++){
            rVal[4+i] = stringBytes[i];
        }
        return rVal;
    }

    public static byte[] serializeDoubleToBytes(double i){
        byte[] rVal = new byte[8];
        bufferLock.acquireUninterruptibly();
        integerCompactor.clear();
        integerCompactor.position(0);
        integerCompactor.limit(8);
        integerCompactor.putDouble(i);
        integerCompactor.position(0);
        integerCompactor.limit(8);
        rVal[0] = integerCompactor.get(0);
        rVal[1] = integerCompactor.get(1);
        rVal[2] = integerCompactor.get(2);
        rVal[3] = integerCompactor.get(3);
        rVal[4] = integerCompactor.get(4);
        rVal[5] = integerCompactor.get(5);
        rVal[6] = integerCompactor.get(6);
        rVal[7] = integerCompactor.get(7);
        bufferLock.release();
        return rVal;
    }

    public static void writeInt(OutputStream stream, int i) throws IOException {
        bufferLock.acquireUninterruptibly();
        integerCompactor.clear();
        integerCompactor.position(0);
        integerCompactor.limit(4);
        integerCompactor.putInt(i);
        integerCompactor.position(0);
        integerCompactor.limit(4);
        stream.write(integerCompactor.get(0));
        stream.write(integerCompactor.get(1));
        stream.write(integerCompactor.get(2));
        stream.write(integerCompactor.get(3));
        bufferLock.release();
    }
    
    public static void writeFloat(OutputStream stream, float i) throws IOException {
        bufferLock.acquireUninterruptibly();
        integerCompactor.clear();
        integerCompactor.position(0);
        integerCompactor.limit(4);
        integerCompactor.putFloat(i);
        integerCompactor.position(0);
        integerCompactor.limit(4);
        stream.write(integerCompactor.get(0));
        stream.write(integerCompactor.get(1));
        stream.write(integerCompactor.get(2));
        stream.write(integerCompactor.get(3));
        bufferLock.release();
    }
    
    public static void writeLong(OutputStream stream, long i) throws IOException {
        bufferLock.acquireUninterruptibly();
        integerCompactor.clear();
        integerCompactor.position(0);
        integerCompactor.limit(8);
        integerCompactor.putLong(i);
        integerCompactor.position(0);
        integerCompactor.limit(8);
        stream.write(integerCompactor.get(0));
        stream.write(integerCompactor.get(1));
        stream.write(integerCompactor.get(2));
        stream.write(integerCompactor.get(3));
        stream.write(integerCompactor.get(4));
        stream.write(integerCompactor.get(5));
        stream.write(integerCompactor.get(6));
        stream.write(integerCompactor.get(7));
        bufferLock.release();
    }
    
    public static void writeString(OutputStream stream, String s) throws IOException {
        byte[] stringBytes = s.getBytes();
        stream.write(stringBytes);
    }

    public static void writeDouble(OutputStream stream, double i) throws IOException {
        bufferLock.acquireUninterruptibly();
        integerCompactor.clear();
        integerCompactor.position(0);
        integerCompactor.limit(8);
        integerCompactor.putDouble(i);
        integerCompactor.position(0);
        integerCompactor.limit(8);
        stream.write(integerCompactor.get(0));
        stream.write(integerCompactor.get(1));
        stream.write(integerCompactor.get(2));
        stream.write(integerCompactor.get(3));
        stream.write(integerCompactor.get(4));
        stream.write(integerCompactor.get(5));
        stream.write(integerCompactor.get(6));
        stream.write(integerCompactor.get(7));
        bufferLock.release();
    }
    
}