import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class PacketReceiver {

    public PacketReceiver(int PortNumber){

        ServerSocket MyService = null;
        Socket serviceSocket = null;
        DataInputStream input = null;
        
        try {

            MyService = new ServerSocket(PortNumber);
            System.out.println("\n***** Server Started *****");
            serviceSocket = MyService.accept();
            System.out.println("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");
            System.out.println("***** Client Accepted *****\n");
            input = new DataInputStream(serviceSocket.getInputStream());  

            String ipDatagram = input.readUTF();
            
            String hex = ipDatagram.split(" ")[1];  
            int length = Integer.parseInt(hex,16); 
            
            String sourceIp = decodeIp(ipDatagram.split(" ")[6] + ipDatagram.split(" ")[7]);
            String payload = decodePayload(ipDatagram);
    
            System.out.println("The data received from " + sourceIp + " is " + payload);
            System.out.println("The data has " + (length-20)*8 + " bits or " + (length-20) + " bytes. Total length of the packet is " + length + " bytes.");
            System.out.println(decode(ipDatagram) ? "The verification of the checksum demonstrates that the packet received is correct." 
            : "The verification of the checksum demonstrates that the packet received is corrupted. Packet discarded!");

            input.close();
            serviceSocket.close();
            MyService.close();

        } catch (IOException e) { System.out.println(e); }

    }

    public static String decodeIp(String ip){

        String decimalIp = "";
        String temp = "";
        String[] ipArr = ip.split("");
        for (int i = 0; i < 8; i++) {
            temp += ipArr[i];
            if (i%2 == 1) {
                if (i == ipArr.length-1) {
                    decimalIp += Integer.parseInt(temp,16);
                    temp = "";
                }
                else{
                    decimalIp += Integer.parseInt(temp,16) + ".";
                    temp = "";
                }
            }
        }

        return decimalIp;
    }

    public static String decodePayload(String datagram) {

        String[] datagramArr = datagram.split(" ");
        String payload = "";
        for (int i = 10; i < datagramArr.length; i++) {
            payload += datagramArr[i];
        }

        String textPayload = "";
        String temp = "";
        String[] payloadArr = payload.split("");
        for (int i = 0; i < payload.length(); i++) {
            temp += payloadArr[i];
            if (i%2 == 1) {
                textPayload += (char)Integer.parseInt(temp,16);
                temp = "";
            }
        }
        
        return textPayload;
    }

    public static boolean decode(String payload){

        // convert each  number to hex and add them into one int variable
        String[] datagram = payload.split(" ");
        int sum = Integer.parseInt(datagram[0], 16) + Integer.parseInt(datagram[1], 16) + Integer.parseInt(datagram[2], 16) 
        + Integer.parseInt(datagram[3], 16) + Integer.parseInt(datagram[4], 16) + Integer.parseInt(datagram[5], 16) + Integer.parseInt(datagram[6], 16) + Integer.parseInt(datagram[7], 16) + Integer.parseInt(datagram[8], 16) + Integer.parseInt(datagram[9], 16);
        int carry = Character.getNumericValue(Integer.toHexString(sum).charAt(0));
        int toBeConverted = Integer.parseInt(Integer.toHexString(sum).substring(1), 16);
        String checksum = Integer.toHexString(65535 - (carry + toBeConverted)); // adds both values and performs one's complement to get the checksum
        return checksum.equals("0");
    }

    public static void main(String[] args) throws IOException {
        
        PacketReceiver server = new PacketReceiver(5001);
    } 
}
