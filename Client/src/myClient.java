import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

public class myClient {

	public static void main(String args[]) {
		try {
			Socket clientSocket = new Socket("localhost", 27104);
			// Log.e("TEST", "CON");
			DataOutputStream toServer = new DataOutputStream(
					clientSocket.getOutputStream());
			BufferedReader fromServer = new BufferedReader(
					new InputStreamReader(clientSocket.getInputStream(),"UTF-8"));
			toServer.write("我是中文:中文是我\n".getBytes("UTF-8"));
			// Log.e("TEST", "SEND");
			String in = fromServer.readLine();
			System.out.println(in);
			// System.out.println(fromServer.readLine());
			toServer.close();
			fromServer.close();
			clientSocket.close();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
