import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;

/**
 * Created by zhuhui on 17-8-14.
 */
public class Test {
    public static void main(String[] args) throws IOException {
        Socket socket = null;
        try {
            socket = new Socket("127.0.0.1", 8000);
            //向服务器端发送数据
            OutputStreamWriter out = new OutputStreamWriter(socket.getOutputStream());
            //out.write("ACT:rent_confirm;STATUS:1;ORDERID:xxxx;ID:xxxx;STATIONID:xxxx;COLORID:xxxx;POWER:xxx;ISDAMAGE:xxxx;VOLTAGE:xxxx;ADAPTER:xxxx;CABLE:xxxx;SLOT:xxxx;TEMPERATURE:xxxx;BATT_TYPE:xxxx\r\n");
            out.write("ACT:borrow_battery;EVENT_CODE:1;MAC:863586037998022;ORDERID:MCS-20170815162323-12345;COLORID:7;CABLE:3;\r\n");
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            socket.close();
        }
    }
}
