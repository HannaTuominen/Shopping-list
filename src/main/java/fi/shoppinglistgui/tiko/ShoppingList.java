package fi.shoppinglistgui.tiko;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.geometry.Pos;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.geometry.Insets;
import javafx.scene.control.TextField;
import javafx.scene.control.ButtonType;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Alert;
import javafx.beans.binding.Bindings;

import java.util.ArrayList;
import java.util.Optional;

import javafx.beans.binding.BooleanBinding;
import java.io.IOException;
import java.io.PrintWriter;

import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.util.converter.IntegerStringConverter;
import javafx.stage.FileChooser;
import java.io.File;

import com.dropbox.core.DbxAppInfo;
import com.dropbox.core.DbxAuthFinish;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.DbxWebAuth;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.WriteMode;

import java.io.ByteArrayInputStream;
import java.net.URL;

import javax.swing.JOptionPane;
import java.awt.Desktop;
import javafx.concurrent.Task;

import fi.jsonparser.tiko.*;

/**
* ShoppingList is used to create the UI of the shopping list and control it trhought that UI.
* 
* The shoppingList is used to create a new shopping list UI program that displays the users wanted shopping list.
* It has the possibility to update,save,add,remove and load the shopping list to either DropBox or locally.
* @author Hanna-Kaisa Tuominen
* @version 2019.1412
* 
*/

public class ShoppingList extends Application {
    Stage window;
    TableView<ShoppingItem> table;
    Button addBtn;
    Button delBtn;
    ObservableList<ShoppingItem> shoppingItems = FXCollections.observableArrayList();
    TextField textField;
    TextField textField2;

    private static final String APP_KEY = "1ofr55a682osx5k";
    private static final String APP_SECRET = "97uvm28hzda532z";

    /**
     * The start method that keeps the shopping list program running with the window.
     * Used to create the main UI and load the items and amount to the table for show in the UI.
     * Also contains the add and delete buttons which are used to add new shopping items to the list that is displayed.
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        window = primaryStage;
        window.setTitle("Shopping list");
        addBtn = new Button("Add");
        delBtn = new Button("Delete");

        // Item column
        TableColumn<ShoppingItem, String> itemColumn = new TableColumn<>("Item");
        itemColumn.setMinWidth(285);
        itemColumn.setCellValueFactory(new PropertyValueFactory<ShoppingItem, String>("key"));
        itemColumn.setCellFactory(TextFieldTableCell.<ShoppingItem>forTableColumn());
        itemColumn.setOnEditCommit((CellEditEvent<ShoppingItem, String> t) -> {
            ((ShoppingItem) t.getTableView().getItems().get(t.getTablePosition().getRow())).setKey(t.getNewValue());
        });

        // Amount column
        TableColumn<ShoppingItem, Integer> valueColumn = new TableColumn<>("Amount");
        valueColumn.setMinWidth(100);
        valueColumn.setCellValueFactory(new PropertyValueFactory<ShoppingItem, Integer>("value"));
        valueColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        valueColumn.setOnEditCommit((CellEditEvent<ShoppingItem, Integer> t) -> {
            ((ShoppingItem) t.getTableView().getItems().get(t.getTablePosition().getRow())).setValue(t.getNewValue());
        });

        table = new TableView<>();
        table.setMaxHeight(315);
        table.setEditable(true);
        ScrollPane pane = new ScrollPane();
        pane.setContent(table);

        table.setItems(getShoppingItems());
        table.getColumns().addAll(itemColumn, valueColumn);

        BorderPane root = new BorderPane();

        //fields for adding items and amounts to the table
        Label label1 = new Label("Item:");
        textField = new TextField();
        textField.setPromptText("Item");
        Label label2 = new Label("Amount:");
        textField2 = new TextField();
        textField2.setPromptText("Amount");

        HBox hb = new HBox();
        hb.getChildren().addAll(addBtn, delBtn);
        hb.setSpacing(8);

        VBox vb = new VBox();
        vb.setPadding(new Insets(10, 10, 10, 10));
        vb.getChildren().addAll(label1, textField, label2, textField2, hb);
        vb.setSpacing(10);

        VBox group = new VBox();
        group.getChildren().addAll(generateMenuBar(), vb, table);
        group.setAlignment(Pos.CENTER);
        root.setTop(group);
        //Bind the add and delete buttons to not working until (addbutton) textfields 1 and 2 are not empty and delete until table has intems in it
        BooleanBinding booleanBind1 = textField.textProperty().isEmpty().or(textField2.textProperty().isEmpty());
        BooleanBinding booleanBind2 = Bindings.isEmpty(table.getItems());

        addBtn.disableProperty().bind(booleanBind1);
        delBtn.disableProperty().bind(booleanBind2);

        addBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                try {
                    String key = textField.getText();
                    if(key.length() > 150) {
                        dialog("item is too long, maximum is 150 characters");
                        textField.clear();
                        key="";
                    } else if(key == null || key.contains("{") || key.contains("[") || key.contains(":") || key.contains("\"") || key.contains("]") || key.contains("}")) {
                        dialog("Invalid item name, cannot contain {[:\"]}");
                        textField.clear();
                        key="";
                    }
                    int value = Integer.parseInt(textField2.getText());
                    if(value < 1 ) {
                        dialog("Amount needs to be 1 or more");
                    }
                    if(key.length()>0 && value > 0) {
                        addShoppingItem(key, value);
                    }
                    
                    textField.clear();
                    textField2.clear();
                } catch (Exception ee) {
                    textField2.clear();
                    dialog("Amount is invalid silly!");
                }
            }
        });

        delBtn.setOnAction(e -> {
            ShoppingItem selectedItem = table.getSelectionModel().getSelectedItem();
            table.getItems().remove(selectedItem);
        });

        Scene scene = new Scene(root, 400, 600);
        window.setMinHeight(640);
        window.setMinWidth(418);
        window.setScene(scene);
        window.show();
    }

    /**
     * Used to add new shopping items to the shoppinItems ObservableList
     * @param key the item
     * @param value the amount
     */
    private void addShoppingItem(String key, int value) {
        shoppingItems.add(new ShoppingItem(key, value));
    }

