package apertus.remote;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Connect extends Activity {
    private Button Connect;

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.connect);

	Button Connect = (Button) findViewById(R.id.ConnectButton01);
	Connect.setOnClickListener(new View.OnClickListener() {
	    public void onClick(View view) {
		Intent myIntent = new Intent(view.getContext(), Main.class);
		startActivityForResult(myIntent, 0);
	    }

	});
    }
}
