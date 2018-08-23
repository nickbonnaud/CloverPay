package com.pockeyt.cloverpay.ui.fragments;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.kunzisoft.switchdatetime.SwitchDateTimeDialogFragment;
import com.pockeyt.cloverpay.R;
import com.pockeyt.cloverpay.handlers.EmployeeHandler;
import com.pockeyt.cloverpay.models.EmployeeModel;
import com.pockeyt.cloverpay.models.TipsModel;
import com.pockeyt.cloverpay.ui.viewModels.CurrentEmployeeViewModel;
import com.pockeyt.cloverpay.ui.viewModels.EmployeesViewModel;
import com.pockeyt.cloverpay.ui.viewModels.SelectedEmployeesViewModel;
import com.pockeyt.cloverpay.ui.viewModels.TipsViewModel;
import com.pockeyt.cloverpay.utils.DisplayHelpers;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

public class TipsTableFragment extends Fragment implements EmployeesCheckboxFragment.EmployeesCheckboxListener {
    private static final String TAG = TipsTableFragment.class.getSimpleName();
    public static final String EMPLOYEE_SELECTOR_FRAGMENT = "employee_selector_fragment";
    public static final int EMPLOYEE_SELECTOR_FRAGMENT_CODE = 0;
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_MANAGER = "MANAGER";
    private List<TipsModel> mCurrentTips;
    private int mOpenedDatePicker;
    private Menu mMenu;
    private List<EmployeeModel> mEmployees;
    private List<EmployeeModel> mSelectedEmployees;
    private EmployeeModel mCurrentEmployee;

