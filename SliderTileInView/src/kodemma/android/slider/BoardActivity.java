package kodemma.android.slider;


import android.app.Activity;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class BoardActivity extends Activity implements BoardViewListener {
	public BoardView boardView;
	public TextView slideCounterView;

	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		boardView = (BoardView)findViewById(R.id.boardView);
		boardView.boardViewListener = this;
		slideCounterView = (TextView)findViewById(R.id.slideCounter);
		new ButtonClickListener();
	}
	public void onTileSlided(int count) {
		slideCounterView.setText(String.valueOf(count));
	}
	public void onGameSolved(int rows, int cols, int slides, long lapseTime) {
		// ここでランキング画面へ遷移する
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
				boardView.pauseButtonPressed();
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