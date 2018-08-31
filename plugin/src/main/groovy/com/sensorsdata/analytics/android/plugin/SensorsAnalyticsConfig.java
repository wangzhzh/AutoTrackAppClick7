package com.sensorsdata.analytics.android.plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SensorsAnalyticsConfig {
    private final static List<SensorsAnalyticsMethodCell> sInterfaceMethods = new ArrayList<>();

    static {
        sInterfaceMethods.add(new SensorsAnalyticsMethodCell("android.view.View$OnClickListener",
                "onClick(Landroid/view/View;)V", 1, 1));
        sInterfaceMethods.add(new SensorsAnalyticsMethodCell("android.content.DialogInterface$OnClickListener",
                "onClick(Landroid/content/DialogInterface;I)V", 1, 2));
        sInterfaceMethods.add(new SensorsAnalyticsMethodCell("android.content.DialogInterface$OnMultiChoiceClickListener",
                "onClick(Landroid/content/DialogInterface;IZ)V", 1, 3));
        sInterfaceMethods.add(new SensorsAnalyticsMethodCell("android.widget.CompoundButton$OnCheckedChangeListener",
                "onCheckedChanged(Landroid/widget/CompoundButton;Z)V", 1, 2));
        sInterfaceMethods.add(new SensorsAnalyticsMethodCell("android.widget.RatingBar$OnRatingBarChangeListener",
                "onRatingChanged(Landroid/widget/RatingBar;FZ)V", 1, 1));
        sInterfaceMethods.add(new SensorsAnalyticsMethodCell("android.widget.SeekBar$OnSeekBarChangeListener",
                "onStopTrackingTouch(Landroid/widget/SeekBar;)V", 1, 1));
        sInterfaceMethods.add(new SensorsAnalyticsMethodCell("android.widget.AdapterView$OnItemSelectedListener",
                "onItemSelected(Landroid/widget/AdapterView;Landroid/view/View;IJ)V", 1, 3));
        sInterfaceMethods.add(new SensorsAnalyticsMethodCell("android.widget.TabHost$OnTabChangeListener",
                "onTabChanged(Ljava/lang/String;)V", 1, 1));
        sInterfaceMethods.add(new SensorsAnalyticsMethodCell("android.widget.AdapterView$OnItemClickListener",
                "onItemClick(Landroid/widget/AdapterView;Landroid/view/View;IJ)V", 1, 3));
        sInterfaceMethods.add(new SensorsAnalyticsMethodCell("android.widget.ExpandableListView$OnGroupClickListener",
                "onGroupClick(Landroid/widget/ExpandableListView;Landroid/view/View;IJ)Z", 1, 3));
        sInterfaceMethods.add(new SensorsAnalyticsMethodCell("android.widget.ExpandableListView$OnChildClickListener",
                "onChildClick(Landroid/widget/ExpandableListView;Landroid/view/View;IIJ)Z", 1, 4));

    }

    public static SensorsAnalyticsMethodCell isMatched(Set<String> interfaceList, String methodDesc) {
        for (SensorsAnalyticsMethodCell methodCell : sInterfaceMethods) {
            if (interfaceList.contains(methodCell.getInterfaces()) && methodCell.getMethodDesc().equals(methodDesc)) {
                return methodCell;
            }
        }
        return null;
    }
}
