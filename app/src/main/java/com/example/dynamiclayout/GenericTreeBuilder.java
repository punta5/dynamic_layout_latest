package com.example.dynamiclayout;

import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GenericTreeBuilder<T> implements Serializable {
    private String id;
    private String name;
    private String level;
    private List<GenericTreeBuilder> children = new ArrayList<>();
    private GenericTreeBuilder parent = null;

    public GenericTreeBuilder(String name, String id, String level) {
        this.name = name;
        this.id = id;
        this.level = level;
    }

    public void addChild(GenericTreeBuilder child) {
        //child.setParent(this);
        this.children.add(child);
    }

    public void addChild(T data) {
        GenericTreeBuilder<T> newChild = new GenericTreeBuilder<>(name, id, level);
        //this.addChild(newChild);
        children.add(newChild);
    }

    public void addChildren(List<GenericTreeBuilder> children) {
        /*
        for(MyTreeNode t : children) {
            t.setParent(this);
        }
         */
        this.children.addAll(children);
    }

    public boolean isLeaf(){
        return(children.isEmpty());
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{").append(getName().toString()).append(",[");
        int i = 0;
        for(GenericTreeBuilder e : getChildren())
        {
            if(i > 0)
            {
                sb.append(",");
            }
            sb.append(e.getName().toString());
            i++;
        }
        sb.append("]").append("}");
        return sb.toString();
    }

    //Preorder traversal
    private void walk(GenericTreeBuilder<T> element, List<GenericTreeBuilder<T>> list)
    {
        list.add(element);
        for(GenericTreeBuilder data : element.getChildren()) {
            walk(data, list);
        }
    }

    public List<GenericTreeBuilder> getChildren() {
        return children;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLevel() {
        return level;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setParent(GenericTreeBuilder parent) {
        this.parent = parent;
    }

    public GenericTreeBuilder getParent() {
        return parent;
    }
}