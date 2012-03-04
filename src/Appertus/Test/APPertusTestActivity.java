package Appertus.Test;

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
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.TextView;
import android.widget.TimePicker;

public class APPertusTestActivity extends Activity {
    private static final String Color = null;
    /** Called when the activity is first created. */
    private Button mReadButton;
    private TextView mTextresult;
    private TextView mIPField;
    private TextView mSliderPos;
    private ApertusSlider mShutterTime;
    Camera Apertus;
    static int Debuglevel;
    static boolean NoCameraParameter = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.main);

	// Evil Workaround for now
	ThreadPolicy tp = ThreadPolicy.LAX;
	StrictMode.setThreadPolicy(tp);

	Apertus = new Camera(this);

	mReadButton = (Button) findViewById(R.id.button1);
	mReadButton.setOnClickListener(new View.OnClickListener() {
	    public void onClick(View v) {
		try {
		    // UpdateCameraData();
		} catch (Exception e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
	    }
	});
	mTextresult = (TextView) findViewById(R.id.result1);
	mIPField = (TextView) findViewById(R.id.IPfield);
	mShutterTime = (ApertusSlider) findViewById(R.id.slider1);
	mShutterTime.getScrollX();
	mSliderPos = (TextView) findViewById(R.id.slidepos);

	mShutterTime.SetTextView(mSliderPos);
    }

    public boolean GetNoCameraParameter() {
	return NoCameraParameter;
    }

    public void WriteLogtoConsole(String log) {
	/*
	 * if (Debuglevel > 2) { SetConsoleColor(Color.WHITE); Calendar cal =
	 * Calendar.getInstance(); SimpleDateFormat sdf = new
	 * SimpleDateFormat("HH:mm:ss.S"); System.out.println("[" +
	 * sdf.format(cal.getTime()) + "] LOG:\033[1m " + log +
	 * "\033[22m\033[0m"); }
	 */
    }

    public void WriteWarningtoConsole(String log) {
	/*
	 * if (Debuglevel > 1) { SetConsoleColor(Color.YELLOW); Calendar cal =
	 * Calendar.getInstance(); SimpleDateFormat sdf = new
	 * SimpleDateFormat("HH:mm:ss.S"); System.out.println("[" +
	 * sdf.format(cal.getTime()) + "] WARNING: \033[1m" + log +
	 * "\033[22m\033[0m"); }
	 */
    }

    public void WriteErrortoConsole(String log) {
	/*
	 * if (Debuglevel > 0) { SetConsoleColor(Color.RED); Calendar cal =
	 * Calendar.getInstance(); SimpleDateFormat sdf = new
	 * SimpleDateFormat("HH:mm:ss.S"); System.out.println("[" +
	 * sdf.format(cal.getTime()) + "] ERROR: \033[1m" + log +
	 * "\033[22m\033[0m"); SetConsoleColor(Color.WHITE); }
	 */
    }

    /*
     * public void UpdateCameraData() throws Exception { URLConnection conn =
     * null; BufferedReader data = null; String line; String result;
     * StringBuffer buf = new StringBuffer(); URL CameraUrl = null; String
     * camera_url = "http://" + mIPField.getText() +
     * "/ElphelVision/elphelvision_interface.php"; try { CameraUrl = new
     * URL(camera_url); } catch (MalformedURLException e) {
     * System.out.println("Bad URL: " + CameraUrl); }
     * 
     * int i = 0; try { conn = CameraUrl.openConnection(); conn.connect();
     * 
     * data = new BufferedReader(new InputStreamReader(conn.getInputStream()));
     * 
     * buf.delete(0, buf.length()); while ((line = data.readLine()) != null) {
     * buf.append(line + "\n"); }
     * 
     * result = buf.toString(); data.close();
     * 
     * // try to extract data from XML structure
     * 
     * try { DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
     * DocumentBuilder db = dbf.newDocumentBuilder();
     * 
     * Document doc = db.parse(new ByteArrayInputStream(result.getBytes()));
     * doc.getDocumentElement().normalize(); NodeList nodeLst =
     * doc.getElementsByTagName("elphel_vision_data"); for (int s = 0; s <
     * nodeLst.getLength(); s++) { Node fstNode = nodeLst.item(s); if
     * (fstNode.getNodeType() == Node.ELEMENT_NODE) { Element fstElmnt =
     * (Element) fstNode; NodeList fstNmElmntLst =
     * fstElmnt.getElementsByTagName("image_width"); Element fstNmElmnt =
     * (Element) fstNmElmntLst.item(0); NodeList fstNm =
     * fstNmElmnt.getChildNodes(); CameraImageWidth = Integer.parseInt(((Node)
     * fstNm.item(0)).getNodeValue());
     * 
     * NodeList lstNmElmntLst = fstElmnt.getElementsByTagName("image_height");
     * Element lstNmElmnt = (Element) lstNmElmntLst.item(0); NodeList lstNm =
     * lstNmElmnt.getChildNodes(); CameraImageHeight = Integer.parseInt(((Node)
     * lstNm.item(0)).getNodeValue()); } } // Create final Text String Content =
     * "Image Width: " + CameraImageWidth + "\n"; Content += "Image Height: " +
     * CameraImageHeight + "\n"; mTextresult.setText(Content); } catch
     * (Exception e) { e.printStackTrace(); System.out.println("error1"); } }
     * catch (IOException e) { e.printStackTrace();
     * System.out.println("error2"); } }
     */
}