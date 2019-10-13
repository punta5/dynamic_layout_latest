package com.example.dynamiclayout;

import android.app.Service;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.core.view.GestureDetectorCompat;

import java.util.ArrayList;
import java.util.List;

public class FloatingViewService extends Service implements View.OnClickListener {

    GenericTreeBuilder<String> rootNode;
    GenericTreeBuilder<String> childLevelOne;
    GenericTreeBuilder<String> childLevelTwo;
    GenericTreeBuilder<String> childLevelThree;
    GenericTreeBuilder<String> childLevelFour;
    GenericTreeBuilder<String> childLevelFive;

    List<GenericTreeBuilder> childrenNodes = null;

    ArrayList linearLayoutLevelList = new ArrayList();
    ArrayList relativeLayoutLevelList = new ArrayList();
    ArrayList buttonLevelList = new ArrayList();

    int previouslySelectButtonID = Integer.MAX_VALUE;       // for removing Containers at higher levels

    LinearLayout mainLayout;

    private WindowManager mWindowManager;
    private View mFloatingView;
    private View collapsedView;
    private View expandedView;
    private View collapsed_IV;
    private static int defaultStates[];
    private final static int[] STATE_PRESSED = {
            android.R.attr.state_pressed,
            android.R.attr.state_focused
                    | android.R.attr.state_enabled };
    private Button mLastButton;


    public FloatingViewService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // get Intent data from FloatingActivity when users starts this service
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        rootNode = (GenericTreeBuilder<String>) intent.getSerializableExtra("ROOT_NODE");
        childLevelOne = (GenericTreeBuilder<String>) intent.getSerializableExtra("CHILD_LEVEL_ONE");
        childLevelTwo = (GenericTreeBuilder<String>) intent.getSerializableExtra("CHILD_LEVEL_TWO");
        childLevelThree = (GenericTreeBuilder<String>) intent.getSerializableExtra("CHILD_LEVEL_THREE");
        childLevelFour = (GenericTreeBuilder<String>) intent.getSerializableExtra("CHILD_LEVEL_FOUR");
        childLevelFive = (GenericTreeBuilder<String>) intent.getSerializableExtra("CHILD_LEVEL_FIVE");
        childrenNodes = rootNode.getChildren();
        if (childrenNodes.size() > 0) {                                     // print children of found node
            int i = 0;
            for (GenericTreeBuilder node : childrenNodes) {
                i++;
                Log.d("kulkul", "Dijete " + i + ": " + node.getName());
            }
        }

        // here we create first level menu (Container at Level 1)
        if (childrenNodes != null) {
            int numberOfButtons = childrenNodes.size();     // so we can calculate weight of the buttons inside linear layout
            Log.d("kulkul", numberOfButtons + "");
            LinearLayout.LayoutParams firstLevelParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
            LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f/numberOfButtons);
            LinearLayout firstLevelLayout = new LinearLayout(this);     // create Linear Layout container for Level 1 nodes, visible when user opens menu for the first time
            firstLevelLayout.setLayoutParams(firstLevelParams);
            firstLevelLayout.setTag("linearLayoutLevel1");                // if we eventually need to get first level container


            for (GenericTreeBuilder node : childrenNodes) {        // set first level buttons inside linear layout
                Button button = new Button(this);
                button.setText(node.getName());
                button.setId(Integer.parseInt(node.getId()));       // set unique ID for every button (derived from line number in .txt file)
                button.setTag(node.getLevel());                     // set unique TAG from every button, so we know on which level button has been clicked
                button.setOnClickListener(this);                    // set buttons onClickListener
                button.setLayoutParams(buttonParams);
                buttonLevelList.add(button.getTag());                // save Button tags into list for hiding buttons on the same level
                Log.d("btntag", button.getTag().toString());
                firstLevelLayout.addView(button);                   // add buttons to first menu container
            }

