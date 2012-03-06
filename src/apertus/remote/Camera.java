/*! Copyright (C) 2012 Apertus, All Rights Reserved
 *! Author : Apertus Team
 *! Description: The Camera class holds everything relevant to communicating
 *! with the camera retrieving and setting parameters, etc.
-----------------------------------------------------------------------------**
 *!
 *!  This program is free software: you can redistribute it and/or modify
 *!  it under the terms of the GNU General Public License as published by
 *!  the Free Software Foundation, either version 3 of the License, or
 *!  (at your option) any later version.
 *!
 *!  This program is distributed in the hope that it will be useful,
 *!  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *!  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *!  GNU General Public License for more details.
 *!
 *!  You should have received a copy of the GNU General Public License
 *!  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *!
-----------------------------------------------------------------------------**/
package apertus.remote;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.app.Activity;

enum CamogmState {

    NOTRUNNING, STOPPED, RECORDING
}

enum HDDState {

    UNMOUNTED, MOUNTED, FULL
}

enum ImageOrientation {

    LANDSCAPE, PORTRAIT
}

enum RecordFormat {

    MOV, OGM, JPEG
}

enum Trigger {

    FREERUNNING, TRIGGERED
}

enum CameraParameter {

    EXPOSURE, GAIN, GAMMA, PRESET, AUTOEXP, JPEGQUAL, COLORMODE, FPS, SENSORWIDTH, SENSORHEIGHT, WHITEBALANCE, RECORDFORMAT
}

enum CameraPreset {

    FULL, AMAX, CIMAX, FULLHD, SMALLHD, CUSTOM
}

enum ColorMode {

    RGB, JP4, JP46
}

enum HistogramScaleMode {

    LINEAR, LOG
}

enum HistogramColorMode {

    LUMINOSITY, RGB
}

enum WhiteBalance {

    AUTO, DAYLIGHT, TUNGSTEN, CLOUDY, CUSTOM, FLOURESCENT
}

enum MirrorImage {

    NONE, VERTICAL, HORIZONTAL, VERTICALHORIZONTAL
}

enum GammaPreset {

    LINEAR, CINE1, CINE2, CINES
}

enum PhotoResolution {

    FULL, ASVIDEO
}

/**
 * The camera class holds all settings/parameters/states/commands of the Elphel
 * Camera.
 */
public class Camera {

    private String IP[] = null;
    private String StereoCameraName[] = null; // can be used to define "Left"
					      // and "Right" camera in stereo 3D
					      // mode
    private URL[] CameraUrl = null;
    private URL[] CameraPingUrl = null;
    private float FPS;
    private RecordFormat RecordFormat = null;
    private float HDDSpaceFree;
    private float HDDSpaceFreeRatio;
    private HDDState HDDState;
    private int RecordedFrames = 0;
    private float Datarate = 0;
    private CamogmState CAMOGMState = CamogmState.NOTRUNNING;
    private int JPEGQuality;
    private int ImageWidth;
    private int ImageHeight;
    private int[] ImageWOILeft = null;
    private int[] ImageWOITop = null;
    private int Stereo3DHit;
    private ImageOrientation ImageOrientation;
    private CameraPreset Preset = CameraPreset.FULLHD;
    private int[][] Histogram;
    private float Gamma;
    private int[] GammaCurve;
    private int Blacklevel;
    private HistogramScaleMode HistogramScaleMode;
    private HistogramColorMode HistogramColorMode;
    private int CoringIndexY;
    private int CoringIndexC;
    private int FPSSkipSeconds;
    private int FPSSkipFrames;
    private Trigger FrameTrigger = Trigger.FREERUNNING;
    private int TriggerPeriod = 0;
    private int TriggerOut = 0;
    private int TriggerCondition = 0;
    private int FrameSizeBytes;
    private int BufferOverruns;
    private int Bufferfree;
    private int Bufferused;
    private boolean AutoExposure = false;
    private boolean GuideDrawCenterX = false;
    private boolean GuideDrawOuterX = false;
    private boolean GuideDrawThirds = false;
    private boolean GuideDrawSafeArea = false;
    private float MultiCameraRecordingStartDelay = 2.0f;
    private GammaPreset GammaPreset;
    private boolean AllowSlowShutter;
    private float ExposureTimeEV[] = { 4, 3.2f, 2.5f, 2, 1.667f, 1.333f, 1, 0.8f, 0.6f, 0.5f, 0.4f, 0.3f, 0.25f, 0.1667f, 0.125f, 0.1f, 0.0769f,
	    0.0667f, 0.05f, 0.04f, 0.03333f, 0.03f, 0.02f, 0.016667f, 0.0125f, 0.01f, 0.008f, 0.00625f, 0.005f, 0.004f, 0.003125f, 0.0025f, 0.002f,
	    0.0015625f, 0.00125f, 0.001f, 0.0008f, 0.000625f, 0.0005f, 0.0004f, 0.0003125f, 0.00025f, 0.0002f, 0.00015625f, 0.000125f, 0.0001f,
	    0.00008f, 0.0000625f, 0.00005f, 0.00004f, 0.00003125f, 0.000025f, 0.00002f, 0.000015625f, 0.0000125f, 0.00001f };
    private String GainNames[] = { "+12dB", "+9dB", "+6dB", "+3dB", "0dB" };
    private int GainIndex;
    private float Gain[] = { 16, 8, 4, 2, 1 };
    private float Gain_R = 1;
    private float Gain_G = 1;
    private float Gain_B = 1;
    private float Gain_GB = 1;
    private float WB_Factor_R = 1;
    private float WB_Factor_G = 1;
    private float WB_Factor_B = 1;
    private float WB_Factor_GB = 1;
    private String ExposureTimeEVNames[] = { "4", "3.2", "2.5", "2", "1.6", "1.3", "1", "0.8", "0.6", "0.5", "0.4", "0.3", "0.25", "1/6", "1/8",
	    "1/10", "1/13", "1/15", "1/20", "1/25", "1/30", "1/40", "1/50", "1/60", "1/80", "1/100", "1/125", "1/160", "1/200", "1/250", "1/320",
	    "1/400", "1/500", "1/640", "1/800", "1/1000", "1/1250", "1/1600", "1/2000", "1/2500", "1/3200", "1/4000", "1/5000", "1/6400", "1/8000",
	    "1/10000", "1/12500", "1/16000", "1/20000", "1/25000", "1/32000", "1/40000", "1/50000", "1/64000", "1/80000", "1/100000" };
    private int ExposureIndex;
    private int JPEGQual;
    private long RecordstartTime = 0;
    private ColorMode Colormode = ColorMode.RGB;
    private ColorMode PhotoColormode = ColorMode.RGB;
    private PhotoResolution Photoresolution = PhotoResolution.FULL;
    private int PhotoQuality = 100;
    private boolean AllowCaptureStillWhileRecording;
    private WhiteBalance ImageWhiteBalance = WhiteBalance.AUTO;
    private MirrorImage ImageFlip = MirrorImage.NONE;
    private int MovieClipMaxChunkSize;
    private int MovieClipMaxChunkDuration;
    private int MovieClipMaxChunkFrames;
    private boolean ConnectionEstablished = false;
    private String RecordPath;
    private String RecordClipName;
    private String SingleCameraName;
    private Main Parent;

    Camera(Main parent) {
	this.Parent = parent;
	this.ImageHeight = 0;
	this.ImageWidth = 0;
	this.ImageWOILeft = new int[2];
	this.ImageWOITop = new int[2];
	this.ImageWOILeft[0] = 0;
	this.ImageWOILeft[1] = 0;
	this.ImageWOITop[0] = 0;
	this.ImageWOITop[1] = 0;
	this.FPS = 0;
	this.JPEGQuality = 0;
	this.IP = new String[] { "192.168.0.9" };
	this.StereoCameraName = new String[] { "A-Left", "B-Right" };
	this.SingleCameraName = "";
	this.ExposureIndex = 20;
	this.GainIndex = 4;
	this.JPEGQual = 80;
	this.Histogram = new int[3][256];
	this.GammaCurve = new int[256];
	this.ImageOrientation = ImageOrientation.LANDSCAPE;
	this.Blacklevel = 10;
	this.Gamma = 0.5f;
	this.GammaPreset = GammaPreset.LINEAR;
	this.GuideDrawCenterX = false;
	this.GuideDrawOuterX = false;
	this.GuideDrawThirds = false;
	this.GuideDrawSafeArea = false;
	this.FPSSkipFrames = 0;
	this.FPSSkipSeconds = 0;
	this.FrameSizeBytes = 0;
	this.MovieClipMaxChunkSize = 2048; // in Megabytes - Default 2 GB = 2 x
					   // 1024 x 1024 x 1024 bytes
	this.HistogramScaleMode = HistogramScaleMode.LOG;
	this.HistogramColorMode = HistogramColorMode.RGB;
    }

    public void SetIP(String[] IP) {
	this.IP = IP;
    }

    public String[] GetIP() {
	return this.IP;
    }

    public boolean GetCameraConnectionEstablished() {
	return this.ConnectionEstablished;
    }

    public void SetMovieClipMaxChunkSize(int newchunksize) {
	long newsize = (long) newchunksize * 1048576; // Megabytes
	for (int i = 0; i < this.IP.length; i++) {
	    Parent.WriteLogtoConsole(GetIP()[i] + ": Setting MovieClipMaxChunkSize to " + newsize);
	    this.ExecuteCommand(i, "set_size&size=" + newsize);
	}
    }

    public int GetMovieClipMaxChunkSize() {
	return this.MovieClipMaxChunkSize;
    }

    public void SetImageOrientation(ImageOrientation orientation) {
	this.ImageOrientation = orientation;
    }

    public ImageOrientation GetImageOrientation() {
	return this.ImageOrientation;
    }

    public MirrorImage GetImageFlipMode() {
	return this.ImageFlip;
    }

    public int GetFrameSizeBytes() {
	return this.FrameSizeBytes;
    }

    public void ReadFrameSizeBytes() {
	URLConnection conn = null;
	BufferedReader data = null;
	String line;
	String result = null;
	StringBuffer buf = new StringBuffer();
	URL FramesizeURL = null;
	int parameter = 0;
	String camera = "http://" + this.IP[0] + "/ElphelVision/getparam.php?parameter=FRAME_SIZE";
	try {
	    FramesizeURL = new URL(camera);
	} catch (MalformedURLException e) {
	    Parent.WriteErrortoConsole("GetDatarate(): Reading FRAME_SIZE data failed at URL: " + FramesizeURL);
	}

	try {
	    conn = FramesizeURL.openConnection();
	    conn.connect();

	    Parent.WriteLogtoConsole(this.IP[0] + ": GetDatarate(): Reading FRAME_SIZE data");

	    data = new BufferedReader(new InputStreamReader(conn.getInputStream()));

	    buf.delete(0, buf.length());
	    while ((line = data.readLine()) != null) {
		buf.append(line + "\n");
	    }

	    result = buf.toString();
	    data.close();
	} catch (IOException e) {
	    Parent.WriteErrortoConsole("Reading FRAME_SIZE data IO Error:" + e.getMessage());
	}

	try {
	    result = result.replace("\n", "");
	    this.FrameSizeBytes = Integer.parseInt(result);

	} catch (Exception e) {
	    Parent.WriteErrortoConsole("Reading FRAME_SIZE data IO Error:" + e.getMessage());
	}
    }

    public void SetAutoExposure(boolean OnOff) {
	this.AutoExposure = OnOff;
	for (int i = 0; i < this.IP.length; i++) {
	    if (this.AutoExposure) {
		SetParameter(i, CameraParameter.AUTOEXP, 1);
	    } else {
		SetParameter(i, CameraParameter.AUTOEXP, 0);
	    }
	    Parent.WriteLogtoConsole(GetIP()[i] + ": Setting AUTOEXPOSURE to " + OnOff);
	}
    }

    public boolean GetAutoExposure() {
	return this.AutoExposure;
    }

    public void SetGuides(boolean drawCenterX, boolean drawOuterX, boolean drawThirds, boolean drawSafeArea) {
	Parent.WriteLogtoConsole("Setting GuideDrawCenterX to " + drawCenterX);
	this.GuideDrawCenterX = drawCenterX;
	Parent.WriteLogtoConsole("Setting GuideDrawOuterX to " + drawOuterX);
	this.GuideDrawOuterX = drawOuterX;
	Parent.WriteLogtoConsole("Setting GuideDrawThirds to " + drawThirds);
	this.GuideDrawThirds = drawThirds;
	Parent.WriteLogtoConsole("Setting GuideDrawSafeArea to " + drawSafeArea);
	this.GuideDrawSafeArea = drawSafeArea;
    }

    public void SetGuide(String name, boolean value) {
	if (name.contentEquals("GuideDrawCenterX")) {
	    Parent.WriteLogtoConsole("Setting GuideDrawCenterX to " + value);
	    this.GuideDrawCenterX = value;
	}
	if (name.contentEquals("GuideDrawOuterX")) {
	    Parent.WriteLogtoConsole("Setting GuideDrawOuterX to " + value);
	    this.GuideDrawOuterX = value;
	}
	if (name.contentEquals("GuideDrawThirds")) {
	    Parent.WriteLogtoConsole("Setting GuideDrawThirds to " + value);
	    this.GuideDrawThirds = value;
	}
	if (name.contentEquals("GuideDrawSafeArea")) {
	    Parent.WriteLogtoConsole("Setting GuideDrawSafeArea to " + value);
	    this.GuideDrawSafeArea = value;
	}
    }

    public boolean[] GetGuides() {
	boolean[] returnval;
	returnval = new boolean[4];
	returnval[0] = this.GuideDrawCenterX;
	returnval[1] = this.GuideDrawOuterX;
	returnval[2] = this.GuideDrawThirds;
	returnval[3] = this.GuideDrawSafeArea;
	return returnval;
    }

