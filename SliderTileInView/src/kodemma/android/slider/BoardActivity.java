package kodemma.android.slider;


import android.app.Activity;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class BoardActivity extends Activity {
	public BoardView boardView;

	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		boardView = (BoardView)findViewById(R.id.boardView);
		new ButtonClickListener();
	}
	private class ButtonClickListener implements View.OnClickListener {
		private ButtonClickListener() {
			TypedArray tArray = getResources().obtainTypedArray(R.array.boardButtons);
			for (int i=0; i<tArray.length(); i++) {
				int resourceId = tArray.getResourceId(i, 0);
				Button button = (Button)findViewById(resourceId);
				button.setOnClickListener(this);
			}
		}
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.board_button_start:
				boardView.startButtonPressed();
				break;
			case R.id.board_button_pause:
				break;
			case R.id.board_button_setting:
				break;
			case R.id.board_button_answer:
				break;
			case R.id.board_button_suspend:
				break;
			}
		}
	}
}