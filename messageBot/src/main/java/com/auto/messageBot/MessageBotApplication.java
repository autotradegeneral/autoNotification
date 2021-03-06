package com.auto.messageBot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;


import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
@EnableScheduling
public class MessageBotApplication extends Frame {
	List<String> originalRows = new ArrayList<>();
	List<String> newRows = new ArrayList<>();
	List<String> latestRows = new ArrayList<>();
	
	String apiToken = "";
	String chatId = "";
	
	
	private TextField apiText ;
	private TextField channelIdText;
	private Button updateBtn;
	public static void main(String[] args){
		SpringApplication.run(MessageBotApplication.class, args);
//		MessageBotApplication app = new MessageBotApplication();

	}

	public MessageBotApplication(){
		Label lblCount = new Label("Notification Bot Settings");  // construct the Label component
	      add(lblCount); 
	      setLayout(new BorderLayout());
	      Panel panelInput = new Panel(new GridLayout(4,1));
	      Label apiKey = new Label("API Token: ");
	      panelInput.add(apiKey);
	    	apiText = new TextField();
	    	panelInput.add(apiText);
//	    	apiText.addActionListener(new ApiInputListener());

	    	Label channelId = new Label("Channel ID: ");
	    	panelInput.add(channelId);
	    	channelIdText = new TextField();
	    	panelInput.add(channelIdText);
//	    	channelIdText.addActionListener(new ChannelIdInputListener());
	    	
	    	Panel panelBtns = new Panel(new FlowLayout());
	    	updateBtn = new Button("Update");   // construct the Button component
	    	panelBtns.add(updateBtn);  
	        updateBtn.addActionListener(new ActionListener() {
	            // ActionEvent handler - Called back upon button-click.
	            @Override
	            public void actionPerformed(ActionEvent evt) {
	            	apiToken = apiText.getText();
	            	chatId = channelIdText.getText();
	            }
	         });
	        
	        
	    	add(panelInput, BorderLayout.NORTH);
	    	add(panelBtns, BorderLayout.SOUTH);
		setTitle("Notification Bot Settings");  // "super" Frame sets its title
		setSize(500, 300);
		setVisible(true);    
		addWindowListener(new MyWindowListener());
	}

	@Scheduled(fixedRate = 55000)
	public void readAndSendNotification() throws IOException, InterruptedException{
		if(!apiToken.isEmpty() && !chatId.isEmpty()) {
			newRows = new ArrayList<>();
			Path original = Paths.get("trade_log.csv");
			Path copied = Paths.get("");
			File copiedFile = new File("trade_log_copied.csv");
			Files.copy(original, copied.resolve(copiedFile.getPath()), StandardCopyOption.REPLACE_EXISTING);
			Thread.sleep(5000);
			String row ;
			BufferedReader csvReader = new BufferedReader(new FileReader("trade_log_copied.csv"));
	//        CSVReader csvReader = new CSVReader(new FileReader("trade_log_copied.csv"));
	//        long rowCount = csvReader.lines().count();
	//        System.out.println(rowCount);
	//		csvReader = new BufferedReader(new FileReader(copiedFile));
	
			while ((row = csvReader.readLine()) != null) {
				String[] data = row.split(",");
	//            + ", " + data[5]
				String echo = data[1] + ", DateTime: " + data[0] + ", " +  data[2]   + ", " + data[11] ;
				echo = echo.replace("{", "");
				if(data[12].contains("FILLED")) {
					newRows.add(echo);
				}
	
			}
			csvReader.close();
	//		copiedFile.delete();
			System.out.println(newRows.size());
			System.out.println(originalRows.size());
			if(!originalRows.isEmpty()) {
				if(newRows.size() > originalRows.size() ){
					int newRowCount = newRows.size() - originalRows.size();
					System.out.println("newRowCount" + newRowCount);
					latestRows = newRows.subList(originalRows.size(), newRows.size());
					originalRows = newRows;
					sendNotification();
				}
			}else {
				latestRows = newRows;
				originalRows = newRows;
				sendNotification();
			}
		}

		

		
	}
	
	public void sendNotification() throws IOException {
			String text = "Hello world!";
			for(String line : latestRows) {
				String urlString = "https://api.telegram.org/bot%s/sendMessage?chat_id=%s&text=%s";
				System.out.println(line);
				text = line;
				urlString = String.format(urlString, apiToken, chatId, text);
	
				URL url = new URL(urlString);
				URLConnection conn = url.openConnection();
	
				StringBuilder sb = new StringBuilder();
				InputStream is = new BufferedInputStream(conn.getInputStream());
				BufferedReader br = new BufferedReader(new InputStreamReader(is));
				String inputLine = "";
				while ((inputLine = br.readLine()) != null) {
					sb.append(inputLine);
				}
				String response = sb.toString();
			}
	}
	// Define an inner class to handle WindowEvent of this Frame
	   private class MyWindowListener implements WindowListener {
	      // Called back upon clicking close-window button
	      @Override
	      public void windowClosing(WindowEvent evt) {
	         System.exit(0);  // Terminate the program
	      }

	      // Not Used, BUT need to provide an empty body to compile.
	      @Override public void windowOpened(WindowEvent evt) { 
	    	  setLocationRelativeTo(null);
	      }
	      @Override public void windowClosed(WindowEvent evt) { }
	      // For Debugging
	      @Override public void windowIconified(WindowEvent evt) { System.out.println("Window Iconified"); }
	      @Override public void windowDeiconified(WindowEvent evt) { System.out.println("Window Deiconified"); }
	      @Override public void windowActivated(WindowEvent evt) { 
	    	  System.out.println("Window Activated");
	    }
	      @Override public void windowDeactivated(WindowEvent evt) { System.out.println("Window Deactivated"); }
	   }
}
