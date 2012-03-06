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

import apertus.remote.R;
import apertus.remote.R.id;
import apertus.remote.R.layout;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

public class Main extends Activity {
    private TextView mSliderPos;
    private ApertusSlider mShutterTime;
    private Camera Apertus;
    private TextView ResolutionTextView;
    private Spinner ResolutionSpinner;
    private Spinner WBSpinner;
    static int Debuglevel;
    static boolean NoCameraParameter = false;
    private Boolean Armed = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.main);

	// Evil Workaround for now
	ThreadPolicy tp = ThreadPolicy.LAX;
	StrictMode.setThreadPolicy(tp);

	Apertus = new Camera(this);

	// Retrieve Bundled Extra Data
	Bundle extras = getIntent().getExtras();
	if (extras != null) {
	    String[] IP = new String[1];
	    IP[0] = extras.getString("IP");
	    Apertus.SetIP(IP);
	    Apertus.InitCameraConnection();

	}

	ResolutionSpinner = (Spinner) findViewById(R.id.ResolutionSpinner);
	ArrayAdapter<CharSequence> ResolutionAdapter = ArrayAdapter.createFromResource(this, R.array.Resolution_array,
		android.R.layout.simple_spinner_item);
	ResolutionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	ResolutionSpinner.setAdapter(ResolutionAdapter);
	ResolutionSpinner.setOnItemSelectedListener(new ResolutionOnItemSelectedListener());

	WBSpinner = (Spinner) findViewById(R.id.WBSpinner);
	ArrayAdapter<CharSequence> WBAdapter = ArrayAdapter.createFromResource(this, R.array.WB_array, android.R.layout.simple_spinner_item);
	WBAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	WBSpinner.setAdapter(WBAdapter);
	WBSpinner.setOnItemSelectedListener(new WBOnItemSelectedListener());

	mShutterTime = (ApertusSlider) findViewById(R.id.slider1);
	mShutterTime.getScrollX();
	mSliderPos = (TextView) findViewById(R.id.slidepos);
	mShutterTime.SetTextView(mSliderPos);
    }

    @Override
    protected void onResume() {
	super.onResume();

	Armed = true;

	// Toast Notification
	Context context = getApplicationContext();
	CharSequence text = Apertus.GetIP()[0] + " Connection Established";
	int duration = Toast.LENGTH_SHORT;
	Toast toast = Toast.makeText(context, text, duration);
	toast.show();
    }

    public Camera GetApertusControl() {
	return Apertus;
    }

    private class WBOnItemSelectedListener implements OnItemSelectedListener {

	private int callcount = 0;

	public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
	    // Evil workaround to prevent this from being called when the
	    // Activity is initialized
	    if (callcount > 0) {
		if (parent.getItemAtPosition(pos).toString().equals("Tungsten")) {
		    Apertus.SetWhiteBalance(WhiteBalance.TUNGSTEN);
		} else if (parent.getItemAtPosition(pos).toString().equals("Daylight")) {
		    Apertus.SetWhiteBalance(WhiteBalance.DAYLIGHT);
		} else if (parent.getItemAtPosition(pos).toString().equals("Flourescent")) {
		    Apertus.SetWhiteBalance(WhiteBalance.FLOURESCENT);
		} else if (parent.getItemAtPosition(pos).toString().equals("Cloudy")) {
		    Apertus.SetWhiteBalance(WhiteBalance.CLOUDY);
		} else if (parent.getItemAtPosition(pos).toString().equals("Auto")) {
		    Apertus.SetWhiteBalance(WhiteBalance.AUTO);
		} else if (parent.getItemAtPosition(pos).toString().equals("Custom")) {
		    Apertus.SetWhiteBalance(WhiteBalance.CUSTOM);
		}
	    }
	    callcount++;
	}

	public void onNothingSelected(AdapterView<?> parent) {
	    // Do nothing.
	}
    }

    public class ResolutionOnItemSelectedListener implements OnItemSelectedListener {
	private int callcount = 0;

	public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
	    // Evil workaround to prevent this from being called when the
	    // Activity is initialized
	    if (callcount > 0) {
		if (parent.getItemAtPosition(pos).toString().equals("Full")) {
		    Apertus.SetPreset(CameraPreset.FULL);
		} else if (parent.getItemAtPosition(pos).toString().equals("Cimax")) {
		    Apertus.SetPreset(CameraPreset.CIMAX);
		} else if (parent.getItemAtPosition(pos).toString().equals("Amax")) {
		    Apertus.SetPreset(CameraPreset.AMAX);
		} else if (parent.getItemAtPosition(pos).toString().equals("1080p")) {
		    Apertus.SetPreset(CameraPreset.FULLHD);
		} else if (parent.getItemAtPosition(pos).toString().equals("720p")) {
		    Apertus.SetPreset(CameraPreset.SMALLHD);
		} else if (parent.getItemAtPosition(pos).toString().equals("Custom")) {
		    Apertus.SetPreset(CameraPreset.CUSTOM);
		}
	    }
	    callcount++;
	}

	public void onNothingSelected(AdapterView<?> parent) {
	    // Do nothing.
	}
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