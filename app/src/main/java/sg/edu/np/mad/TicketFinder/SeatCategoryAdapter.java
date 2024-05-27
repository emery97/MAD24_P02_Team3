package sg.edu.np.mad.TicketFinder;

import android.content.Context;

import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;

public class SeatCategoryAdapter {
    private ArrayList<SeatCategory> seatCategoryList;
    private Context context;

    public SeatCategoryAdapter(Context context, ArrayList<SeatCategory> seatCategoryList){
        this.context = context;
        this.seatCategoryList = seatCategoryList;
    }
}
