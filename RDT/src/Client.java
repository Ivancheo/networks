import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Random;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) throws IOException, InterruptedException {
        DatagramSocket clientSocket = new DatagramSocket(Constants.CLIENT_PORT);
        //получаем адрес локального хоста чтобы отправлять пакеты на сервер
        InetAddress address = InetAddress.getLocalHost();
        int[] seqAck = new int[2];
        seqAck[Constants.SEQ_NUM] = 0;
        seqAck[Constants.ACK_NUM] = 0;

        makeConnection(clientSocket, address, seqAck);

        FileReader fr = new FileReader(Constants.FILE_WITH_MESSAGES_NAME);
        Scanner scan = new Scanner(fr);
        int sendNumber = 0;
        for (int sendPackets = 0; sendPackets != 5; sendPackets++) {
            String str = scan.nextLine();
            boolean packetIsSend = false;
            while (!packetIsSend) {
                sendNumber++;
                System.out.println("send number " + sendNumber);
                sendPacket(str, clientSocket, seqAck, address);
                packetIsSend = receiveAck(clientSocket, seqAck, address);
            }
            System.out.println("Packet number " + (sendPackets+1) + " is sended");
        }
        fr.close();
        scan.close();
        clientSocket.close();
    }


    private static void sendPacket(String str, DatagramSocket clientSocket, int[] seqAck, InetAddress address) throws IOException {
        Packet sendPacket = new Packet(str, seqAck[Constants.SEQ_NUM], seqAck[Constants.ACK_NUM]);
        byte[] sendData = sendPacket.getData();
        DatagramPacket send = new DatagramPacket(sendData, sendData.length, address, Constants.SERVER_PORT);
        clientSocket.send(send);
    }

    private static boolean receiveAck(DatagramSocket clientSocket, int[] seqAck, InetAddress address) {
        byte[] ackData = new byte[Constants.DATA_SIZE];
        DatagramPacket ackPacket = new DatagramPacket(ackData, ackData.length);
        try {
            clientSocket.setSoTimeout(Constants.TIMEOUT);
            if (packetIsLost()) {
                System.out.println("ack is lost!");
            } else {
                clientSocket.receive(ackPacket);
                System.out.println("ack is received");
                if (ackData[Constants.ACK_NUM] - 1 == seqAck[Constants.SEQ_NUM]) {
                    System.out.println("but ack is bad. something is wrong.");
                    return false;
                }
                return true;
            }
        } catch (IOException e) {
            return false;
        }
        return false;
    }



    private static boolean packetIsLost() {
        double x = new Random().nextDouble();
        if (x < Constants.LOSS_PROBABILITY) // вероятность 40%
            return true;
        else return false;
    }



    private static void makeConnection(DatagramSocket clientSocket, InetAddress address, int[] seqAck) throws IOException {
        //make syn and send it to server
        Packet synPacket = new Packet(seqAck[Constants.SEQ_NUM], seqAck[Constants.ACK_NUM]);
        byte[] synData = synPacket.getData();
        DatagramPacket syn = new DatagramPacket(synData, synData.length, address, Constants.SERVER_PORT);
        clientSocket.send(syn);
        System.out.println("sending SYN");

        // receive SYN-ACK
        byte[] synAckData = new byte[Constants.DATA_SIZE];
        DatagramPacket synAck = new DatagramPacket(synAckData, synAckData.length);
        clientSocket.receive(synAck);
        System.out.println("receive SYN-ACK");

        //send ACK
        if (synAckData[Constants.ACK_NUM] == synData[Constants.ACK_NUM]+1) {
            seqAck[Constants.ACK_NUM]++;
            seqAck[Constants.SEQ_NUM]++;
            Packet ackPacket = new Packet(seqAck[Constants.SEQ_NUM], seqAck[Constants.ACK_NUM]);
            byte[] ackData = ackPacket.getData();
            DatagramPacket ack = new DatagramPacket(ackData, ackData.length, address, Constants.SERVER_PORT);
            clientSocket.send(ack);
            System.out.println("sending ACK");
        }
        else
            System.out.println("something is bad. try again. connection is false");
    }
}
