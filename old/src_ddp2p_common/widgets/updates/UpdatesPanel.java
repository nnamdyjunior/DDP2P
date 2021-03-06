package widgets.updates;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusListener;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;
import javax.swing.ButtonGroup;
import javax.swing.JFrame;

import recommendationTesters.RecommendationOfTestersSender;
import recommendationTesters.RecommenderOfTesters;
import recommendationTesters.TesterAndScore;

import util.P2PDDSQLException;

import config.Application;
import config.Application_GUI;
import config.DD;
import util.DBInterface;
import static util.Util.__;
import widgets.components.GUI_Swing;
import widgets.dir_management.DirPanel;
import widgets.updatesKeys.*;

public class UpdatesPanel extends JPanel implements ActionListener, FocusListener {
    private static final boolean DEBUG = false;
	String numberString=__("Number");
    String percentageString=__("Percentage");
    String manualRatingString=__("Manual Rating");
    String autoRatingString=__("Automatic Rating");
    public JTextField numberTxt = new JTextField(3);
    public JTextField percentageTxt = new JTextField(3);
    public JRadioButton numberButton = new JRadioButton(numberString);
    public JRadioButton percentageButton = new JRadioButton(percentageString);
    public JRadioButton autoRatingButton = new JRadioButton(autoRatingString);
    public JRadioButton manualRatingButton = new JRadioButton(manualRatingString);
	public JCheckBox absoluteCheckBox =  new JCheckBox();
	private UpdatesKeysTable updateKeysTable;
	public static TesterAndScore[] knownTestersList = null;
	public static TesterAndScore[] usedTestersList = null;
	public static Float scoreMatrix[][] = null;
	public static Long[] receivedTesters;
	public static Long[] sourcePeers;
    public UpdatesPanel() {
    	super( new GridLayout(2,1));// hold two tables (UpdateTatble+QualitiesTable)
    	numberTxt.setText(""+DD.UPDATES_TESTERS_THRESHOLD_COUNT_DEFAULT);
    	percentageTxt.setText(""+DD.UPDATES_TESTERS_THRESHOLD_WEIGHT_DEFAULT);
    	init();
    	GUI_Swing.panelUpdates = this;
    }
   public JPanel buildTesterControlsPanel(){
		JButton recalculateTestersRating = new JButton(__("Recalculate Testers Rating"));
		JButton ConsultRecommender = new JButton(__("Consult the Recommender System"));
		JButton sendRecommendations = new JButton(__("Send Recommendations"));
	   
		JPanel testerControls = new JPanel(new BorderLayout());
		//testerControls.setBackground(Color.DARK_GRAY);
		testerControls.add(buildThresholdPanel() , BorderLayout.WEST);
		
		JPanel buttonsPanel = new JPanel();
		buttonsPanel.add(recalculateTestersRating);
		recalculateTestersRating.addActionListener(this);
		recalculateTestersRating.setActionCommand("recalculate");
		
		buttonsPanel.add(sendRecommendations);
		buttonsPanel.setBackground(Color.DARK_GRAY);
		sendRecommendations.addActionListener(this);
		sendRecommendations.setActionCommand("sendRecommendations");
		
		buttonsPanel.add(ConsultRecommender);
		buttonsPanel.setBackground(Color.DARK_GRAY);
		ConsultRecommender.addActionListener(this);
		ConsultRecommender.setActionCommand("Consult");
		
		
		
		testerControls.add(buttonsPanel, BorderLayout.EAST);
		 
		return testerControls;
	}
   public JPanel buildAutoManualPanel() {
	   boolean autoSeleced = true;
	   try {
		autoSeleced = DD.getAppBoolean(DD.AUTOMATIC_TESTERS_RATING_BY_SYSTEM);
	} catch (P2PDDSQLException e) {
		e.printStackTrace();
	}  
   	   JLabel ratingL = new JLabel("Testers Rating  : ");
   	   ratingL.setFont(new Font("Times New Roman",Font.BOLD,14));
   	
       manualRatingButton.setMnemonic(KeyEvent.VK_M);
       manualRatingButton.setActionCommand(manualRatingString);
       manualRatingButton.setSelected(!autoSeleced);
       manualRatingButton.addActionListener(this);
       manualRatingButton.setFont(new Font(null,Font.BOLD,12));
       JLabel spaceL = new JLabel("       ");
       JLabel space2L = new JLabel("       ");
   	
       autoRatingButton.setMnemonic(KeyEvent.VK_A);
       autoRatingButton.setActionCommand(autoRatingString);
       autoRatingButton.setSelected(autoSeleced);
       autoRatingButton.setFont(new Font(null,Font.BOLD,12));
       autoRatingButton.addActionListener(this);
       
       JPanel autoManualPanel = new JPanel();
       autoManualPanel.add(ratingL);
       autoManualPanel.add(autoRatingButton);
       autoManualPanel.add(spaceL);   
       autoManualPanel.add(manualRatingButton);
       autoManualPanel.add(space2L);
 
   	
   	//Group the radio buttons.
       ButtonGroup group = new ButtonGroup();
       group.add(manualRatingButton);
       group.add(autoRatingButton);

   	
   	JPanel autoManualPanel2 = new JPanel(new BorderLayout());
   	autoManualPanel2.add(autoManualPanel,BorderLayout.WEST );
   	return autoManualPanel2;
   }
    public JPanel buildThresholdPanel() {
    
    	JLabel thresholdL = new JLabel("Threshold  : ");
    	thresholdL.setFont(new Font("Times New Roman",Font.BOLD,14));
    	
        numberButton.setMnemonic(KeyEvent.VK_N);
        numberButton.setActionCommand(numberString);
        numberButton.setSelected(true);
        numberButton.addActionListener(this);
        numberButton.setFont(new Font(null,Font.BOLD,12));
        JLabel spaceL = new JLabel("       ");
//    	numberL.setFont(new Font(null,Font.BOLD,12));
    	
        numberTxt.addActionListener(this);
        numberTxt.addFocusListener(this);
        numberTxt.setActionCommand(numberString);
    	JPanel thresholdPanel = new JPanel();
    	
    	thresholdPanel.add(thresholdL);
    	thresholdPanel.add(numberButton);
    	thresholdPanel.add(numberTxt);
    	thresholdPanel.add(spaceL);
    	
    	
        percentageButton.setMnemonic(KeyEvent.VK_P);
        percentageButton.setActionCommand(percentageString);
        percentageButton.addActionListener(this);
        
        JLabel percentageL = new JLabel(" %");
//    	percentageL.setFont(new Font(null,Font.BOLD,12));
    	
    	percentageTxt.addActionListener(this);
    	percentageTxt.addFocusListener(this);
    	percentageTxt.setActionCommand(percentageString);
    	percentageTxt.setEnabled(false);
    	thresholdPanel.add(percentageButton);
    	thresholdPanel.add(percentageTxt);
    	thresholdPanel.add(percentageL);
    	
    	//Group the radio buttons.
        ButtonGroup group = new ButtonGroup();
        group.add(numberButton);
        group.add(percentageButton);

    	
    	JPanel thresholdPanel2 = new JPanel(new BorderLayout());
    	thresholdPanel2.add(thresholdPanel,BorderLayout.WEST );
    	return thresholdPanel2;
    }
    public void init(){
        JPanel updatePanel = new JPanel(new BorderLayout());
        
        JLabel updateMirrorsTitleL = new JLabel(" Update Mirrors Preferences ");
        updateMirrorsTitleL.setFont(new Font("Times New Roman",Font.BOLD,20));
        updateMirrorsTitleL.setHorizontalAlignment(SwingConstants.CENTER);
        updateMirrorsTitleL.setVerticalAlignment(SwingConstants.CENTER);
        JPanel mirrorsTitilePanel = new JPanel(new BorderLayout());
        mirrorsTitilePanel.add(new JPanel(), BorderLayout.NORTH);
        mirrorsTitilePanel.add(updateMirrorsTitleL);
        mirrorsTitilePanel.add(new JPanel(), BorderLayout.SOUTH);
        
        
    	updatePanel.add(mirrorsTitilePanel,BorderLayout.NORTH );
    	
    	UpdatesTable updateTable = new UpdatesTable(this);
    	JPanel updateTablePanel = new JPanel(new BorderLayout());
		updateTablePanel.add(updateTable.getTableHeader(),BorderLayout.NORTH);
		updateTablePanel.add(updateTable.getScrollPane());
        updatePanel.add(updateTablePanel);
        
        updatePanel.add(new JPanel(), BorderLayout.SOUTH);
        this.add(updatePanel);
        
        
        JPanel updateKeysPanel = new JPanel(new BorderLayout());
        JLabel updateKeysTitleL = new JLabel(" Testers Preferences ");
        updateKeysTitleL.setHorizontalAlignment(SwingConstants.CENTER);
        updateKeysTitleL.setVerticalAlignment(SwingConstants.CENTER);
        JPanel titilePanel = new JPanel(new BorderLayout());
        titilePanel.add(new JPanel(), BorderLayout.NORTH);
        titilePanel.add(updateKeysTitleL);
        titilePanel.add(buildAutoManualPanel(), BorderLayout.SOUTH);
    	updateKeysTitleL.setFont(new Font("Times New Roman",Font.BOLD,20));
    	updateKeysPanel.add(titilePanel,BorderLayout.NORTH );
    	
        updateKeysTable = new UpdatesKeysTable();
    	JPanel updateKeysTablePanel = new JPanel(new BorderLayout());
		updateKeysTablePanel.add(updateKeysTable.getTableHeader(),BorderLayout.NORTH);
		updateKeysTablePanel.add(updateKeysTable.getScrollPane());
        updateKeysPanel.add(updateKeysTablePanel );
        updateKeysPanel.add(buildTesterControlsPanel(), BorderLayout.SOUTH);
        this.add(updateKeysPanel);
            
    }
    @Override
    public void focusGained(FocusEvent e) {
     
    }
    @Override
    public void focusLost(FocusEvent e) {
        if( e.getSource().equals(numberTxt))
     	   handleTxtFiled(numberString);
        if( e.getSource().equals(percentageTxt))
           handleTxtFiled(percentageString);
    }
    public void handleTxtFiled(String txtType){
    //	System.out.println("e.getSource() instanceof JTextField : "+ numberTxt.getText());
       	if(txtType.equals(numberString)){
       		// System.out.println("numberTxt.getText() : "+ numberTxt.getText());
    		try {
    			String text = numberTxt.getText();
 				try{if(text != null)text = ""+Integer.parseInt(text);}
				catch(Exception e){numberTxt.setText(text=""+DD.UPDATES_TESTERS_THRESHOLD_COUNT_DEFAULT);};
				if(text == null) text = ""+DD.UPDATES_TESTERS_THRESHOLD_COUNT_DEFAULT;
    			DD.setAppTextNoSync(DD.UPDATES_TESTERS_THRESHOLD_COUNT_VALUE, text);
			} catch (P2PDDSQLException e1) {
				e1.printStackTrace();
				return;
			}
    	}
       	if(txtType.equals(percentageString)){
      // 		System.out.println("percentageTxt.getText() : "+percentageTxt.getText());
    		try {
    			String text = percentageTxt.getText();
 				try{if(text != null)text = ""+Float.parseFloat(text);}
				catch(Exception e){percentageTxt.setText(text=""+DD.UPDATES_TESTERS_THRESHOLD_WEIGHT_DEFAULT);};
				if(text == null) text = ""+DD.UPDATES_TESTERS_THRESHOLD_WEIGHT_DEFAULT;
				DD.setAppTextNoSync(DD.UPDATES_TESTERS_THRESHOLD_WEIGHT_VALUE, text);
			} catch (P2PDDSQLException e1) {
				e1.printStackTrace();
				return;
			}
    	}  	
    }
        @Override
        public void actionPerformed(ActionEvent e) {
        	if(e.getSource() instanceof JRadioButton){
        		JRadioButton r = (JRadioButton)e.getSource();
        		if(r.getActionCommand().equals(numberString) && r.isSelected()){
        			numberTxt.setEnabled(true);
        			percentageTxt.setEnabled(false);
        			DD.setAppBoolean(DD.UPDATES_TESTERS_THRESHOLD_WEIGHT, false);
        		}
        		if(r.getActionCommand().equals(percentageString)&& r.isSelected()){
        			numberTxt.setEnabled(false);
        			percentageTxt.setEnabled(true);
        			DD.setAppBoolean(DD.UPDATES_TESTERS_THRESHOLD_WEIGHT, true);
        		}
        		if(r.getActionCommand().equals(autoRatingString)&& r.isSelected()){
        			DD.setAppBoolean(DD.AUTOMATIC_TESTERS_RATING_BY_SYSTEM, true);
        			updateKeysTable.setColumnBackgroundColor(Color.GRAY);
        			refreshRecommendations();
        			updateKeysTable.getModel().update(null, null);
        			updateKeysTable.repaint();
        			
        		}
        		if(r.getActionCommand().equals(manualRatingString)&& r.isSelected()){
        			DD.setAppBoolean(DD.AUTOMATIC_TESTERS_RATING_BY_SYSTEM, false);
        			updateKeysTable.setColumnBackgroundColor(Color.WHITE);
        			updateKeysTable.getModel().update(null, null);
        			//updateKeysTable.repaint();
        		}
        	}
        	if(e.getSource() instanceof JTextField){
        		handleTxtFiled(e.getActionCommand());	
        	}
        	if(e.getSource() instanceof JButton){
        		JButton b = (JButton)e.getSource();
        		if(b.getActionCommand().equals("Consult")){
        			showConsultingPanel();
        		}else if(b.getActionCommand().equals("recalculate")){
        			refreshRecommendations();
        		}else if(b.getActionCommand().equals("sendRecommendations")){
        			sendRecommendations();
        		}
        		
        			
        	}
        }
    	private void sendRecommendations() {
    		if(knownTestersList == null || usedTestersList == null){
				Application_GUI.warning("You need to click on recalculate ratings first!!", "No Data to Show");
				return;
			}
    		//RecommenderOfTesters.runningRecommender.recommendTesters();
    		RecommendationOfTestersSender.announceRecommendation(knownTestersList, usedTestersList);
			
		}
		private void refreshRecommendations() {
//    		RecommenderOfTesters rt = new RecommenderOfTesters(false);
//			rt.recommendTesters();
			if (RecommenderOfTesters.runningRecommender == null) {
				Application_GUI.warning("Recommender Process Not Started Yet!", "Failure to Refresh Recommender Data!");
				return;
			}
			RecommenderOfTesters.runningRecommender.recommendTesters();
			updateKeysTable.getModel().update(null, null);
			updateKeysTable.repaint();
			
		}
		private void showConsultingPanel() {
			//if(knownTestersList == null)
			//	RecommenderOfTesters.startRecommenders(true);
//			if(knownTestersList == null){
//				if(!DEBUG) System.out.println("UpdatesPanel:showConsultingPanel(): knownTestersList = null ?");
//				return;
//			}
//			if(usedTestersList == null){
//				if(!DEBUG) System.out.println("UpdatesPanel:showConsultingPanel(): usedTestersList = null ?");
//				return;
//			}
//			
//			if(scoreMatrix == null){
//				if(!DEBUG) System.out.println("UpdatesPanel:showConsultingPanel(): scoreMatrix = null ?");
//				return;
//			}
//			RecommenderOfTesters rt = new RecommenderOfTesters(false);
//			rt.recommendTesters();
//			
			if(knownTestersList == null){
				Application_GUI.warning("You need to click on recalculate ratings first!!", "No Data to Show");
				return;
			}
			
			TestersListsTable knownTestersTable = new TestersListsTable(new TestersListsModel(knownTestersList)) ;
			TestersListsTable usedTestersTable = new TestersListsTable(new TestersListsModel(usedTestersList)) ;
			RecommendationsTable recommendationsTable = new RecommendationsTable(new RecommendationsModel(scoreMatrix, sourcePeers, receivedTesters)) ;
			ConsultingRecommendationsPanel p = new ConsultingRecommendationsPanel(knownTestersTable, usedTestersTable, recommendationsTable);
			//p.setSize(200, 300);
			JOptionPane.showMessageDialog(null,p,"Consulting Recommendations", JOptionPane.DEFAULT_OPTION, null);
			//JOptionPane.showMessageDialog(null,p);
    	}
		public static void main(String args[]) {
		JFrame frame = new JFrame();
		try {
			Application.db = new DBInterface(Application.DEFAULT_DELIBERATION_FILE);
			UpdatesPanel updatePanel = new UpdatesPanel();
			frame.setContentPane(updatePanel);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.pack();
			frame.setSize(800,300);
			frame.setVisible(true);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
		public JTable getTestersTable() {
			return this.updateKeysTable;
		}
    
}