    private ImageView mSpinnerImageView;
    private Animation mAnimationRotation;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_employee_tips, container, false);
        getCurrentEmployee().observe(this, currentEmployee -> {
            Log.d(TAG, "Get current Employee observer");
            if (didEmployeeChange(currentEmployee)) {
                TipsViewModel tipsViewModel = ViewModelProviders.of(getActivity()).get(TipsViewModel.class);
                tipsViewModel.resetParams();
                tipsViewModel.resetDates();
                SelectedEmployeesViewModel selectedEmployeesViewModel = ViewModelProviders.of(getActivity()).get(SelectedEmployeesViewModel.class);
                selectedEmployeesViewModel.setSelectedEmployees(null);

                mSelectedEmployees = new ArrayList<>();
                if (!isManager(currentEmployee)) {
                    mSelectedEmployees.add(currentEmployee);
                }
            }
            mCurrentEmployee = currentEmployee;
            EmployeesViewModel employeesViewModel = ViewModelProviders.of(getActivity()).get(EmployeesViewModel.class);
            employeesViewModel.getEmployees().observe(this, employees -> {
                Log.d(TAG, "5");
                mEmployees = employees;

                TipsViewModel tipsViewModel = ViewModelProviders.of(getActivity()).get(TipsViewModel.class);
                tipsViewModel.getEmployeeTransactions(mSelectedEmployees, employees).observe(this, tips -> {
                    Log.d(TAG, "6");
                    mCurrentTips = tips;
                    setTipsTable(view);
                    if (mCurrentTips.size() > 0) {
                        setHeaders(view);
                    }
                });
            });
        });

        setHasOptionsMenu(true);
        setTipTotalTitle();
        setLoadMoreData(view);
        return view;
    }

    private boolean didEmployeeChange(EmployeeModel currentEmployee) {
        return mCurrentEmployee == null || !mCurrentEmployee.getCloverId().equals(currentEmployee.getCloverId());
    }

    private void setTipTotalTitle() {
        TipsViewModel tipsViewModel = ViewModelProviders.of(getActivity()).get(TipsViewModel.class);
        tipsViewModel.getTipTotal().observe(this, tipTotal -> {
            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (tipTotal != null) {
                actionBar.setTitle("Total Tips: " + formatCurrency(tipTotal));
                actionBar.setDisplayShowTitleEnabled(true);
            } else {
                actionBar.setDisplayShowTitleEnabled(false);
            }
        });
    }

    private void handleLoadingApiData() {
        TipsViewModel tipsViewModel = ViewModelProviders.of(getActivity()).get(TipsViewModel.class);
        tipsViewModel.getIsLoading().observe(this, shouldStart -> {
            toggleSearchAnimation(shouldStart);

            if (!shouldStart) {
                if ((tipsViewModel.getStartDate().getValue() == null && tipsViewModel.getEndDate().getValue() == null) || (tipsViewModel.getStartDate().getValue().getDate() == null && tipsViewModel.getEndDate().getValue().getDate() == null)) {
                    getMenuItem(mMenu, R.id.action_tips_dates_clear).setVisible(false);
                } else {
                    getMenuItem(mMenu, R.id.action_tips_dates_clear).setVisible(true);
                }
            }
    });
    }

    private void setOptionsTitles() {
        TipsViewModel tipsViewModel = ViewModelProviders.of(getActivity()).get(TipsViewModel.class);

        tipsViewModel.getStartDate().observe(this, startDate -> {
            String title;
            if (startDate != null && startDate.getDate() != null) {
                getMenuItem(mMenu, R.id.action_tips_dates_clear).setVisible(true);
                title = formatDisplayDate(startDate.getDate());
            } else {
                title = getString(R.string.tips_start_date);
            }
            getMenuItem(mMenu, R.id.action_date_picker_start).setTitle(title);
            doSearch();
        });

        tipsViewModel.getEndDate().observe(this, endDate -> {
            String title;
            if (endDate != null && endDate.getDate() != null) {
                getMenuItem(mMenu, R.id.action_tips_dates_clear).setVisible(true);
                title = formatDisplayDate(endDate.getDate());
            } else {
                title = getString(R.string.tips_end_date);
            }
            getMenuItem(mMenu, R.id.action_date_picker_end).setTitle(title);
            doSearch();
        });
    }

    private void doSearch() {
        TipsViewModel tipsViewModel = ViewModelProviders.of(getActivity()).get(TipsViewModel.class);
        TipsViewModel.FetchableDate startDate = tipsViewModel.getStartDate().getValue();
        TipsViewModel.FetchableDate endDate = tipsViewModel.getEndDate().getValue();

        if ((startDate != null && endDate != null) && (startDate.getDate() != null && endDate.getDate() != null) && startDate.getDate().before(endDate.getDate())) {
            getMenuItem(mMenu, R.id.action_tips_dates_clear).setVisible(false);
            tipsViewModel.paramsChangedSearchTransactions(mSelectedEmployees, mEmployees);
        }

        if ((startDate != null && endDate != null) && (startDate.getDate() == null && endDate.getDate() == null)) {
            getMenuItem(mMenu, R.id.action_tips_dates_clear).setVisible(false);
            tipsViewModel.setTipTotal(null);
            if (startDate.isShouldFetch() && endDate.isShouldFetch()) {
                tipsViewModel.paramsChangedSearchTransactions(mSelectedEmployees, mEmployees);
            }
        }
    }

    private void setHeaders(View view) {
        ViewTreeObserver viewTreeObserver = view.getViewTreeObserver();
        if (viewTreeObserver.isAlive()) {
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    formatHeaders(view);
                    if (viewTreeObserver.isAlive()) {
                        viewTreeObserver.removeOnGlobalLayoutListener(this);
                    }
                }
            });
        }
    }

    private void setTipsTable(View view) {
        TableLayout tableLayout = view.findViewById(R.id.tipsTable);
        tableLayout.removeAllViews();

        TipsViewModel tipsViewModel = ViewModelProviders.of(getActivity()).get(TipsViewModel.class);
        TipsViewModel.FetchableDate startDate = tipsViewModel.getStartDate().getValue();
        TipsViewModel.FetchableDate endDate = tipsViewModel.getEndDate().getValue();
        int color = (startDate != null && startDate.getDate() != null) && (endDate != null && endDate.getDate() != null) ? Color.LTGRAY : Color.TRANSPARENT;


        for (int i = 0; i < mCurrentTips.size(); i++) {
            TableRow row = new TableRow(getActivity());
            row.setMinimumHeight(DisplayHelpers.dipToPixels(getContext(), 30));

            TableLayout.LayoutParams tableRowParams = new TableLayout.LayoutParams();
            int marginTen = DisplayHelpers.dipToPixels(getContext(), 10);
            tableRowParams.setMargins(marginTen, marginTen, marginTen, DisplayHelpers.dipToPixels(getContext(), 20));
            row.setLayoutParams(tableRowParams);
            row.setClickable(true);
            row.setId(i);
            row.setOnClickListener(tableRowOnClickListener);
            row.setBackgroundColor(color);

            tableLayout.addView(addDataToRow(row, mCurrentTips.get(i)), i);
        }
    }

    private View.OnClickListener tableRowOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            TipsViewModel tipsViewModel = ViewModelProviders.of(getActivity()).get(TipsViewModel.class);
            int color = tipsViewModel.addRemoveTransaction(v.getId()) ? Color.LTGRAY : Color.TRANSPARENT;
            v.setBackgroundColor(color);
        }
    };

    private void formatHeaders(View view) {
        TableRow headerRow = view.findViewById(R.id.tipsHeader);
        TableLayout tableLayout = view.findViewById(R.id.tipsTable);

        TableRow row = (TableRow) tableLayout.getChildAt(0);

        if (row != null) {
            for (int i = 0; i < row.getChildCount(); i++) {
                AppCompatTextView dataView = (AppCompatTextView) row.getChildAt(i);

                AppCompatTextView headerView = (AppCompatTextView) headerRow.getChildAt(i);
                headerView.setWidth(dataView.getMeasuredWidth());
            }
        }
    }

    private TableRow addDataToRow(TableRow row, TipsModel tip) {
        row.addView(addDataToTextView(tip.getDate()));
        row.addView(addDataToTextView(tip.getEmployeeName()));
        row.addView(addDataToTextView(tip.getBillId()));
        row.addView(addDataToTextView(tip.getBillTotal()));
        row.addView(addDataToTextView(tip.getTipAmountFormatted()));
        return row;
    }

    private TextView addDataToTextView(String tipData) {
        AppCompatTextView textView = new AppCompatTextView(getActivity());
        textView.setText(tipData);

        TableRow.LayoutParams params = new TableRow.LayoutParams();
        params.height = TableRow.LayoutParams.MATCH_PARENT;
        textView.setLayoutParams(params);

        TextViewCompat.setAutoSizeTextTypeWithDefaults(textView, TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM);
        textView.setGravity(Gravity.CENTER);
        return textView;
    }

    private LiveData<EmployeeModel> getCurrentEmployee() {
        CurrentEmployeeViewModel currentEmployeeViewModel = ViewModelProviders.of(getActivity()).get(CurrentEmployeeViewModel.class);
        return currentEmployeeViewModel.getCurrentEmployee();
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        Log.d(TAG, "On prepare options menu");
        mMenu = menu;
        getMenuItem(menu, R.id.action_tips).setVisible(false);
        getMenuItem(menu, R.id.action_date_picker_start).setVisible(true);
        getMenuItem(menu, R.id.action_date_picker_end).setVisible(true);
        getMenuItem(menu, R.id.action_employee_filter).setVisible(true);

        setOptionsTitles();
        handleLoadingApiData();
    }

    private MenuItem getMenuItem(Menu menu, int actionId) {
        return menu.findItem(actionId);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_date_picker_start || item.getItemId() == R.id.action_date_picker_end) {
            return showDateTimePickers(item);
        } else if (item.getItemId() == R.id.action_tips_dates_clear) {
            return clearDatePicker();
        } else if (item.getItemId() == R.id.action_employee_filter) {
            showEmployeeFilterDialog();
        }
        return super.onOptionsItemSelected(item);
    }

    private void showEmployeeFilterDialog() {
        if (isManager(mCurrentEmployee)) {
            EmployeesCheckboxFragment fragment = new EmployeesCheckboxFragment().newInstance(mEmployees);
            fragment.setTargetFragment(this, EMPLOYEE_SELECTOR_FRAGMENT_CODE);
            fragment.show(getActivity().getSupportFragmentManager(), EMPLOYEE_SELECTOR_FRAGMENT);
        } else {
            Toast.makeText(getContext(), "Must be Admin/Manager to filter transactions by employee.", Toast.LENGTH_LONG).show();
        }
    }

    private boolean clearDatePicker() {
        TipsViewModel tipsViewModel = ViewModelProviders.of(getActivity()).get(TipsViewModel.class);
        tipsViewModel.setEndDate(true, null);
        tipsViewModel.setStartDate(true, null);
        return true;
    }

    private boolean showDateTimePickers(MenuItem item) {
        showDatePicker(item.getItemId());
        return true;
    }


    private void toggleSearchAnimation(boolean shouldStart) {
        MenuItem searchSpinner = mMenu.findItem(R.id.action_tips_search);
        if (shouldStart) {
            if (mSpinnerImageView == null) {
                LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                mSpinnerImageView = (ImageView) inflater.inflate(R.layout.iv_refresh, null);
                mAnimationRotation = AnimationUtils.loadAnimation(getContext(), R.anim.rotate_refresh);
                mAnimationRotation.setRepeatCount(Animation.INFINITE);
            }
            mSpinnerImageView.startAnimation(mAnimationRotation);
            searchSpinner.setActionView(mSpinnerImageView);
        } else {
            if (searchSpinner.getActionView() != null) {
                searchSpinner.getActionView().clearAnimation();
                searchSpinner.setActionView(null);
            }
        }
        searchSpinner.setVisible(shouldStart);
    }

    private void showDatePicker(int itemId) {
        mOpenedDatePicker = itemId;
        String title = itemId == R.id.action_date_picker_start ? "Set Start" : " Set End";
        SwitchDateTimeDialogFragment dateTimeDialogFragment = SwitchDateTimeDialogFragment.newInstance(
                title,
                "Set",
                "Cancel",
                "Clear"
        );
        dateTimeDialogFragment.startAtCalendarView();
        dateTimeDialogFragment.set24HoursMode(false);
        dateTimeDialogFragment.setDefaultDateTime(new GregorianCalendar().getTime());

        setListener(dateTimeDialogFragment);
        dateTimeDialogFragment.show(getActivity().getSupportFragmentManager(), "datepicker_dialog");
    }

    private void setListener(SwitchDateTimeDialogFragment dateTimeDialogFragment) {
        dateTimeDialogFragment.setOnButtonClickListener(new SwitchDateTimeDialogFragment.OnButtonWithNeutralClickListener() {
            @Override
            public void onNeutralButtonClick(Date date) {
                TipsViewModel tipsViewModel = ViewModelProviders.of(getActivity()).get(TipsViewModel.class);
                if (mOpenedDatePicker == R.id.action_date_picker_start) {
                    tipsViewModel.setStartDate(true, null);
                } else {
                    tipsViewModel.setEndDate(true, null);
                }
            }

            @Override
            public void onPositiveButtonClick(Date date) {
                TipsViewModel tipsViewModel = ViewModelProviders.of(getActivity()).get(TipsViewModel.class);
                if (mOpenedDatePicker == R.id.action_date_picker_start) {
                    tipsViewModel.setStartDate(true, date);
                } else {
                    tipsViewModel.setEndDate(true, date);
                }
            }

            @Override
            public void onNegativeButtonClick(Date date) {
                Log.d(TAG, date + "");
            }
        });
    }


    private String formatDisplayDate(Date date) {
        return new SimpleDateFormat("EEE, MMM d 'at' h:mm a", Locale.getDefault())
                .format(date);
    }

    private boolean isManager(EmployeeModel currentEmployee) {
        return currentEmployee.getRole().equals(EmployeeHandler.ROLE_ADMIN) || currentEmployee.getRole().equals(EmployeeHandler.ROLE_MANAGER);
    }

    private String formatCurrency(int amount) {
        NumberFormat n = NumberFormat.getCurrencyInstance(Locale.US);
        return n.format(amount / 100.0);
    }

    private void setLoadMoreData(View view) {
        TipsViewModel tipsViewModel = ViewModelProviders.of(getActivity()).get(TipsViewModel.class);
        Button loadMoreButton = view.findViewById(R.id.loadMoreButton);
        loadMoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadMoreButton.setVisibility(View.GONE);
                tipsViewModel.fetchTransactions(mSelectedEmployees);
            }
        });
        tipsViewModel.getIsLoading().observe(this, isLoading -> {
            loadMoreButton.setClickable(!isLoading);
        });
        tipsViewModel.getHasMore().observe(this, hasMore -> {
            loadMoreButton.setVisibility(hasMore ? View.VISIBLE : View.GONE);
        });
    }


    @Override
    public void onEmployeesCheckboxPositiveClicked(List<EmployeeModel> selectedEmployees) {
        TipsViewModel tipsViewModel = ViewModelProviders.of(getActivity()).get(TipsViewModel.class);
        mSelectedEmployees = selectedEmployees;
        tipsViewModel.paramsChangedSearchTransactions(mSelectedEmployees, mEmployees);
    }

    @Override
    public void onEmployeesCheckboxNegativeClicked(DialogFragment dialogFragment) {

    }

    @Override
    public void onEmployeesCheckboxNeutralClicked(DialogFragment dialogFragment) {
        TipsViewModel tipsViewModel = ViewModelProviders.of(getActivity()).get(TipsViewModel.class);
        mSelectedEmployees = null;
        tipsViewModel.paramsChangedSearchTransactions(mSelectedEmployees, mEmployees);
    }

    @Override
    public void onResume() {
        CurrentEmployeeViewModel currentEmployeeViewModel = ViewModelProviders.of(getActivity()).get(CurrentEmployeeViewModel.class);
        currentEmployeeViewModel.fetchCurrentEmployee();
        super.onResume();
    }
}
