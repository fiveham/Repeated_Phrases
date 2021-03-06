package repeatedphrases;

import java.awt.event.ActionEvent;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.swing.border.LineBorder;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;
import operate.Folder;
import operate.RepeatedPhrasesApp;
import operate.Trail;
import text.Chapter;

/**
 * <p>The GUI for this application. Creates a small window that displays its current working
 * directory, has text fields for entering the name of a trail file and a phrase size minimum for
 * linking, has an exit button that closes the program, has a label for displaying messages about
 * the current operation, and has four buttons to perform individual operations: create needed
 * folders; turn the HTML books into fully linked chapters; change the order of chapters after
 * having created fully linked chapters, and; change the order of chapters without changing the
 * chapter sequence used by inter-phrase links.</p>
 */
public class RepeatedPhrasesUI extends JFrame {
	
  /**
   * <p>Automatically generated.</p>
   */
	private static final long serialVersionUID = -5770990784488877775L;
	
	private final RepeatedPhrasesApp app;
	
  /**
   * <p>Creates new form RepeatedPhrasesUI</p>
   */
  public RepeatedPhrasesUI() {
    this.app = new RepeatedPhrasesApp(statusLabelMsg);
    initComponents();
    this.setTitle("Repeated Phrase Analyser");
  }
  
  /**
   * <p>Initializes the components. This was created automagically in NetBeans; so, it's "that
   * dark shadowy place" in The Lion King: "You must never go there, my son."</p>
   */
  private void initComponents() {
    
    jPanel1 = new JPanel();
    pwdTitleLabel = new JLabel();
    pwdLabel = new JLabel();
    trailFileLabel = new JLabel();
    phraseSizeLimitLabel = new JLabel();
    phraseSizeLimitField = new JTextField();
    trailFileField = new JTextField();
    createFoldersButton = new JButton();
    chapterizeLinkButton = new JButton();
    changeOrderButton = new JButton();
    changeTrailButton = new JButton();
    exitButton = new JButton();
    statusTitleLabel = new JLabel();
    statusLabel = new JLabel();
    
    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    
    jPanel1.setBorder(new LineBorder(new Color(0, 0, 0), 2, true));
    jPanel1.setFont(new Font("Tahoma", 0, 10)); // NOI18N
    
    pwdTitleLabel.setText("Operating location (working directory):");
    
    pwdLabel.setText(new File(".").getAbsolutePath());
    
    trailFileLabel.setText("Trail file");
    trailFileLabel.setToolTipText("Address/name of a trail file relative to the current working" 
        + " directory");
    
    phraseSizeLimitLabel.setText("Min word-count for phrase-link");
    phraseSizeLimitLabel.setToolTipText("The minimum number of words a repeated phrase must " 
    		+ "have to have links applied on its instances");
    
    createFoldersButton.setText("Create Folders");
    createFoldersButton.setToolTipText("Creates the folders needed for this program to work in" 
    		+ " the current working direcory");
    createFoldersButton.addActionListener(this::createFoldersButtonActionPerformed);
    
    chapterizeLinkButton.setToolTipText("Splits the books into chapters, finds linkable " 
    		+ "repeated phrases, and links them");
    chapterizeLinkButton.setText("Chapterize Books; Add Links");
    chapterizeLinkButton.addActionListener(this::chapterizeLinkButtonActionPerformed);
    
    changeOrderButton.setToolTipText("Changes the order of chapters used for linking to a " 
    		+ "phrase's next instance and for linking to previous and next chapters");
    changeOrderButton.setText("Change Chapter Order");
    changeOrderButton.addActionListener(this::changeOrderButtonActionPerformed);
    
    changeTrailButton.setText("Change Trail (Keep Link Order)");
    changeTrailButton.setToolTipText("Changes the order of chapters represented in previous-" 
    		+ "and next-chapter links without changing next-quote order");
    changeTrailButton.addActionListener(this::changeTrailButtonActionPerformed);
    
    exitButton.setText("Exit");
    exitButton.addActionListener(this::exitButtonActionPerformed);
    
    statusTitleLabel.setText("Status:");
    
    statusLabel.setText("undefined");
    
    GroupLayout jPanel1Layout = new GroupLayout(jPanel1);
    jPanel1.setLayout(jPanel1Layout);
    jPanel1Layout.setHorizontalGroup(
        jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
        .addGroup(jPanel1Layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                    .addComponent(trailFileLabel)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                    .addComponent(
                    		trailFileField, 
                    		GroupLayout.PREFERRED_SIZE, 
                    		250, 
                    		GroupLayout.PREFERRED_SIZE)
                    .addGap(74, 74, 74))
                .addGroup(GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                    .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                            .addComponent(statusLabel)
                            .addPreferredGap(
                            		LayoutStyle.ComponentPlacement.RELATED, 
                            		GroupLayout.DEFAULT_SIZE, 
                            		Short.MAX_VALUE)
                            .addComponent(
                            		chapterizeLinkButton, 
                            		GroupLayout.PREFERRED_SIZE, 
                            		200, 
                            		GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel1Layout.createSequentialGroup()
                            .addComponent(statusTitleLabel)
                            .addPreferredGap(
                            		LayoutStyle.ComponentPlacement.RELATED, 
                            		GroupLayout.DEFAULT_SIZE, 
                            		Short.MAX_VALUE)
                            .addComponent(
                            		createFoldersButton, 
                            		GroupLayout.PREFERRED_SIZE, 
                            		200, 
                            		GroupLayout.PREFERRED_SIZE)))
                    .addContainerGap())
                .addGroup(GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                    .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(
                        		GroupLayout.Alignment.TRAILING, 
                        		jPanel1Layout.createSequentialGroup()
                            .addGap(0, 0, Short.MAX_VALUE)
                            .addComponent(
                            		changeOrderButton, 
                            		GroupLayout.PREFERRED_SIZE, 
                            		200, 
                            		GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel1Layout.createSequentialGroup()
                            .addGroup(jPanel1Layout.createParallelGroup(
                            		GroupLayout.Alignment.LEADING)
                                .addComponent(pwdTitleLabel)
                                .addComponent(pwdLabel)
                                .addGroup(jPanel1Layout.createSequentialGroup()
                                    .addComponent(phraseSizeLimitLabel)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(
                                    		phraseSizeLimitField, 
                                    		GroupLayout.PREFERRED_SIZE, 
                                    		40, 
                                    		GroupLayout.PREFERRED_SIZE)))
                            .addGap(0, 0, Short.MAX_VALUE))
                        .addGroup(jPanel1Layout.createSequentialGroup()
                            .addComponent(exitButton)
                            .addPreferredGap(
                            		LayoutStyle.ComponentPlacement.RELATED, 
                            		220, 
                            		Short.MAX_VALUE)
                            .addComponent(
                            		changeTrailButton, 
                            		GroupLayout.PREFERRED_SIZE, 
                            		200, 
                            		GroupLayout.PREFERRED_SIZE)))
                    .addContainerGap())))
    );
    jPanel1Layout.setVerticalGroup(
        jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
        .addGroup(jPanel1Layout.createSequentialGroup()
            .addContainerGap()
            .addComponent(pwdTitleLabel)
            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(pwdLabel)
            .addGap(18, 18, 18)
            .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(trailFileLabel)
                .addComponent(
                		trailFileField, 
                		GroupLayout.PREFERRED_SIZE, 
                		GroupLayout.DEFAULT_SIZE, 
                		GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                .addGroup(jPanel1Layout.createSequentialGroup()
                    .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(phraseSizeLimitLabel)
                        .addComponent(
                        		phraseSizeLimitField, 
                        		GroupLayout.PREFERRED_SIZE,
                        		GroupLayout.DEFAULT_SIZE, 
                        		GroupLayout.PREFERRED_SIZE))
                    .addGap(18, 18, 18)
                    .addComponent(createFoldersButton))
                .addComponent(statusTitleLabel))
            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(chapterizeLinkButton)
                .addComponent(statusLabel))
            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(changeOrderButton)
            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(changeTrailButton)
                .addComponent(exitButton))
            .addContainerGap(23, Short.MAX_VALUE))
    );
    
    GroupLayout layout = new GroupLayout(getContentPane());
    getContentPane().setLayout(layout);
    layout.setHorizontalGroup(
        layout.createParallelGroup(GroupLayout.Alignment.LEADING)
        .addGroup(layout.createSequentialGroup()
            .addComponent(
            		jPanel1, 
            		GroupLayout.PREFERRED_SIZE, 
            		GroupLayout.DEFAULT_SIZE, 
            		GroupLayout.PREFERRED_SIZE)
            .addGap(0, 0, Short.MAX_VALUE))
    );
    layout.setVerticalGroup(
        layout.createParallelGroup(GroupLayout.Alignment.LEADING)
        .addGroup(layout.createSequentialGroup()
            .addComponent(
            		jPanel1, 
            		GroupLayout.PREFERRED_SIZE, 
            		GroupLayout.DEFAULT_SIZE, 
            		GroupLayout.PREFERRED_SIZE)
            .addGap(0, 36, Short.MAX_VALUE))
    );
    
    pack();
  }
  
  /**
   * <p>Ends the program when the "Exit" button is pressed.</p>
   * @param evt
   */
  private void exitButtonActionPerformed(ActionEvent evt) {
  	System.exit(0);
  }
  
  /**
   * <p>When the "Create Folders" button is pressed. Checks that no other button's operation is in
   * progress, and if so, establishes that this is the current operation and initiates a worker
   * thread that
   * {@link EnsureFolders#ensureFolders(Consumer<String>) ensures the needed folders exist},
   * which, when it finishes, establishes that there is no longer an operation in progress,
   * allowing other buttons to work again.</p>
   * @param evt
   */
  private void createFoldersButtonActionPerformed(ActionEvent evt) {
    buttonPress(
        createFoldersButton, 
    		"Creating needed folders", 
    		() -> "Done: Put html books in " + Folder.HTML_BOOKS.getFolderName(), 
    		() -> app.ensureFolders(statusLabelMsg));
  }
  
  /**
   * <p>The second button, "Chapterize Books; Add Links"</p>
   * @param evt
   */
  private void chapterizeLinkButtonActionPerformed(ActionEvent evt) {
    buttonPress(
        chapterizeLinkButton, 
    		"Doing all the work (" 
            + trailFileField.getText() + ", " 
            + phraseSizeLimitField.getText() + ")", 
    		() -> "Done: Chapters ready: " + Folder.READABLE.getFolderName(), 
    		() -> {
  		    int limit;
  		    try{
		        limit = limit();
  		    } catch(NumberFormatException e){
		        statusLabelMsg.accept(
                "Cannot parse specified phrase size as an int: " 
                + phraseSizeLimitField.getText());
		        return;
  		    }
  		    
  		    Trail trail;
  		    try{
		        trail = trail();
  		    } catch(FileNotFoundException e){
		        statusLabelMsg.accept(
                "Could not read trail from " + trailFileField.getText());
		        return;
  		    }
  		    
  		    app.isolateChaptersAndLink(trail, limit, statusLabelMsg);
    		});
  }
  
  /**
   * <p>The third button, "Change Chapter Order"</p>
   * @param evt
   */
  private void changeOrderButtonActionPerformed(ActionEvent evt) {
    buttonPress(
        changeOrderButton, 
    		"Changing chapter order (" 
            + trailFileField.getText() + ", " 
            + phraseSizeLimitField.getText() + ")", 
    		() -> "Done: Chapter order changed", 
    		() -> {
  		    int limit;
          try{
            limit = limit();
          } catch(NumberFormatException e){
            statusLabelMsg.accept(
                "Cannot parse specified phrase size as an int: " 
                + phraseSizeLimitField.getText());
            return;
          }
          
          Trail trail;
          try{
            trail = trail();
          } catch(FileNotFoundException e){
            statusLabelMsg.accept(
                "Could not read trail from " + trailFileField.getText());
            return;
          }
  		    
  		    app.linksAndTrail(limit, trail);
    		});
  }
  
  /**
   * <p>The fourth button, "Change Trail (Keep Link Order)"</p>
   * @param evt
   */
  private void changeTrailButtonActionPerformed(ActionEvent evt) {
    String trailText = trailFileField.getText();
    buttonPress(
        changeTrailButton, 
    		"Changing trail sequence (" + trailText + ")", 
    		() -> "Done: Trail changed to " + trailText, 
    		() -> {
  		    Trail trail;
  		    try{
		        trail = trail();
  		    } catch(FileNotFoundException e){
		        statusLabelMsg.accept("Could not read trail from " + trailFileField.getText());
		        return;
  		    }
  		    
  		    app.setTrail(trail, statusLabelMsg);
    		});
  }
  
  /**
   * <p>Provides the basic structure of all button-press event responses: if {@link opState} is
   * null (no operation is being performed), then opState is changed to specify the operation
   * being initiated, a staring message is set as the GUI's status, then a ButtonOperation is
   * created and executed which performs the action contained in {@code action} and then sets the
   * GUI's status to an ending message.</p>
   * @param newOpState the button that was pressed
   * @param startMsg message displayed on the GUI as soon as this process begins
   * @param endMsg message displayed on the GUI once this process finishes
   * @param action the actions taken as a result of pressing the button {@code newOpState}
   */
  private void buttonPress(
      JButton newOpState, 
      String startMsg, 
      Supplier<String> endMsg, 
      Runnable action){
    
  	if(opState == null){
  		opState = newOpState;
  		statusLabel.setText(startMsg);
  		
  		new ButtonOperation(
    			() -> {
    			  action.run();
    			  statusLabel.setText(endMsg.get());
    			})
    			.execute();
  	}
  }
  
  /**
   * <p>Sets look and feel for the GUI, and creates/queues a Runnable that instantiates the GUI
   * window and makes its window {@linkplain java.awt.Window#setVisible() visible}.</p>
   * @param args the command line arguments
   */
  public static void main(String args[]) {
      /* Set the Nimbus look and feel */
      //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
      /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
       * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
       */
      try {
        for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
          if ("Nimbus".equals(info.getName())) {
            UIManager.setLookAndFeel(info.getClassName());
            break;
          }
        }
      } catch (ClassNotFoundException ex) {
        Logger.getLogger(RepeatedPhrasesUI.class.getName()).log(Level.SEVERE, null, ex);
      } catch (InstantiationException ex) {
        Logger.getLogger(RepeatedPhrasesUI.class.getName()).log(Level.SEVERE, null, ex);
      } catch (IllegalAccessException ex) {
        Logger.getLogger(RepeatedPhrasesUI.class.getName()).log(Level.SEVERE, null, ex);
      } catch (UnsupportedLookAndFeelException ex) {
        Logger.getLogger(RepeatedPhrasesUI.class.getName()).log(Level.SEVERE, null, ex);
      }
      //</editor-fold>

      /* Create and display the form */
      EventQueue.invokeLater(() -> new RepeatedPhrasesUI().setVisible(true));
  }
  
  //TODO store arg indices in a utility class Args
  
  private JButton createFoldersButton;
  private JButton chapterizeLinkButton;
  private JButton changeOrderButton;
  private JButton changeTrailButton;
  private JButton exitButton;
  
  private JPanel jPanel1;
  
  private JLabel phraseSizeLimitLabel;
  private JTextField phraseSizeLimitField;
  
  private JLabel trailFileLabel;
  private JTextField trailFileField;
  
  private JLabel pwdTitleLabel;
  private JLabel pwdLabel;
  
  private JLabel statusTitleLabel;
  private JLabel statusLabel;
  
  /**
   * <p>Displays a message on the GUI identifying an action that the current process has
   * taken.</p>
   */
  private Consumer<String> statusLabelMsg = statusLabel::setText;
  
  /**
   * 
   * @return
   * @throws NumberFormatException if the content of the phrase-size text field cannot be parsed 
   * as an int.
   */
  private int limit(){
    return Integer.parseInt(phraseSizeLimitField.getText());
  }
  
  private Trail trail() throws FileNotFoundException{
    String name = trailFileField.getText();
    File file = new File(name);
    return Trail.fromFile(file, app.getChapters().stream()
        .collect(Collectors.toMap(Chapter::getName, Function.identity())));
  }
  
  /**
   * <p>Identifies the application's operating state and prevents multiple processes from running
   * at once. This is set to refer to the most recently pressed button by the methods called by
   * the buttons' event-listeners and is reset to null once the processes initiated by those
   * method-calls finish.</p>
   */
  private JButton opState = null;
  
  /**
   * <p>A {@link SwingWorker} for the operations performed when one of the buttons (other than
   * exit) of this window is pressed.</p>
   */
  private class ButtonOperation extends SwingWorker<Void,Void>{
  	
    /**
     * <p>The action this ButtonOperation performs when its button is pressed.</p>
     */
  	private Runnable action;
  	
    /**
     * <p>Constructs a ButtonOperation with the specified action.</p>
     * @param action the action this ButtonOperation performs when its button is pressed
     */
  	private ButtonOperation(Runnable action){
  		this.action = action;
  	}
  	
    /**
     * <p>Calls {@link #action action}'s {@code run} method inside a try-block, with two
     * subsequent catch blocks for OutOfMemoryError and RuntimeException, sending a message to
     * the GUI in either case, with the latter including the exception's message.</p>
     */
  	@Override
  	public Void doInBackground(){
  		try{
  			action.run();
  		} catch(OutOfMemoryError e){
  			statusLabelMsg.accept("ERR: out of memory");
  		} catch(RuntimeException e){
  			statusLabelMsg.accept("EXCEPTION: "+e.getMessage());
  			try{
  				PrintStream ps = new PrintStream(new File("oops_i_crashed.txt"));
  				e.printStackTrace(ps);
  				ps.close();
  			} catch(FileNotFoundException ex){
  			}
  		}
  		return null;
  	}
  	
    /**
     * <p>Resets the {@linkplain RepeatedPhrasesUI#opState operation-state} to {@code null},
     * allowing a button's operation to begin when a button is next pressed.</p>
     */
  	@Override
  	public final void done(){
  		opState = null;
  	}
  }
}
