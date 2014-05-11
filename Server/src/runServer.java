import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

class mainServer extends Thread {
	Socket connectIn;

	mainServer(Socket connectIn) {
		this.connectIn = connectIn;
	}

	public void run() {
		try {
			BufferedReader inFromClient = new BufferedReader(
					new InputStreamReader(connectIn.getInputStream(), "UTF-8"));
			DataOutputStream outToClient = new DataOutputStream(
					connectIn.getOutputStream());
			String in = inFromClient.readLine();
			String user[] = in.split(":");
			Date date = new Date();
			outToClient.write(("Get String from: " + connectIn.getInetAddress()
					+ "Time: " + date.getTime() + "\n").getBytes("UTF-8"));
			System.out.println("User: " + user[0] + " Connected, Time: "
					+ date.getTime());
			outToClient.close();
			inFromClient.close();
			connectIn.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

public class runServer extends Thread {
	private static int start = 1;
	static ServerSocket server;

	public void run() {
		try {
			ServerSocket server2 = new ServerSocket(27104);
			server = server2;
			System.out.println("Server Start , using port : "
					+ server.getLocalPort() + " , using ip : "
					+ server.getInetAddress());
			while (start == 1) {
				Socket connectIn = server.accept();
				System.out.println("User connect , Ip : "
						+ connectIn.getInetAddress());
				mainServer thread = new mainServer(connectIn);
				thread.start();
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String args[]) {
		BufferedReader inFromAdmin = new BufferedReader(new InputStreamReader(
				System.in));
		runServer create = new runServer();
		create.start();
		while (start == 1) {
			try {
				String command = inFromAdmin.readLine();
				if (command.matches("/shutdown")) {
					start = 0;
					server.close();
					System.out.println("Shutdown the Server");
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
			}
		}
	}
}