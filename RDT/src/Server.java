import java.io.IOException;
import java.net.*;
import java.util.Random;


public class Server {
    public static void main(String[] args) throws IOException {
        //создаем сокет и привязываем его к порту
        DatagramSocket serverSocket = new DatagramSocket(Constants.SERVER_PORT);

        //массив для удобной передачи seq и ack в функции
        int[] seqAck = new int[2];
        seqAck[Constants.SEQ_NUM] = 0;
        seqAck[Constants.ACK_NUM] = 0;
        // получаем адрес локального хоста для того чтобы знать куда посылать пакеты-подтверждения
        InetAddress localAddress = InetAddress.getLocalHost();
        //устанавливаем соединение с клиентом через тройное рукопожатие
        makeConnection(serverSocket, localAddress, seqAck);

        while (true) {
            //получаем пакет
            byte[] receivedData = new byte[Constants.DATA_SIZE];
            DatagramPacket receivedPacket = new DatagramPacket(receivedData, receivedData.length);
            serverSocket.receive(receivedPacket);

            //если пакет был потерян, то просто переходим на след итерацию и опять ждем пакет
            if (packetIsLost()){
                System.out.println("packet is lost!");
                continue;
            }

            String receivedStr = Packet.getString(receivedPacket);
            System.out.println("packet received. Text:" + receivedStr);

            //делаем пакет-подтверждение акк = seq+1 и отправляем его
            Packet ackPacket = new Packet(seqAck[Constants.SEQ_NUM], seqAck[Constants.SEQ_NUM] + 1);
            byte[] ackData = ackPacket.getData();
            DatagramPacket sendingAckPacket = new DatagramPacket(ackData, ackData.length, localAddress, Constants.CLIENT_PORT);
            serverSocket.send(sendingAckPacket);
            System.out.println("sending ACK to client");
        }
    }

    //тройное рукопожатие
    private static void makeConnection(DatagramSocket serverSocket, InetAddress address, int[] seqAck) throws IOException {
        //SYN
        byte[] synData = new byte[Constants.DATA_SIZE];
        DatagramPacket syn = new DatagramPacket(synData, synData.length);
        serverSocket.receive(syn);
        synData = syn.getData();
        System.out.println("received SYN. seq: " + synData[Constants.SEQ_NUM]);

        //SYN-ACK
        seqAck[Constants.ACK_NUM] = synData[Constants.ACK_NUM] + 1;
        Packet synAckPacket = new Packet(seqAck[Constants.SEQ_NUM], seqAck[Constants.ACK_NUM]);
        byte[] synAckBytes = synAckPacket.getData();
        DatagramPacket synAck = new DatagramPacket(synAckBytes, synAckBytes.length, address, Constants.CLIENT_PORT);
        serverSocket.send(synAck);
        System.out.println("sending SYN-ACK");

        //ACK
        byte[] ackData = new byte[Constants.DATA_SIZE];
        DatagramPacket ackPacket = new DatagramPacket(ackData, ackData.length);
        serverSocket.receive(ackPacket);
        ackData = ackPacket.getData();
        System.out.println("received ACK. seq: " + ackData[Constants.SEQ_NUM] + ", ack: " + ackData[Constants.ACK_NUM] + ", connection is success!");
    }


    private static boolean packetIsLost() {
        double x = new Random().nextDouble();
        if (x < Constants.LOSS_PROBABILITY) // вероятность 40%
            return true;
        else return false;
    }
}