    public void SetImageFlipMode(MirrorImage newmode) {
	this.ImageFlip = newmode;

	for (int i = 0; i < this.IP.length; i++) {
	    if (newmode == MirrorImage.NONE) {
		Parent.WriteLogtoConsole(this.IP[i] + ": Setting FlipMode to NONE");
		SendParametertoCamera(i, "FLIPH=0&FLIPV=0");
	    }
	    if (newmode == MirrorImage.HORIZONTAL) {
		Parent.WriteLogtoConsole(this.IP[i] + ": Setting FlipMode to HORIZONTAL");
		SendParametertoCamera(i, "FLIPH=1&FLIPV=0");
	    }
	    if (newmode == MirrorImage.VERTICAL) {
		Parent.WriteLogtoConsole(this.IP[i] + ": Setting FlipMode to VERTICAL");
		SendParametertoCamera(i, "FLIPH=0&FLIPV=1");
	    }
	    if (newmode == MirrorImage.VERTICALHORIZONTAL) {
		Parent.WriteLogtoConsole(this.IP[i] + ": Setting FlipMode to VERTICALHORIZONTAL");
		SendParametertoCamera(i, "FLIPH=1&FLIPV=1");
	    }
	}
    }

    public int GetImageWidth() {
	return this.ImageWidth;
    }

    public int GetImageHeight() {
	return this.ImageHeight;
    }

    public float GetGamma() {
	return this.Gamma;
    }

    public GammaPreset GetGammaPreset() {
	return this.GammaPreset;
    }

    public float GetMultiCameraRecordingStartDelay() {
	return this.MultiCameraRecordingStartDelay;
    }

    public void SetMultiCameraRecordingStartDelay(float newdelay) {
	Parent.WriteLogtoConsole("Setting MultiCameraRecordingStartDelay to " + newdelay);
	MultiCameraRecordingStartDelay = newdelay;
    }

    public void SetFPSSkipSeconds(int newseconds) {
	for (int i = 0; i < this.IP.length; i++) {
	    Parent.WriteLogtoConsole(GetIP()[i] + ": Setting SecondsSkip to " + newseconds);
	    this.FPSSkipSeconds = newseconds;
	    this.ExecuteCommand(i, "SETSKIPSECONDS", newseconds + "");
	}
    }

    public int GetFPSSkipSeconds() {
	return this.FPSSkipSeconds;
    }

    public void SetFPSSkipFrames(int newSkipFrames) {
	for (int i = 0; i < this.IP.length; i++) {
	    Parent.WriteLogtoConsole(GetIP()[i] + ": Setting FramesSkip to " + newSkipFrames);
	    this.FPSSkipFrames = newSkipFrames;
	    this.ExecuteCommand(i, "SETSKIPFRAMES", newSkipFrames + "");
	}
    }

    public int GetFPSSkipFrames() {
	return this.FPSSkipFrames;
    }

    public void SetGammaPreset(GammaPreset newpreset) {
	// TODO for all IPs
	this.GammaPreset = newpreset;
    }

    public int GetBlacklevel() {
	return this.Blacklevel;
    }

    public void SetBlacklevel(int newblacklevel) {
	// TODO for all IPs
	if (newblacklevel > 255) {
	    newblacklevel = 255;
	}
	if (newblacklevel < 0) {
	    newblacklevel = 0;
	}
	this.Blacklevel = newblacklevel;

	for (int i = 0; i < this.IP.length; i++) {
	    Parent.WriteLogtoConsole(this.IP[i] + ": Setting Blacklevel to " + newblacklevel);
	    this.SendCamVCParameters(i, "set=0/gam:" + this.Gamma + "/pxl:" + this.Blacklevel + "/");
	}
    }

    public double GetRecordstartTime() {
	return this.RecordstartTime;
    }

    public int GetImageJPEGQuality() {
	return this.JPEGQuality;
    }

    public CamogmState GetCamogmState() {
	return this.CAMOGMState;
    }

    public RecordFormat GetRecordFormat() {
	return this.RecordFormat;
    }

    public float GetVideoAspectRatio() {
	return ((float) this.ImageWidth / (float) this.ImageHeight);
    }

    public float GetFreeHDDSpace() {
	return this.HDDSpaceFree;
    }

    public float GetFreeHDDRatio() {
	return this.HDDSpaceFreeRatio;
    }

    public int GetRecordedFramesCount() {
	return this.RecordedFrames;
    }

    public float GetDatarate() {
	return this.Datarate;
    }

    public String GetExposure() {
	return this.ExposureTimeEVNames[ExposureIndex];
    }

    public float GetExposurefromIndex(int Index) {
	return this.ExposureTimeEV[Index];
    }

    public int GetExposureIndex() {
	return this.ExposureIndex;
    }

    public boolean GetAllowSlowShutter() {
	return AllowSlowShutter;
    }

    public void SetAllowSlowShutter(boolean newvalue) {
	AllowSlowShutter = newvalue;
    }

    public String GetGain() {
	return this.GainNames[GainIndex];
    }

    public String GetGain(int index) {
	return this.GainNames[index];
    }

    public int GetGainIndex() {
	return this.GainIndex;
    }

    public void SetGain(float newgain) {
	for (int i = 0; i < this.IP.length; i++) {
	    float GainR, GainB, GainG, GainGB;
	    GainR = newgain * 65536 * WB_Factor_R;
	    GainG = newgain * 65536 * WB_Factor_G;
	    GainB = newgain * 65536 * WB_Factor_B;
	    GainGB = newgain * 65536 * WB_Factor_GB;
	    Parent.WriteLogtoConsole(GetIP()[i] + ": Setting GAIN to " + newgain);
	    SendParametertoCamera(i, "framedelay=1&GAINR=" + (int) GainR + "&GAING=" + (int) GainG + "&GAINB=" + (int) GainB + "&GAINGB="
		    + (int) GainGB);
	}
    }

    public void SetCoringIndex(int newcoreY, int newcoreC) {
	this.CoringIndexC = newcoreC;
	this.CoringIndexY = newcoreY;

	int temp = 0x10000 * newcoreC + newcoreY;
	// Coring = 0x10000*Coring_C + Coring_Y, where Coring_C/Y = 0...100
	// decimal
	for (int i = 0; i < this.IP.length; i++) {
	    Parent.WriteLogtoConsole(GetIP()[i] + ": Setting CORING_INDEX to " + temp);
	    SendParametertoCamera(i, "framedelay=1&CORING_INDEX=" + (int) temp);
	}
    }

    public int GetCoringIndexC() {
	return CoringIndexC;
    }

    public int GetCoringIndexY() {
	return CoringIndexY;
    }

    public void SetGainIndex(int newindex) {
	if (newindex > Gain.length - 1) {
	    newindex = Gain.length - 1;
	}
	if (newindex < 0) {
	    newindex = 0;
	}
	this.GainIndex = newindex;

	for (int i = 0; i < this.IP.length; i++) {
	    Parent.WriteLogtoConsole(GetIP()[i] + ": Setting GAIN_INDEX to " + newindex);

	    float GainR, GainB, GainG, GainGB;
	    GainR = this.Gain[GainIndex] * 65536 * WB_Factor_R;
	    GainG = this.Gain[GainIndex] * 65536 * WB_Factor_G;
	    GainB = this.Gain[GainIndex] * 65536 * WB_Factor_B;
	    GainGB = this.Gain[GainIndex] * 65536 * WB_Factor_GB;
	    SendParametertoCamera(i, "framedelay=3&GAINR=" + (int) GainR + "&GAING=" + (int) GainG + "&GAINB=" + (int) GainB + "&GAINGB="
		    + (int) GainGB);
	}
    }

    public void SetExposure(float newexposure) {
	for (int i = 0; i < this.IP.length; i++) {
	    Parent.WriteLogtoConsole(GetIP()[i] + ": Setting EXPOSURE to " + newexposure);
	    this.SetParameter(i, CameraParameter.EXPOSURE, newexposure);
	}
    }

    public void SetExposureIndex(int newindex) {
	if (newindex > ExposureTimeEV.length - 1) {
	    newindex = ExposureTimeEV.length - 1;
	}
	if (newindex < 0) {
	    newindex = 0;
	}

	this.ExposureIndex = newindex;

	for (int i = 0; i < this.IP.length; i++) {
	    Parent.WriteLogtoConsole(GetIP()[i] + ": Setting EXPOSUREINDEX to " + newindex + " value = " + ExposureTimeEV[ExposureIndex]);
	    this.SetParameter(i, CameraParameter.EXPOSURE, ExposureTimeEV[ExposureIndex]);
	}
    }

    public void SetJPEGQuality(int newquality) {
	newquality = Utils.MinMaxRange(newquality, 0, 100);

	this.JPEGQual = newquality;

	for (int i = 0; i < this.IP.length; i++) {
	    Parent.WriteLogtoConsole(GetIP()[i] + ": Setting JPEGQuality to " + newquality);
	    this.SetParameter(i, CameraParameter.JPEGQUAL, newquality);
	}
    }

    public void SetColorMode(ColorMode Mode) {
	for (int i = 0; i < this.IP.length; i++) {
	    if (Mode == ColorMode.JP4) {
		Parent.WriteLogtoConsole(GetIP()[i] + ": Setting COLORMODE to JP4 RAW");
	    }
	    if (Mode == ColorMode.JP46) {
		Parent.WriteLogtoConsole(GetIP()[i] + ": Setting COLORMODE to JP46 RAW");
	    }
	    if (Mode == ColorMode.RGB) {
		Parent.WriteLogtoConsole(GetIP()[i] + ": Setting COLORMODE to RGB");
	    }
	    this.Colormode = Mode;
	    final int Tindex = i;
	    final ColorMode TMode = Mode;
	    new Thread() {

		public void run() {
		    SetParameter(Tindex, CameraParameter.COLORMODE, ColorModeTranslate(TMode));
		}
	    }.start();
	}
    }

    public ColorMode GetColorMode() {
	return this.Colormode;
    }

    public void SetPhotoresolution(PhotoResolution Res) {
	// This is not a parameter we need to send to the cameras as it
	// only comes in effect when actually sending the command to shoot a
	// photo
	Parent.WriteLogtoConsole("Setting Photoresolution to " + Res);
	this.Photoresolution = Res;
    }

    public PhotoResolution GetPhotoresolution() {
	return this.Photoresolution;
    }

    public void SetAllowCaptureStillWhileRecording(boolean newsetting) {
	// This is not a parameter we need to send to the cameras as it
	// only comes in effect when actually sending the command to shoot a
	// photo
	Parent.WriteLogtoConsole("Setting AllowCaptureStillWhileRecording to " + newsetting);
	this.AllowCaptureStillWhileRecording = newsetting;
    }

    public boolean GetAllowCaptureStillWhileRecording() {
	return this.AllowCaptureStillWhileRecording;
    }

    public void SetPhotoQuality(int newquality) {
	// This is not a parameter we need to send to the cameras as it
	// only comes in effect when actually sending the command to shoot a
	// photo
	Parent.WriteLogtoConsole("Setting PhotoQuality to " + newquality);
	this.PhotoQuality = newquality;
    }

    public int GetPhotoQuality() {
	return this.PhotoQuality;
    }

    public void SetPhotoColorMode(ColorMode Mode) {
	// This is not a parameter we need to send to the cameras as it
	// only comes in effect when actually sending the command to shoot a
	// photo
	Parent.WriteLogtoConsole("Setting PhotoColorMode to " + Mode);
	this.PhotoColormode = Mode;
    }

    public ColorMode GetPhotoColorMode() {
	return this.PhotoColormode;
    }

    public void SetWhiteBalance(WhiteBalance newbalance) {
	for (int i = 0; i < this.IP.length; i++) {
	    //Parent.WriteLogtoConsole(GetIP()[i] + ": Setting WhiteBalance to " + newbalance);

	    this.ImageWhiteBalance = newbalance;
	    float GainR, GainB, GainG, GainGB;
	    String Parameters = "";
	    switch (newbalance) {
	    case AUTO:
		Parameters = "WB_EN=1&framedelay=3";
		break;
	    case DAYLIGHT:
		WB_Factor_R = 1.5f; // TODO just estimated badly
		WB_Factor_G = 1.0f;
		WB_Factor_B = 1.6f; // TODO just estimated badly
		WB_Factor_GB = 1.0f;
		GainR = this.Gain[GainIndex] * 65536 * WB_Factor_R;
		GainG = this.Gain[GainIndex] * 65536 * WB_Factor_G;
		GainB = this.Gain[GainIndex] * 65536 * WB_Factor_B;
		GainGB = this.Gain[GainIndex] * 65536 * WB_Factor_GB;
		Parameters = "WB_EN=0&framedelay=3&GAINR=" + (int) GainR + "&GAING=" + (int) GainG + "&GAINB=" + (int) GainB + "&GAINGB="
			+ (int) GainGB;
		break;
	    case CLOUDY:
		WB_Factor_R = 1.4f; // TODO just estimated badly
		WB_Factor_G = 1.0f;
		WB_Factor_B = 1.4f; // TODO just estimated badly
		WB_Factor_GB = 1.0f;
		GainR = this.Gain[GainIndex] * 65536 * WB_Factor_R;
		GainG = this.Gain[GainIndex] * 65536 * WB_Factor_G;
		GainB = this.Gain[GainIndex] * 65536 * WB_Factor_B;
		GainGB = this.Gain[GainIndex] * 65536 * WB_Factor_GB;
		Parameters = "WB_EN=0&framedelay=3&GAINR=" + (int) GainR + "&GAING=" + (int) GainG + "&GAINB=" + (int) GainB + "&GAINGB="
			+ (int) GainGB;
		break;
	    case TUNGSTEN:
		WB_Factor_R = 1.1286f; // TODO just estimated badly
		WB_Factor_G = 1.0f;
		WB_Factor_B = 2.3317f; // TODO just estimated badly
		WB_Factor_GB = 1.0f;
		GainR = this.Gain[GainIndex] * 65536 * WB_Factor_R;
		GainG = this.Gain[GainIndex] * 65536 * WB_Factor_G;
		GainB = this.Gain[GainIndex] * 65536 * WB_Factor_B;
		GainGB = this.Gain[GainIndex] * 65536 * WB_Factor_GB;
		Parameters = "WB_EN=0&framedelay=3&GAINR=" + (int) GainR + "&GAING=" + (int) GainG + "&GAINB=" + (int) GainB + "&GAINGB="
			+ (int) GainGB;
		break;
	    case FLOURESCENT:
		WB_Factor_R = 1.2f; // TODO just estimated badly
		WB_Factor_G = 1.0f;
		WB_Factor_B = 1.5f; // TODO just estimated badly
		WB_Factor_GB = 1.0f;
		GainR = this.Gain[GainIndex] * 65536 * WB_Factor_R;
		GainG = this.Gain[GainIndex] * 65536 * WB_Factor_G;
		GainB = this.Gain[GainIndex] * 65536 * WB_Factor_B;
		GainGB = this.Gain[GainIndex] * 65536 * WB_Factor_GB;
		Parameters = "WB_EN=0&framedelay=3&GAINR=" + (int) GainR + "&GAING=" + (int) GainG + "&GAINB=" + (int) GainB + "&GAINGB="
			+ (int) GainGB;
		break;
	    case CUSTOM:
		// TODO
		// SendParametertoCamera("WB_EN=0");
		break;
	    }
	    SendParametertoCamera(i, Parameters);
	}
    }

