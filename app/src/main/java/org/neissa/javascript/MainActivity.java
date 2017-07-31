package org.neissa.javascript;

import android.app.*;
import android.os.*;
import java.io.*;
import android.text.*;

public class MainActivity extends Activity 
{
	public static String filename = "user.js";
	public static android.webkit.WebView js;
	public static String lastjavascript = "";
	public static String[] lines;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

		((android.widget.TextView) findViewById(R.id.output)).setMovementMethod(new android.text.method.ScrollingMovementMethod());

		try
		{
			java.io.FileInputStream fileinput = openFileInput(filename);
			byte[] data = new byte[fileinput.available()];
			fileinput.read(data);
			((android.widget.EditText) findViewById(R.id.staticinput)).setText(new String(data));
		}
		catch (Exception e)
		{
		}

		js = new android.webkit.WebView(this);
		js.getSettings().setJavaScriptEnabled(true);
		js.evaluateJavascript("(function(){window.errors=[];window.onerror=function(e){window.errors.push(e);};})()", null);

		((android.widget.EditText) findViewById(R.id.staticinput)).addTextChangedListener(new android.text.TextWatcher() {
				public void afterTextChanged(android.text.Editable s)
				{
				}
				public void beforeTextChanged(CharSequence s, int start, int count, int after)
				{
				}
				public void onTextChanged(CharSequence s, int start, int before, int count)
				{
					savestaticinput();
					generateoutput();
				}
			});
		((android.widget.EditText) findViewById(R.id.input)).addTextChangedListener(new android.text.TextWatcher() {
				public void afterTextChanged(android.text.Editable s)
				{
				}
				public void beforeTextChanged(CharSequence s, int start, int count, int after)
				{
				}
				public void onTextChanged(CharSequence s, int start, int before, int count)
				{
					generateoutput();
				}
			});
	}
	public void savestaticinput()
	{
		String staticjavascript = ((android.widget.EditText) findViewById(R.id.staticinput)).getText().toString();
		try
		{
			java.io.FileOutputStream fileoutput =openFileOutput(filename, android.content.Context.MODE_PRIVATE);
			fileoutput.write(staticjavascript.getBytes());
			fileoutput.close();
		}
		catch (Exception e)
		{
		}
	}
	public void generateoutput()
	{
		String staticjavascript = ((android.widget.EditText) findViewById(R.id.staticinput)).getText().toString();
		String javascript = "(function(){var results={};" + staticjavascript.split("\0")[0] + "\n";
		lines = ((android.widget.EditText)findViewById(R.id.input)).getText().toString().split("\n");
		for (int i=0; i < lines.length; i++)
		{
			if (lines[i].trim().length() == 0)
				continue;
			javascript += "results[" + i + "] = JSON.stringify(" + lines[i] + ");";
		}
		javascript += "return results;})();";
		if(javascript == lastjavascript)
			return;
		lastjavascript = javascript;
		//android.util.Log.i("JS-go",javascript);
		js.evaluateJavascript(javascript, new android.webkit.ValueCallback<String>() {
				@Override
				public void onReceiveValue(String json)
				{
					receiveSuccess(json);
				}
			});
	}
	public void receiveSuccess(String json)
	{
		//android.util.Log.i("JS-rcv",json);
		String msg = "";
		try
		{
			org.json.JSONObject obj = new org.json.JSONObject(json);
			for (int i=0; i < lines.length; i++)
			{
				if (obj.has("" + i))
					msg += lines[i] + " = " + obj.get("" + i) + "\n";
			}
			((android.widget.TextView) findViewById(R.id.erroroutput)).setText("");
		}
		catch (Exception e)
		{
			js.evaluateJavascript("(function(){return [window.errors.shift()];})();", new android.webkit.ValueCallback<String>() {
					@Override
					public void onReceiveValue(String json)
					{
						receiveError(json);
					}
				});
		}
		((android.widget.TextView) findViewById(R.id.output)).setText(msg.trim());
    }
	public void receiveError(String json)
	{
		//android.util.Log.e("JS-rcv",json);
		String msg = "";
		try
		{
			org.json.JSONArray obj = new org.json.JSONArray(json);
			for (int i=0; i < obj.length(); i++)
			{
				msg += obj.get(i) + "\n";
			}
		}
		catch (Exception e)
		{
			msg += e.getMessage();
		}
		((android.widget.TextView) findViewById(R.id.output)).setText("-");
		((android.widget.TextView) findViewById(R.id.erroroutput)).setText(msg.trim());
	}
}
