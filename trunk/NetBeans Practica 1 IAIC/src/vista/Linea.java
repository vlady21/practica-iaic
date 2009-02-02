/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package vista;

import java.awt.Color;

/**
 *
 * @author Victor
 */
public class Linea {

    int x1, y1, x2, y2;

    Color color;

    public Linea(int x1, int y1, int x2, int y2){

        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;

        color = Color.BLACK;

    }

    public Linea(int x1, int y1, int x2, int y2, Color color){

        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;

        this.color = color;

    }

    public void setColor(Color color){

        this.color = color;
    }

    public Color getColor(){

        return color;
    }

    public int getX1(){

        return x1;
    }

    public int getY1(){

        return y1;
    }

    public int getX2(){

        return x2;
    }

    public int getY2(){

        return y2;
    }

    public String toString(){

        return "("+x1+","+y1+","+x2+","+y2+")";
    }

    public boolean isEqual(Linea l){

        if(l.getX1()==x1&&l.getX2()==x2&&l.getY1()==y1&&l.getY2()==y1)
            return true;
        else
            return false;
    }
}