    /**
     * Used to return shoppingItems
     * @return a observable list of the shopping items
     */
    private ObservableList<ShoppingItem> getShoppingItems() {
        return shoppingItems;
    }

    /**
     * Used to warn the user about an invalid Amount number. The number must be an Integer.
     */
    private void dialog(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Alert");
        alert.setHeaderText(null);
        alert.setContentText(message);
        Optional<ButtonType> result = alert.showAndWait();
    }

    /**
     * Used to parse the shopping list to a JSONArray which is then returned.
     * The for loop goes through the shoppingItems list for all of the shoppingItems
     * and with them it creates new JSONObjects which it adds to the array to be returned.
     * @return the creted JSONArray filled with the shoppingItems item and amount.
     */
    private JSONArray parseShoppingListToArray() {
        JSONArray array = new JSONArray();

        for (ShoppingItem i : shoppingItems) {
            JSONObject obj = new JSONObject();
            obj.add("item", i.getKey());
            obj.add("amount", i.getValue());
            array.add(obj);
        }

        return array;
    }
    /**
     * Used to save the file locally to the users computer.
     * 
     * Uses filechooser to save the file to a mandatory json file.
     * @param array the JSONArray with all of the needed shopping list items
     */
    public void save(JSONArray array) {
        JSONObject things = new JSONObject();
        things.add("Shopping list", array);

        // System.out.println(things.toString());
        FileChooser fileChooser = new FileChooser();

        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("JSON files (*.json)", "*.json");
        fileChooser.getExtensionFilters().add(extFilter);
        File selectedFile = fileChooser.showSaveDialog(window);

        if (selectedFile != null) {
            saveTextToFile(things.toString(), selectedFile);
        }
    }

    private void saveTextToFile(String content, File file) {
        try {
            PrintWriter writer;
            writer = new PrintWriter(file);
            writer.println(content);
            writer.close();
        } catch (IOException ex) {}
    }

