/*
 * Student ID: 1394392
 * Name: Renfei Yu (Klaus)
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.rmi.RemoteException;

public class GameGUI {
    private JFrame frame;
    // the label that will display the text of "Time Left: XX"
    private JLabel timerLabel;
    // the input field that let the user input chat message
    private JTextField chatInput;
    private JLabel statusLabel;
    private Timer moveTimer;
    private final int time = 20;
    private JTextArea chatArea;
    private int timeLeft;
    private JButton[][] boardButtons = new JButton[3][3];
    private String username;
    private Client correspondClient;

    public GameGUI(String username, Client client) {
        this.username = username;
        this.correspondClient = client;

        // set the title of the GUI
        frame = new JFrame(username + " - Playing Tic-Tac-Toe");

        
        // initialize the time left
        this.timeLeft = time;

        // set the frame size
        frame.setSize(700, 450); 
        frame.setLayout(new BorderLayout());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // when the user closed the window, call the server's corresponding function
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e){
                // handle the disconnection of current client
                quitGame();
                // system exit since client has been disconnected
                // System.exit(0);
            }
        });

        // center panel initialization
        JPanel centerPanel = new JPanel(new BorderLayout());

        
        JPanel boardContainer = new JPanel();
        boardContainer.setLayout(new BoxLayout(boardContainer, BoxLayout.Y_AXIS));
        boardContainer.add(Box.createVerticalGlue()); 


        statusLabel = new JLabel("Waiting for a game ...");

        boardContainer.add(statusLabel); 
        

        // set the board panel to make it 3*3
        JPanel boardPanel = new JPanel(new GridLayout(3, 3));
        // set the size of board
        boardPanel.setPreferredSize(new Dimension(100, 250));
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                // add every grid as a button on board
                boardButtons[i][j] = new JButton();
                // set the color and font of button
                boardButtons[i][j].setBackground(Color.WHITE);
                Font buttonFont = new Font("Arial", Font.BOLD, 27);
                boardButtons[i][j].setFont(buttonFont);
                // assign the coordinate with final value to make it transfer to client
                final int p = i;
                final int k = j;
                // add action listener so that the button can show different sign based on different player
                boardButtons[i][j].addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e){
                        // if this is the input client's turn, let it make movement
                        // otherwise it cannot make movement
                        try {
                            if(client.myTurn()){
                               
                                client.boardClick(p,k);
                            }
                        } catch (RemoteException e1) {
                            e1.printStackTrace();
                        }
                        // if not "current client's turn", cannot make movement on board
                    }   
                });

                boardPanel.add(boardButtons[i][j]);
            }
        }

        boardContainer.add(boardPanel);  
        boardContainer.add(Box.createVerticalGlue()); 

        centerPanel.add(boardContainer, BorderLayout.CENTER);

        frame.add(centerPanel, BorderLayout.CENTER);



        // set the timer, the default count down will be 20 seconds
        JPanel leftPanel = new JPanel(new BorderLayout());
        timerLabel = new JLabel("Time Left "+timeLeft, SwingConstants.CENTER);
        moveTimer = new Timer(1000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // counjt down and show time left until 0
                if (timeLeft > 0) {
                    timeLeft--;
                    timerLabel.setText("Time Left " + timeLeft);
                } else {
                    
                    // if time is up, then force the client to make random move
                    try {
                        if(client.myTurn()){
                            client.forceMove();
                            
                            
                        }
                    } catch (RemoteException e1) {
                        e1.printStackTrace();
                    }

                    startTime();
                }
            }
        });

        JButton quitButton = new JButton("QUIT");
        quitButton.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e){
            quitGame();
          }  
        });

        leftPanel.add(timerLabel, BorderLayout.NORTH);
        leftPanel.add(new JLabel("Distributed Tic-Tac-Toe", SwingConstants.CENTER), BorderLayout.CENTER);
        leftPanel.add(quitButton, BorderLayout.SOUTH);

        frame.add(leftPanel, BorderLayout.WEST);

        JPanel rightPanel = new JPanel(new BorderLayout());
        chatArea = new JTextArea(20, 20);
        chatArea.setEditable(false);
        JScrollPane chatScrollPane = new JScrollPane(chatArea);

        // make the length per message not more than 20
        chatInput = new JTextField(20);
        chatInput.addActionListener(e -> {
            // chatArea.append(username + " : " + chatInput.getText() + "\n");
            client.sendMessage(chatInput.getText());
            chatInput.setText("");
        });

        rightPanel.add(chatScrollPane, BorderLayout.CENTER);
        rightPanel.add(chatInput, BorderLayout.SOUTH);

        frame.add(rightPanel, BorderLayout.EAST);

        timeLeft = time;
        moveTimer.start();

        frame.setVisible(true);
    }

    public JLabel getStatusLabel(){
        return this.statusLabel;
    }

    public Frame getFrame(){
        return this.frame;
    }


    public void startTime() {
        timeLeft = time;
        timerLabel.setText("Time Left " + timeLeft);
        moveTimer.start();
    }

    public void resetTime(){
        moveTimer.stop();
        timeLeft = time;
        timerLabel.setText("Time Left " + timeLeft);
    }

    public void modifyWinInformation(String string) {
        // use invoke later to make sure the server runs the rest of the code then run this function
        
        SwingUtilities.invokeLater(() -> {
            getFrame().setEnabled(true);
            getStatusLabel().setText("Waiting for a game ...");
            resetTime();

            hadnleOutCome(string);
        });
    }


    public void updateBoard(char[][] board){
        for(int i = 0; i < 3; i ++){
            for(int j = 0; j < 3; j ++){
                this.boardButtons[i][j].setText(Character.toString(board[i][j]));
            }
        }
    }

    

    public void hadnleOutCome(String message) {
        boolean userWantsToPlayAgain = promptForReplay(message);
    
        if (userWantsToPlayAgain) {
            tryToPlayAgain();
        } else {
            exitGame();
        }
    }
    
    private boolean promptForReplay(String message) {
        int option = JOptionPane.showConfirmDialog(this.getFrame(),
                                                   message + "\nWant to play another game?",
                                                   "Game Over",
                                                   JOptionPane.YES_NO_OPTION);
        return option == JOptionPane.YES_OPTION;
    }

    public void refresh() {
        // 1. Reset board buttons text
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                boardButtons[i][j].setText("");
            }
        }
    
        // 2. Clear chat area
        chatArea.setText("");
    
        // 3. Reset timer and timer label
        timeLeft = time;
        timerLabel.setText("Time Left " + timeLeft);
        resetTime();
    

    }
    
    private void tryToPlayAgain() {
        try {
            this.refresh();
            getClient().setmyTurn(false);
            getClient().askForReRegisterPlayer();
            
            
        } catch (RemoteException e) {
            handleError(e);
        }
    }


    public JButton[][] geButtons(){
        return this.boardButtons;
    }


    
    private void handleError(RemoteException e) {
        e.printStackTrace();
        System.exit(1);
    }
    
    private void exitGame() {
        System.exit(0);
    }

    public String getUsername(){
        return this.username;
    }

    public Client getClient(){
        return this.correspondClient;
    }

    public void quitGame(){
        if (getClient().getGameId() != -1 && userWantsToQuit()) {
            quitCurrentGame();
            closeClientApplication();
        }else if(getClient().getGameId() == -1 && userWantsToQuit()) {
            getClient().quitWaitingQueue();
            closeClientApplication();
        }
        
    }
    
    private boolean userWantsToQuit() {
        int confirm = JOptionPane.showConfirmDialog(this.getFrame(),
                                                    "Want to quit the game?",
                                                    "Quit",
                                                    JOptionPane.YES_NO_OPTION);
        
        System.out.println(confirm);
        return confirm == JOptionPane.YES_OPTION;
    }
    
    private void quitCurrentGame() {
            getClient().quiGame();
            
        
    }
    
    private void closeClientApplication() {
        System.exit(0);
    }



    public void modifTurnyInformation(String string) {

        getFrame().setEnabled(true);
        getStatusLabel().setText(string);

    }

    public void setChatHistory(String finalChat) {
        chatArea.setText("");
        chatArea.append(finalChat + "\n");
        getFrame().setEnabled(true);
    }


    public void banButton(){
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                boardButtons[i][j].setEnabled(false);
            }
        }
        
    }


    public void restartButton(){
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                boardButtons[i][j].setEnabled(true);
            }
        }
    }






}