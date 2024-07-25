package sg.edu.np.mad.TicketFinder;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class SeatSelectionFragment extends Fragment {

    private SeatSelectionViewModel viewModel;
    private static final String ARG_INDEX = "index";
    private AutoCompleteTextView autoCompleteTextView;
    private AutoCompleteTextView seatAutoCompleteTextView;
    private ArrayList<SeatCategory> seatCategoryList = new ArrayList<>();
    private int index;

    private static final String TAG = "fragment";

    // In your activity or fragment manager
    private List<Boolean> fragmentValidStates = new ArrayList<>(Arrays.asList(false, false));

    public SeatSelectionFragment() {
        // Required empty public constructor
    }

    private OnInputValidListener listener;

    // Creating instance of fragment
    public static SeatSelectionFragment newInstance(int index){
        SeatSelectionFragment fragment = new SeatSelectionFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_INDEX, index);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        if(getArguments() != null){
            index = getArguments().getInt(ARG_INDEX);
        }
        // Get the ViewModel
        viewModel = new ViewModelProvider(requireActivity()).get(SeatSelectionViewModel.class);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnInputValidListener) {
            listener = (OnInputValidListener) context;
        } else {
            throw new ClassCastException(context.toString() + " must implement OnInputValidListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_seat_selection, container, false);

        autoCompleteTextView = view.findViewById(R.id.auto_complete_txt);
        seatAutoCompleteTextView = view.findViewById(R.id.auto_complete_txt2);

        // Fetch seat category data from Firestore
        dbHandler handler = new dbHandler();
        handler.getSeatCategoryData(new FirestoreCallback<SeatCategory>() {
            @Override
            public void onCallback(ArrayList<SeatCategory> retrievedSeatCategoryList) {
                seatCategoryList.addAll(retrievedSeatCategoryList);
                setupCategoryDropdown();
            }
        });

        // Add Text Watchers
        autoCompleteTextView.addTextChangedListener(createTextWatcher());
        seatAutoCompleteTextView.addTextChangedListener(createTextWatcher());

        return view;
    }

    private TextWatcher createTextWatcher() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Add logging if needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d(TAG, "Text changed: " + s);
                notifyActivityOfInputValidity();
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Add logging if needed
            }
        };
    }

    // Interface to check if user input all fields
    public interface OnInputValidListener {
        void onInputValid(boolean isValid, int fragmentIndex);
    }

    private void notifyActivityOfInputValidity() {
        boolean isValid = !autoCompleteTextView.getText().toString().trim().isEmpty() &&
                !seatAutoCompleteTextView.getText().toString().trim().isEmpty();
        Log.d(TAG, "Validation check - AutoCompleteTextView: " + autoCompleteTextView.getText() +
                ", SeatAutoCompleteTextView: " + seatAutoCompleteTextView.getText() +
                ", Is Valid: " + isValid);

        if (listener != null) {
            listener.onInputValid(isValid, index);
        }
        if (isValid) {
            String seatCategory = autoCompleteTextView.getText().toString().trim();
            String seatNumber = seatAutoCompleteTextView.getText().toString().trim();

            HashMap<String, ArrayList<String>> seatMap = viewModel.getSeatMap().getValue();

            // Log current state of seatMap for debugging
            Log.d(TAG, "Current seatMap: " + seatMap);

            // Check for duplicate seat number in the same category
            boolean categoryExists = seatMap != null && seatMap.containsKey(seatCategory);
            Log.d(TAG, "notifyActivityOfInputValidity: SEAT CAT " + categoryExists);
            boolean numberExists = categoryExists && seatMap.get(seatCategory).contains(seatNumber);
            Log.d(TAG, "notifyActivityOfInputValidity: SEAT NUM " + numberExists);

            if (numberExists) {
                // Clear the input fields and show a message to the user
                autoCompleteTextView.setText("");
                seatAutoCompleteTextView.setText("");
                Toast.makeText(getActivity(), "Duplicate entry! Please input a unique seat number.", Toast.LENGTH_SHORT).show();
            } else {
                // Update the ViewModel
                viewModel.updateSeatMap(seatCategory, seatNumber);
                Log.d(TAG, "notifyActivityOfInputValidity: " + seatMap);
            }
        }
    }

    private int getFragmentIndex() {
        // Example logic to get the current fragment index
        // Replace with actual implementation
        return 1; // or whatever index this fragment should be
    }

    private void setupCategoryDropdown() {
        ArrayList<String> seatCategoryNames = new ArrayList<>();
        for (SeatCategory seatCategory : seatCategoryList) {
            if (seatCategory.getCategory() != null) {
                seatCategoryNames.add(seatCategory.getCategory());
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, seatCategoryNames);
        autoCompleteTextView.setAdapter(adapter);
        autoCompleteTextView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedItem = (String) parent.getItemAtPosition(position);
            filterSeatsByCategory(selectedItem);
            seatAutoCompleteTextView.setText(""); // Reset seat number when category changes
        });
    }

    private void filterSeatsByCategory(String category) {
        ArrayList<String> seatNumbers = new ArrayList<>();
        for (SeatCategory seatCategory : seatCategoryList) {
            if (seatCategory.getCategory().equals(category)) {
                seatNumbers.addAll(seatCategory.getSeats());
                break;
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, seatNumbers);
        seatAutoCompleteTextView.setAdapter(adapter);
    }
}
