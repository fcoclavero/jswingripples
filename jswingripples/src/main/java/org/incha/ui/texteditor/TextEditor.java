package org.incha.ui.texteditor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.*;

import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.incha.core.texteditor.FileOpen;
import org.incha.ui.JSwingRipplesApplication;

public class TextEditor extends JFrame {

    private ArrayList<FileOpen> openFiles;
    private static TextEditor instance;
    private int tab;
    private JTabbedPane jTabbedPane;

    private JMenu fileMenu = new JMenu( "File" );

    private JMenu syntax = new JMenu( "Syntax" );
    private JMenuBar menubar = new JMenuBar();

    private Map<String,String> extensionMap = new HashMap<String,String>();

    /**
     * Set up the menus in the TextEditor.
     */
    private void setUpJMenuBar(){
        //put the syntax in the extensionMap.
        extensionMap.put( "C", SyntaxConstants.SYNTAX_STYLE_C );
        extensionMap.put( "C++", SyntaxConstants.SYNTAX_STYLE_CPLUSPLUS );
        extensionMap.put( "C#", SyntaxConstants.SYNTAX_STYLE_CSHARP );
        extensionMap.put( "HTML", SyntaxConstants.SYNTAX_STYLE_HTML );
        extensionMap.put( "JAVA", SyntaxConstants.SYNTAX_STYLE_JAVA );
        extensionMap.put( "JAVASCRIPT", SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT );
        extensionMap.put( "PHP", SyntaxConstants.SYNTAX_STYLE_PHP );
        extensionMap.put( "PYTHON", SyntaxConstants.SYNTAX_STYLE_PYTHON );
        extensionMap.put( "RUBY", SyntaxConstants.SYNTAX_STYLE_RUBY );
        extensionMap.put( "SQL", SyntaxConstants.SYNTAX_STYLE_SQL );
        extensionMap.put( "XML", SyntaxConstants.SYNTAX_STYLE_XML );
        //add a tab pane to the window.
        jTabbedPane = new JTabbedPane();

        //add a click listener to the tab pane, and intersect the mouse location with the tab pane.
        jTabbedPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON2){
                    openFiles.get(jTabbedPane.indexAtLocation(e.getX(),e.getY())).close(instance,jTabbedPane.indexAtLocation(e.getX(),e.getY()));
                }
                super.mouseClicked(e);
            }
        });
        openFiles = new ArrayList<FileOpen>();
        JMenuItem fileSave = new JMenuItem( "Save" );
        JMenuItem revertAll = new JMenuItem( "Revert All" );
        //build Menu.
        fileMenu.add( fileSave );
        fileMenu.add( revertAll );
        menubar.add( fileMenu );
        menubar.add( syntax );
        add( menubar, BorderLayout.NORTH );

        //add ClickListener to the fileSave.
        fileSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    openFiles.get( jTabbedPane.getSelectedIndex() ).save();
                    JOptionPane.showMessageDialog( instance, "Saved" );

                } catch ( Exception exception ) {
                    exception.printStackTrace();
                }
            }
        });
        revertAll.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    openFiles.get( jTabbedPane.getSelectedIndex() ).revertText();
                } catch ( Exception exception ) {
                    exception.printStackTrace();
                }
            }
        });

        //add ClickListener to the syntax menu.
        for (final String string : extensionMap.keySet()){
            JMenuItem extension = new JMenuItem( string );
            syntax.add(extension);
            extension.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    openFiles.get(jTabbedPane.getSelectedIndex()).setSyntax(extensionMap.get(string));
                }
            });
        }
    }

    /**
     * Close a Tab in the JTabbedPane.
     * @param tab the index of the Tab that be close.
     */
    public void closeTab(int tab){
        jTabbedPane.remove(tab);
    }

    /**
     * Close the Current tab in the View.
     */
    public void closeSelectedTab(){
        jTabbedPane.remove(jTabbedPane.getSelectedIndex());
    }

    /**
     * Constructor.
     * @param jSwingRipplesApplication in case to be necesary.
     */
    public TextEditor (JSwingRipplesApplication jSwingRipplesApplication){
        super( "Text Editor" );

        instance = this;
        setUpJMenuBar();
        getContentPane().add( jTabbedPane );

        setSize( 1024, 768 );
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation( d.width/2 - 512, d.height/2 - 384 );
        setVisible( true );

        //close option to don't close the program.
        setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
        //save files when close the program.
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if(jTabbedPane.getTabCount()>0)
                {
                    int confirm = JOptionPane.showConfirmDialog(instance,"Would you like to save the changes?","Confirm",JOptionPane.YES_NO_OPTION);

                    if(confirm ==0){
                        for (int i=0;i<jTabbedPane.getTabCount();i++){
                            try{
                                openFiles.get((i)).save();
                            }
                            catch (Exception exception)
                            {
                                exception.printStackTrace();
                            }
                        }
                        JOptionPane.showMessageDialog(instance,"Saved");
                    }
                }
                super.windowClosing(e);
                instance=null;
            }
        });
    }

    /**
     * open a File in the Text Editor and add a new Tab to the window with
     * the file.
     * @param filename String with the path of the File.
     */
    public void openFile ( String filename ) {
        //search if the File isn't open.
        for(FileOpen file: openFiles){
            //if found the file open, select this tab.
            if ( file.getPath().equals(filename)){
                jTabbedPane.setSelectedIndex(openFiles.indexOf(file));
                return;
            }
        }
        FileOpen newFile = new FileOpen(filename);
        if (newFile.open()) {
            openFiles.add(newFile);
            JScrollPane jScrollPane = new JScrollPane(newFile.getText());
            jTabbedPane.addTab(newFile.getFileName(), jScrollPane);
        }

        /**
         * if need a limit to the files open for any reason use this.
        if(openFiles.size()<15) {
            FileOpen newFile = new FileOpen(filename);
            if (newFile.open()) {
                openFiles.add(newFile);
                JScrollPane jScrollPane = new JScrollPane(newFile.getText());
                jTabbedPane.addTab(newFile.getFileName(), jScrollPane);
            }
        }
        else{

        }**/

    }

    /**
     * Instance the TextEditor.
     * @return the only instance to the TextEditor.
     */
    public static TextEditor getInstance(){
        if(instance==null){
            instance=new TextEditor(JSwingRipplesApplication.getInstance());
        }
        return instance;
    }


}
