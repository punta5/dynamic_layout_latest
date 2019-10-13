package com.example.dynamiclayout;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.w3c.dom.Node;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements Serializable {

    public static int PICK_FILE = 321;
    GenericTreeBuilder<String> rootNode;
    GenericTreeBuilder<String> childLevelOne;
    GenericTreeBuilder<String> childLevelTwo;
    GenericTreeBuilder<String> childLevelThree;
    GenericTreeBuilder<String> childLevelFour;
    GenericTreeBuilder<String> childLevelFive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button chooseFileBtn = findViewById(R.id.chooseFileBtn);
        chooseFileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("text/plain");
                startActivityForResult(intent, PICK_FILE);
            }
        });


        GenericTreeBuilder<String> root = new GenericTreeBuilder<>("Root", "#52363", "1");

        GenericTreeBuilder<String> child1 = new GenericTreeBuilder<>("Child1", "#52363", "2");
        child1.addChild("Grandchild1");
        child1.addChild("Grandchild2");

        GenericTreeBuilder<String> child2 = new GenericTreeBuilder<>("Child2", "#52363", "3");
        child2.addChild("Grandchild3");

        root.addChild(child1);
        root.addChild(child2);
        root.addChild("Child3");

        root.addChildren(Arrays.<GenericTreeBuilder>asList(
                new GenericTreeBuilder<>("Child4", "#52363", "4"),
                new GenericTreeBuilder<>("Child5", "#52363", "4"),
                new GenericTreeBuilder<>("Child6", "#52363", "4")
        ));

        child1.addChild(new GenericTreeBuilder<>("Dijete 1", "#52363", "5"));
        child1.addChild(new GenericTreeBuilder<>("Dijete 2", "#79675", "6"));
        child1.addChild(new GenericTreeBuilder<>("Dijete 3", "#385843", "7"));

        for (GenericTreeBuilder cvor : child1.getChildren()) {
            Log.d("fa", cvor.getId());
        }

        for (GenericTreeBuilder node : root.getChildren()) {
            System.out.println(node.getName());
        }


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_FILE) {
            if (resultCode == RESULT_OK) {
                // User pick the file
                Uri uri = data.getData();
                String fileContent = readTextFile(uri);

                Intent floatingActivity = new Intent(MainActivity.this, FloatingActivity.class);
                floatingActivity.putExtra("ROOT_NODE", (Serializable) rootNode);
                floatingActivity.putExtra("CHILD_LEVEL_ONE", (Serializable) childLevelOne);
                floatingActivity.putExtra("CHILD_LEVEL_TWO", (Serializable) childLevelTwo);
                floatingActivity.putExtra("CHILD_LEVEL_THREE", (Serializable) childLevelThree);
                floatingActivity.putExtra("CHILD_LEVEL_FOUR", (Serializable) childLevelFour);
                floatingActivity.putExtra("CHILD_LEVEL_FIVE", (Serializable) childLevelFive);
                startActivity(floatingActivity);
                finish();

            } else {
                Log.d("ovo", data.toString());
            }
        }
    }


    private String readTextFile(Uri uri) {
        BufferedReader reader = null;
        StringBuilder builder = new StringBuilder();
        try {
            reader = new BufferedReader(new InputStreamReader(getContentResolver().openInputStream(uri)));
            String line = "";

            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
                // Log.d("ovo", line);
                int numberOfTabsInLine = line.replaceAll("[^\t]", "").length();
                // Log.d("da", numberOfTabsInLine + " ");

                if (numberOfTabsInLine == 0) {
                    rootNode = new GenericTreeBuilder<>("Root node", lineNumber + "", "0");                                      // Create Root node at Level 0
                    Log.d("ajde", rootNode.getId() + " -- " + rootNode.getName());
                } else if (numberOfTabsInLine == 1) {
                    childLevelOne = new GenericTreeBuilder<>(line.replace("\t", "") + lineNumber, lineNumber + "", "1");         // Create children of Root node at Level 1
                    childLevelOne.setParent(rootNode);
                    rootNode.addChild(childLevelOne);
                } else if (numberOfTabsInLine == 2) {
                    childLevelTwo = new GenericTreeBuilder<>(line.replace("\t", "") + lineNumber, lineNumber + "", "2");         // Create children of childLevelOne node at Level 2
                    childLevelTwo.setParent(childLevelOne);
                    // Log.d("koji", childLevelOne.getName() + "  --> " + childLevelTwo.getName());
                    childLevelOne.addChild(childLevelTwo);
                } else if (numberOfTabsInLine == 3) {
                    childLevelThree = new GenericTreeBuilder<>(line.replace("\t", "") + lineNumber, lineNumber + "", "3");       // Create children of childLevelTwo node at Level 3
                    childLevelThree.setParent(childLevelTwo);
                    // Log.d("koji", childLevelOne.getName() + "  --> " + childLevelTwo.getName());
                    childLevelTwo.addChild(childLevelThree);
                } else if (numberOfTabsInLine == 4) {
                    childLevelFour = new GenericTreeBuilder<>(line.replace("\t", "") + lineNumber, lineNumber + "", "4");       // Create children of childLevelThree node at Level 4
                    childLevelFour.setParent(childLevelThree);
                    // Log.d("koji", childLevelOne.getName() + "  --> " + childLevelTwo.getName());
                    childLevelThree.addChild(childLevelFour);
                } else if (numberOfTabsInLine == 5) {
                    childLevelFive = new GenericTreeBuilder<>(line.replace("\t", "") + lineNumber, lineNumber + "", "5");       // Create children of childLevelFour node at Level 5
                    childLevelFive.setParent(childLevelFour);
                    // Log.d("koji", childLevelOne.getName() + "  --> " + childLevelTwo.getName());
                    childLevelFour.addChild(childLevelFive);
                }
                lineNumber++;
            }

            for (GenericTreeBuilder node : rootNode.getChildren()) {
                //Log.d("hehe", node.getName().toString());
                //Log.d("hehe", "Roditelj: " + node.getParent().getName());
                //Log.d("haha", node.toString());
            }

            //    !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            GenericTreeBuilder foundNode = findNodeWithId(rootNode, "0");      // this is the node that we've found
            if (foundNode != null) {
                if (foundNode.getParent() != null) {
                    Log.d("ajde", "Roditelj: " + foundNode.getParent().getName());
                }
                Log.d("ajde", "Pronašao!: " + foundNode.getName() + "   " + foundNode.getChildren().size());
                List<GenericTreeBuilder> childrenNodes;
                childrenNodes = foundNode.getChildren();
                if (childrenNodes.size() > 0) {                               // print children of found node
                    int i=0;
                    for (GenericTreeBuilder jedan : childrenNodes) {
                        i++;
                        Log.d("ajde", "Dijete " +  i + ": " + jedan.getName());
                    }
                }

                //printChildrenOfSpecificNode(mojNode, childrenNodes);
            }
            // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return builder.toString();
    }

    public void printChildrenOfSpecificNode(GenericTreeBuilder mojNode, List djeca) {
        for (Object svaki : mojNode.getChildren()) {
            djeca.add(svaki);
            Log.d("cast", svaki.toString());
            printChildrenOfSpecificNode((GenericTreeBuilder) svaki, djeca);
        }
    }


    public GenericTreeBuilder findNodeWithId(GenericTreeBuilder n, String id) {
        if (n.getId().equals(id)) {
            return n;
        } else {
            for (int i = 0; i < n.getChildren().size(); i++) {
                GenericTreeBuilder jedanNode = (GenericTreeBuilder) n.getChildren().get(i);
                Log.d("kw", jedanNode.getId() + " " + jedanNode.getName());
                GenericTreeBuilder result = findNodeWithId(jedanNode, id);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }


}