    public WhiteBalance GetWhiteBalance() {
	return this.ImageWhiteBalance;
    }

    public void SetFPS(float fps) {
	this.FPS = fps;
	for (int i = 0; i < this.IP.length; i++) {
	    Parent.WriteLogtoConsole(GetIP()[i] + ": Setting FPS to " + fps);
	    this.SetParameter(i, CameraParameter.FPS, fps);
	}
    }

    public void SetGamma(float newgamma) {
	if (newgamma < 0) {
	    newgamma = 0;
	}
	if (newgamma > 1) {
	    newgamma = 1;
	}
	this.Gamma = newgamma;

	for (int i = 0; i < this.IP.length; i++) {
	    Parent.WriteLogtoConsole(this.IP[i] + ": Setting Gamma to " + newgamma);
	    this.SendCamVCParameters(i, "set=0/gam:" + this.Gamma + "/pxl:" + this.Blacklevel + "/");
	}
    }

    public void SetRecordFormat(RecordFormat newformat) {
	for (int i = 0; i < this.IP.length; i++) {
	    if (newformat == RecordFormat.JPEG) {
		Parent.WriteLogtoConsole(GetIP()[i] + ": Setting RecordFormat to " + newformat);
		this.ExecuteCommand(i, "SETCONTAINERFORMATJPEG");
	    }
	    if (newformat == RecordFormat.MOV) {
		Parent.WriteLogtoConsole(GetIP()[i] + ": Setting RecordFormat to " + newformat);
		this.ExecuteCommand(i, "SETCONTAINERFORMATQUICKTIME");
	    }

	    this.RecordFormat = newformat;
	}
    }

    public float GetFPS() {
	return this.FPS;
    }

    public int GetJPEGQuality() {
	return this.JPEGQual;
    }

    public int[][] GetHistogram(int color) {
	return Histogram;
    }

    public int[] GetGammaCurve() {
	return GammaCurve;
    }

    public void ReadHistogram() {
	// TODO
	// currently we are reading always from IP with Index 0
	// but how to deal with multiple histograms?
	// maybe make the one displayed switchable to select which camera the
	// data should come from?

	URLConnection conn = null;
	BufferedReader data = null;
	String line;
	String result = null;
	StringBuffer buf = new StringBuffer();
	URL HistURL = null;
	int parameter = 0;
	String camera = null;
	if (GetHistogramScaleMode() == HistogramScaleMode.LINEAR) {
	    camera = "http://" + this.IP[0] + "/ElphelVision/histogram.php?mode=linear&" + (int) (Math.random() * 32000); // to
															  // prevent
															  // reading
															  // a
															  // cached
															  // result
															  // we
															  // add
															  // a
															  // random
															  // number
															  // to
															  // the
															  // URL
	} else if (GetHistogramScaleMode() == HistogramScaleMode.LOG) {
	    camera = "http://" + this.IP[0] + "/ElphelVision/histogram.php?mode=log&" + (int) (Math.random() * 32000); // to
														       // prevent
														       // reading
														       // a
														       // cached
														       // result
														       // we
														       // add
														       // a
														       // random
														       // number
														       // to
														       // the
														       // URL
	}

	try {
	    HistURL = new URL(camera);
	} catch (MalformedURLException e) {
	    Parent.WriteErrortoConsole("Reading histogram data failed at URL: " + HistURL);
	}

	try {
	    conn = HistURL.openConnection();
	    conn.connect();

	    Parent.WriteLogtoConsole(this.IP[0] + ": ReadHistogram(): Reading histogram data");

	    data = new BufferedReader(new InputStreamReader(conn.getInputStream()));

	    buf.delete(0, buf.length());
	    while ((line = data.readLine()) != null) {
		buf.append(line + "\n");
	    }

	    result = buf.toString();
	    data.close();
	} catch (IOException e) {
	    Parent.WriteErrortoConsole("Reading histogram data IO Error:" + e.getMessage());
	}

	try {
	    String[] x = Pattern.compile(";").split(result);
	    int a = 0;

	    // RED
	    for (int j = 0; j < 256; j++) {
		Histogram[0][j] = (int) (Integer.parseInt(x[a++]));
		// System.out.println(j + " \"" + x[j] + "\"");
	    }
	    // GREEN
	    for (int j = 0; j < 256; j++) {
		Histogram[1][j] = (int) (Integer.parseInt(x[a++]));
		// System.out.println(j + " \"" + x[j] + "\"");
	    }
	    // BLUE
	    for (int j = 0; j < 256; j++) {
		Histogram[2][j] = (int) (Integer.parseInt(x[a++]));
		// System.out.println(j + " \"" + x[j] + "\"");
	    }
	    int b = 1;
	} catch (Exception e) {
	    Parent.WriteErrortoConsole("Reading histogram data IO Error:" + e.getMessage());
	}
    }

    public void SetHistogramScaleMode(HistogramScaleMode newmode) {
	HistogramScaleMode = newmode;
    }

    public HistogramScaleMode GetHistogramScaleMode() {
	return this.HistogramScaleMode;
    }

    public void SetHistogramColorMode(HistogramColorMode newmode) {
	HistogramColorMode = newmode;
    }

    public HistogramColorMode GetHistogramColorMode() {
	return this.HistogramColorMode;
    }

    public void ReadGammaCurve() {
	// TODO
	// currently we are reading always from IP with Index 0
	// but how to deal with multiple gamma curves?
	// maybe make the one displayed switchable to select which camera the
	// data should come from?

	URLConnection conn = null;
	BufferedReader data = null;
	String line;
	String result = null;
	StringBuffer buf = new StringBuffer();
	URL GammaURL = null;
	int parameter = 0;

	String camera = "http://" + this.IP[0] + "/ElphelVision/gamma.php?" + (int) (Math.random() * 32000);
	try {
	    GammaURL = new URL(camera);
	} catch (MalformedURLException e) {
	    Parent.WriteErrortoConsole("Reading gamma curve data failed at URL: " + GammaURL);
	}

	try {
	    conn = GammaURL.openConnection();
	    conn.connect();

	    Parent.WriteLogtoConsole(this.IP[0] + ": ReadGammaCurve(): Reading gamma curve data");

	    data = new BufferedReader(new InputStreamReader(conn.getInputStream()));

	    buf.delete(0, buf.length());
	    while ((line = data.readLine()) != null) {
		buf.append(line + "\n");
	    }

	    result = buf.toString();
	    data.close();
	} catch (IOException e) {
	    Parent.WriteErrortoConsole("Reading gamma curve data failed IO error:" + e.getMessage());
	}
	String[] x = Pattern.compile(";").split(result);
	int a = 0;

	// TODO GAMMA CURVE FOR EVERY CHANNEL
	for (int j = 0; j < 256; j++) {
	    GammaCurve[j] = (int) (Integer.parseInt(x[j]));
	}
    }

    public boolean InitCameraConnection() {
	boolean error = false;
	this.CameraUrl = new URL[this.IP.length];
	this.CameraPingUrl = new URL[this.IP.length];

	for (int i = 0; i < this.IP.length; i++) {

	    String camera_url = "http://" + this.IP[i] + "/ElphelVision/elphelvision_interface.php";
	    try {
		this.CameraUrl[i] = new URL(camera_url);
		error = true;
	    } catch (MalformedURLException e) {
		System.out.println("Bad URL: " + this.CameraUrl);
		error = false;
	    }

	    String Camera_Ping_Url = "http://" + this.IP[i] + "/ElphelVision/ping.php";
	    try {
		this.CameraPingUrl[i] = new URL(Camera_Ping_Url);
		error = true;
	    } catch (MalformedURLException e) {
		System.out.println("Bad URL: " + this.CameraPingUrl);
		error = false;
	    }
	    if (error) {
		this.ConnectionEstablished = true;
	    }
	}
	return error;
    }

    public String CreateStereo3DClipName() {
	String RetVal = null;
	if (this.IP.length > 1) {
	    int Index = 1;
	    while (true) {
		String result1 = SetRecordDirectory(0, "LEFT-CLIP" + String.format("%05d", Index));
		if (result1.equals("success")) {
		    String result2 = SetRecordDirectory(1, "RIGHT-CLIP" + String.format("%05d", Index));
		    if (result2.equals("success")) {
			RetVal = String.format("%05d", Index);
			break;
		    }
		} else {
		    Index++;
		}
	    }
	}
	return RetVal;
    }

    public String SetRecordDirectory(int CameraIndex, String directory) {
	String returnVal = null;
	URLConnection conn = null;
	BufferedReader data = null;
	String line;
	String result = null;
	StringBuilder buf = new StringBuilder();
	URL DirCameraUrl = null;
	String ReturnValue = null;

	String camera_url = "http://" + this.IP[CameraIndex] + "/ElphelVision/elphelvision_interface.php?cmd=setrecdir&directory=" + directory;
	try {
	    DirCameraUrl = new URL(camera_url);
	} catch (MalformedURLException e) {
	    System.out.println("Bad URL: " + DirCameraUrl);
	}
	try {
	    conn = DirCameraUrl.openConnection();
	    conn.connect();

	    data = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	    buf.delete(0, buf.length());
	    while ((line = data.readLine()) != null) {
		buf.append(line + "\n");
	    }

	    result = buf.toString();
	    data.close();

	    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	    DocumentBuilder db;
	    try {
		db = dbf.newDocumentBuilder();
		Document doc = null;

		try {
		    doc = db.parse(new ByteArrayInputStream(result.getBytes()));
		} catch (SAXException ex) {
		    Logger.getLogger(Camera.class.getName()).log(Level.SEVERE, null, ex);
		}

		doc.getDocumentElement().normalize();
		NodeList nodeLst = doc.getElementsByTagName("elphel_vision_data");
		Element fstElmnt = (Element) nodeLst.item(0);
		NodeList fstNmElmntLst = fstElmnt.getElementsByTagName("result");

		Element fstNmElmnt = (Element) fstNmElmntLst.item(0);
		NodeList fstNm = fstNmElmnt.getChildNodes();
		ReturnValue = ((Node) fstNm.item(0)).getNodeValue();
	    } catch (ParserConfigurationException ex) {
		Logger.getLogger(Camera.class.getName()).log(Level.SEVERE, null, ex);
	    }
	} catch (IOException ex) {
	    Logger.getLogger(Camera.class.getName()).log(Level.SEVERE, null, ex);
	}
	return ReturnValue;
    }

    public boolean InitCameraServices(int CameraIndex) {
	URLConnection conn = null;
	BufferedReader data = null;
	String line;
	String result;
	StringBuffer buf = new StringBuffer();
	boolean ReturnValue = false;

	try {
	    conn = this.CameraUrl[CameraIndex].openConnection();
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
		NodeList nodeLst = doc.getElementsByTagName("elphel_vision_data");
		for (int s = 0; s < nodeLst.getLength(); s++) {
		    Node fstNode = nodeLst.item(s);
		    if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
			Element fstElmnt = (Element) fstNode;
			NodeList fstNmElmntLst = fstElmnt.getElementsByTagName("camogm");

			Element fstNmElmnt = (Element) fstNmElmntLst.item(0);
			NodeList fstNm = fstNmElmnt.getChildNodes();
			if ((((Node) fstNm.item(0)).getNodeValue().compareTo("not running")) == 0) {
			    this.CAMOGMState = CamogmState.NOTRUNNING;
			} else {
			    NodeList NmElmntLst5 = fstElmnt.getElementsByTagName("camogm_state");
			    Element NmElmnt5 = (Element) NmElmntLst5.item(0);
			    NodeList Elmnt5 = NmElmnt5.getChildNodes();
			    if (((Node) Elmnt5.item(0)) != null) {
				if (((Node) Elmnt5.item(0)).getNodeValue().compareTo("stopped") != 0) {
				    this.CAMOGMState = CamogmState.STOPPED;
				}
				if (((Node) Elmnt5.item(0)).getNodeValue().compareTo("running") != 0) {
				    this.CAMOGMState = CamogmState.RECORDING;
				}
				NodeList NmElmntLst6 = fstElmnt.getElementsByTagName("hdd_freespaceratio");
				Element NmElmnt6 = (Element) NmElmntLst6.item(0);
				NodeList Elmnt6 = NmElmnt6.getChildNodes();
				if (((Node) Elmnt6.item(0)).getNodeValue().compareTo("unmounted") == 0) {
				    this.HDDState = HDDState.UNMOUNTED;
				} else {
				    this.HDDState = HDDState.MOUNTED;
				}
			    }
			}
		    }
		}
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	} catch (IOException e) {
	    Parent.WriteErrortoConsole("InitCameraServices: " + e.getMessage());
	}

