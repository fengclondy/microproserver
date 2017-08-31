import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;

/**
 * Created by zhuhui on 17-8-15.
 */
public class SyncBatteryTest {
    public static void main(String[] args) throws IOException {
        Socket socket = null;
        try {
            socket = new Socket("127.0.0.1", 8000);
            OutputStreamWriter out = new OutputStreamWriter(socket.getOutputStream());
            out.write("ACT:sync_battery_cmd;EVENT_CODE:58;MAC:863586037998022;\r\n");
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            socket.close();
        }
    }
}
