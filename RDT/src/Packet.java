import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;

public class Packet {
    byte[] data;

    public Packet(int seq, int ack){
        data = new byte[Constants.DATA_SIZE];
        data[Constants.SEQ_NUM] = (byte) seq;
        data[Constants.ACK_NUM] = (byte) ack;
    }


    public Packet(String str, int seq, int ack) {
        data = new byte[Constants.DATA_SIZE];
        data[Constants.SEQ_NUM] = (byte) seq;
        data[Constants.ACK_NUM] = (byte) ack;
        data[Constants.LEN_POSITION] = (byte) str.length();
        byte[] strInBytes = str.getBytes();
        System.arraycopy(strInBytes, 0, data, Constants.MESSAGE_POSITION, strInBytes.length);
    }

    public byte[] getData (){
        return data;
    }

    public static String getString(DatagramPacket packet) throws UnsupportedEncodingException {
        byte[] tmpData = packet.getData();
        int tmpDataLen = tmpData[Constants.LEN_POSITION];
        byte[] tmp = new byte[tmpDataLen+2];
        System.arraycopy(tmpData, Constants.LEN_POSITION, tmp, 0, tmpData[Constants.LEN_POSITION]);
        return new String(tmp, "UTF-8");
    }
}
