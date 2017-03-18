/* 
 * Copyright (C) 2009 Roman Masek
 * 
 * This file is part of OpenSudoku.
 * 
 * OpenSudoku is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * OpenSudoku is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with OpenSudoku.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package alvi17.sudokubangla.gui;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;

import alvi17.sudokubangla.R;
import alvi17.sudokubangla.db.FolderColumns;
import alvi17.sudokubangla.db.SudokuDatabase;
import alvi17.sudokubangla.game.FolderInfo;
import alvi17.sudokubangla.gui.FolderDetailLoader.FolderDetailCallback;

/**
 * List of puzzle's folder. This activity also serves as root activity of application.
 *
 * @author romario
 */
public class FolderListActivity extends ListActivity {

	public static final int MENU_ITEM_ADD = Menu.FIRST;
	public static final int MENU_ITEM_RENAME = Menu.FIRST + 1;
	public static final int MENU_ITEM_DELETE = Menu.FIRST + 2;
	public static final int MENU_ITEM_ABOUT = Menu.FIRST + 3;
	public static final int MENU_ITEM_EXPORT = Menu.FIRST + 4;
	public static final int MENU_ITEM_EXPORT_ALL = Menu.FIRST + 5;
	public static final int MENU_ITEM_IMPORT = Menu.FIRST + 6;

	private static final int DIALOG_ABOUT = 0;
	private static final int DIALOG_ADD_FOLDER = 1;
	private static final int DIALOG_RENAME_FOLDER = 2;
	private static final int DIALOG_DELETE_FOLDER = 3;

	private static final String TAG = "FolderListActivity";

	private Cursor mCursor;
	private SudokuDatabase mDatabase;
	private FolderListViewBinder mFolderListBinder;

	// input parameters for dialogs
	private TextView mAddFolderNameInput;
	private TextView mRenameFolderNameInput;
	private long mRenameFolderID;
	private long mDeleteFolderID;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.folder_list);


		setDefaultKeyMode(DEFAULT_KEYS_SHORTCUT);
		// Inform the list we provide context menus for items
		getListView().setOnCreateContextMenuListener(this);



		mDatabase = new SudokuDatabase(getApplicationContext());
		mCursor = mDatabase.getFolderList();
		startManagingCursor(mCursor);
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.folder_list_item,
				mCursor, new String[]{FolderColumns.NAME, FolderColumns._ID},
				new int[]{R.id.name, R.id.detail});
		mFolderListBinder = new FolderListViewBinder(this);
		adapter.setViewBinder(mFolderListBinder);

		setListAdapter(adapter);


	}

	@Override
	protected void onStart() {
		super.onStart();

		updateList();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mDatabase.close();
		mFolderListBinder.destroy();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

	}

	@Override
	protected void onRestoreInstanceState(Bundle state) {
		super.onRestoreInstanceState(state);

	}



	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Intent i = new Intent(this, SudokuListActivity.class);
		i.putExtra(SudokuListActivity.EXTRA_FOLDER_ID, id);
		startActivity(i);
	}

	private void updateList() {
		mCursor.requery();
	}

	private static class FolderListViewBinder implements ViewBinder {
		private Context mContext;
		private FolderDetailLoader mDetailLoader;


		public FolderListViewBinder(Context context) {
			mContext = context;
			mDetailLoader = new FolderDetailLoader(context);
		}

		@Override
		public boolean setViewValue(View view, Cursor c, int columnIndex) {

			switch (view.getId()) {
				case R.id.name:
					((TextView) view).setText(c.getString(columnIndex));
					break;
				case R.id.detail:
					final long folderID = c.getLong(columnIndex);
					final TextView detailView = (TextView) view;
					detailView.setText(mContext.getString(R.string.loading));
					mDetailLoader.loadDetailAsync(folderID, new FolderDetailCallback() {
						@Override
						public void onLoaded(FolderInfo folderInfo) {
							if (folderInfo != null)
								detailView.setText(folderInfo.getDetail(mContext));
						}
					});
			}

			return true;
		}

		public void destroy() {
			mDetailLoader.destroy();
		}
	}


}
