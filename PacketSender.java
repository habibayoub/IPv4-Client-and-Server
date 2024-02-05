import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Random;

public class PacketSender {

    public PacketSender(String address, int port, String ipDatagram) {

        Socket myClient = null;
        DataInputStream input = null;
        DataOutputStream output = null;
        
        try {

            myClient = new Socket(address, port);
            System.out.println("\n***** Client Connected *****\n");

            System.out.println("IP Datagram: " + ipDatagram);
            
            input = new DataInputStream(myClient.getInputStream());

            output = new DataOutputStream(myClient.getOutputStream());
            output.writeUTF(ipDatagram);

        } catch (IOException e) { System.out.println(e); }
        
        try {

            output.close();
            input.close();
            myClient.close();

        } catch (IOException e) { System.out.println(e); }
        
    }       
    
    public static String arrToString(String[] arr) {

        String datagram = "";
        for (int i = 0; i < arr.length; i++) {
            if(i != arr.length-1){
                datagram += arr[i] + " ";
            }
            else{
                datagram += arr[i];
            }
        }
        return datagram;
    }

    public static String encapsulate(String payload, String clientIp, String serverIp){
        
        String[] datagram = new String[10];

        datagram[0] = "4500";
        datagram[3] = "4000";
        datagram[4] = "4006";
        datagram[5] = "0000";

        datagram[1] = lengthField(payload);

        datagram[2] = idField();

        String[] clientIpArray = clientIp.split("\\.");
        datagram[6] = sourceIpHeader(clientIpArray[0], clientIpArray[1]);
        datagram[7] = sourceIpHeader(clientIpArray[2], clientIpArray[3]);
        
        String[] serverIpArray = serverIp.split("\\.");
        datagram[8] = destinationIpHeader(serverIpArray[0], serverIpArray[1]);
        datagram[9] = destinationIpHeader(serverIpArray[2], serverIpArray[3]);

        String payloadWithChecksum = checksum(datagram);
        datagram[5] = payloadWithChecksum;

        String datagramFinal = arrToString(datagram)+" "+payload;

        return datagramFinal;
    }

    public static String lengthField(String payload) {

        int totalLength = payload.split(" ").length*2 + 20;
        String hexString = Integer.toHexString(totalLength);

        if (hexString.length()==1){
            hexString = "000"+hexString;
        }
        else if(hexString.length()==2){
            hexString = "00"+hexString;
        }
        else if(hexString.length()==3){
            hexString = "0"+hexString;
        }
        return hexString;
    }

    public static String idField() {

        Random rand = new Random(); 
        int max = 65535;
        int random = rand.nextInt(max+1); 

        String hexString = Integer.toHexString((random));

        if (hexString.length()==1){
            hexString = "000"+hexString;
        }
        else if(hexString.length()==2){
            hexString = "00"+hexString;
        }
        else if(hexString.length()==3){
            hexString = "0"+hexString;
        }
        return hexString;
    }

    public static String checksum(String[] payload) {
        
        // convert each  number to hex and add them into one int variable
        int sum = Integer.parseInt(payload[0], 16) + Integer.parseInt(payload[1], 16) + Integer.parseInt(payload[2], 16) + Integer.parseInt(payload[3], 16) + Integer.parseInt(payload[4], 16) + Integer.parseInt(payload[5], 16) + Integer.parseInt(payload[6], 16) + Integer.parseInt(payload[7], 16) + Integer.parseInt(payload[8], 16) + Integer.parseInt(payload[9], 16);
        int carry = Character.getNumericValue(Integer.toHexString(sum).charAt(0));
        int toBeConverted = Integer.parseInt(Integer.toHexString(sum).substring(1), 16);
        String checksum = Integer.toHexString(65535 - (carry + toBeConverted)); // adds both values and performs one's complement to get the checksum
        return checksum;
    }

    public static String sourceIpHeader(String sourceIp, String sourceIp2) {

        String hexString = Integer.toHexString((Integer.parseInt(sourceIp)));
        String hexString2 = Integer.toHexString((Integer.parseInt(sourceIp2)));

        if (hexString.length() != 2) {
            hexString = "0"+hexString;
        }

        if (hexString2.length() != 2) {
            hexString2 = "0"+hexString2;
        }

        String finalHexString = hexString+hexString2;

        return finalHexString;
    }
    public static String destinationIpHeader(String destinationIp, String destinationIp2) {
        String hexString = Integer.toHexString((Integer.parseInt(destinationIp)));
        String hexString2 = Integer.toHexString((Integer.parseInt(destinationIp2)));

        if (hexString.length() != 2) {
            hexString = "0"+hexString;
        }

        if (hexString2.length() != 2) {
            hexString2 = "0"+hexString2;
        }

        String finalHexString = hexString+hexString2;

        return finalHexString;
    }

    public static String stringToHex(String data){

        StringBuffer sb = new StringBuffer();
        char ch[] = data.toCharArray();

        for(int i = 0; i < ch.length; i++) {
            String hexString = Integer.toHexString(ch[i]);
            sb.append(hexString);
        }

        if (((sb.length()/2 + 20) % 8) != 0){
            int num = 8 - ((sb.length()/2 + 20) % 8);
            
            for (int i = 0; i < num*2; i++) {
                sb.append("0");
            }
        }

        StringBuffer finalPayload = new StringBuffer();
        char[] sbChar = sb.toString().toCharArray();
        int counter = 0;
        Boolean flag = true;
        Boolean flagCounter = true;

        while(flag){

            if (counter % 4 == 0 && counter != 0 && flagCounter == true){
                    finalPayload.append(" "); 
                    flagCounter = false;
                    continue;    
                }

                flagCounter = true;
                finalPayload.append(sbChar[counter]);
                
                if (counter == sbChar.length-1){
                    flag = false;
                }
                counter++;
        }

        return finalPayload.toString();
    }

    public static void main(String[] args) throws IOException {

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        System.out.println("\nEnter Server IP:");
        String serverIp = br.readLine();
        System.out.println("Enter Payload");
        String payload = br.readLine();

        String payloadHex = stringToHex(payload);

        String clientIp = InetAddress.getLocalHost().getHostAddress();
        PacketSender packet = new PacketSender("localhost", 5001, encapsulate(payloadHex, clientIp, serverIp));

    }
}