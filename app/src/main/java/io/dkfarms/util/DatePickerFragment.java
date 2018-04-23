package io.dkfarms.util;


import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;


import java.util.Calendar;

/**
 * A simple [DialogFragment] subclass for picking date
 * All activities calling this fragment must implement [DatePickerDialog.OnDateSetListener]
 */
public class DatePickerFragment extends DialogFragment {
	
	public static final String TAG = DatePickerFragment.class.getCanonicalName();
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		Calendar calendar = Calendar.getInstance();
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH);
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		
		//Create dialog
		DatePickerDialog dialog = new DatePickerDialog(getActivity(), (OnDateSetListener) getActivity(), year, month, day);
		//Return dialog
		return dialog;
	}
}
