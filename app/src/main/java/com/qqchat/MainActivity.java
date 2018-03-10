package com.qqchat;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Editable;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.qqchat.adapter.ChatLVAdapter;
import com.qqchat.adapter.FaceGVAdapter;
import com.qqchat.adapter.FaceVPAdapter;
import com.qqchat.bean.ChatInfo;
import com.qqchat.view.DropdownListView;
import com.qqchat.view.MyEditText;

/**
 * @author xianghairui
 * */
public class MainActivity extends Activity {
	private ViewPager mViewPager;
	private LinearLayout mDotsLayout;
	private MyEditText input;
	private Button send;
	private DropdownListView mListView;
	private ChatLVAdapter mLvAdapter;
	// 7列3行
	private int columns = 7;
	private int rows = 3;
	private List<View> views = new ArrayList<View>();
	private List<String> staticFacesList;
	private LinkedList<ChatInfo> infos = new LinkedList<ChatInfo>();

	private void initViews() {
		mListView = (DropdownListView) findViewById(R.id.message_chat_listview);
		mLvAdapter = new ChatLVAdapter(this, infos);
		mListView.setAdapter(mLvAdapter);
		mViewPager = (ViewPager) findViewById(R.id.face_viewpager);
		mViewPager.setOnPageChangeListener(new PageChange());
		mDotsLayout = (LinearLayout) findViewById(R.id.face_dots_container);
		input = (MyEditText) findViewById(R.id.input_sms);
		send = (Button) findViewById(R.id.send_sms);
		InitViewPager();
	}

	/*
	 * 初始表情 *
	 */
	private void InitViewPager() {
		// 获取页数
		for (int i = 0; i < getPagerCount(); i++) {
			views.add(viewPagerItem(i));
			LayoutParams params = new LayoutParams(16, 16);
			// LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT,
			// LayoutParams.WRAP_CONTENT);
			mDotsLayout.addView(dotsItem(i), params);
		}
		FaceVPAdapter mVpAdapter = new FaceVPAdapter(views);
		mViewPager.setAdapter(mVpAdapter);
		mDotsLayout.getChildAt(0).setSelected(true);
	}