	if (this.CAMOGMState == CamogmState.NOTRUNNING) {
	    this.ExecuteCommand(CameraIndex, "CAMOGMSTART");
	    ReturnValue = false;
	}
	if (this.HDDState == HDDState.UNMOUNTED) {
	    this.ExecuteCommand(CameraIndex, "MOUNTHDD");
	    this.ExecuteCommand(CameraIndex, "SETRECDIR");
	    this.ExecuteCommand(CameraIndex, "SETCONTAINERFORMATQUICKTIME");
	    ReturnValue = true; // ElphelVision should also work with cameras
				// without HDD
	} else {
	    ReturnValue = true;
	}
	return ReturnValue;
    }

    public boolean InitStereo3DSettings() {
	boolean error = false;
	URL[] CameraURL = new URL[this.IP.length];
	URLConnection conn = null;
	BufferedReader data = null;

	String camera_url1 = "http://" + this.IP[0] + "/ElphelVision/elphelvision_interface.php?cmd=init_stereo3d_master";
	try {
	    CameraURL[0] = new URL(camera_url1);
	    error = true;
	} catch (MalformedURLException e) {
	    System.out.println("Bad URL: " + this.CameraUrl);
	    error = false;
	}
	try {
	    conn = CameraURL[0].openConnection();
	    conn.connect();
	    Parent.WriteLogtoConsole(this.IP[0] + ": Setting Trigger Parameters");

	    data = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	    data.close();
	} catch (IOException e) {
	    Parent.WriteErrortoConsole("InitStereo3DSettings: " + e.getMessage());
	}

	String camera_url2 = "http://" + this.IP[1] + "/ElphelVision/elphelvision_interface.php?cmd=init_stereo3d_slave";
	try {
	    CameraURL[1] = new URL(camera_url2);
	    error = true;
	} catch (MalformedURLException e) {
	    System.out.println("Bad URL: " + this.CameraUrl);
	    error = false;
	}
	try {
	    conn = CameraURL[1].openConnection();
	    conn.connect();
	    Parent.WriteLogtoConsole(this.IP[1] + ": Setting Trigger Parameters");

	    data = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	    data.close();
	} catch (IOException e) {
	    Parent.WriteErrortoConsole("InitStereo3DSettings: " + e.getMessage());
	}
	return error;
    }

    public void SendCommandToCamera(int CameraIndex, String Command) {
	URL CameraURL = null;
	URLConnection conn = null;
	BufferedReader data = null;

	String camera_url = "http://" + this.IP[CameraIndex] + "/ElphelVision/elphelvision_interface.php?cmd=" + Command;
	try {
	    CameraURL = new URL(camera_url);
	} catch (MalformedURLException e) {
	    System.out.println("Bad URL: " + this.CameraUrl);
	}
	try {
	    conn = CameraURL.openConnection();
	    conn.connect();
	    Parent.WriteLogtoConsole(this.IP[CameraIndex] + ": Sending Command: " + Command);

	    data = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	    data.close();
	} catch (IOException e) {
	    Parent.WriteErrortoConsole("SendCommandToCamera(): " + e.getMessage());
	}
    }

    public void SetParameter(int CameraIPIndex, CameraParameter par, float value) {
	switch (par) {
	case EXPOSURE:
	    this.SendParameter(CameraIPIndex, CameraParameter.EXPOSURE, value);
	    break;
	case GAIN:
	    this.SendParameter(CameraIPIndex, CameraParameter.GAIN, value);
	    break;
	case AUTOEXP:
	    this.SendParameter(CameraIPIndex, CameraParameter.AUTOEXP, value);
	    break;
	case JPEGQUAL:
	    this.SendParameter(CameraIPIndex, CameraParameter.JPEGQUAL, value);
	    break;
	case COLORMODE:
	    this.SendParameter(CameraIPIndex, CameraParameter.COLORMODE, value);
	    break;
	case FPS:
	    this.SendParameter(CameraIPIndex, CameraParameter.FPS, value);
	    break;
	case SENSORWIDTH:
	    this.SendParameter(CameraIPIndex, CameraParameter.SENSORWIDTH, value);
	    break;
	case SENSORHEIGHT:
	    this.SendParameter(CameraIPIndex, CameraParameter.SENSORHEIGHT, value);
	    break;
	case RECORDFORMAT:
	    this.SendParameter(CameraIPIndex, CameraParameter.RECORDFORMAT, value);
	    break;
	}
    }

    public CameraPreset GetPreset() {
	return this.Preset;
    }

    public void SetPreset(CameraPreset preset) {
	this.Preset = preset;

	// Dont apply any settings if its a custom preset
	if (preset == CameraPreset.CUSTOM) {
	    return;
	}
	String param_url = "";
	try {

	    URLConnection conn = null;
	    BufferedReader data = null;
	    String line;

	    String result;

	    StringBuffer buf = new StringBuffer();
	    URL ParamURL = null;
	    int parameter = 0;
	    int woi_left = 0;
	    int woi_top = 0;
	    int width = 0;
	    int height = 0;
	    int fps = 0;
	    int binning = 0;

	    for (int i = 0; i < this.IP.length; i++) {
		Parent.WriteLogtoConsole(this.IP[i] + ": Setting ResolutionPreset to " + preset);

		if (Parent.GetNoCameraParameter()) {
		    return;
		}

		switch (preset) {
		case FULL:
		    // 2592x1936
		    woi_left = 0;
		    woi_top = 0;
		    width = 2592;
		    height = 1936;
		    binning = 1;
		    param_url = "http://" + this.IP[i] + "/ElphelVision/setparam.php?WOI_LEFT=" + woi_left + "&WOI_TOP=" + woi_top + "&WOI_WIDTH="
			    + width;
		    param_url += "&WOI_HEIGHT=" + height + "&DCM_HOR=" + binning + "&DCM_VERT=" + binning + "&BIN_HOR=" + binning + "&BIN_VERT="
			    + binning;
		    param_url += "&framedelay=3";
		    break;
		case AMAX:
		    // 2224x1251
		    woi_left = 184;
		    woi_top = 340;
		    width = 2224;
		    height = 1264;
		    binning = 1;
		    param_url = "http://" + this.IP[i] + "/ElphelVision/setparam.php?WOI_LEFT=" + woi_left + "&WOI_TOP=" + woi_top + "&WOI_WIDTH="
			    + width;
		    param_url += "&WOI_HEIGHT=" + height + "&DCM_HOR=" + binning + "&DCM_VERT=" + binning + "&BIN_HOR=" + binning + "&BIN_VERT="
			    + binning;
		    param_url += "&framedelay=3";
		    break;
		case CIMAX:
		    // 2592x1120
		    woi_left = 0;
		    woi_top = 416;
		    width = 2592;
		    height = 1120;
		    binning = 1;
		    param_url = "http://" + this.IP[i] + "/ElphelVision/setparam.php?WOI_LEFT=" + woi_left + "&WOI_TOP=" + woi_top + "&WOI_WIDTH="
			    + width;
		    param_url += "&WOI_HEIGHT=" + height + "&DCM_HOR=" + binning + "&DCM_VERT=" + binning + "&BIN_HOR=" + binning + "&BIN_VERT="
			    + binning;
		    param_url += "&framedelay=3";
		    break;
		case FULLHD:
		    woi_left = 336;
		    woi_top = 442;
		    width = 1920;
		    height = 1088;
		    binning = 1;
		    param_url = "http://" + this.IP[i] + "/ElphelVision/setparam.php?WOI_LEFT=" + woi_left + "&WOI_TOP=" + woi_top + "&WOI_WIDTH="
			    + width;
		    param_url += "&WOI_HEIGHT=" + height + "&DCM_HOR=" + binning + "&DCM_VERT=" + binning + "&BIN_HOR=" + binning + "&BIN_VERT="
			    + binning;
		    param_url += "&framedelay=3";
		    break;
		case SMALLHD:
		    woi_left = 656;
		    woi_top = 612;
		    width = 1280;
		    height = 720;
		    binning = 1;
		    param_url = "http://" + this.IP[i] + "/ElphelVision/setparam.php?WOI_LEFT=" + woi_left + "&WOI_TOP=" + woi_top + "&WOI_WIDTH="
			    + width;
		    param_url += "&WOI_HEIGHT=" + height + "&DCM_HOR=" + binning + "&DCM_VERT=" + binning + "&BIN_HOR=" + binning + "&BIN_VERT="
			    + binning;
		    param_url += "&framedelay=3";
		    break;
		case CUSTOM:
		    woi_left = 16;
		    woi_top = 252;
		    width = 2560;
		    height = 1440;
		    binning = 1;
		    param_url = "http://" + this.IP[i] + "/ElphelVision/setparam.php?WOI_LEFT=" + woi_left + "&WOI_TOP=" + woi_top + "&WOI_WIDTH="
			    + width;
		    param_url += "&WOI_HEIGHT=" + height + "&DCM_HOR=" + binning + "&DCM_VERT=" + binning + "&BIN_HOR=" + binning + "&BIN_VERT="
			    + binning;
		    param_url += "&framedelay=3";
		    break;
		}

		try {
		    ParamURL = new URL(param_url);
		} catch (MalformedURLException e) {
		    Parent.WriteErrortoConsole("SetPreset(" + param_url + ") Error: Bad URL: " + param_url);
		}

		conn = ParamURL.openConnection();
		conn.connect();

		data = new BufferedReader(new InputStreamReader(conn.getInputStream()));

		buf.delete(0, buf.length());
		while ((line = data.readLine()) != null) {
		    buf.append(line + "\n");
		}

		result = buf.toString();
		data.close();
	    }
	} catch (IOException e) {
	    Parent.WriteErrortoConsole("SetPreset(" + param_url + ") Error: " + e.getMessage());
	}
    }

    public boolean CheckHDD() {
	try {
	    UpdateCameraData();
	} catch (Exception ex) {
	    Logger.getLogger(Camera.class.getName()).log(Level.SEVERE, null, ex);
	}

	if (this.HDDSpaceFree == -1) {
	    String message = "No HDD detected, video recording disabled";
	    // JOptionPane.showMessageDialog(new JFrame(), message, "Dialog",
	    // JOptionPane.ERROR_MESSAGE);
	    return false;
	} else {
	    return true;
	}
    }

    public void WriteConfigFile(String FileName) throws IOException {
	File ConfigFile = new File(FileName);

	if (FileName == null) {
	    throw new IllegalArgumentException("File should not be null.");
	}

	// use buffering
	Writer output = new BufferedWriter(new FileWriter(ConfigFile));
	try {
	    // FileWriter always assumes default encoding is OK!
	    String line = "";
	    line += "IP=" + this.GetIP()[0] + "\n";
	    if (this.GetIP().length == 2) {
		line += "IP2=" + this.GetIP()[1] + "\n";
	    }
	    line += "ImageWidth=" + Integer.toString(this.GetImageWidth()) + "\n";
	    line += "ImageHeight=" + Integer.toString(this.GetImageHeight()) + "\n";
	    line += "Preset=";
	    if (this.GetPreset() == Preset.FULL) {
		line += "FULL";
	    }
	    if (this.GetPreset() == Preset.AMAX) {
		line += "AMAX";
	    }
	    if (this.GetPreset() == Preset.CIMAX) {
		line += "CIMAX";
	    }
	    if (this.GetPreset() == Preset.FULLHD) {
		line += "FULLHD";
	    }
	    if (this.GetPreset() == Preset.SMALLHD) {
		line += "SMALLHD";
	    }
	    line += "\n";
	    line += "FPS=" + Float.toString(this.GetFPS()) + "\n";
	    line += "JPEGQuality=" + Integer.toString(this.GetJPEGQuality()) + "\n";
	    line += "ColorMode=";
	    if (this.GetColorMode() == ColorMode.RGB) {
		line += "RGB";
	    } else if (this.GetColorMode() == ColorMode.JP4) {
		line += "JP4";
	    }
	    line += "\n";
	    line += "ExposureIndex=" + Integer.toString(this.GetExposureIndex()) + "\n";
	    line += "AutoExposure=" + Boolean.toString(this.GetAutoExposure()) + "\n";
	    line += "RecordFormat=" + this.GetRecordFormat() + "\n";
	    line += "GainIndex=" + Integer.toString(this.GetGainIndex()) + "\n";
	    line += "WB=";
	    if (this.GetWhiteBalance() == WhiteBalance.AUTO) {
		line += "AUTO";
	    } else if (this.GetWhiteBalance() == WhiteBalance.CLOUDY) {
		line += "CLOUDY";
	    } else if (this.GetWhiteBalance() == WhiteBalance.CUSTOM) {
		line += "CUSTOM";
	    } else if (this.GetWhiteBalance() == WhiteBalance.DAYLIGHT) {
		line += "DAYLIGHT";
	    } else if (this.GetWhiteBalance() == WhiteBalance.FLOURESCENT) {
		line += "FLOURESCENT";
	    } else if (this.GetWhiteBalance() == WhiteBalance.TUNGSTEN) {
		line += "TUNGSTEN";
	    }
	    line += "\n";
	    line += "WB_Factor_R=" + Float.toString(this.WB_Factor_R) + "\n";
	    line += "WB_Factor_G=" + Float.toString(this.WB_Factor_G) + "\n";
	    line += "WB_Factor_B=" + Float.toString(this.WB_Factor_B) + "\n";
	    line += "WB_Factor_GB=" + Float.toString(this.WB_Factor_GB) + "\n";
	    line += "ImageOrientation=";
	    if (this.GetImageOrientation() == ImageOrientation.LANDSCAPE) {
		line += "LANDSCAPE";
	    } else if (this.GetImageOrientation() == ImageOrientation.PORTRAIT) {
		line += "PORTRAIT";
	    }
	    line += "\n";
	    line += "ImageFlip=";
	    if (this.GetImageFlipMode() == MirrorImage.NONE) {
		line += "NONE";
	    } else if (this.GetImageFlipMode() == MirrorImage.HORIZONTAL) {
		line += "HORIZONTAL";
	    } else if (this.GetImageFlipMode() == MirrorImage.VERTICAL) {
		line += "VERTICAL";
	    } else if (this.GetImageFlipMode() == MirrorImage.VERTICALHORIZONTAL) {
		line += "VERTICALHORIZONTAL";
	    }
	    line += "\n";
	    line += "RecordFormat=";
	    if (this.GetRecordFormat() == RecordFormat.JPEG) {
		line += "JPEG";
	    } else if (this.GetRecordFormat() == RecordFormat.MOV) {
		line += "MOV";
	    } else if (this.GetRecordFormat() == RecordFormat.OGM) {
		line += "OGM";
	    }
	    line += "\n";
	    line += "Gamma=" + Float.toString(this.GetGamma()) + "\n";
	    line += "Gammapreset=";
	    if (this.GetGammaPreset() == GammaPreset.LINEAR) {
		line += "LINEAR";
	    } else if (this.GetGammaPreset() == GammaPreset.CINE1) {
		line += "CINE1";
	    } else if (this.GetGammaPreset() == GammaPreset.CINE2) {
		line += "CINE2";
	    } else if (this.GetGammaPreset() == GammaPreset.CINES) {
		line += "CINES";
	    }
	    line += "\n";
	    line += "Blacklevel=" + Integer.toString(this.GetBlacklevel()) + "\n";
	    line += "PhotoColorMode=";
	    if (this.GetPhotoColorMode() == ColorMode.JP4) {
		line += "JP4";
	    } else if (this.GetPhotoColorMode() == ColorMode.RGB) {
		line += "RBG";
	    }
	    line += "\n";
	    line += "PhotoResolution=";
	    if (this.GetPhotoresolution() == PhotoResolution.ASVIDEO) {
		line += "ASVIDEO";
	    } else if (this.GetPhotoresolution() == PhotoResolution.FULL) {
		line += "FULL";
	    }
	    line += "\n";
	    line += "PhotoQuality=" + Integer.toString(this.GetPhotoQuality()) + "\n";
	    line += "AllowCaptureStillWhileRecording=" + Boolean.toString(this.GetAllowCaptureStillWhileRecording()) + "\n";
	    line += "GuidesCenterX=" + Boolean.toString(this.GetGuides()[0]) + "\n";
	    line += "GuidesOuterX=" + Boolean.toString(this.GetGuides()[1]) + "\n";
	    line += "GuidesThirds=" + Boolean.toString(this.GetGuides()[2]) + "\n";
	    line += "GuidesSafeArea=" + Boolean.toString(this.GetGuides()[3]) + "\n";
	    line += "CoringIndexY=" + this.GetCoringIndexY() + "\n";
	    line += "CoringIndexC=" + this.GetCoringIndexC() + "\n";
	    line += "FrameSkip=" + this.GetFPSSkipFrames() + "\n";
	    line += "SecondsSkip=" + this.GetFPSSkipSeconds() + "\n";
	    line += "AllowSlowShutter=" + this.GetAllowSlowShutter() + "\n";
	    line += "MultiCameraRecordingStartDelay=" + Float.toString(this.GetMultiCameraRecordingStartDelay()) + "\n";
	    // line += "DisableLiveVideo=" +
	    // Boolean.toString(Parent.Settings.isVideoStreamEnabled()) + "\n";
	    line += "MovieMaxChunkSize=" + Integer.toString(this.GetMovieClipMaxChunkSize()) + "\n";
	    line += "MovieMaxChunkDuration=" + Integer.toString(getMovieClipMaxChunkDuration()) + "\n";
	    line += "MovieMaxChunkFrames=" + Integer.toString(getMovieClipMaxChunkFrames()) + "\n";
	    if (getSingleCameraName().equals("")) {
		line += "SingleCameraName=%null\n"; // we need to do this as an
						    // empty string after the
						    // "=" will break the file
						    // reader
	    } else {
		line += "SingleCameraName=" + getSingleCameraName() + "\n";
	    }
	    if (getStereoCameraNames()[0].equals("")) {
		line += "MasterCameraName=%null\n"; // we need to do this as an
						    // empty string after the
						    // "=" will break the file
						    // reader
	    } else {
		line += "MasterCameraName=" + getStereoCameraNames()[0] + "\n";
	    }
	    if (getStereoCameraNames()[1].equals("")) {
		line += "SlaveCameraName=%null\n"; // we need to do this as an
						   // empty string after the "="
						   // will break the file reader
	    } else {
		line += "SlaveCameraName=" + getStereoCameraNames()[1] + "\n";
	    }

	    output.write(line);
	} finally {
	    output.close();
	}
    }

    public ArrayList ReadConfigFileIP(String FileName) throws FileNotFoundException {
	File ConfigFile = new File(FileName);

	if (!ConfigFile.exists()) {
	    return null;
	}

	ArrayList RetValue = new ArrayList();

	Scanner scanner1 = new Scanner(ConfigFile);
	try {
	    // first use a Scanner to get each line
	    while (scanner1.hasNextLine()) {
		// use a second Scanner to parse the content of each line
		Scanner scanner2 = new Scanner(scanner1.nextLine());
		scanner2.useDelimiter("=");
		if (scanner2.hasNext()) {
		    String name = scanner2.next();
		    if (!scanner2.hasNext()) {
			break;
		    }
		    String value = scanner2.next();
		    if (name.trim().equals("IP")) {
			RetValue.add(new String(value.trim()));
		    }
		    if (name.trim().equals("IP2")) {
			RetValue.add(new String(value.trim()));
		    }
		} else {
		    // Empty or invalid line. Unable to process
		}
		scanner2.close();
	    }
	} finally {
	    // ensure the underlying stream is always closed
	    scanner1.close();
	}

	return RetValue;
    }

    public void ReadConfigFile(String FileName) throws FileNotFoundException {
	File ConfigFile = new File(FileName);

	Scanner scanner1 = new Scanner(ConfigFile);
	try {
	    // first use a Scanner to get each line
	    while (scanner1.hasNextLine()) {
		// use a second Scanner to parse the content of each line
		Scanner scanner2 = new Scanner(scanner1.nextLine());
		scanner2.useDelimiter("=");
		if (scanner2.hasNext()) {
		    String name = scanner2.next();
		    if (!scanner2.hasNext()) {
			break;
		    }
		    String value = scanner2.next();

		    if (name.trim().equals("ImageWidth")) {
			this.ImageWidth = Integer.parseInt(value.trim());
		    }
		    if (name.trim().equals("ImageHeight")) {
			this.ImageHeight = Integer.parseInt(value.trim());
		    }
		    if (name.trim().equals("JPEGQuality")) {
			this.SetJPEGQuality(Integer.parseInt(value.trim()));
		    }
		    if (name.trim().equals("FPS")) {
			this.SetFPS(Float.parseFloat(value.trim()));
		    }
		    if (name.trim().equals("Preset")) {
			if (value.trim().contentEquals("AMAX")) {
			    this.SetPreset(Preset.AMAX);
			}
			if (value.trim().contentEquals("CIMAX")) {
			    this.SetPreset(Preset.CIMAX);
			}
			if (value.trim().contentEquals("FULLHD")) {
			    this.SetPreset(Preset.FULLHD);
			}
			if (value.trim().contentEquals("SMALLHD")) {
			    this.SetPreset(Preset.SMALLHD);
			}
			if (value.trim().contentEquals("CUSTOM")) {
			    this.SetPreset(Preset.CUSTOM);
			}
		    }
		    if (name.trim().equals("ColorMode")) {
			if (value.trim().contentEquals("RGB")) {
			    this.SetColorMode(ColorMode.RGB);
			}
			if ((value.trim()).contentEquals("JP4")) {
			    this.SetColorMode(ColorMode.JP4);
			}
		    }
		    if (name.trim().equals("ExposureIndex")) {
			this.SetExposureIndex(Integer.parseInt(value.trim()));
		    }

		    if ((name.trim()).equals("RecordFormat")) {
			if (value.trim().contentEquals("MOV")) {
			    this.SetRecordFormat(RecordFormat.MOV);
			}
			if (value.trim().contentEquals("OGM")) {
			    this.SetRecordFormat(RecordFormat.OGM);
			}
			if (value.trim().contentEquals("JPEG")) {
			    this.SetRecordFormat(RecordFormat.JPEG);
			}
		    }
		    if ((name.trim()).equals("WB")) {
			if (value.trim().contentEquals("AUTO")) {
			    this.SetWhiteBalance(WhiteBalance.AUTO);
			}
			if (value.trim().contentEquals("CLOUDY")) {
			    this.SetWhiteBalance(WhiteBalance.CLOUDY);
			}
			if (value.trim().contentEquals("CUSTOM")) {
			    this.SetWhiteBalance(WhiteBalance.CUSTOM);
			}
			if (value.trim().contentEquals("DAYLIGHT")) {
			    this.SetWhiteBalance(WhiteBalance.DAYLIGHT);
			}
			if (value.trim().contentEquals("FLOURESCENT")) {
			    this.SetWhiteBalance(WhiteBalance.FLOURESCENT);
			}
			if (value.trim().contentEquals("TUNGSTEN")) {
			    this.SetWhiteBalance(WhiteBalance.TUNGSTEN);
			}
		    }
		    if ((name.trim()).equals("ImageOrientation")) {
			if (value.trim().contentEquals("LANDSCAPE")) {
			    this.SetImageOrientation(ImageOrientation.LANDSCAPE);
			}
			if (value.trim().contentEquals("PORTRAIT")) {
			    this.SetImageOrientation(ImageOrientation.PORTRAIT);
			}
		    }
		    if ((name.trim()).equals("ImageFlip")) {
			if (value.trim().contentEquals("NONE")) {
			    this.SetImageFlipMode(MirrorImage.NONE);
			}
			if (value.trim().contentEquals("HORIZONTAL")) {
			    this.SetImageFlipMode(MirrorImage.HORIZONTAL);
			}
			if (value.trim().contentEquals("VERTICAL")) {
			    this.SetImageFlipMode(MirrorImage.VERTICAL);
			}
			if (value.trim().contentEquals("VERTICALHORIZONTAL")) {
			    this.SetImageFlipMode(MirrorImage.VERTICALHORIZONTAL);
			}
		    }
		    if (name.trim().equals("GainIndex")) {
			this.SetGainIndex(Integer.parseInt(value.trim()));
		    }
		    if (name.trim().equals("WB_Factor_R")) {
			this.WB_Factor_R = (Float.parseFloat(value.trim()));
		    }
		    if (name.trim().equals("WB_Factor_G")) {
			this.WB_Factor_G = (Float.parseFloat(value.trim()));
		    }
		    if (name.trim().equals("WB_Factor_B")) {
			this.WB_Factor_B = (Float.parseFloat(value.trim()));
		    }
		    if (name.trim().equals("WB_Factor_GB")) {
			this.WB_Factor_GB = (Float.parseFloat(value.trim()));
		    }
		    if (name.trim().equals("Gamma")) {
			this.SetGamma(Float.parseFloat(value.trim()));
		    }
		    if (name.trim().equals("Blacklevel")) {
			this.SetBlacklevel(Integer.parseInt(value.trim()));
		    }
		    if (name.trim().equals("MovieMaxChunkSize")) {
			this.SetMovieClipMaxChunkSize(Integer.parseInt(value.trim()));
		    }
		    if (name.trim().equals("MovieMaxChunkDuration")) {
			this.setMovieClipMaxChunkDuration(Integer.parseInt(value.trim()));
		    }
		    if (name.trim().equals("MovieMaxChunkFrames")) {
			this.setMovieClipMaxChunkFrames(Integer.parseInt(value.trim()));
		    }
		    if (name.trim().equals("PhotoColorMode")) {
			if (value.trim().contentEquals("JP4")) {
			    this.SetPhotoColorMode(ColorMode.JP4);
			}
			if (value.trim().contentEquals("RGB")) {
			    this.SetPhotoColorMode(Colormode.RGB);
			}
		    }
		    if (name.trim().equals("PhotoResolution")) {
			if (value.trim().contentEquals("FULL")) {
			    this.SetPhotoresolution(PhotoResolution.FULL);
			}
			if (value.trim().contentEquals("ASVIDEO")) {
			    this.SetPhotoresolution(PhotoResolution.ASVIDEO);
			}
		    }
		    if (name.trim().equals("PhotoQuality")) {
			this.SetPhotoQuality(Integer.parseInt(value.trim()));
		    }
		    if (name.trim().equals("AllowCaptureStillWhileRecording")) {
			if (value.trim().contentEquals("true")) {
			    this.SetAllowCaptureStillWhileRecording(true);
			}
			if (value.trim().contentEquals("false")) {
			    this.SetAllowCaptureStillWhileRecording(false);
			}
		    }
		    if (name.trim().equals("GuidesCenterX")) {
			if (value.trim().contentEquals("true")) {
			    this.SetGuide("GuideDrawCenterX", true);
			}
			if (value.trim().contentEquals("false")) {
			    this.SetGuide("GuideDrawCenterX", false);
			}
		    }
		    if (name.trim().equals("GuidesOuterX")) {
			if (value.trim().contentEquals("true")) {
			    this.SetGuide("GuideDrawOuterX", true);
			}
			if (value.trim().contentEquals("false")) {
			    this.SetGuide("GuideDrawOuterX", false);
			}
		    }
		    if (name.trim().equals("GuidesThirds")) {
			if (value.trim().contentEquals("true")) {
			    this.SetGuide("GuideDrawThirds", true);
			}
			if (value.trim().contentEquals("false")) {
			    this.SetGuide("GuideDrawThirds", false);
			}
		    }
		    if (name.trim().equals("GuidesSafeArea")) {
			if (value.trim().contentEquals("true")) {
			    this.SetGuide("GuideDrawSafeArea", true);
			}
			if (value.trim().contentEquals("false")) {
			    this.SetGuide("GuideDrawSafeArea", false);
			}
		    }
		    if (name.trim().equals("CoringIndex")) {
			// this.SetCoringIndex(Integer.parseInt(value.trim()));
			// TODO
		    }
		    if (name.trim().equals("FrameSkip")) {
			this.SetFPSSkipFrames(Integer.parseInt(value.trim()));
		    }
		    if (name.trim().equals("SecondsSkip")) {
			this.SetFPSSkipSeconds(Integer.parseInt(value.trim()));
		    }
		    if (name.trim().equals("AllowSlowShutter")) {
			if (value.trim().contentEquals("true")) {
			    this.SetAllowSlowShutter(true);
			}
			if (value.trim().contentEquals("false")) {
			    this.SetAllowSlowShutter(false);
			}
		    }
		    if (name.trim().equals("MultiCameraRecordingStartDelay")) {
			this.MultiCameraRecordingStartDelay = (Float.parseFloat(value.trim()));
		    }
		    if (name.trim().equals("SingleCameraName")) {
			if ("%null".equals(value.trim())) {
			    setSingleCameraName("");
			} else {
			    setSingleCameraName(value.trim());
			}
		    }
		    if (name.trim().equals("MasterCameraName")) {
			if ("%null".equals(value.trim())) {
			    setStereoCameraName("", 0);
			} else {
			    setStereoCameraName(value.trim(), 0);
			}
		    }
		    if (name.trim().equals("SlaveCameraName")) {
			if ("%null".equals(value.trim())) {
			    setStereoCameraName("", 1);
			} else {
			    setStereoCameraName(value.trim(), 1);
			}
		    }
		} else {
		    // Empty or invalid line. Unable to process
		}
		scanner2.close();
	    }
	} finally {
	    // ensure the underlying stream is always closed
	    scanner1.close();
	}
    }

    private void SendParameter(int CameraIPIndex, CameraParameter par, float value) {
	if (Parent.GetNoCameraParameter()) {
	    return;
	}
	String param_url = "";
	try {
	    URLConnection conn = null;
	    BufferedReader data = null;
	    String line;

	    String result;

	    StringBuffer buf = new StringBuffer();
	    URL ParamURL = null;
	    int parameter = 0;
	    int margin = 0;

	    switch (par) {
	    case EXPOSURE:
		parameter = (int) (value * 1000000);
		param_url = "http://" + this.IP[CameraIPIndex] + "/ElphelVision/setparam.php?framedelay=1&EXPOS=" + parameter;
		break;

	    case AUTOEXP:
		parameter = (int) value;
		param_url = "http://" + this.IP[CameraIPIndex] + "/ElphelVision/setparam.php?framedelay=1&AUTOEXP_ON=" + parameter;
		break;

	    case JPEGQUAL:
		parameter = (int) value;
		param_url = "http://" + this.IP[CameraIPIndex] + "/ElphelVision/setparam.php?framedelay=3&QUALITY=" + parameter;
		break;

	    case COLORMODE:
		parameter = (int) value;
		param_url = "http://" + this.IP[CameraIPIndex] + "/ElphelVision/setparam.php?framedelay=3&COLOR=" + parameter;
		break;

	    case FPS:
		if (this.FrameTrigger == Trigger.FREERUNNING) {
		    float fps_parameter = value * 1000;
		    param_url = "http://" + this.IP[CameraIPIndex] + "/ElphelVision/setparam.php?framedelay=3&FPSFLAGS=1&FP1000SLIM=" + fps_parameter;
		} else if (this.FrameTrigger == Trigger.TRIGGERED) {
		    float fps_parameter = value * 1000;
		    int trig_period = (int) (96000000 / value);
		    param_url = "http://" + this.IP[CameraIPIndex] + "/ElphelVision/setparam.php?framedelay=3&FPSFLAGS=0&FP1000SLIM=" + fps_parameter
			    + "&TRIG_PERIOD=" + trig_period;
		}
		break;

	    case SENSORHEIGHT:
		margin = Math.round(1936 / 2 - value / 2);
		param_url = "http://" + this.IP[CameraIPIndex] + "/ElphelVision/setparam.php?WOI_HEIGHT=" + value + "WOI_TOP=" + margin;
		break;

	    case SENSORWIDTH:
		margin = Math.round(2592 / 2 - value / 2);
		param_url = "http://" + this.IP[CameraIPIndex] + "/ElphelVision/setparam.php?WOI_WIDTH=" + value + "WOI_LEFT=" + margin;
		break;
	    }
	    try {
		ParamURL = new URL(param_url);
	    } catch (MalformedURLException e) {
		System.out.println("Bad URL: " + param_url);
	    }

	    conn = ParamURL.openConnection();
	    conn.connect();

	    data = new BufferedReader(new InputStreamReader(conn.getInputStream()));

	    buf.delete(0, buf.length());
	    while ((line = data.readLine()) != null) {
		buf.append(line + "\n");
	    }

	    result = buf.toString();
	    data.close();
	} catch (IOException e) {
	    Parent.WriteErrortoConsole("SendParameter(" + param_url + ") IO Error: " + e.getMessage());
	}
    }

    private void SendCamVCParameters(int CameraIPIndex, String UrlParameter) {
	if (Parent.GetNoCameraParameter()) {
	    return;
	}
	String param_url = "";
	try {

	    URLConnection conn = null;
	    BufferedReader data = null;
	    String line;

	    String result;

	    StringBuffer buf = new StringBuffer();
	    URL ParamURL = null;
	    int parameter = 0;
	    int margin = 0;

	    param_url = "http://" + this.IP[CameraIPIndex] + "/camvc.php?" + UrlParameter;

	    try {
		ParamURL = new URL(param_url);
	    } catch (MalformedURLException e) {
		System.out.println("Bad URL: " + param_url);
	    }

	    conn = ParamURL.openConnection();
	    conn.connect();

	    data = new BufferedReader(new InputStreamReader(conn.getInputStream()));

	    buf.delete(0, buf.length());
	    while ((line = data.readLine()) != null) {
		buf.append(line + "\n");
	    }
	    result = buf.toString();
	    data.close();

	} catch (IOException e) {
	    Parent.WriteErrortoConsole("SendCamVCParameters(" + param_url + ") IO Error:" + e.getMessage());
	}
    }

    private void SendParametertoCamera(int CameraIPIndex, String UrlParameter) {
	if (Parent.GetNoCameraParameter()) {
	    return;
	}

	try {
	    String param_url = "";
	    URLConnection conn = null;
	    BufferedReader data = null;
	    String line;

	    String result;

	    StringBuffer buf = new StringBuffer();
	    URL ParamURL = null;
	    int parameter = 0;
	    int margin = 0;

	    param_url = "http://" + this.IP[CameraIPIndex] + "/ElphelVision/setparam.php?" + UrlParameter;

	    try {
		ParamURL = new URL(param_url);
	    } catch (MalformedURLException e) {
		Parent.WriteErrortoConsole("SendParametertoCamera Bad URL: " + param_url);
	    }

	    conn = ParamURL.openConnection();
	    conn.connect();

	    data = new BufferedReader(new InputStreamReader(conn.getInputStream()));

	    buf.delete(0, buf.length());
	    while ((line = data.readLine()) != null) {
		buf.append(line + "\n");
	    }

	    result = buf.toString();
	    data.close();
	} catch (IOException e) {
	    Parent.WriteErrortoConsole("SendParametertoCamera IO Error: " + e.getMessage());
	}
    }

    public String CaptureStillImage(String UrlParameter) {
	if (Parent.GetNoCameraParameter()) {
	    return "";
	}
	String ReturnValue = "";

	try {
	    String param_url = "";
	    URLConnection conn = null;
	    BufferedReader data = null;
	    String line;
	    String result;

	    StringBuffer buf = new StringBuffer();
	    URL ParamURL = null;

	    param_url = "http://" + this.IP + "/ElphelVision/savestill.php?" + UrlParameter;

	    try {
		ParamURL = new URL(param_url);
	    } catch (MalformedURLException e) {
		System.out.println("Bad URL: " + param_url);
	    }

	    conn = ParamURL.openConnection();
	    conn.connect();

	    data = new BufferedReader(new InputStreamReader(conn.getInputStream()));

	    buf.delete(0, buf.length());
	    while ((line = data.readLine()) != null) {
		buf.append(line + "\n");
	    }

	    result = buf.toString();
	    data.close();

	    // try to extract data from XML structure
	    /*
	     * <SaveStill> <path>/var/hdd/stills/still_00034.jpg</path>
	     * <size>50390</size> <save_duration>0.343< </save_duration>
	     * </SaveStill>
	     */
	    try {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();

		Document doc = db.parse(new ByteArrayInputStream(result.getBytes()));
		doc.getDocumentElement().normalize();
		NodeList nodeLst = doc.getElementsByTagName("SaveStill");
		for (int s = 0; s < nodeLst.getLength(); s++) {
		    Node fstNode = nodeLst.item(s);
		    if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
			Element fstElmnt = (Element) fstNode;
			NodeList fstNmElmntLst = fstElmnt.getElementsByTagName("path");

			Element fstNmElmnt = (Element) fstNmElmntLst.item(0);
			NodeList fstNm = fstNmElmnt.getChildNodes();
			String response = ((Node) fstNm.item(0)).getNodeValue();

			ReturnValue = "saved " + response;
		    }
		}
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	} catch (IOException e) {
	    Parent.WriteErrortoConsole("CaptureStillImage IO Error: " + e.getMessage());
	}
	return ReturnValue;
    }

    private int ColorModeTranslate(ColorMode mode) {
	int colormode = 1;
	switch (mode) {
	case RGB:
	    colormode = 1;
	    break;

	case JP4:
	    colormode = 5;
	    break;

	case JP46:
	    colormode = 2;
	    break;

	}
	return colormode;
    }

    public void ExecuteCommand(int CameraIPIndex, String Command) {
	if (Parent.GetNoCameraParameter()) {
	    return;
	}

	ExecuteCommand(CameraIPIndex, Command, "");
    }

    public double GetCameraTime(int CameraIPIndex) {
	if (Parent.GetNoCameraParameter()) {
	    return 0;
	}

	URLConnection conn = null;
	BufferedReader data = null;
	String line;
	String result;
	StringBuffer buf = new StringBuffer();
	URL CommandURL = null;
	double Time = 0;

	// try to connect
	try {
	    String command_url = "http://" + this.IP[CameraIPIndex] + "/camogmgui/camogm_interface.php?cmd=gettime";
	    try {
		CommandURL = new URL(command_url);
	    } catch (MalformedURLException e) {
		System.out.println("Bad URL: " + command_url);
	    }
	    conn = CommandURL.openConnection();
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
		NodeList nodeLst = doc.getElementsByTagName("camogm_interface");
		for (int s = 0; s < nodeLst.getLength(); s++) {
		    Node fstNode = nodeLst.item(s);
		    if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
			Element fstElmnt = (Element) fstNode;
			NodeList fstNmElmntLst = fstElmnt.getElementsByTagName("gettime");

			Element fstNmElmnt = (Element) fstNmElmntLst.item(0);
			NodeList fstNm = fstNmElmnt.getChildNodes();
			String response = ((Node) fstNm.item(0)).getNodeValue();
			Time = Double.parseDouble(response.replace(".", "")); // we
									      // get
									      // rid
									      // of
									      // the
									      // comma
									      // and
									      // by
									      // doing
									      // that
									      // actually
									      // mulitply
									      // with
									      // 10000
		    }
		}
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	} catch (IOException e) {
	    Parent.WriteErrortoConsole("Getting Camera Time IO Error: " + e.getMessage());
	}
	return Time;
    }

    public void StartRecording() {
	Calendar currentDate = Calendar.getInstance();
	SimpleDateFormat formatter = new SimpleDateFormat("yy-MM-dd_HH-mm-ss");
	String foldername = formatter.format(currentDate.getTime());

	Thread[] t = new Thread[GetIP().length];
	for (int j = 0; j < GetIP().length; j++) {
	    final int index = j;

	    // Append Camera Name if it was set
	    if (GetIP().length > 1) { // Stereo 3D mode
		if ((StereoCameraName[j] != null) && (!"".equals(StereoCameraName[j]))) {
		    foldername = foldername + "_" + this.getStereoCameraNames()[j];
		}
	    } else { // single camera mode
		if ((this.getSingleCameraName() != null) && (!"".equals(this.getSingleCameraName()))) {
		    foldername = foldername + "_" + this.getSingleCameraName();
		}
	    }

	    final String Foldername = foldername;
	    t[j] = new Thread() {

		@Override
		public void run() {
		    SendCommandToCamera(index, "camogmstartrecording&foldername=" + Foldername);
		    Parent.WriteLogtoConsole(GetIP()[index] + ": Setting Record Directory to: " + Foldername);
		    Parent.WriteLogtoConsole(GetIP()[index] + ": Recording started");
		}
	    };
	    t[j].setPriority(Thread.MAX_PRIORITY);
	    t[j].start();
	}
	Calendar now = Calendar.getInstance();
	RecordstartTime = now.getTimeInMillis();
    }

    public void StopRecording() {
	Thread[] t = new Thread[GetIP().length];
	for (int j = 0; j < GetIP().length; j++) {
	    final int index = j;
	    t[j] = new Thread() {

		@Override
		public void run() {
		    SendCommandToCamera(index, "camogmstoprecording");
		    Parent.WriteLogtoConsole(GetIP()[index] + ": Recording Stopped");
		}
	    };
	    t[j].setPriority(Thread.MAX_PRIORITY);
	    t[j].start();
	}
    }

    public void ArmRecording() {
	Calendar currentDate = Calendar.getInstance();
	SimpleDateFormat formatter = new SimpleDateFormat("yy-MM-dd_HH-mm-ss");
	String foldername = "";

	Thread[] t = new Thread[GetIP().length];
	for (int j = 0; j < GetIP().length; j++) {
	    foldername = formatter.format(currentDate.getTime());
	    // Append Camera Name if it was set
	    if (GetIP().length > 1) { // Stereo 3D mode
		if ((StereoCameraName[j] != null) && (!"".equals(StereoCameraName[j]))) {
		    foldername = foldername + "_" + this.getStereoCameraNames()[j];
		}
	    } else { // single camera mode
		if ((this.getSingleCameraName() != null) && (!"".equals(this.getSingleCameraName()))) {
		    foldername = foldername + "_" + this.getSingleCameraName();
		}
	    }

	    final String Foldername = foldername;
	    final int index = j;
	    t[j] = new Thread() {

		@Override
		public void run() {
		    // Parent.Camera.SendCommandToCamera(index,
		    // "camogmstartrecording");
		    SendCommandToCamera(index, "camogmstartrecording&foldername=" + Foldername);
		    Parent.WriteLogtoConsole(GetIP()[index] + ": Setting Record Directory to: " + Foldername);
		    Parent.WriteLogtoConsole(GetIP()[index] + ": Recording Armed");
		}
	    };
	    t[j].setPriority(Thread.MAX_PRIORITY);
	    t[j].start();
	}

	Calendar now = Calendar.getInstance();
	RecordstartTime = now.getTimeInMillis();

    }

    public void StartRecording(String Starttime) {
	Thread[] t = new Thread[GetIP().length];
	for (int j = 0; j < GetIP().length; j++) {

	    final int index = j;
	    final String Parameter = Starttime;
	    t[j] = new Thread() {

		@Override
		public void run() {
		    ExecuteCommand(index, "RECORDSTARTTIMESTAMP", Parameter);
		}
	    };
	    t[j].setPriority(Thread.MAX_PRIORITY);
	    t[j].start();
	}
	// recording does not really start right now but since we do not know
	// when it really will...
	Calendar now = Calendar.getInstance();
	RecordstartTime = now.getTimeInMillis();
    }

    public void ExecuteCommand(int CameraIPIndex, String Command, String parameter) {
	if (Parent.GetNoCameraParameter()) {
	    return;
	}

	URLConnection conn = null;
	BufferedReader data = null;
	String line;

	String result;

	StringBuffer buf = new StringBuffer();
	URL CommandURL = null;
	String command_name = "";

	if (Command.equals("CAMOGMSTART")) {
	    command_name = "run_camogm";
	} else if (Command.equals("RECORDSTART")) {
	    // old: dont use this anymore
	    Parent.WriteWarningtoConsole("Outdated function call: ExecuteCommand(RECORDSTART)");
	    Parent.WriteLogtoConsole(this.IP[CameraIPIndex] + ": Recording started");
	    command_name = "start";
	    Calendar now = Calendar.getInstance();
	    RecordstartTime = now.getTimeInMillis();
	} else if (Command.equals("RECORDSTARTARM")) {
	    // old: dont use this anymore
	    Parent.WriteWarningtoConsole("Outdated function call: ExecuteCommand(RECORDSTARTARM)");
	    Parent.WriteLogtoConsole(this.IP[CameraIPIndex] + ": Recording armed - will start at target time");
	    command_name = "start";
	    Calendar now = Calendar.getInstance();
	    RecordstartTime = now.getTimeInMillis();
	} else if (Command.equals("RECORDSTARTTIMESTAMP")) {
	    Parent.WriteLogtoConsole(this.IP[CameraIPIndex] + ": Recording will start at: " + parameter + " second(s)");
	    command_name = "set_start_after_timestamp&start_after_timestamp=" + parameter;
	    Calendar now = Calendar.getInstance();
	    RecordstartTime = now.getTimeInMillis();
	} else if (Command.equals("RECORDSTOP")) {
	    // old: dont use this anymore
	    Parent.WriteWarningtoConsole("Outdated function call: ExecuteCommand(RECORDSTOP)");
	    Parent.WriteLogtoConsole(this.IP[CameraIPIndex] + ": Recording Stopped");
	    command_name = "stop";
	} else if (Command.equals("MOUNTHDD")) {
	    command_name = "mount";
	} else if (Command.equals("SETRECDIR")) {
	    command_name = "set_prefix&prefix=/var/hdd/";
	} else if (Command.equals("SETCONTAINERFORMATQUICKTIME")) {
	    command_name = "setmov";
	} else if (Command.equals("SETCONTAINERFORMATJPEG")) {
	    command_name = "setjpeg";
	} else if (Command.equals("SETSKIPFRAMES")) {
	    command_name = "set_frameskip&frameskip=" + parameter;
	} else if (Command.equals("SETSKIPSECONDS")) {
	    command_name = "set_timelapse&timelapse=" + parameter;
	} else {
	    command_name = Command;
	}

	// try to connect
	try {
	    String command_url = "http://" + this.IP[CameraIPIndex] + "/camogmgui/camogm_interface.php?cmd=" + command_name;
	    try {
		CommandURL = new URL(command_url);
	    } catch (MalformedURLException e) {
		System.out.println("Bad URL: " + command_url);
	    }

	    conn = CommandURL.openConnection();
	    conn.connect();

	    data = new BufferedReader(new InputStreamReader(conn.getInputStream()));

	    buf.delete(0, buf.length());
	    while ((line = data.readLine()) != null) {
		buf.append(line + "\n");
	    }

	    result = buf.toString();
	    data.close();
	} catch (IOException e) {
	    Parent.WriteErrortoConsole("ExecuteCommand(" + Command + ") IO Error: " + e.getMessage());
	}
    }

   

    public void UpdateCameraData() throws Exception {
	if (Parent.GetNoCameraParameter()) {
	    return;
	}
	URLConnection conn = null;
	BufferedReader data = null;
	String line;

	String result;

	StringBuffer buf = new StringBuffer();

	// try to connect
	// for (int i = 0; i < this.IP.length; i++) { //just get all data from
	// camera with IP Index 0 for now -> TODO
	int i = 0;
	try {
	    conn = CameraUrl[i].openConnection();
	    conn.connect();

	    Parent.WriteLogtoConsole(this.IP[0] + ": UpdateCameraData(): Reading various camera data");

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
		NodeList nodeLst = doc.getElementsByTagName("elphel_vision_data");
		for (int s = 0; s < nodeLst.getLength(); s++) {
		    Node fstNode = nodeLst.item(s);
		    if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
			Element fstElmnt = (Element) fstNode;
			NodeList fstNmElmntLst = fstElmnt.getElementsByTagName("image_width");
			Element fstNmElmnt = (Element) fstNmElmntLst.item(0);
			NodeList fstNm = fstNmElmnt.getChildNodes();
			this.ImageWidth = Integer.parseInt(((Node) fstNm.item(0)).getNodeValue());

			NodeList lstNmElmntLst = fstElmnt.getElementsByTagName("image_height");
			Element lstNmElmnt = (Element) lstNmElmntLst.item(0);
			NodeList lstNm = lstNmElmnt.getChildNodes();
			this.ImageHeight = Integer.parseInt(((Node) lstNm.item(0)).getNodeValue());

			NodeList ElmntLstWOITop = fstElmnt.getElementsByTagName("WOI_top");
			if (ElmntLstWOITop.getLength() > 0) {
			    Element lstElmntWOITop = (Element) ElmntLstWOITop.item(0);
			    NodeList lstNWOITop = lstElmntWOITop.getChildNodes();
			    this.ImageWOITop[0] = Integer.parseInt(((Node) lstNWOITop.item(0)).getNodeValue());
			    // TODO currently we only get the WOI offset from
			    // camera 0 but not from any other camera
			}

			NodeList ElmntLstWOILeft = fstElmnt.getElementsByTagName("WOI_left");
			if (ElmntLstWOILeft.getLength() > 0) {
			    Element lstElmntWOILeft = (Element) ElmntLstWOILeft.item(0);
			    NodeList lstNWOILeft = lstElmntWOILeft.getChildNodes();
			    this.ImageWOILeft[0] = Integer.parseInt(((Node) lstNWOILeft.item(0)).getNodeValue());
			    // TODO currently we only get the WOI offset from
			    // camera 0 but not from any other camera
			}

			NodeList nxtNmElmntLst = fstElmnt.getElementsByTagName("fps");
			Element nxtNmElmnt = (Element) nxtNmElmntLst.item(0);
			NodeList nxtNm = nxtNmElmnt.getChildNodes();
			this.FPS = Float.parseFloat(((Node) nxtNm.item(0)).getNodeValue()) / 1000.0f;

			NodeList NmElmntLst4 = fstElmnt.getElementsByTagName("jpeg_quality");
			Element NmElmnt4 = (Element) NmElmntLst4.item(0);
			NodeList Elmnt4 = NmElmnt4.getChildNodes();
			this.JPEGQuality = Integer.parseInt(((Node) Elmnt4.item(0)).getNodeValue());

			NodeList fstNmElmntLst1 = fstElmnt.getElementsByTagName("camogm");
			Element fstNmElmnt1 = (Element) fstNmElmntLst1.item(0);
			NodeList fstNm1 = fstNmElmnt1.getChildNodes();
			if ((((Node) fstNm.item(0)).getNodeValue().compareTo("not running")) == 0) {
			    this.CAMOGMState = CamogmState.NOTRUNNING;
			} else {
			    NodeList NmElmntLst11 = fstElmnt.getElementsByTagName("camogm_state");
			    Element NmElmnt11 = (Element) NmElmntLst11.item(0);
			    NodeList Elmnt11 = NmElmnt11.getChildNodes();
			    if (((Node) Elmnt11.item(0)) != null) {
				if (((Node) Elmnt11.item(0)).getNodeValue().startsWith("stopped")) {
				    this.CAMOGMState = CamogmState.STOPPED;
				}

				if (((Node) Elmnt11.item(0)).getNodeValue().startsWith("running")) {
				    this.CAMOGMState = CamogmState.RECORDING;
				}

			    }
			}

			NodeList NmElmntLst5 = fstElmnt.getElementsByTagName("camogm_format");
			Element NmElmnt5 = (Element) NmElmntLst5.item(0);
			NodeList Elmnt5 = NmElmnt5.getChildNodes();
			if (((Node) Elmnt5.item(0)) != null) {
			    String formatname = ((Node) Elmnt5.item(0)).getNodeValue();
			    if (formatname.startsWith("mov")) {
				this.RecordFormat = RecordFormat.MOV;
			    } else if (formatname.startsWith("ogm")) {
				this.RecordFormat = RecordFormat.OGM;
			    } else if (formatname.startsWith("jpeg")) {
				this.RecordFormat = RecordFormat.JPEG;
			    }

			}

			NodeList NmElmntLst6 = fstElmnt.getElementsByTagName("hdd_freespaceratio");
			Element NmElmnt6 = (Element) NmElmntLst6.item(0);
			NodeList Elmnt6 = NmElmnt6.getChildNodes();
			if (((Node) Elmnt6.item(0)).getNodeValue().startsWith("unmounted")) {
			    this.HDDSpaceFreeRatio = -1;
			} else {
			    try {
				this.HDDSpaceFreeRatio = Float.parseFloat(((Node) Elmnt6.item(0)).getNodeValue());
			    } catch (Exception e) {
				this.HDDSpaceFreeRatio = -1;
			    }

			}

			NodeList NmElmntLstHDD = fstElmnt.getElementsByTagName("hdd_freespace");
			Element NmElmntHDD = (Element) NmElmntLstHDD.item(0);

			if (NmElmntHDD == null) { // if we get nothing returned
			    this.HDDSpaceFree = -1;
			} else {
			    NodeList ElmntHDD = NmElmntHDD.getChildNodes();
			    if (((Node) Elmnt6.item(0)).getNodeValue().startsWith("unmounted")) {
				this.HDDSpaceFree = -1;
			    } else {
				if ((((Node) ElmntHDD.item(0)).getNodeValue() != "") && (((Node) ElmntHDD.item(0)).getNodeValue() != "0")) {
				    try {
					this.HDDSpaceFree = Float.parseFloat(((Node) ElmntHDD.item(0)).getNodeValue());
				    } catch (Exception e) {
					this.HDDSpaceFree = -1;
				    }
				}
			    }
			}

			NodeList NmElmntLst7 = fstElmnt.getElementsByTagName("camogm_fileframeduration");
			Element NmElmnt7 = (Element) NmElmntLst7.item(0);
			NodeList Elmnt7 = NmElmnt7.getChildNodes();
			if (((Node) Elmnt7.item(0)) != null) {
			    this.RecordedFrames = Integer.parseInt(((Node) Elmnt7.item(0)).getNodeValue());
			}

			NodeList NmElmntLstDatarate = fstElmnt.getElementsByTagName("camogm_datarate");
			Element NmElmntDatarate = (Element) NmElmntLstDatarate.item(0);
			NodeList ElmntDatarate = NmElmntDatarate.getChildNodes();
			if (((Node) ElmntDatarate.item(0)) != null) {
			    this.Datarate = Float.parseFloat(((Node) ElmntDatarate.item(0)).getNodeValue());
			}

			NodeList NmElmntLstGainR = fstElmnt.getElementsByTagName("gain_r");
			Element NmElmntGainR = (Element) NmElmntLstGainR.item(0);
			NodeList ElmntGainR = NmElmntGainR.getChildNodes();
			if (((Node) ElmntGainR.item(0)) != null) {
			    this.Gain_R = Integer.parseInt(((Node) ElmntGainR.item(0)).getNodeValue()) / 65536.0f;
			}

			NodeList NmElmntLstGainB = fstElmnt.getElementsByTagName("gain_b");
			Element NmElmntGainB = (Element) NmElmntLstGainB.item(0);
			NodeList ElmntGainB = NmElmntGainB.getChildNodes();
			if (((Node) ElmntGainB.item(0)) != null) {
			    this.Gain_B = Integer.parseInt(((Node) ElmntGainB.item(0)).getNodeValue()) / 65536.0f;
			}

			NodeList NmElmntLstGainG = fstElmnt.getElementsByTagName("gain_g");
			Element NmElmntGainG = (Element) NmElmntLstGainG.item(0);
			NodeList ElmntGainG = NmElmntGainG.getChildNodes();
			if (((Node) ElmntGainG.item(0)) != null) {
			    this.Gain_G = Integer.parseInt(((Node) ElmntGainG.item(0)).getNodeValue()) / 65536.0f;
			    if (this.Gain_G == 1.0f) {
				this.GainIndex = 4;
			    }

			    if (this.Gain_G == 2.0f) {
				this.GainIndex = 3;
			    }

			    if (this.Gain_G == 4.0f) {
				this.GainIndex = 2;
			    }

			    if (this.Gain_G == 8.0f) {
				this.GainIndex = 1;
			    }

			    if (this.Gain_G == 16.0f) {
				this.GainIndex = 0;
			    }
			}

			NodeList NmElmntLstGainGB = fstElmnt.getElementsByTagName("gain_gb");
			Element NmElmntGainGB = (Element) NmElmntLstGainGB.item(0);
			NodeList ElmntGainGB = NmElmntGainGB.getChildNodes();
			if (((Node) ElmntGainGB.item(0)) != null) {
			    this.Gain_GB = Integer.parseInt(((Node) ElmntGainGB.item(0)).getNodeValue()) / 65536.0f;
			}

			NodeList NmElmntLstCamOGMMaxDuration = fstElmnt.getElementsByTagName("camogm_max_duration"); // seconds
			Element NmElmntCamOGMMaxDuration = (Element) NmElmntLstCamOGMMaxDuration.item(0);
			NodeList ElmntCamOGMMaxDuration = NmElmntCamOGMMaxDuration.getChildNodes();
			if (((Node) ElmntCamOGMMaxDuration.item(0)) != null) {
			    this.MovieClipMaxChunkDuration = Integer.parseInt(((Node) ElmntCamOGMMaxDuration.item(0)).getNodeValue());
			}

			NodeList NmElmntLstCamOGMMaxLength = fstElmnt.getElementsByTagName("camogm_max_length"); // bytes
			Element NmElmntCamOGMMaxLength = (Element) NmElmntLstCamOGMMaxLength.item(0);
			NodeList ElmntCamOGMMaxLength = NmElmntCamOGMMaxLength.getChildNodes();
			if (((Node) ElmntCamOGMMaxLength.item(0)) != null) {
			    this.MovieClipMaxChunkSize = Integer.parseInt(((Node) ElmntCamOGMMaxLength.item(0)).getNodeValue()) / 1024 / 1024;
			}

			NodeList NmElmntLstCamOGMMaxFrames = fstElmnt.getElementsByTagName("camogm_max_frames"); // frames
			Element NmElmntCamOGMMaxFrames = (Element) NmElmntLstCamOGMMaxFrames.item(0);
			NodeList ElmntCamOGMMaxFrames = NmElmntCamOGMMaxFrames.getChildNodes();
			if (((Node) ElmntCamOGMMaxFrames.item(0)) != null) {
			    this.MovieClipMaxChunkFrames = Integer.parseInt(((Node) ElmntCamOGMMaxFrames.item(0)).getNodeValue());
			}

			NodeList NmElmntLst9 = fstElmnt.getElementsByTagName("exposure");
			Element NmElmnt9 = (Element) NmElmntLst9.item(0);
			NodeList Elmnt9 = NmElmnt9.getChildNodes();
			if (((Node) Elmnt9.item(0)) != null) {
			    // Todo
			    // Integer.parseInt(((Node)
			    // Elmnt9.item(0)).getNodeValue());
			}

			NodeList NmElmntLstauto_exposure = fstElmnt.getElementsByTagName("auto_exposure");
			Element NmElmntauto_exposure = (Element) NmElmntLstauto_exposure.item(0);
			NodeList Elmntauto_exposure = NmElmntauto_exposure.getChildNodes();
			if (((Node) Elmntauto_exposure.item(0)) != null) {
			    if (((Node) Elmntauto_exposure.item(0)).getNodeValue().equals("1")) {
				this.AutoExposure = true;
			    } else {
				this.AutoExposure = false;
			    }
			}

			NodeList NmElmntLstCoringC = fstElmnt.getElementsByTagName("coringindex");
			Element NmElmntCoringC = (Element) NmElmntLstCoringC.item(0);
			NodeList ElmntCoringC = NmElmntCoringC.getChildNodes();
			if (((Node) ElmntCoringC.item(0)) != null) {
			    int temp = Integer.parseInt(((Node) ElmntCoringC.item(0)).getNodeValue());

			    // Coring = 0x10000*Coring_C + Coring_Y, where
			    // Coring_C/Y = 0...100 decimal.

			    String temp2 = Integer.toHexString(temp);
			    this.CoringIndexC = Integer.parseInt(temp2.substring(0, temp2.length() - 2), 16) / 0x100;
			    this.CoringIndexY = Integer.parseInt(temp2.substring(temp2.length() - 2, temp2.length()), 16);
			}

			NodeList NmElmntLstFrameSkip = fstElmnt.getElementsByTagName("camogm_frameskip");
			Element NmElmntFrameSkip = (Element) NmElmntLstFrameSkip.item(0);
			NodeList ElmntFrameSkip = NmElmntFrameSkip.getChildNodes();
			if (((Node) ElmntFrameSkip.item(0)) != null) {
			    this.FPSSkipFrames = Integer.parseInt(((Node) ElmntFrameSkip.item(0)).getNodeValue());
			}

			NodeList NmElmntLstSecondsSkip = fstElmnt.getElementsByTagName("camogm_secondsskip");
			Element NmElmntSecondsSkip = (Element) NmElmntLstSecondsSkip.item(0);
			NodeList ElmntSecondsSkip = NmElmntSecondsSkip.getChildNodes();
			if (((Node) ElmntSecondsSkip.item(0)) != null) {
			    this.FPSSkipSeconds = Integer.parseInt(((Node) ElmntSecondsSkip.item(0)).getNodeValue());
			}

			NodeList NmElmntLstFlipH = fstElmnt.getElementsByTagName("fliph");
			Element NmElmntFlipH = (Element) NmElmntLstFlipH.item(0);
			NodeList ElmntFlipH = NmElmntFlipH.getChildNodes();
			NodeList NmElmntLstFlipV = fstElmnt.getElementsByTagName("flipv");
			Element NmElmntFlipV = (Element) NmElmntLstFlipV.item(0);
			NodeList ElmntFlipV = NmElmntFlipV.getChildNodes();
			if (((Node) ElmntFlipV.item(0)) != null) {
			    int flipv = Integer.parseInt(((Node) ElmntFlipV.item(0)).getNodeValue());
			    int fliph = Integer.parseInt(((Node) ElmntFlipH.item(0)).getNodeValue());
			    if ((flipv == 1) && (fliph == 1)) {
				this.ImageFlip = MirrorImage.VERTICALHORIZONTAL;
			    }

			    if ((flipv == 1) && (fliph == 0)) {
				this.ImageFlip = MirrorImage.VERTICAL;
			    }

			    if ((flipv == 0) && (fliph == 1)) {
				this.ImageFlip = MirrorImage.HORIZONTAL;
			    }

			    if ((flipv == 0) && (fliph == 0)) {
				this.ImageFlip = MirrorImage.NONE;
			    }
			}

			NodeList NmElmntLstBufferFree = fstElmnt.getElementsByTagName("bufferfree");
			if (NmElmntLstBufferFree.getLength() > 0) {
			    Element NmElmntBufferFree = (Element) NmElmntLstBufferFree.item(0);
			    NodeList ElmntBufferFree = NmElmntBufferFree.getChildNodes();
			    if (((Node) ElmntBufferFree.item(0)) != null) {
				int tempvalue = Integer.parseInt(((Node) ElmntBufferFree.item(0)).getNodeValue());
				this.Bufferfree = tempvalue;
			    }
			}

			NodeList NmElmntLstBufferUsed = fstElmnt.getElementsByTagName("bufferused");
			if (NmElmntLstBufferUsed.getLength() > 0) {
			    Element NmElmntBufferUsed = (Element) NmElmntLstBufferUsed.item(0);
			    NodeList ElmntBufferUsed = NmElmntBufferUsed.getChildNodes();
			    if (((Node) ElmntBufferUsed.item(0)) != null) {
				int tempvalue = Integer.parseInt(((Node) ElmntBufferUsed.item(0)).getNodeValue());
				this.Bufferused = tempvalue;
			    }
			}

			NodeList NmElmntLstTrigger = fstElmnt.getElementsByTagName("trigger");
			if (NmElmntLstTrigger.getLength() > 0) {
			    Element NmElmntTrigger = (Element) NmElmntLstTrigger.item(0);
			    NodeList ElmntTrigger = NmElmntTrigger.getChildNodes();
			    if (((Node) ElmntTrigger.item(0)) != null) {
				int trigger = Integer.parseInt(((Node) ElmntTrigger.item(0)).getNodeValue());
				if (trigger == 0) {
				    this.FrameTrigger = Trigger.FREERUNNING;
				} else if (trigger == 4) {
				    this.FrameTrigger = Trigger.TRIGGERED;
				}
			    }
			}

			NodeList NmElmntLstTriggerPeriod = fstElmnt.getElementsByTagName("trigger_period");
			if (NmElmntLstTriggerPeriod.getLength() > 0) {
			    Element NmElmntTriggerPeriod = (Element) NmElmntLstTriggerPeriod.item(0);
			    NodeList ElmntTriggerPeriod = NmElmntTriggerPeriod.getChildNodes();
			    if (((Node) ElmntTriggerPeriod.item(0)) != null) {
				this.TriggerPeriod = Integer.parseInt(((Node) ElmntTriggerPeriod.item(0)).getNodeValue());
			    }
			}

			NodeList NmElmntLstTriggerCondition = fstElmnt.getElementsByTagName("trigger_condition");
			if (NmElmntLstTriggerCondition.getLength() > 0) {
			    Element NmElmntTriggerCondition = (Element) NmElmntLstTriggerCondition.item(0);
			    NodeList ElmntTriggerCondition = NmElmntTriggerCondition.getChildNodes();
			    if (((Node) ElmntTriggerCondition.item(0)) != null) {
				this.TriggerCondition = Integer.parseInt(((Node) ElmntTriggerCondition.item(0)).getNodeValue());
			    }
			}

			NodeList NmElmntLstTriggerOut = fstElmnt.getElementsByTagName("trigger_out");
			if (NmElmntLstTriggerOut.getLength() > 0) {
			    Element NmElmntTriggerOut = (Element) NmElmntLstTriggerOut.item(0);
			    NodeList ElmntTriggerOut = NmElmntTriggerOut.getChildNodes();
			    if (((Node) ElmntTriggerOut.item(0)) != null) {
				this.TriggerOut = Integer.parseInt(((Node) ElmntTriggerOut.item(0)).getNodeValue());
			    }
			}

			NodeList NmElmntLstRecordDirectory = fstElmnt.getElementsByTagName("record_directory");
			if (NmElmntLstRecordDirectory.getLength() > 0) {
			    Element NmElmntRecordDirectory = (Element) NmElmntLstRecordDirectory.item(0);
			    NodeList ElmntRecordDirectory = NmElmntRecordDirectory.getChildNodes();
			    if (((Node) ElmntRecordDirectory.item(0)) != null) {
				String tempvalue = ((Node) (ElmntRecordDirectory.item(0))).getNodeValue() + "";
				this.RecordPath = tempvalue;
				String[] parts = tempvalue.split("/");
				this.RecordClipName = parts[parts.length - 1];
			    }
			}
		    }
		}
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	} catch (IOException e) {
	    Parent.WriteErrortoConsole("UpdateCameraData() IO Error:" + e.getMessage());
	}
    }

    public Trigger getFrameTrigger() {
	return FrameTrigger;
    }

    public void setFrameTrigger(Trigger newFrameTrigger) {
	this.FrameTrigger = newFrameTrigger;
	for (int i = 0; i < this.IP.length; i++) {
	    Parent.WriteLogtoConsole(GetIP()[i] + ": Setting FrameTrigger to " + newFrameTrigger);
	    if (FrameTrigger == Trigger.FREERUNNING) {
		this.SendParametertoCamera(i, "TRIG=0");
	    } else if (FrameTrigger == Trigger.TRIGGERED) {
		this.SendParametertoCamera(i, "TRIG=4");
	    }
	}
    }

    public int getTriggerPeriod() {
	return TriggerPeriod;
    }

    /**
     * @param TRIG_PERIOD
     *            = 96MHz / FPS
     */
    public void setTriggerPeriod(int TriggerPeriod) {
	this.TriggerPeriod = TriggerPeriod;
	for (int i = 0; i < this.IP.length; i++) {
	    Parent.WriteLogtoConsole(GetIP()[i] + ": Setting TriggerPeriod to " + TriggerPeriod);
	    this.SendParametertoCamera(i, "TRIG_PERIOD=" + TriggerPeriod);
	}
    }

    public int getTriggerOut() {
	return TriggerOut;
    }

    public void setTriggerOut(int TriggerOut) {
	this.TriggerOut = TriggerOut;
	for (int i = 0; i < this.IP.length; i++) {
	    Parent.WriteLogtoConsole(GetIP()[i] + ": Setting TriggerOut to " + TriggerOut);
	    this.SendParametertoCamera(i, "TRIG_OUT=" + TriggerOut);
	}
    }

    public int getTriggerCondition() {
	return TriggerCondition;
    }

    public void setTriggerCondition(int TriggerCondition) {
	this.TriggerCondition = TriggerCondition;
	for (int i = 0; i < this.IP.length; i++) {
	    Parent.WriteLogtoConsole(GetIP()[i] + ": Setting TriggerCondition to " + TriggerCondition);
	    this.SendParametertoCamera(i, "TRIG_CONDITION=" + TriggerCondition);
	}
    }

    public String getRecordPath() {
	return RecordPath;
    }

    public String getRecordClipName() {
	return RecordClipName;
    }

    public int getMovieClipMaxChunkDuration() {
	return MovieClipMaxChunkDuration;
    }

    public int getMovieClipMaxChunkFrames() {
	return MovieClipMaxChunkFrames;
    }

    public void setMovieClipMaxChunkDuration(int MovieClipMaxChunkDuration) {
	for (int i = 0; i < this.IP.length; i++) {
	    Parent.WriteLogtoConsole(GetIP()[i] + ": Setting MovieClipMaxChunkDuration to " + MovieClipMaxChunkDuration);
	    this.ExecuteCommand(i, "set_duration&duration=" + MovieClipMaxChunkDuration);
	}
    }

    public void setMovieClipMaxChunkFrames(int MovieClipMaxChunkFrames) {
	for (int i = 0; i < this.IP.length; i++) {
	    Parent.WriteLogtoConsole(GetIP()[i] + ": Setting MovieClipMaxChunkFrames to " + MovieClipMaxChunkFrames);
	    this.ExecuteCommand(i, "set_max_frames&max_frames=" + MovieClipMaxChunkFrames);
	}
    }

    public String[] getStereoCameraNames() {
	return StereoCameraName;
    }

    public void setStereoCameraNames(String[] CameraName) {
	this.StereoCameraName = CameraName;
    }

    public void setStereoCameraName(String CameraName, int Index) {
	this.StereoCameraName[Index] = CameraName;
	Parent.WriteLogtoConsole("Setting StereoCameraName[" + Index + "] to " + CameraName);
    }

    public void setSingleCameraName(String CameraName) {
	this.SingleCameraName = CameraName;
	Parent.WriteLogtoConsole("Setting SingleCameraName to " + CameraName);
    }

    public String getSingleCameraName() {
	return this.SingleCameraName;
    }

    public int getBufferfree() {
	return this.Bufferfree;
    }

    public void setBufferfree(int Bufferfree) {
	this.Bufferfree = Bufferfree;
    }

    public int getBufferused() {
	return this.Bufferused;
    }

    public void setBufferused(int Bufferused) {
	this.Bufferused = Bufferused;
    }

    public int getImageWOILeft(int cameraindex) {
	return ImageWOILeft[cameraindex];
    }

    public void setImageWOILeft(int cameraindex, int ImageWOILeft) {
	this.ImageWOILeft[cameraindex] = ImageWOILeft;

	Parent.WriteLogtoConsole(GetIP()[cameraindex] + ": Setting Image WOI Shift Distance Left to " + ImageWOILeft);
	this.SendParametertoCamera(cameraindex, "WOI_LEFT=" + ImageWOILeft);
    }

    public int getImageWOITop(int cameraindex) {
	return ImageWOITop[cameraindex];
    }

    public void setImageWOITop(int cameraindex, int ImageWOITop) {
	this.ImageWOITop[cameraindex] = ImageWOITop;

	Parent.WriteLogtoConsole(GetIP()[cameraindex] + ": Setting Image WOI Shift Distance Top to " + ImageWOITop);
	this.SendParametertoCamera(cameraindex, "WOI_TOP=" + ImageWOITop);
    }

    public void SetStereo3DHIT(int shift) {
	// in pixels from center position
	// positive values mean the images are shifted "towards" each other

	this.Stereo3DHit = shift;

	// Left camera shifts right 50% of the value
	this.setImageWOILeft(0, 1296 + shift / 2 - (this.GetImageWidth() / 2));

	// right camera shifts left 50% of the value
	if (GetIP().length > 1) {
	    this.setImageWOILeft(1, 1296 - shift / 2 - (this.GetImageWidth() / 2));
	}
	Parent.WriteLogtoConsole("Setting Stereo3D HIT to " + shift);
    }

    public int GetStereo3DHIT() {
	// in pixels from center position
	// positive values mean the images are shifted "towards" each other

	return this.Stereo3DHit;
    }
}
