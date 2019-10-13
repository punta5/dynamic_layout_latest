package com.example.dynamiclayout;

import android.app.Activity;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FloatingActivity extends Activity implements View.OnClickListener, Serializable {

    private static final int SYSTEM_ALERT_WINDOW_PERMISSION = 2084;
    GenericTreeBuilder<String> rootNode;
    GenericTreeBuilder<String> childLevelOne;
    GenericTreeBuilder<String> childLevelTwo;
    GenericTreeBuilder<String> childLevelThree;
    GenericTreeBuilder<String> childLevelFour;
    GenericTreeBuilder<String> childLevelFive;

    LinearLayout mainLayout;

    List nextLayoutList = new ArrayList();
    ArrayList layoutLevelList = new ArrayList();
    ArrayList buttonLevelList = new ArrayList();

    int previouslySelectButtonID = Integer.MAX_VALUE;       // for removing Containers at higher levels

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_layout);

        Bundle extras = getIntent().getExtras();

        List<GenericTreeBuilder> childrenNodes = null;
        if (extras != null) {
            rootNode = (GenericTreeBuilder<String>) getIntent().getSerializableExtra("ROOT_NODE");
            childLevelOne = (GenericTreeBuilder<String>) getIntent().getSerializableExtra("CHILD_LEVEL_ONE");
            childLevelTwo = (GenericTreeBuilder<String>) getIntent().getSerializableExtra("CHILD_LEVEL_TWO");
            childLevelThree = (GenericTreeBuilder<String>) getIntent().getSerializableExtra("CHILD_LEVEL_THREE");
            childLevelFour = (GenericTreeBuilder<String>) getIntent().getSerializableExtra("CHILD_LEVEL_FOUR");
            childLevelFive = (GenericTreeBuilder<String>) getIntent().getSerializableExtra("CHILD_LEVEL_FIVE");

            childrenNodes = rootNode.getChildren();
            if (childrenNodes.size() > 0) {                                     // print children of found node
                int i = 0;
                for (GenericTreeBuilder node : childrenNodes) {
                    i++;
                    Log.d("cool", "Dijete " + i + ": " + node.getName());
                    nextLayoutList.add(node.getName());
                }
            }
        }

        mainLayout = findViewById(R.id.mainLayout);          // main activity layout, always visible, never changes

        // here we create first level menu (Container at Level 1)
        if (childrenNodes != null) {
            int numberOfButtons = childrenNodes.size();     // so we can calculate weight of the buttons inside linear layout
            LinearLayout.LayoutParams firstLevelParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
            LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f/numberOfButtons);
            LinearLayout firstLevelLayout = new LinearLayout(this);     // create Linear Layout container for Level 1 nodes, visible when user opens menu for the first time
            firstLevelLayout.setLayoutParams(firstLevelParams);
            firstLevelLayout.setTag("layoutLevel1");                // if we eventually need to get first level container

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

            mainLayout.addView(firstLevelLayout);                   // add first menu container to root view

        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            askPermission();
        }

        // start floating layout service on button click
        findViewById(R.id.buttonCreateWidget).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    Intent service = new Intent(FloatingActivity.this, FloatingViewService.class);
                    service.putExtra("ROOT_NODE", rootNode);
                    service.putExtra("CHILD_LEVEL_ONE", childLevelOne);
                    service.putExtra("CHILD_LEVEL_TWO", childLevelTwo);
                    service.putExtra("CHILD_LEVEL_THREE", childLevelThree);
                    service.putExtra("CHILD_LEVEL_FOUR", childLevelFour);
                    service.putExtra("CHILD_LEVEL_FIVE", childLevelFive);
                    startService(service);
                    finish();
                    //startService(new Intent(FloatingActivity.this, FloatingViewService.class));
                    //finish();
                } else if (Settings.canDrawOverlays(getApplicationContext())) {
                    Intent service = new Intent(FloatingActivity.this, FloatingViewService.class);
                    service.putExtra("ROOT_NODE", rootNode);
                    service.putExtra("CHILD_LEVEL_ONE", childLevelOne);
                    startService(service);
                    finish();
                } else {
                    askPermission();
                    Toast.makeText(getApplicationContext(), "You need to enable System Alert Window Permission!", Toast.LENGTH_SHORT).show();
                }
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
            LinearLayout.LayoutParams nextLevelParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
            LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f/numberOfButtons);
            LinearLayout nextLevelLayout = new LinearLayout(this);
            int nodeLevel = Integer.parseInt(foundNode.getLevel()) + 1;         // Linear Layout levels start at 1
            nextLevelLayout.setTag("layoutLevel" + nodeLevel);

            View sameLevelViewToRemove = mainLayout.findViewWithTag("layoutLevel" + nodeLevel);                // find previously created menu for this level, remove it so new container can be drawn instead of previous one, i.e. if user selects different menu on the same level

            if (previouslySelectButtonID == nodeLevel-1) {                                                      // replace same level containers by removing previously created layout
                Log.d("daa", "nextLevelLayout" + nodeLevel);
                mainLayout.removeView(sameLevelViewToRemove);
            }


            int checkButtonLevel = nodeLevel-1;
            Log.d("btntag", checkButtonLevel  + " isto kao i " + v.getTag());

            final LinearLayout linearLayoutAtCurrentLevel = mainLayout.findViewWithTag("layoutLevel" + checkButtonLevel);         // get linear layout on same level the user has clicked
            final LinearLayout linearLayoutAtNextLevel = mainLayout.findViewWithTag("layoutLevel" + nodeLevel);         // get linear layout on same level the user has clicked
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
                for (int i=0; i<layoutLevelList.size(); i++) {
                    LinearLayout currentLayout = mainLayout.findViewWithTag(layoutLevelList.get(i));
                    if (currentLayout != null) {
                        Log.d("gjds", currentLayout.getTag().toString());
                        mainLayout.removeView(currentLayout);
                    }
                }
            }


            previouslySelectButtonID = Integer.parseInt(v.getTag().toString());

            nextLevelLayout.setLayoutParams(nextLevelParams);
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

            layoutLevelList.add(nextLevelLayout.getTag());

            mainLayout.addView(nextLevelLayout);
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


    private void askPermission() {
        Intent intent = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
        }
        startActivityForResult(intent, SYSTEM_ALERT_WINDOW_PERMISSION);
    }


    @Override
    public void onSaveInstanceState(Bundle bundle) {
        bundle.putString("key", "value"); // use the appropriate 'put' method
        // store as much info as you need
        super.onSaveInstanceState(bundle);
    }

    @Override
    public void onRestoreInstanceState(Bundle bundle) {
        super.onRestoreInstanceState(bundle);
        bundle.getString("key"); // again, use the appropriate 'get' method.
        // get your stuff
        // add views dynamically
    }

}