	private View viewPagerItem(int position) {
		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.face_gridview, null);
		GridView gridview = (GridView) layout.findViewById(R.id.chart_face_gv);
		/**
		 * 注：因为每一页末尾都有一个删除图标，所以每一页的实际表情columns *　rows　－　1; 空出最后一个位置给删除图标
		 * */
		List<String> subList = new ArrayList<String>();
		subList.addAll(staticFacesList
				.subList(position * (columns * rows - 1),
						(columns * rows - 1) * (position + 1) > staticFacesList
								.size() ? staticFacesList.size() : (columns
								* rows - 1)
								* (position + 1)));
		// 0-20 20-40 40-60 60-80
		/**
		 * 末尾添加删除图标
		 * */
		subList.add("emotion_del_normal.png");
		FaceGVAdapter mGvAdapter = new FaceGVAdapter(subList, this);
		gridview.setAdapter(mGvAdapter);
		gridview.setNumColumns(columns);
		// 单击表情执行的操作
		gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				try {
					String png = ((TextView) ((LinearLayout) view)
							.getChildAt(1)).getText().toString();
					if (!png.contains("emotion_del_normal")) {// 如果不是删除图标
						// input.setText(sb);
						insert(getFace(png));
					} else {
						delete();
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		input.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub
				// try {
				// if (count == "#[face/png/f_static_000.png]#".length()) {
				// String tempText = s.subSequence(start, start +
				// count).toString();
				// String regex =
				// "(\\#\\[face/png/f_static_)\\d{3}(.png\\]\\#)";
				// Pattern p = Pattern.compile(regex);
				// Matcher m = p.matcher(tempText);
				// if (m.matches()) {
				// SpannableStringBuilder sb = new SpannableStringBuilder(
				// input.getText());
				// String png = tempText.substring("#[".length(),
				// tempText.length() - "]#".length());
				// sb.setSpan(
				// new ImageSpan(MainActivity.this,
				// BitmapFactory
				// .decodeStream(getAssets()
				// .open(png))),
				// start, start + count,
				// Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				// }
				// }
				// } catch (Exception e) {
				// e.printStackTrace();
				// }
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
			}
		});
		// 发送
		send.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (!TextUtils.isEmpty(input.getText().toString())) {
					infos.add(getChatInfoTo(input.getText().toString()));
					mLvAdapter.setList(infos);
					mLvAdapter.notifyDataSetChanged();
					mListView.setSelection(infos.size() - 1);
					input.setText("");
				}
			}
		});
		
		/***
		 * listview下拉刷新
		 * */
		mListView.setOnRefreshListenerHead(new DropdownListView.OnRefreshListenerHeader() {

			@Override
			public void onRefresh() {
				// TODO Auto-generated method stub
				new Thread() {
					@Override
					public void run() {
						try {
							sleep(1000);
							Message msg = mHandler.obtainMessage(0);
							mHandler.sendMessage(msg);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}

				}.start();
			}
		});

		return gridview;
	}

	private SpannableStringBuilder getFace(String png) {
		SpannableStringBuilder sb = new SpannableStringBuilder();
		try {
			/**
			 * 经过测试，虽然这里tempText被替换为png显示，但是但我单击发送按钮时，获取到輸入框的内容是tempText的值而不是png
			 * 所以这里对这个tempText值做特殊处理
			 * 格式：#[face/png/f_static_000.png]#，以方便判斷當前圖片是哪一個
			 * */
			String tempText = "#[" + png + "]#";
			sb.append(tempText);
			sb.setSpan(
					new ImageSpan(MainActivity.this, BitmapFactory
							.decodeStream(getAssets().open(png))), sb.length()
							- tempText.length(), sb.length(),
					Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return sb;
	}

	/**
	 * 向输入框里添加表情
	 * */
	private void insert(CharSequence text) {
		int iCursorStart = Selection.getSelectionStart((input.getText()));
		int iCursorEnd = Selection.getSelectionEnd((input.getText()));
		if (iCursorStart != iCursorEnd) {
			((Editable) input.getText()).replace(iCursorStart, iCursorEnd, "");
		}
		int iCursor = Selection.getSelectionEnd((input.getText()));
		((Editable) input.getText()).insert(iCursor, text);
	}

	/**
	 * 删除图标执行事件
	 * 注：如果删除的是表情，在删除时实际删除的是tempText即图片占位的字符串，所以必需一次性删除掉tempText，才能将图片删除
	 * */
	private void delete() {
		if (input.getText().length() != 0) {
			int iCursorEnd = Selection.getSelectionEnd(input.getText());
			int iCursorStart = Selection.getSelectionStart(input.getText());
			if (iCursorEnd > 0) {
				if (iCursorEnd == iCursorStart) {
					if (isDeletePng(iCursorEnd)) {
						String st = "#[face/png/f_static_000.png]#";
						((Editable) input.getText()).delete(
								iCursorEnd - st.length(), iCursorEnd);
					} else {
						((Editable) input.getText()).delete(iCursorEnd - 1,
								iCursorEnd);
					}
				} else {
					((Editable) input.getText()).delete(iCursorStart,
							iCursorEnd);
				}
			}
		}
	}

	/**
	 * 判断即将删除的字符串是否是图片占位字符串tempText 如果是：则讲删除整个tempText
	 * **/
	private boolean isDeletePng(int cursor) {
		String st = "#[face/png/f_static_000.png]#";
		String content = input.getText().toString().substring(0, cursor);
		if (content.length() >= st.length()) {
			String checkStr = content.substring(content.length() - st.length(),
					content.length());
			String regex = "(\\#\\[face/png/f_static_)\\d{3}(.png\\]\\#)";
			Pattern p = Pattern.compile(regex);
			Matcher m = p.matcher(checkStr);
			return m.matches();
		}
		return false;
	}

	private ImageView dotsItem(int position) {
		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.dot_image, null);
		ImageView iv = (ImageView) layout.findViewById(R.id.face_dot);
		iv.setId(position);
		return iv;
	}

	private int getPagerCount() {
		int count = staticFacesList.size();
		return count % (columns * rows - 1) == 0 ? count / (columns * rows - 1)
				: count / (columns * rows - 1) + 1;
	}

	private void initStaticFaces() {
		try {
			staticFacesList = new ArrayList<String>();
			String[] faces = getAssets().list("face/png");
			for (int i = 0; i < faces.length; i++) {
				staticFacesList.add(faces[i]);
			}
			staticFacesList.remove("emotion_del_normal.png");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 表情页改变时，dots效果也要跟着改变
	 * */
	class PageChange implements OnPageChangeListener {

		@Override
		public void onPageScrollStateChanged(int arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onPageSelected(int arg0) {
			// TODO Auto-generated method stub
			for (int i = 0; i < mDotsLayout.getChildCount(); i++) {
				mDotsLayout.getChildAt(i).setSelected(false);
			}
			mDotsLayout.getChildAt(arg0).setSelected(true);
		}

	}

	private ChatInfo getChatInfoTo(String message) {
		ChatInfo info = new ChatInfo();
		info.content = message;
		info.fromOrTo = 1;
		// if ((System.currentTimeMillis() - upTime) > 60000) {
		// upTime = System.currentTimeMillis();
		// info.time = DateFormatUtil.getCurrDate(Constant.DATE_PATTERN_1);
		// }else{
		// info.time = "";
		// }
		// info.time = DateFormatUtil.getCurrDate(Constant.DATE_PATTERN_1);
		return info;
	}
	
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				mLvAdapter.setList(infos);
				mLvAdapter.notifyDataSetChanged();
				mListView.onRefreshCompleteHeader();
				break;
			}
		}
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chat_main);
		initStaticFaces();
		initViews();
		// final TextView gifTextView = (TextView) findViewById(R.id.text);
		// SpannableStringBuilder sb = new SpannableStringBuilder();
		// sb.append("Text followed by animated gif: ");
		// String dummyText = "dummy";
		// sb.append(dummyText);
		// try {
		//
		// sb.setSpan(new AnimatedImageSpan(new AnimatedGifDrawable(
		// getAssets().open("face/gif/f031.gif"),
		// new AnimatedGifDrawable.UpdateListener() {
		// @Override
		// public void update() {
		// gifTextView.postInvalidate();
		// }
		// })), sb.length() - dummyText.length(), sb.length(),
		// Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		// } catch (Exception e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// sb.append(dummyText);
		// try {
		// sb.setSpan(
		// new ImageSpan(this, BitmapFactory.decodeStream(getAssets()
		// .open("face/f007.png"))), sb.length()
		// - dummyText.length(), sb.length(),
		// Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		//
		// gifTextView.setText(sb);
		//
		// LinearLayout container = (LinearLayout) findViewById(R.id.container);
		// LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT,
		// LayoutParams.WRAP_CONTENT);
		// ImageView ig = new ImageView(this);
		// ig.setLayoutParams(params);
		// try {
		// Bitmap mBitmap = BitmapFactory.decodeStream(getAssets().open(
		// "face/f007.png"));
		// ig.setImageBitmap(mBitmap);
		// container.addView(ig);
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		//
		// Face face = new Face();
		// String[] staticFaces = face.getStaticFaces(this);
		// String[] dynamicFaces = face.getDynamicFaces(this);
		// System.out.println(staticFaces.length);
		// System.out.println(Arrays.toString(staticFaces));
		// System.out.println(dynamicFaces.length);
		// System.out.println(Arrays.toString(dynamicFaces));
	}
}
