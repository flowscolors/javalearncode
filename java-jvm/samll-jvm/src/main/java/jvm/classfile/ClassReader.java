package jvm.classfile;

import java.math.BigInteger;

/**
 * @author flowscolors
 * @date 2021-11-10 9:39
 * java虚拟机定义了u1、u2、u4、u8三种数据类型来表示；1字节、2字节、4字节，无符号整数。
 * 在如下实现中，用增位方式表示无符号类型：
 * u1、u2可以用int类型存储，因为int类型是4字节
 * u4 可以用long、int类型存储，因为long类型是8字节
 * u8 可以是double、long、float
 */
public class ClassReader {

    private byte[] date;

    public ClassReader(byte[] data) {
        this.date = data;
    }

    //每次执行readbytes 相当于把现在data的前多少位读取，然后把这些位从data中移除，data会越读越端
    public byte[] readByte(int length){
        byte[] copy = new byte[length];
        System.arraycopy(date,0,copy,0,length);
        System.arraycopy(date,length,date,0,date.length-length);
        return copy;
    }

    //直接返回现在的前多少位，并在data中移除这些位
    public byte[] readBytes(int n){
        return readByte(n);
    }

    //把输入16进制的字节码转成int型
    private int byte2int(byte[] val){
        String str_hex = new BigInteger(1,val).toString(16);
        return Integer.parseInt(str_hex,16);
    }

    //把输入16进制的字节码转成int型
    private long byte2long(byte[] val){
        String str_hex = new BigInteger(1,val).toString(16);
        return Long.parseLong(str_hex,16);
    }

    //u1 读1位byte，意味着8个bit，只返回int
    public int readUnit8(){
        byte[] val = readBytes(1);
        return byte2int(val);
    }

    //u2 读2位byte，意味着16个bit，只返回int
    public int readUnit16(){
        byte[] val = readBytes(2);
        return byte2int(val);
    }

    //u4 读4位byte，意味着32个bit,可能返回long
    public long readUnit32(){
        byte[] val = readBytes(4);
        return byte2long(val);
    }

    //u4 读4位byte，意味着32个bit,可能返回int
    public Integer readUnit32TInteger(){
        byte[] val = readBytes(4);
        return new BigInteger(1,val).intValue();
    }

    public int[] readUnit16s() {
        int n = this.readUnit16();
        int[] s = new int[n];
        for (int i = 0; i < n; i++) {
            s[i] = this.readUnit16();
        }
        return s;
    }

    //u8,可能是float 、 long 、double类型
    public float readUint64TFloat() {
        byte[] val = readByte(8);
        return new BigInteger(1, val).floatValue();
    }

    public long readUint64TLong() {
        byte[] val = readByte(8);
        return new BigInteger(1, val).longValue();
    }

    public double readUint64TDouble() {
        byte[] val = readByte(8);
        return new BigInteger(1, val).doubleValue();
    }
}
