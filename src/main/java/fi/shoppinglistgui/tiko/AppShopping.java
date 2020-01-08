package fi.shoppinglistgui.tiko;

import javafx.application.Application;

/**
* The App class is used to launch the program through the main method.
*
* The app class is used to launch the soppinglist program.
* @author Hanna-Kaisa Tuominen
* @version 2019.1412
* 
*/
public class AppShopping {
    public static void main(String[] args) throws Exception{
        System.out.println("Author: Hanna-Kaisa Tuominen");
        Application.launch(ShoppingList.class,args);
    }
}
