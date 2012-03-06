package apertus.remote;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

public class Connect extends Activity {
    private Button Connect;
    private CheckBox NoCamera;
    private EditText IP;
    private TextView Notice;

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.connect);

	// Evil Workaround for now
	ThreadPolicy tp = ThreadPolicy.LAX;
	StrictMode.setThreadPolicy(tp);

	Connect = (Button) findViewById(R.id.ConnectButton01);
	Connect.setOnClickListener(new View.OnClickListener() {
	    public void onClick(View view) {
		ConnectCamera(view);
	    }
	});
	NoCamera = (CheckBox) findViewById(R.id.NoCameraCheckBox);
	IP = (EditText) findViewById(R.id.IPEditText);
	Notice = (TextView) findViewById(R.id.NoticeTextView);
	Notice.setTextColor(Color.RED);
    }

    private void ConnectCamera(View view) {
	if (NoCamera.isChecked()) {
	    // Ignore Ping Camera
	    Intent myIntent = new Intent(view.getContext(), Main.class);
	    startActivityForResult(myIntent, 0);
	} else {
	    // Ping Camera
	    if (PingCamera(IP.getText().toString())) {
		Intent myIntent = new Intent(view.getContext(), Main.class);
		startActivityForResult(myIntent, 0);
	    } else {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.S");
		Notice.append("[" + sdf.format(cal.getTime()) + "] Cannot Connect to Camera with IP: " + IP.getText().toString() + "\n");
	    }
	}
    }

    private boolean PingCamera(String IP) {
	URLConnection conn = null;
	BufferedReader data = null;
	URL CameraPingUrl = null;
	String line;
	String result;

	StringBuffer buf = new StringBuffer();

	String Camera_Ping_Url = "http://" + IP + "/ElphelVision/ping.php";
	try {
	    CameraPingUrl = new URL(Camera_Ping_Url);
	} catch (MalformedURLException e) {
	    System.out.println("Bad URL: " + CameraPingUrl);
	}

	// try to connect
	try {
	    conn = CameraPingUrl.openConnection();
	    conn.setConnectTimeout(1000);
	    conn.setReadTimeout(1000);
	    conn.connect();

	    data = new BufferedReader(new InputStreamReader(conn.getInputStream()));

	    buf.delete(0, buf.length());
	    while ((line = data.readLine()) != null) {
		buf.append(line + "\n");
	    }

	    result = buf.toString();
	    data.close();

	    // try to extract data from XML structure
	    try {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();

		Document doc = db.parse(new ByteArrayInputStream(result.getBytes()));
		doc.getDocumentElement().normalize();
		NodeList nodeLst = doc.getElementsByTagName("response");
		for (int s = 0; s < nodeLst.getLength(); s++) {
		    Node fstNode = nodeLst.item(s);
		    if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
			Element fstElmnt = (Element) fstNode;
			NodeList fstNmElmntLst = fstElmnt.getElementsByTagName("ping");

			Element fstNmElmnt = (Element) fstNmElmntLst.item(0);
			NodeList fstNm = fstNmElmnt.getChildNodes();
			String response = ((Node) fstNm.item(0)).getNodeValue();
			if (response.compareTo("\"pong\"") != 0) {
			    return true;
			}
		    }
		}
	    } catch (Exception e) {
		e.printStackTrace();
		return false;
	    }
	} catch (IOException e) {
	    return false;
	}
	return false;
    }
}