    /**
     * The saveDropBox is used to save the generated file to dropBox instead of locally.
     * The user must choose to save to DropBox in the menu bar for this method to run.
     * @param array The gotten JSONArray that needs to be saved to dropBox in a json file
     * @throws Exception if the URL  or the uploadbuilder is invalid throws the needed exception
     */
    private void saveDropBox(JSONArray array) throws Exception {

        JSONObject things = new JSONObject();
        things.add("Shopping list", array);

        DbxRequestConfig config = new DbxRequestConfig("tamk_shopping_list"); // Client name can be whatever you like

        DbxAppInfo appInfo = new DbxAppInfo(APP_KEY, APP_SECRET);
        DbxWebAuth webAuth = new DbxWebAuth(config, appInfo);
        DbxWebAuth.Request webAuthRequest = DbxWebAuth.newRequestBuilder().withNoRedirect().build();

        String url = webAuth.authorize(webAuthRequest);

        Desktop.getDesktop().browse(new URL(url).toURI());
        String code = JOptionPane.showInputDialog("Please click \"allow\" then enter your access code: " +
        "\n ATTENTION: This will overwrite a file named shopping_list.json if it already exists.");

        DbxAuthFinish authFinish = webAuth.finishFromCode(code);
        String accessToken = authFinish.getAccessToken(); // Store this for future use

        DbxClientV2 client = new DbxClientV2(config, accessToken);

        String fileContents = things.toString();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(fileContents.getBytes());
        
        client.files().uploadBuilder("/shopping_list.json").withMode(WriteMode.OVERWRITE).uploadAndFinish(inputStream);
      
    }

    /**
     * The generateMenuBar method is used to generate the menu bar for the top of the shopping list UI
     * It will contain the possibility to save and load and exit the program, with about box also.
     * @return The created menuBar
     */
    public MenuBar generateMenuBar() {

        MenuBar menuBar = new MenuBar();

        Menu menuFile = new Menu("File");
        MenuItem save = new MenuItem("Save...");
        MenuItem saveDropBox = new MenuItem("Save to DropBox");
        MenuItem load = new MenuItem("Load...");
        MenuItem exit = new MenuItem("Exit");

        menuFile.getItems().addAll(save, saveDropBox, load, exit);

        //Used for the save buttons action. Calls the save method after parsing the shopping list to a JSONArray
        save.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                JSONArray writing = parseShoppingListToArray();
                save(writing);
            }
        });

        //Used for the save to dropbox buttons action. calls the saveDropBox method after parsing the shopping list to a JSONArray
        saveDropBox.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {     
                //Prevent lagging    
                Task task = new Task<Void>() {
                    @Override public Void call() {
                        JSONArray writing = parseShoppingListToArray();
                        try {
                            saveDropBox(writing);
                        } catch (Exception e1) {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                        }
                        return null;
                    }
                };
                new Thread(task).start();
            }});
        //Used for the load buttons action. Uses fileChooser to load a file from the user locally and parse it to fit the shopping list and load the info.
        load.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                FileChooser filechooser = new FileChooser();
                filechooser.setTitle("Load existing shopping file");
                File file = filechooser.showOpenDialog(window);
                
                if(file != null) {
                    shoppingItems.clear();

                    JSONParser p = new JSONParser();
                    JSONObject obj = (JSONObject) p.parseJsonFile(file.toString());
                    JSONArray a = (JSONArray) obj.get("shopping list");
    
                    ArrayList<String> itemArray = new ArrayList<>();
                    ArrayList<Integer> amountArray = new ArrayList<>();
                    a.get("item");
                    for(Object item : a.getValueArray()) {
                        itemArray.add(item.toString());
                    }
    
                    a.get("amount");
                    for(Object amount : a.getValueArray()) {
                        amountArray.add((int) amount);
                    }
                    if(itemArray.isEmpty() || amountArray.isEmpty()) {
                        dialog("Invalid file, could not find items and amounts!");
                    }
    
                    int j = 0;
                    for(String i : itemArray) {
                        addShoppingItem(i, amountArray.get(j));
                        j++;
                    }
                }
            }});

        //Used for the exit buttons action. Exits the program when pressed.
        exit.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {     
                window.close();
            }});

        Menu menuView = new Menu("About");
        MenuItem about = new MenuItem("About The Shopping App");
  
        menuView.getItems().addAll(about);
        about.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {     
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("About");
                alert.setHeaderText(null);
                alert.setContentText("Creator: Hanna-Kaisa Tuominen\nDate: 15.12.2019\n18TIKOOT\n");
                Optional<ButtonType> result = alert.showAndWait();
                result.ifPresent((buttonType) -> System.out.println("Ok was pressed"));
            }});
        menuBar.getMenus().addAll(menuFile, menuView);
  
        return menuBar;
     }
     
}