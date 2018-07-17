// Robert Kucera
// CSCI 437W
// Battle.java
//
// This program creates the game frame for the rts game

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

public class Battle extends JFrame
{

    public Battle()
    {
        initUI();
    }
    
    private void initUI()
    {
        add(new Map());
        pack();
        setTitle("Battle!");
        
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
    }
    
    public static void main(String[]args)
    {
        EventQueue.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                Battle win = new Battle();
                win.setVisible(true);
            }
        });

        }
}