            //relativeLayout.addView(firstLevelLayout);
            mainLayout.addView(firstLevelLayout, 1);                   // add first menu container to root view

        }

        return super.onStartCommand(intent, flags, startId);
    }



    @Override
    public void onCreate() {
        super.onCreate();

        //getting the widget layout from xml using layout inflater
        mFloatingView = LayoutInflater.from(this).inflate(R.layout.test_layout, null);

        mainLayout = mFloatingView.findViewById(R.id.mainLayout);

        mFloatingView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.d("haha", event.toString());
                return false;
            }
        });




        //we need to check for Android Oreo
        int LAYOUT_FLAG;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
        }

        //setting the layout parameters
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);


        //getting windows services and adding the floating view to it
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        if (mWindowManager != null) {
            mWindowManager.addView(mFloatingView, params);
        }

        Button moveLayout = mFloatingView.findViewById(R.id.buttonCreateWidget);
        moveLayout.setText("Drag to move");

        //adding an touchlistener to make drag movement of the floating widget
        mFloatingView.findViewById(R.id.buttonCreateWidget).setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                Log.d("okej3", event.toString());

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;

                    case MotionEvent.ACTION_UP:
                        //when the drag is ended switching the state of the widget
                        //collapsedView.setVisibility(View.GONE);
                        //expandedView.setVisibility(View.VISIBLE);
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        //this code is helping the widget to move around the screen with fingers
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        mWindowManager.updateViewLayout(mFloatingView, params);
                        return true;
                }
                return false;
            }
        });
    }


    @Override
    public void onClick(final View v) {
        Toast.makeText(getApplicationContext(), "ID: " + v.getId(), Toast.LENGTH_SHORT).show();
        GenericTreeBuilder foundNode = findNodeWithSpecificID(rootNode, String.valueOf(v.getId()));

        if (foundNode.isLeaf()) {       // node has no children, there's no need to create new container!
            //Toast.makeText(getApplicationContext(), "Nema djece!", Toast.LENGTH_SHORT).show();
        } else {                        // node HAS children, create new container for next (sub)menu
            int broj = foundNode.getChildren().size();
            List<GenericTreeBuilder> childrenNodes = getChildrenOfSpecificNode(foundNode);

            int numberOfButtons = childrenNodes.size();     // how many buttons we need to draw side by side inside layout, scale them according to this number

            LinearLayout nextLevelLayout = new LinearLayout(this);
            LinearLayout.LayoutParams nextLevelParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
            LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f/numberOfButtons);
            nextLevelLayout.setLayoutParams(nextLevelParams);

            int nodeLevel = Integer.parseInt(foundNode.getLevel()) + 1;         // Linear Layout levels start at 1
            nextLevelLayout.setTag("linearLayoutLevel" + nodeLevel);

            View sameLevelLinearViewToRemove = mainLayout.findViewWithTag("linearLayoutLevel" + nodeLevel);                // find previously created menu for this level, remove it so new container can be drawn instead of previous one, i.e. if user selects different menu on the same level

            if (previouslySelectButtonID == nodeLevel-1) {                                                      // replace same level containers by removing previously created layout
                Log.d("daa", "nextLevelLayout" + nodeLevel);
                mainLayout.removeView(sameLevelLinearViewToRemove);
            }


            int checkButtonLevel = nodeLevel-1;
            Log.d("btntag", checkButtonLevel  + " isto kao i " + v.getTag());

            final LinearLayout linearLayoutAtCurrentLevel = mainLayout.findViewWithTag("linearLayoutLevel" + checkButtonLevel);         // get linear layout on same level the user has clicked
            linearLayoutAtCurrentLevel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // mainLayout.removeView(linearLayoutAtCurrentLevel);
                    for (int i=0; i< linearLayoutAtCurrentLevel.getChildCount(); i++) {
                        Button buttonToShow = (Button) linearLayoutAtCurrentLevel.getChildAt(i);
                        if (buttonToShow.getId() != v.getId()) {
                            buttonToShow.setVisibility(View.VISIBLE);                   // hide Buttons on same level except the one user has clicked on
                            buttonToShow.setClickable(true);
                        }
                    }
                }
            });
            if (v.getTag().equals(String.valueOf(checkButtonLevel))) {
                Log.d("btntag", linearLayoutAtCurrentLevel.getTag().toString() + " ------ " + checkButtonLevel);
                for (int i=0; i< linearLayoutAtCurrentLevel.getChildCount(); i++) {
                    Button buttonToHide = (Button) linearLayoutAtCurrentLevel.getChildAt(i);
                    if (buttonToHide.getId() != v.getId()) {
                        buttonToHide.setVisibility(View.INVISIBLE);                   // hide Buttons on same level except the one user has clicked on
                        buttonToHide.setClickable(false);
                    }
                }
            }


            if (previouslySelectButtonID > Integer.parseInt(foundNode.getLevel()) && previouslySelectButtonID != Integer.MAX_VALUE) {       // remove all containers below selected one, if user wants to go back to previous level and select different menu
                Toast.makeText(getApplicationContext(), "Manji je lvl", Toast.LENGTH_SHORT).show();
                for (int i=0; i<linearLayoutLevelList.size(); i++) {
                    LinearLayout currentLinearLayout = mainLayout.findViewWithTag(linearLayoutLevelList.get(i));
                    if (currentLinearLayout != null) {
                        Log.d("gjds", currentLinearLayout.getTag().toString());
                        mainLayout.removeView(currentLinearLayout);
                    }
                }
            }


            previouslySelectButtonID = Integer.parseInt(v.getTag().toString());

            for (GenericTreeBuilder node : childrenNodes) {
                Button button = new Button(this);
                button.setText(node.getName());
                button.setId(Integer.parseInt(node.getId()));
                button.setTag(node.getLevel());
                button.setOnClickListener(this);
                button.setLayoutParams(buttonParams);
                buttonLevelList.add(button.getTag());           // save Button tags into list for hiding buttons on the same level
                nextLevelLayout.addView(button);
            }

            linearLayoutLevelList.add(nextLevelLayout.getTag());

            //relativeLayout.addView(nextLevelLayout);
            mainLayout.addView(nextLevelLayout, 1);             // number "1" means - add nextLevelLayout at first position, that is ABOVE currently drawn linear layout
            Toast.makeText(getApplicationContext(), "Ima " + broj + " djece!", Toast.LENGTH_SHORT).show();
        }
    }


    public GenericTreeBuilder findNodeWithSpecificID(GenericTreeBuilder n, String id) {
        if (n.getId().equals(id)) {
            return n;
        } else {
            for (int i = 0; i < n.getChildren().size(); i++) {
                GenericTreeBuilder jedanNode = (GenericTreeBuilder) n.getChildren().get(i);
                Log.d("kw", jedanNode.getId() + " " + jedanNode.getName());
                GenericTreeBuilder result = findNodeWithSpecificID(jedanNode, id);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }


    public List getChildrenOfSpecificNode(GenericTreeBuilder specificNode) {
        List<GenericTreeBuilder> childrenNodes = new ArrayList<>();
        childrenNodes.clear();
        childrenNodes = specificNode.getChildren();
        if (childrenNodes.size() > 0) {                               // print children of found node
            int i=0;
            for (GenericTreeBuilder node : new ArrayList<>(childrenNodes)) {
                i++;
                // Log.d("gggg", "Dijete " +  i + ": " + node.getName());
                if (!childrenNodes.contains(node)) {
                    childrenNodes.add(node);
                }
            }
        }
        return childrenNodes;
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mFloatingView != null) mWindowManager.removeView(mFloatingView);
    }




    static boolean isPointWithin(int x, int y, int x1, int x2, int y1, int y2) {
        return (x <= x2 && x >= x1 && y <= y2 && y >= y1);
    }


}