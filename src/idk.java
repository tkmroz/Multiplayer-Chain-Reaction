import java.net.InetAddress;
import java.net.UnknownHostException;

public class idk {
    private static String server_IP ;
    public static void main(String[] args){
        try {
            InetAddress iAddress = InetAddress.getLocalHost();
            server_IP = iAddress.getHostAddress();
            System.out.println("Server IP address : " +server_IP);
        } catch (UnknownHostException e) {
        }


    }
}
